(ns clomoios.seededcontextsearcher
  (:use [clomoios.core :as core])
  (:require [opennlp.nlp :as nlp]))


; Memoize this, since we will be getting scored terms quite often with the same
; seeded text. This way it won't be recalculated unless the seed changes
(def memoized-get-scored-terms (memoize core/get-scored-terms))


(defn- get-terms
  "Given a SeededSearcher and a term, return a map of the scored words."
  [searcher term]
  (let [get-sentences (:get-sentences searcher)
        tokenizer (:tokenize searcher)
        pos-tagger (:pos-tag searcher)
        seeded-text (:seeded-text searcher)
        seeded-score-words (:seeded-score-words searcher)]
    (merge @seeded-score-words
           (reduce merge (map #(memoized-get-scored-terms % term get-sentences tokenizer pos-tagger) @seeded-text)))))


(defprotocol SeededSearcher
  "An interface for searching using seeded text"
  (add-seed        [this seedtext]  "Add seed text to this searcher")
  (add-score-words [this words]     "Add score words to this searcher")
  (score-words     [this term]      "Get the computed score words for a given term")
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

  (score-words
    [this term]
    (get-terms this term))

  (score
    [this term text]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)]
      (core/score-text text (get-terms this term) get-sentences tokenizer)))

  (rank
    [this term text]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)]
      (reverse (sort-by second (core/score-sentences text (get-terms this term) get-sentences tokenizer))))))



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
    (SeededContextSearcher. (atom seed-words) (atom seed-text) get-sentences tokenizer pos-tagger)))


(comment

  (use 'clomoios.seededcontextsearcher)
  (def foo (make-seeded-context-searcher "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))
  (rank foo "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
  (add-seed foo "I have a pet fox.")
  (add-score-words foo {"dog" 1/2})
  (rank foo "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")

)
