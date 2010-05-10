(ns clomoios.seededcontextsearcher
  (:use [clomoios.core :as core])
  (:require [opennlp.nlp :as nlp]))


; Since we calculate the score-words every time, it makes sense to memoize this
; function so it will only be re-run when the seed data has changed.
(def memoized-get-scored-terms (memoize get-scored-terms))


(defprotocol SeededSearcher
  "An interface for searching using seeded text"
  (add-seed [this seed]      "Add seed text to this searcher")
  (score    [this term text] "Score this text in similarity")
  (rank     [this term text] "Rank sentences in this text"))


(defrecord SeededContextSearcher [seedtexts get-sentences tokenize pos-tag])

(extend-protocol SeededSearcher SeededContextSearcher

  (add-seed
    [this seedtext]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)
          pos-tagger (:pos-tag this)
          seedtexts (:seedtexts this)]
      (swap! seedtexts concat [seedtext])))

  (score
    [this term text]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)
          pos-tagger (:pos-tag this)
          seedtexts (:seedtexts this)
          score-words (into {}
                            (map #(memoized-get-scored-terms % term get-sentences tokenizer pos-tagger) @seedtexts))]
      (core/score-text text score-words get-sentences tokenizer)))

  (rank
    [this term text]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)
          pos-tagger (:pos-tag this)
          seedtexts (:seedtexts this)
          score-words (into {}
                            (map #(memoized-get-scored-terms % term get-sentences tokenizer pos-tagger) @seedtexts))]
      (reverse (sort-by second (core/score-sentences text score-words get-sentences tokenizer))))))


(defn make-seeded-context-searcher
  "Generate a new Seeded Context Searcher using the seed text and given models.
  3 models are required, a sentence detector model, a tokenizing model and a
  pos-tagging model."
  [seedtext smodel tmodel pmodel]
  (let [get-sentences (nlp/make-sentence-detector smodel)
        tokenizer (nlp/make-tokenizer tmodel)
        pos-tagger (nlp/make-pos-tagger pmodel)]
    (SeededContextSearcher. (if (vector? seedtext) (atom seedtext) (atom [seedtext])) get-sentences tokenizer pos-tagger)))


(comment

  (use 'clomoios.seededcontextsearcher)
  (def foo (make-seeded-context-searcher "The quick brown fox jumped over the sleeping lazy dog biscuit."  "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))
  (rank foo "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
  (add-seed foo "I have a pet fox.")
  (rank foo "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")

)
