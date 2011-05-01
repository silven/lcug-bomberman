(ns se.lcug.bomberman.world
  (:use [se.lcug.bomberman.tile]))

(defn- char-to-tile
  "Translates a single char to a tile to be used in the game world."
  [c]
  (condp = c
      \space nil
      \b (block)
      \w (wall)))

(defn load-ascii
  "Reads a n*m ascii-map representing a game world. Returns a vector
  [dict spawns]. Where dict maps positions as [x y]to Tile records.
  Spawns is a list of valid spawnpoints."
  [level]
  (let [spawnpoints (ref '())
	world-map (ref {})
	height (count level)
	width (count (first level))]
    (dotimes [row height]
      (dotimes [coll width]
	(let [cell (-> level (get row) (get coll))]
	  (if (= \s cell) ;; We want to know what cells are spawnpoints.
	    (dosync
	     (alter spawnpoints conj [coll row])
	     (alter world-map assoc [coll row] (char-to-tile \space)))
	    (dosync
	     (alter world-map assoc [coll row] (char-to-tile cell)))))))
    {:width width :height height :map @world-map :spawnpoints @spawnpoints}))

(def lvl-1
     [ "wwwww"
       "ws bw"
       "w w w"
       "wb sw"
       "wwwww" ])

(def lvl-2
     [ "wwwwwwwwwwwwwww"
       "ws  bbbbbbb  sw"
       "w wbwbwbwbwbw w"
       "w bbb  bbbbbb w"
       "wbwbwbwbwbwbwbw"
       "w  bbbbbb bb bw"
       "wbwbwbwbw wbwbw"
       "wbbbbb bbbbbbbw"
       "wbwbwbwbw wbwbw"
       "w bbb bbbbb  bw"
       "wbwbwbwbwbwbwbw"
       "w bbbbb bbbbb w"
       "w wbwbwbwbw w w"
       "ws  bbbbbbb  sw"
       "wwwwwwwwwwwwwww" ])

(def lvl-3
     [ "wwwwwwwwwwwwwwwwwwwwwwwww"
       "ws  bbbbbbbbbb bbbbbb  sw"
       "w wbwbwbwbwbwbwbwbwbwbw w"
       "wbbb  bbbbbb bbbbbb bbbbw"
       "w wbwbw wbwbwbw wbwbwbwbw"
       "wbbbbbbbbbb bbb bbb  bbbw"
       "w wbwbwbwbwbwbw wbwbwbw w"
       "ws  bbbbbb  bbbbbbbbb  sw"
       "wwwwwwwwwwwwwwwwwwwwwwwww" ])

(defn- ws [len]
  (seq (for [i (range len)] "w")))

(defn- some-bs [len]
  (seq (for [i (range len)] (rand-nth "b bb"))))

(defn- some-wbs [len]
  (concat (interleave (ws (/ len 2)) (some-bs (/ len 2))) "w"))

(defn random-lvl
  "Function to generate larger random maps. Size 13x11 and above (Note: sizes will be made UNeven by inc if needed)"
  [w h]
  (let [width (if (even? w) (inc w) w)
	height (if (even? h) (inc h) h)
	bsfn #(apply str (some-bs (- %1 %2)))
	wbsfn #(apply str (some-wbs (- %1 %2 2)))

	end-piece #(seq [(apply str (ws width)) 
			 (str "ws  b" (bsfn width 10) "b  sw")
			 (str "w wb"  (wbsfn width 8)  "bw w")
			 (str "w bb"  (bsfn width 8)   "bb w")
			 (str "wb"    (wbsfn width 4)    "bw")])
	middle nil
	]
    (vec (concat (end-piece)
		 (concat (for [f (take (- height 10)
		       (cycle (list #(str "w" (bsfn width 2) "w")
				    #(str (wbsfn width 0)))))]
			   (f)))
		 (reverse (end-piece))))))
