(ns se.lcug.bomberman.view
  (:use [se.lcug.bomberman.world :only (load-ascii lvl-1 lvl-2 lvl-3)])
  (:require [clojure.java.io :as io])
  (:import (java.awt Graphics Color Dimension Image)
	   (javax.swing JFrame JPanel SwingUtilities)
	   (java.io File)
	   (java.awt.event KeyAdapter)
	   (javax.imageio ImageIO)))

(defn read-image [name]
  (let [full-name (str "se/lcug/bomberman/" name ".png")
	res (io/resource full-name)]
    (if res
      (ImageIO/read (io/input-stream res))
      (println (str "Could not load file: " full-name)))))

(def colors {:red Color/RED :blue Color/BLUE})

(def tileset (let [names '("bomberman" "fire-cap-south" "fire-horizontal"  "tile-bomb"
"fire-cap-east" "fire-cap-west" "fire-vertical" "tile-wall"
"fire-cap-north" "fire-cross" "tile-block")]
	       (into {} (for [name names]
			  [(keyword name) (read-image name)]))))


(defn- render-cell [g x y width height tile]
  (.drawImage g (:bomberman tileset) x y width height nil))

(defn- do-render [this #^Graphics g world]
  (let [w (.getWidth this)
	h (.getHeight this)
	world-w (:width @world)
	world-h (:height @world)
	tile-w (int (/ w world-w))
	tile-h (int (/ h world-h))]
    (doseq [cell (:map @world)]
      (let [[[x y] type] cell]
	(render-cell g (* x tile-w) (* y tile-h) tile-w tile-h type)))
    (doseq [player (vals (:players @world))]
      (let [[x y] (:pos player)]
	(.setColor g (colors (:color player)))
	(.fillRect g (* x tile-w) (* y tile-h) tile-w tile-h)))))

(defn- start-view
  "Starts a JFrame watching a given world."
  [world]
  (let [pane (doto (proxy [JPanel] []
	       (paintComponent [#^Graphics g]
			       (do-render this g world)))
	       (.setPreferredSize (new Dimension 640 480)))
	frame (doto (new JFrame "LCUG Bomberman 0.1")
		(.setContentPane pane)
		(.pack)
		(.setLocationRelativeTo nil)
		(.setVisible true))]
    (add-watch world :repainter (fn [key refr old new] (.repaint pane)))))
		
(defn start-swing-view [world]
  (SwingUtilities/invokeLater #(start-view world)))
    