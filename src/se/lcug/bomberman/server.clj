(ns se.lcug.bomberman.server
  (:use [clojure.data.json :only (json-str write-json read-json)])
  (:use [se.lcug.bomberman.world]
	[se.lcug.bomberman.view :only (start-swing-view colors)])
  (:import (java.net InetAddress ServerSocket Socket SocketException)
	   (java.io BufferedReader InputStreamReader OutputStream OutputStreamWriter PrintWriter)))

(declare *world*)

(defn get-proper-color [n]
  (get (keys colors) (- (count (keys colors)) n)))

(defn- on-thread
  "Helper method to run function in other thread."
  [f]
  (doto (Thread. #^Runnable f)
    (.start)))

(defn- close-socket
  "Gracefully shuts down a client socket."
  [#^Socket s]
  (when-not (.isClosed s)
    (doto s
      (.shutdownInput)
      (.shutdownOutput)
      (.close))))

(defn move [controller dir]
  (dosync (alter controller :dir dir)
	  (alter controller :move? true)))

(defn handle-client-command
  "Function that translates a command-string to state-change."
  [world-map players me cmd]
  (condp = cmd
      "UP"      (move me :up)
      "DOWN"    (move me :down)
      "LEFT"    (move me :left)
      "RIGHT"   (move me :right)
      "BOMB"    (dosync (alter me :bomb? true))
      "STOP"    (dosync (alter me :move? false))
      ;; TODO: Rewrite the update function, give it a limit
      "UPDATE"  (do (println world-map)
		    (println players))
      (println "UNKNOWN COMMAND")))

(defn- handle-client
  "Function that continuously reads from
   client socket and respondes.
   TODO: Handle JSON parsing. Dont override binds."
  [world socket]
  (let [instream (.getInputStream socket)
	outstream (.getOutputStream socket)
	j-writer (PrintWriter. outstream)
	me (get (:players @world) socket)
	controller (get (:controllers @world) socket)]
    (binding [*in* (BufferedReader. (InputStreamReader. instream))
	      *out* (OutputStreamWriter. outstream)
	      *err* (PrintWriter. #^OutputStream outstream true)]
      (println (json-str me))
      (loop []
	(when-not (.isClosed socket)
	  ;; TODO: Make this a proper blocking statement
	  ;; with a time-out.
	  (let [command (read-line)]
	    (handle-client-command (:map @world)
				   (vals (:players @world))
				   controller
				   command))
	  (recur))))
    (close-socket socket)))

(defn- deny-client-socket
  "Function that informs client that they cannot connect."
  [s]
  (let [outstream (.getOutputStream s)
	outwriter (PrintWriter. outstream)]
    (.println outwriter "I'm sorry Dave, I can't let you do that.")
    (.flush outwriter)
    (close-socket s)))

(defn create-player
  "Create a player, in the world at the givven spawn point."
  [spawns]
  {:alive? true
   :pos (vec (map #(+ 0.5 %) (first spawns)))
   :max-bombs 1
   :flame-lenght 2
   :speed 1
   :color (get-proper-color (count spawns))})

(defn- accept-fn
  "Function to add client to game client pool and create player.."
  [#^Socket socket world]
  (let [spawns (:spawnpoints @world)]
    (println "Available spawns:" spawns)
    (if (first spawns)
      (let [player (create-player spawns)
	    state (ref {:move? false :dir :left :bomb? false})]
	(dosync
	 (alter world update-in [:spawnpoints] pop)
	 (alter world update-in [:clients] conj socket)
	 (alter world update-in [:players] assoc socket player)
	 (alter world update-in [:controllers] assoc socket state)
	 (on-thread #(handle-client world socket))))
      (deny-client-socket socket))))


(defn create-server
  "Setup a Bomberman game server. Returns the a map with
   ServerSocket :server and set of Client Sockets :clients"
  [world port]
  (let [server (ServerSocket. port)]
    (on-thread #(when-not (.isClosed server)
		  (accept-fn (.accept server) world)
		  (recur)))
    server))
  
(defn- close-server
  "Gracefully closes a ServerSocket and it's clients."
  [server]
  (doseq [client (:clients @*world*)]
    (close-socket client))
  (.close #^ServerSocket server))

(defn run-game [lvl]
  (let [running (atom true)]
    (on-thread 
     #(binding [*world* (ref (load-ascii lvl))]
	(let [server (create-server *world* 9004)]
	  (start-swing-view *world*)
	  (while @running
	    (Thread/sleep 100))
	  (close-server server))))
  running))