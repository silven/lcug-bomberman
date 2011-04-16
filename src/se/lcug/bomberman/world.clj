(ns se.lcug.bomberman.world)

(defn- load-ascii [level]
  (let [spawnpoints (ref '())
	world-map (ref {})
	hmap (vec (for [line level]
		    (vec (for [c line]
			   {:char c}))))]
    (dotimes [row (count hmap)]
      (dotimes [coll (count (first hmap))]
	(let [cell (-> hmap (get row) (get coll))]
	  (if (= \s (:char cell))
	    (dosync
	     (alter spawnpoints conj [coll row])
	     (alter world-map assoc [coll row]
		    {:char \space :pos [coll row]}))
	    (dosync
	     (alter world-map assoc [coll row] cell))))))
    [world-map spawnpoints]))

(def lvl-1
     [ "wwwww"
       "ws bw"
       "w wbw"
       "wbbbw"
       "wwwww" ])

(def lvl-2
     [ "wwwwwwwwwwwwwww"
       "w   bbbbbbb   w"
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
       "w   bbbbbbb   w"
       "wwwwwwwwwwwwwww" ])
     