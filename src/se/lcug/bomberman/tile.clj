(ns se.lcug.bomberman.tile)

(defprotocol Tile
  (stepable? [tile]
    "Returns true if you can step onto the tile. Implies that tile is
    Stepable.")
  (ends-flame? [tile]
    "Returns true if a flame does not pass right through the
    tile. Implies that the tile is Burnable.")
  (sprite [tile]
    "Returns an id (e.g. a keyword) for the \"look\" of the tile."))

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

(extend-type nil
  Tile
  (stepable? [_]
    true)
  (ends-flame? [_]
    false)
  (sprite [_]
    :background)
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
  BurnableTile
  (burn [tile]
    [tile #{}]))

(defrecord BurningBlock []
  Tile
  (stepable? [_]
    false)
  (ends-flame? [_]
    true)
  (sprite [_]
    :burning-block)
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
  BurnableTile
  (burn [tile]
    [(merge (BurningBlock.) tile) #{}]))

(defrecord BurningPowerup []
  Tile
  (stepable? [_]
    true)
  (ends-flame? [_]
    true)
  (sprite [_]
    :burning-powerup)
  StepableTile
  (step [tile]
    [tile #{}])
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
  StepableTile
  (step [_]
    [nil #{:take-powerup}])
  BurnableTile
  (burn [_]
    [(BurningPowerup.) #{}]))

(defrecord Flame []
  Tile
  (stepable? [_]
    true)
  (ends-flame? [_]
    false)
  (sprite [_]
    :flame)
  StepableTile
  (step [tile]
    [tile #{:kill-player}]))

(defrecord Bomb []
  Tile
  (stepable? [_]
    false)
  (ends-flame? [_]
    true)
  (sprite [_]
    :bomb)
  BurnableTile
  (burn [_]
    [nil #{:explode-bomb}]))

