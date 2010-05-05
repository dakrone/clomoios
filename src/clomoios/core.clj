(ns clomoios.core)

(defprotocol Searcher
   "An interface for searching"
   (score  [this] "Score this text in similarity")
   (rank   [this] "Rank sentences in this text"))

(deftype ContextSearcher [] Searcher
  (score [this] nil)
  (rank  [this] nil))

(defn make-context-searcher
  []
  (ContextSearcher.))

(defn score
  [s]
  (.score s))

(defn rank
  [s]
  (.rank s))
