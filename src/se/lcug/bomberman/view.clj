(ns se.lcug.bomberman.view
  (:use [se.lcug.bomberman.world :only (load-ascii lvl-1)])
  (:require [clojure.java.io :as io])
  (:import (java.awt Graphics Color Dimension Image)
	   (javax.swing JFrame JPanel SwingUtilities)
	   (java.io File)
	   (javax.imageio ImageIO)))

(defn read-image [name]
  (let [full-name (str "se/lcug/bomberman/" name ".png")
	res (io/resource full-name)]
    (if res
      (ImageIO/read (io/input-stream res))
      (println (str "Could not load file: " full-name)))))

(def tileset (let [names '("bomberman" "fire-cap-south" "fire-horizontal"  "tile-bomb"
"fire-cap-east" "fire-cap-west" "fire-vertical" "tile-wall"
"fire-cap-north" "fire-cross" "tile-block")]
	       (into {} (for [name names]
			  [(keyword name) (read-image name)]))))


(def counter (atom 0))

(defn- render-cell [g width height tile]
  (.drawImage g (:bomberman tileset) 0 0 width height nil))

(defn- do-render [this #^Graphics g world]
  (let [w (.getWidth this) 
	h (.getHeight this)
	world-w (:width world)
	world-h (:height world)
	tile-w (/ w world-w)
	tile-h (/ h world-h)]
    (doseq [row (range world-h)]
	   (doseq [col (range world-w)]
		  (render-cell g tile-w tile-h ((:map world) [col row]))
		  (.translate g tile-w 0))
	   (.translate g (- w) tile-h))))



(defn- start-view
  "Starts a JFrame watching a given world."
  [world]
  (let [pane (doto (proxy [JPanel] []
	       (paintComponent [#^Graphics g]
			       (do-render this g world)))
	       (.setPreferredSize (new Dimension 640 480)))
	frame (doto (new JFrame "LCUG Bomberman")
		(.setContentPane pane)
		(.pack)
		(.setLocationRelativeTo nil)
		(.setVisible true))]
    ;;(add-watcher world #(.repaint pane))
    ))
		
(defn- start-in-swing [world]
  (SwingUtilities/invokeLater #(start-view world)))

(defn init-view
  "Starter function, subject to be removed."
  [lvl]
  (let [world (load-ascii lvl)]
    (start-in-swing world)))
    