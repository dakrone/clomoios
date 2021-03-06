(ns clomoios.core
  (:use [clojure.contrib.math :only [abs]])
  (:use [clojure.contrib.seq-utils :only [indexed]])
  (:require [opennlp.tools.filters :as nlpf]))


(defn- mindist
  "Give the minimum distance from the first arg to any value in the second arg."
  [n ns]
  (apply min (map #(abs (- n %)) ns)))


(defn- score-word
  "Score the index-term based on it's distance from any intex-term in the given
  search list. Use the base score to boost matches from 1 up to another value."
  [iword iterms base]
  (let [dist (mindist (first iword) iterms)
        score (if (zero? dist)
                base
                (/ base (* 2 dist)))]
    (if (> dist 2) 0 score)))


(defn- score-words
  "Score a list of words linearly based on how far they are from the
  term.  Base score is optional and is 1 by default.  Case sensitive."
  ([term words] (score-words term words 1))
  ([term words base]
     (let [iwords (indexed words)
           iterms (map first (filter (fn [e] (= (second e) term)) iwords))]
       (if (= 0 (count iterms))
         (map #(vector % 0) words)
         (map #(vector (second %) (score-word % iterms base)) iwords)))))


(defn- nv-filter
  "Filter tagged sentences by noun, verb and >= 3 characters."
  [tagged-sentence]
  (filter #(>= (count (first %)) 3) (nlpf/nouns-and-verbs tagged-sentence)))


(defn- contains-token?
  "Given a sentence, does the given term exist in that sentence?"
  [sentence term tokenizer]
  (let [tokens (tokenizer sentence)]
    (boolean (some #{term} tokens))))


(defn- get-matching-sentences
  "Given a sequence of sentences, return the sentences containing
  the term."
  [sentences term tokenizer]
  (filter #(contains-token? % term tokenizer) sentences))


(defn- get-tagged-sentences
  "Return a sequence of POS-tagged sentences."
  [sentences pos-tagger tokenizer]
  (map #(pos-tagger (tokenizer %)) sentences))


(defn- get-weighted-sentences
  "Given POS-tagged sentences and a term, return a sequence of
  sentences that have been weighted."
  [tagged-sentences term]
  (map #(score-words term (map first (nv-filter %))) tagged-sentences))


(defn- get-new-terms
  "Given a sequence of weighted sentences, return a map of new terms
  to be used for searching."
  [weighted-sentences]
  (into {}
        (map vec
             (partition 2
                        (flatten (map #(filter (fn [pair]
                                                 (not= 0 (second pair))) %)
                                      weighted-sentences))))))


(defn get-scored-terms
  "Given a block of text and a search term, return a map of new search
  terms as keys with weighted score values."
  [text term sentence-finder tokenizer pos-tagger]
  (let [sentences (sentence-finder text)
        matched-sentences (get-matching-sentences sentences term tokenizer)
        tagged-sentences (get-tagged-sentences matched-sentences pos-tagger tokenizer)
        weighted-sentences (get-weighted-sentences tagged-sentences term)
        new-terms (get-new-terms weighted-sentences)]
    new-terms))


(defn score-sentence
  "Given a sentence and a map of words & their scores, return the score
  of the sentence."
  [sentence score-map tokenizer]
  (let [tokens (tokenizer sentence)]
    (reduce + (map #(get score-map % 0) tokens))))


(defn score-sentences
  "Given a text and a map of words/scores. Return a list of sentences
  and their scores."
  [text score-map sentence-finder tokenizer]
  (let [sentences (sentence-finder text)]
    (for [s sentences]
      [s (score-sentence s score-map tokenizer)])))


(defn score-text
  "Score a block of text, given a map of score-words."
  [text score-map sentence-finder tokenizer]
  (let [sentences (sentence-finder text)]
    (reduce + (map #(score-sentence % score-map tokenizer) sentences))))


