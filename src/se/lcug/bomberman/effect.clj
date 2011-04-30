(ns se.lcug.bomberman.effect)

(defmulti elaborate
  {:arglists '([type pos tile player])}
  (fn [type pos tile player]
    type))

(defmethod elaborate :kill-player
  [type _ _ player]
  {:type type
   :player player})

(defmethod elaborate :take-powerup
  [type _ {:keys [kind]} player]
  {:type type
   :player player
   :powerup kind})

(defmethod elaborate :explode
  [type pos _ _]
  {:type type
   :pos pos})

