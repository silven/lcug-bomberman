(ns se.lcug.bomberman.bomb)

(defn blast-zone
  "Calculates the positions to be blasted away
  by a bomb placed at [x y] with a given strength."
  [[x y] strength]
  (let [xs (range (- x strength) (+ x (inc strength)))
	ys (range (- y strength) (+ y (inc strength)))]
    (set (interleave (for [dx xs] [dx y]) (for [dy ys] [x dy])))))

(defn place-bomb
  "Places a bomb in the world at position [x y] effectivly
  replacing the tile at the givven position.
  TODO: Make it actually place a bomb."
  [world [x y]]
  (assoc world [x y] {:is-a-bomb "motherfucker"}))