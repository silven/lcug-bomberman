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
    (dosync
     (alter world update-in [:clients] disj socket)
     (alter world update-in [:players] dissoc socket)
     (alter world update-in [:controllers] dissoc socket))))

(def min-update-delay 100)
(defn- calc-update-reply [world last-time]
  (let [now (System/currentTimeMillis)
	delta (- now @last-time)]
    (if (>= delta min-update-delay)
      (do (reset! last-time now)
	  (json-str {:now now
		     :world (:map @world)
		     :players (vals (:players @world))}))
      (str "DELAY MORE " delta))))

(defn- name-player [world socket name]
  (dosync (alter world update-in [:players socket] assoc :name name)))

(defn- handle-client
  "Function that continuously reads from
   client socket and respondes.
   TODO: Handle JSON parsing. Dont override binds."
  [world socket]
  (let [instream (.getInputStream socket)
	outstream (.getOutputStream socket)
	writer (PrintWriter. outstream)
	controller (get (:controllers @world) socket)
	last-update-time (atom 0)]
    (binding [*in* (BufferedReader. (InputStreamReader. instream))]
      (name-player world socket (read-line))
      (write-json (get (:players @world) socket) writer true)
      (.println writer "")
      (.flush writer)
      (while (not (.isClosed socket))
	;; TODO: Make this a proper blocking statement
	;; with a time-out.
	(try 
	  (if-let [command (read-line)]
	    (let [reply-code (player/handle-client-command
				     controller command)
		  reply-string
		  (condp = reply-code
		      :update (calc-update-reply
			       world last-update-time)
		      :unknown "UNKNOWN"
		      "OK")]
	      (when reply-string
		(.println writer reply-string)
		(.flush writer)))
	    (throw (Exception. "Broken pipe")))
	  (catch Exception e
	    (println "Client dropped from game")
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
   set of Client Sockets :clients"
  [world port]
  (dosync (alter world assoc :clients #{}))
  (let [server (ServerSocket. port)]
    (on-thread #(while (not (.isClosed server))
		  (try (accept-fn (.accept server) world)
		       (catch SocketException e
			 (println "Server Socket closed while accepting. Normal termination.")))))
    server))
  
(defn- close-server
  "Gracefully closes a ServerSocket and it's clients."
  [server]
  (doseq [client (:clients @*world*)]
    (close-socket client))
  (.close #^ServerSocket server))

(defn run-game [lvl port]
  (let [running (atom true)]
    (on-thread 
     #(binding [*world* (ref (load-ascii lvl))]
	(try
	  (let [server (create-server *world* port)]
	    (start-swing-view *world*)
	    (while @running
	      (println "running game loop")
	      (player/update-players-by-action *world*)
	      (Thread/sleep 1000))
	    (close-server server))
	  (catch Exception e
	    (println "Server got exception" e)
	    (.printStackTrace e)))))
  running))