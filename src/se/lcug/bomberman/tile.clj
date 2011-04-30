(ns se.lcug.bomberman.tile)

(def flame-duration 1)

(def bomb-duration 5)

(def ^{:dynamic true} *now*)

;;; Protocols

(defprotocol Tile
  (stepable? [tile]
    "Returns true if you can step onto the tile. Implies that tile is
    Stepable.")
  (ends-flame? [tile]
    "Returns true if a flame does not pass right through the
    tile. Implies that the tile is Burnable.")
  (sprite [tile]
    "Returns an id (e.g. a keyword) for the \"look\" of the tile.")
  (refresh [tile]
    "Returns a 2-vector of a replacement tile and a set of
    effects. This method is called every now and then."))

(defprotocol StepableTile
  (step [tile]
    "Returns a 2-vector of a replacement tile and a set of
    effects. This method is called when a player steps onto the
    tile."))

(defprotocol BurnableTile
  (burn [tile]
    "Returns a 2-vector of a replacement tile and a set of
    effects. This method is called when an expanding flame touches the
    tile."))

;;; Record Types

(declare wall block burning-block powerup burning-powerup flame bomb)

(extend-type nil
  Tile
  (stepable? [_]
    true)
  (ends-flame? [_]
    false)
  (sprite [_]
    :background)
  (refresh [tile]
    [tile #{}])
  StepableTile
  (step [tile]
    [tile #{}]))

(defrecord Wall []
  Tile
  (stepable? [_]
    false)
  (ends-flame? [_]
    true)
  (sprite [_]
    :wall)
  (refresh [tile]
    [tile #{}])
  BurnableTile
  (burn [tile]
    [tile #{}]))

(defrecord Block []
  Tile
  (stepable? [_]
    false)
  (ends-flame? [_]
    true)
  (sprite [_]
    :block)
  (refresh [tile]
    [tile #{}])
  BurnableTile
  (burn [tile]
    [(burning-block *now*) #{}]))

(defrecord BurningBlock [expiration-time]
  Tile
  (stepable? [_]
    false)
  (ends-flame? [_]
    true)
  (sprite [_]
    :burning-block)
  (refresh [tile]
    (if (>= *now* expiration-time)
      [nil #{}]
      [tile #{}]))
  BurnableTile
  (burn [tile]
    [tile #{}]))

(defrecord Powerup [kind]
  Tile
  (stepable? [_]
    true)
  (ends-flame? [_]
    true)
  (sprite [_]
    kind)
  (refresh [tile]
    [tile #{}])
  StepableTile
  (step [_]
    [nil #{:take-powerup}])
  BurnableTile
  (burn [_]
    [(burning-powerup *now*) #{}]))

(defrecord BurningPowerup [expiration-time]
  Tile
  (stepable? [_]
    true)
  (ends-flame? [_]
    true)
  (sprite [_]
    :burning-powerup)
  (refresh [tile]
    (if (>= *now* expiration-time)
      [nil #{}]
      [tile #{}]))
  StepableTile
  (step [tile]
    [tile #{}])
  BurnableTile
  (burn [tile]
    [tile #{}]))

(defrecord Flame [expiration-time]
  Tile
  (stepable? [_]
    true)
  (ends-flame? [_]
    false)
  (sprite [_]
    :flame)
  (refresh [tile]
    (if (>= *now* expiration-time)
      [nil #{}]
      [tile #{}]))
  StepableTile
  (step [tile]
    [tile #{:kill-player}]))

(defrecord Bomb [expiration-time]
  Tile
  (stepable? [_]
    false)
  (ends-flame? [_]
    true)
  (sprite [_]
    :bomb)
  (refresh [tile]
    (if (>= *now* expiration-time)
      [nil #{:explode}]
      [tile #{}]))
  BurnableTile
  (burn [_]
    [nil #{:explode}]))

;;; Constructor Functions

(defn wall []
  (Wall.))

(defn block []
  (Block.))

(defn burning-block [now]
  (BurningBlock. (+ now flame-duration)))

(defn powerup [kind]
  (Powerup. kind))

(defn burning-powerup [now]
  (BurningPowerup. (+ now flame-duration)))

(defn flame [now]
  (Flame. (+ now flame-duration)))

(defn bomb [now]
  (Bomb. (+ now bomb-duration)))

