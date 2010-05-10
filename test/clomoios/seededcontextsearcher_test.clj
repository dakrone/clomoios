(ns clomoios.seededcontextsearcher-test
  (:use [clomoios.seededcontextsearcher] :reload-all)
  (:use [clojure.test]))

(deftest seeded-context-searcher-score-test
         (let [scs (make-seeded-context-searcher "The quick brown fox jumped over the sleeping lazy dog biscuit."  "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz")] 
           (is (= (score scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
                  1))
           (add-seed scs "I have a pet fox.")
           (is (= (do
                    (score scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog."))
                  3/2))))

(deftest seeded-context-searcher-rank-test
         (let [scs (make-seeded-context-searcher "The quick brown fox jumped over the sleeping lazy dog biscuit."  "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz")] 
           (is (= (rank scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
                  '(["I own a brown fox. " 1] ["I like him more than my dog." 0] ["He's my favorite pet. " 0])))
           (add-seed scs "I have a pet fox.")
           (is (= (rank scs "fox" "I own a brown fox. He's my favorite pet. I like him more than my dog.")
                  '(["I own a brown fox. " 1] ["He's my favorite pet. " 1/2] ["I like him more than my dog." 0])))))

