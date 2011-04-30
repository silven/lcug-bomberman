(ns se.lcug.bomberman.server
;;  (:use [clojure.data.json :only (json-str write-json read-json)])
  (:import (java.net InetAddress ServerSocket Socket SocketException)
	   (java.io BufferedReader InputStreamReader OutputStreamWriter)))
  
(defn- on-thread
  "Helper method to run function in other thread."
  [f]
  (doto (Thread. #^Runnable f)
    (.start)))

(defn- accept-fn
  "Function to add client to game client pool.
  TODO: Make game aware of client maximum."
  [#^Socket s others]
  (dosync (alter others conj s)))

(defn create-server
  "Setup a Bomberman game server. Returns the a map with
   ServerSocket :server and set of Client Sockets :clients"
  [port]
  (let [server (ServerSocket. port)
	clients (ref #{})]
    (on-thread #(when-not (.isClosed server)
		  (accept-fn (.accept server) clients)
		  (recur)))
    {:server server :clients clients}))

(defn- close-socket
  "Gracefully shuts down a client socket."
  [#^Socket s]
  (when-not (.isClosed s)
    (doto s
      (.shutdownInput)
      (.shutdownOutput)
      (.close))))
  
(defn- close-server
  "Gracefully closes a ServerSocket and it's clients."
  [server]
  (doseq [s @(:clients server)]
    (close-socket s))
  (dosync (ref-set (:clients server) #{}))
  (.close #^ServerSocket (:server server)))

(comment
  "Example usage."
  (defn run-game []
    (let [server (create-server 9000)
	  clients (:clients server)]
      ;; Do stuff in here
      (close-server server))))