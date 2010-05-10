(ns clomoios.contextsearcher-test
  (:use [clomoios.contextsearcher] :reload-all)
  (:use [clojure.test]))

(def cs (make-context-searcher "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))

(deftest context-searcher-score-test
         (is (= (score cs "foo" "This is a sentence foo is in. In this one foo goes fishing with Tom.")
                15/4))
         (is (= (score cs "foo" "This is a sentence foo is in.")
                3/2))
         (is (= (score cs "foo" "This is a sentence bar is in.")
                0)))

(deftest context-searcher-rank-test
         (is (= (rank cs "foo" "This is a sentence foo is in. In this one foo goes fishing with Tom.")
                '(["In this one foo goes fishing with Tom." 9/4] ["This is a sentence foo is in. " 3/2])))
         (is (= (rank cs "foo" "This is a sentence foo is in.")
                '(["This is a sentence foo is in." 3/2])))
         (is (= (rank cs "foo" "This is a sentence bar is in.")
                '(["This is a sentence bar is in." 0]))))

