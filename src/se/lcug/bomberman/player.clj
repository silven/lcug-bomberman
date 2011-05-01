(ns se.lcug.bomberman.player
  (:use [se.lcug.bomberman.view :only (colors)]))

(defn get-proper-color [n]
  (get (vec (keys colors)) (- (count (keys colors)) n)))

(defn- move-controller
  "Change the state of a controller to indicate
   that the client wants to move."
  [controller dir]
  (dosync (alter controller assoc :dir dir)
	  (alter controller assoc :move? true)))

(defn handle-client-command
  "Function that translates a command-string to state-change."
  [me cmd]
  (condp = cmd
      "UP"      (move-controller me :up)
      "DOWN"    (move-controller me :down)
      "LEFT"    (move-controller me :left)
      "RIGHT"   (move-controller me :right)
      "BOMB"    (dosync (alter me assoc :bomb? true))
      "STOP"    (dosync (alter me assoc :move? false))
      ;; TODO: Rewrite the update function, give it a limit
      "UPDATE"  :update
      :unknown))

(defn create-player
  "Create a player, in the world at the givven spawn point."
  [spawns]
  {:alive? true
   :pos (vec (map #(+ 0.5 %) (first spawns)))
   :max-bombs 1
   :flame-lenght 2
   :speed 0.1
   :color (get-proper-color (count spawns))})


;;; EXAMPLE CODE! SUBJECT TO BE CHANGED!
(defn move-player [player dir]
  (let [speed (:speed player)
	[x y] (:pos player)
	[nx ny] (condp = dir
		    :left [(- x speed) y]
		    :right [(+ x speed) y]
		    :up [x (- y speed)]
		    :down [x (+ y speed)])]
	(assoc player :pos [nx ny])))
		    
(defn update-players-by-action [aworld]
  (doseq [key (:clients @aworld)]
    (let [control @((:controllers @aworld) key)
	  player ((:players @aworld) key)]
      (when (:move? control)
	(dosync (alter aworld update-in [:players]
		       assoc key (move-player
				  player (:dir control))))))))