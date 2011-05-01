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

(def pad-map {:none :neutral-pad
	      :left :left-pad
	      :right :right-pad
	      :up :up-pad
	      :down :down-pad})

(defn- render-cell [g x y width height tile]
  (let [k (sprite tile)]
    (.drawImage g (k tileset) x y width height nil)))

(defn- do-render [this #^Graphics g world]
  (let [w (int (* (.getWidth this) 0.8)) ;; Leave 20% 
	h (.getHeight this)
	world-w (:width @world)
	world-h (:height @world)
	tile-w (int (/ w world-w))
	tile-h (int (/ h world-h))]
    (.clearRect g 0 0 w h)
    (doseq [cell (:map @world)]
      (let [[[x y] type] cell]
	(render-cell g (* x tile-w) (* y tile-h) tile-w tile-h type)))
    (.translate g (int (- (/ tile-w 2))) (int (- (/ tile-h 2))))
    (doseq [player (vals (:players @world))]
      (let [[x y] (:pos player)]
	(.setColor g (colors (:color player)))
	(.fillRect g (* x tile-w) (* y tile-h) tile-w tile-h)))
    (.translate g (int (/ tile-w 2)) (int (/ tile-h 2)))
    (.translate g w 0)
    (.setColor g Color/GRAY)
    (.fillRect g 0 0 w h)
    (.setColor g Color/BLACK)
    (doseq [key (:clients @world)]
      (let [control @(get (:controllers @world) key)
	    player (get (:players @world) key)
	    image (tileset
		   (pad-map
		    (if (:move? control)
		      (:dir control)
		      :none)))
	    pad-size (int (/ w 4))]
	(.drawImage g image 0 0 pad-size pad-size nil)
	(.drawString g (:name player) 10 15)
	(.drawString g (str (:color player)) pad-size 15)
	(.translate g 0 pad-size)))))

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
    (add-watch world :repainter
	       (fn [key refr old new] (.repaint pane)))))
		
(defn start-swing-view [world]
  (SwingUtilities/invokeLater #(start-view world)))
    