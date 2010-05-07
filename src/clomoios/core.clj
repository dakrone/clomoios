(ns clomoios.core)


(defprotocol Searcher
  "An interface for searching"
  (score  [this] "Score this text in similarity")
  (rank   [this] "Rank sentences in this text"))

