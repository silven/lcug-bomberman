(ns se.lcug.bomberman.world)

(defn- char-to-tile [c]
  {:char c})

(defn- load-ascii [level]
  (let [spawnpoints (ref '())
	world-map (ref {})]
    (dotimes [row (count level)]
      (dotimes [coll (count (first level))]
	(let [cell (-> level (get row) (get coll))]
	  (if (= \s cell) ;; We want to know what cells are spawnpoints.
	    (dosync
	     (alter spawnpoints conj [coll row])
	     (alter world-map assoc [coll row] (char-to-tile \space)))
	    (dosync
	     (alter world-map assoc [coll row] (char-to-tile cell)))))))
    [world-map spawnpoints]))

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
       "w bbbbbbbbbbb w"
       "wbwbwbwbwbwbwbw"
       "wbbbbbbbbbbbbbw"
       "wbwbwbwbwbwbwbw"
       "wbbbbbbbbbbbbbw"
       "wbwbwbwbwbwbwbw"
       "wbbbbbbbbbbbbbw"
       "wbwbwbwbwbwbwbw"
       "w bbbbbbbbbbb w"
       "w wbwbwbwbwbw w"
       "ws  bbbbbbb  sw"
       "wwwwwwwwwwwwwww" ])
     