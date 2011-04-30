(ns se.lcug.bomberman.player
  (:use [se.lcug.bomberman.view :only (colors)]))

(defn get-proper-color [n]
  (get (vec (keys colors)) (- (count (keys colors)) n)))

(defn- move
  "Change the state of a controller to indicate
   that the client wants to move."
  [controller dir]
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

(defn create-player
  "Create a player, in the world at the givven spawn point."
  [spawns]
  {:alive? true
   :pos (vec (map #(+ 0.5 %) (first spawns)))
   :max-bombs 1
   :flame-lenght 2
   :speed 1
   :color (get-proper-color (count spawns))})