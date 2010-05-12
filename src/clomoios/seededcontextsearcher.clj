(ns clomoios.seededcontextsearcher
  (:use [clomoios.core :as core])
  (:require [opennlp.nlp :as nlp]))


; Memoize this, since we will be getting scored terms quite often with the same
; seeded text. This way it won't be recalculated unless the seed changes
(def memoized-get-scored-terms (memoize core/get-scored-terms))


(defprotocol SeededSearcher
  "An interface for searching using seeded text"
  (add-seed        [this seedtext]  "Add seed text to this searcher")
  (add-score-words [this words]     "Add score words to this searcher")
  (score           [this term text] "Score this text in similarity")
  (rank            [this term text] "Rank sentences in this text"))


(defrecord SeededContextSearcher [seeded-score-words seeded-text get-sentences tokenize pos-tag])


(extend-protocol SeededSearcher SeededContextSearcher

  (add-seed
    [this seedtext]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)
          pos-tagger (:pos-tag this)
          seeded-text (:seeded-text this)]
      (swap! seeded-text concat [seedtext])))

  (add-score-words
    [this words]
    (let [seeded-score-words (:seeded-score-words this)]
      (swap! seeded-score-words merge words)))

  (score
    [this term text]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)
          pos-tagger (:pos-tag this)
          seeded-text (:seeded-text this)
          seeded-score-words (:seeded-score-words this)
          score-words (merge
                        @seeded-score-words
                        (reduce merge (map #(memoized-get-scored-terms % term get-sentences tokenizer pos-tagger) @seeded-text))
                        (memoized-get-scored-terms text term get-sentences tokenizer pos-tagger))]
      (println score-words)
      (core/score-text text score-words get-sentences tokenizer)))

  (rank
    [this term text]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)
          pos-tagger (:pos-tag this)
          seeded-text (:seeded-text this)
          seeded-score-words (:seeded-score-words this)
          score-words (merge
                        @seeded-score-words
                        (reduce merge (map #(memoized-get-scored-terms % term get-sentences tokenizer pos-tagger) @seeded-text))
                        (memoized-get-scored-terms text term get-sentences tokenizer pos-tagger))]
      (println score-words)
      (reverse (sort-by second (core/score-sentences text score-words get-sentences tokenizer))))))



(defn make-seeded-context-searcher
  "Generate a new Seeded Context Searcher using the seed text and given models.
  3 models are required, a sentence detector model, a tokenizing model and a
  pos-tagging model."
  [smodel tmodel pmodel & seeds]
  (let [get-sentences (nlp/make-sentence-detector smodel)
        tokenizer (nlp/make-tokenizer tmodel)
        pos-tagger (nlp/make-pos-tagger pmodel)
        seed-words (if (:seed-words (first seeds)) (:seed-words (first seeds)) {})
        seed-text (if (:seed-text (first seeds)) [(:seed-text (first seeds))] [])]
    (println (:seed-text seeds))
    (SeededContextSearcher. (atom seed-words) (atom seed-text) get-sentences tokenizer pos-tagger)))


(comment

  (use 'clomoios.seededcontextsearcher)
  (def foo (make-seeded-context-searcher "The quick brown fox jumped over the sleeping lazy dog biscuit."  "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))
  (rank foo "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
  (add-seed foo "I have a pet fox.")
  (rank foo "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")

)
