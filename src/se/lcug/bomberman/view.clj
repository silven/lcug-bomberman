(ns se.lcug.bomberman.view
  (:require [clojure.java.io :as io])
  (:use [se.lcug.bomberman.tile])
  (:import (java.awt Graphics Color Dimension Image)
	   (javax.swing JFrame JPanel SwingUtilities)
	   (java.io File)
	   (java.awt.event KeyAdapter)
	   (javax.imageio ImageIO)))

(defn- read-image [name]
  (let [image (ImageIO/read name)]
    image))

(def colors (sorted-map
	     :white Color/WHITE
	     :red Color/RED
	     :green Color/GREEN
	     :blue Color/BLUE
	     :black Color/BLACK))

(defn- base-name [file]
  (let [name (.getName file)]
    (.substring name 0 (.lastIndexOf name "."))))

(def tileset (let [files  (file-seq (File. "resources/se/lcug/bomberman"))
		   images (for [f files :when (not (.isDirectory f))] f)]
	       (into {} (for [name images]
			  [(keyword (base-name name)) (read-image name)]))))


(defn- render-cell [g x y width height tile]
  (let [k (sprite tile)]
    (println k)
    (.drawImage g (k tileset) x y width height nil)))

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
    