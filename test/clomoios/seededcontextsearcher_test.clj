(ns clomoios.seededcontextsearcher-test
  (:use [clomoios.seededcontextsearcher] :reload-all)
  (:use [clojure.test]))

(defn make-scs [& opts]
  (apply make-seeded-context-searcher "models/en-sent.bin" "models/en-token.bin" "models/en-pos-maxent.bin" opts))

;; Test without adding any seeds at creation time
(deftest seeded-context-searcher-noseed-test
  (let [scs (make-scs)]
    (is (= (score scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
           3/2))
    (is (= (reduce + (map second (rank scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")))
           3/2))
    (add-seed scs "I have a pet fox.")
    (is (= (score scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog."))
        3/2)
    (is (= (rank scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
           '(["I own a brown fox." 3/2]
               ["He's my favorite pet." 1/2]
                 ["I like him more than my dog." 0])))))


;; Test adding seed text at creation time
(deftest seeded-context-searcher-seed-text-test
  (let [scs (make-scs {:seed-text "I have a pet fox."})]
    (is (= (score scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog."))
        2)
    (is (= (rank scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
           '(["I own a brown fox." 3/2] ["He's my favorite pet." 1/2] ["I like him more than my dog." 0])))
    (add-seed scs "My fox is not a dog.")
    (is (= (score scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog."))
        5/2)
    (is (= (rank scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
           '(["I own a brown fox." 3/2]
               ["I like him more than my dog." 1/2]
                 ["He's my favorite pet." 1/2])))))


;; Test adding seed score words at creation time
(deftest seeded-context-searcher-seed-words-test
  (let [scs (make-scs {:seed-words {"pet" 1}})]
    (is (= (score scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog."))
        5/2)
    (is (= (rank scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
           '(["I own a brown fox." 3/2]
               ["He's my favorite pet." 1]
                 ["I like him more than my dog." 0])))
    (add-score-words scs {"dog" 2})
    (is (= (score scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog."))
        9/2)
    (is (= (rank scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
           '(["I like him more than my dog." 2]
               ["I own a brown fox." 3/2]
                 ["He's my favorite pet." 1])))))

(deftest seeded-context-searcher-get-words-test
  (let [scs (make-scs {:seed-words {"pet" 1} :seed-text "I have a tame fox."})]
    (is (= (score-words scs "fox")
           {"pet" 1 "fox" 1 "have" 1/2}))))
