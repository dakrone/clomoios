(ns clomoios.contextsearcher
  (:use [clomoios.core :as core])
  (:require [opennlp.nlp :as nlp]))


(defprotocol Searcher
  "An interface for searching"
  (score  [this term text] "Score this text in similarity")
  (rank   [this term text] "Rank sentences in this text"))


(defrecord ContextSearcher [get-sentences tokenize pos-tag])

(extend-protocol Searcher ContextSearcher

  (score
    [this term text]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)
          pos-tagger (:pos-tag this)
          score-words (core/get-scored-terms text term get-sentences tokenizer pos-tagger)]
      (core/score-text text score-words get-sentences tokenizer)))

  (rank
    [this term text]
    (let [get-sentences (:get-sentences this)
          tokenizer (:tokenize this)
          pos-tagger (:pos-tag this)
          score-words (core/get-scored-terms text term get-sentences tokenizer pos-tagger)]
      (reverse (sort-by second (core/score-sentences text score-words get-sentences tokenizer))))))


(defn make-context-searcher
  "Generate a new Context Searcher using the given models. 3 models are
  required, a sentence detector model, a tokenizing model and a pos-tagging
  model."
  [smodel tmodel pmodel]
  (let [get-sentences (nlp/make-sentence-detector smodel)
        tokenizer (nlp/make-tokenizer tmodel)
        pos-tagger (nlp/make-pos-tagger pmodel)]
    (ContextSearcher. get-sentences tokenizer pos-tagger)))


(comment

  (use 'clomoios.contextsearcher)
  (def cs (make-context-searcher "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))
  (score cs "foo" "This is a sentence foo is in. In this one foo goes fishing with Tom.")
  (rank cs "foo" "This is a sentence foo is in. In this one foo goes fishing with Tom.")
  (score cs "foo" "This is a sentence foo is in.")
  (rank cs "foo" "This is a sentence foo is in.")
  (score cs "foo" "This is a sentence bar is in.")
  (rank cs "foo" "This is a sentence bar is in.")

)
