(ns se.lcug.bomberman.server
  (:use [clojure.data.json :only (json-str write-json read-json)]
        [se.lcug.bomberman.world]
	[se.lcug.bomberman.view :only (start-swing-view colors)]
	[se.lcug.bomberman.player :as player])
  (:import (java.net InetAddress InetSocketAddress ServerSocket Socket SocketException)
	   (java.io BufferedReader InputStreamReader OutputStream OutputStreamWriter PrintWriter)))

(declare *world*)

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

(defn disconnect-client [world socket]
  (when (.isClosed socket)
    (dosync (alter world update-in [:clients] disj socket)
		      (alter world update-in [:players] dissoc socket)
		      (alter world update-in [:controllers] dissoc socket))))

(defn- handle-client
  "Function that continuously reads from
   client socket and respondes.
   TODO: Handle JSON parsing. Dont override binds."
  [world socket]
  (let [instream (.getInputStream socket)
	outstream (.getOutputStream socket)
	j-writer (PrintWriter. outstream)
	controller (get (:controllers @world) socket)]
    (binding [*in* (BufferedReader. (InputStreamReader. instream))
	      *out* (OutputStreamWriter. outstream)
	      *err* (PrintWriter. #^OutputStream outstream true)]
      (println (json-str (get (:players @world) socket)))
      (while (not (.isClosed socket))
	;; TODO: Make this a proper blocking statement
	;; with a time-out.
	(try 
	  (let [command (read-line)]
	    (player/handle-client-command (:map @world)
					  (vals (:players @world))
					  controller
					  command))
	  (catch Exception e
	    (close-socket socket)
	    (disconnect-client world socket)))))
    (close-socket socket)))
  
(defn- deny-client-socket
  "Function that informs client that they cannot connect."
  [s]
  (let [outstream (.getOutputStream s)
	outwriter (PrintWriter. outstream)]
    (.println outwriter "I'm sorry Dave, I can't let you do that.")
    (.flush outwriter)
    (close-socket s)))

(defn- accept-fn
  "Function to add client to game client pool and create player.."
  [#^Socket socket world]
  (let [spawns (:spawnpoints @world)]
    (println "Available spawns:" spawns)
    (if (first spawns)
      (let [player (player/create-player spawns)
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
  (dosync (alter world assoc :clients #{}))
  (let [server (ServerSocket. port)]
    (on-thread #(while (not (.isClosed server))
		  (accept-fn (.accept server) world)))
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
	(try
	  (let [server (create-server *world* 9004)]
	    (start-swing-view *world*)
	    (while @running
	      ;; TODO: Game loop here.
	      (Thread/sleep 100))
	    (close-server server))
	  (catch Exception e
	    (println "Server got exception" e)
	    (.printStackTrace e)))))
  running))