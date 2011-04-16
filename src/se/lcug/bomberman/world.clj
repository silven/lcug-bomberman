(ns se.lcug.bomberman.world)

(defn- char-to-tile
  "Translates a single char to a tile to be used in the game world.
  TODO: Implement using the Tile records"
  [c]
  {:char c})

(defn- load-ascii
  "Reads a n*m ascii-map representing a game world. Returns a vector
  [dict spawns]. Where dict maps positions as [x y]to Tile records.
  Spawns is a list of valid spawnpoints."
  [level]
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

(def lvl-3
     [ "wwwwwwwwwwwwwwwwwwwwwwwww"
       "ws  bbbbbbbbbbbbbbbbb  sw"
       "w wbwbwbwbwbwbwbwbwbwbw w"
       "wbbbbbbbbbbbbbbbbbbbbbbbw"
       "wbwbwbwbwbwbwbwbwbwbwbwbw"
       "wbbbbbbbbbbbbbbbbbbbbbbbw"
       "w wbwbwbwbwbwbwbwbwbwbw w"
       "ws  bbbbbbbbbbbbbbbbb  sw"
       "wwwwwwwwwwwwwwwwwwwwwwwww" ])
