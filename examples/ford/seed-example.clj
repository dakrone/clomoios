(ns seed-example
  (:require [clomoios.contextsearcher :as cs])
  (:require [clomoios.seededcontextsearcher :as scs])
  (:require [opennlp.nlp :as nlp])
  (:use [clojure.contrib.pprint :only [pprint]]))


(def wiki-ford-pres (.toLowerCase (slurp "examples/ford/pres.txt")))
(def wiki-ford-cars (.toLowerCase (slurp "examples/ford/cars.txt")))
(def essay-ford-pres (.toLowerCase (slurp "examples/ford/pres-essay.txt")))
(def essay-ford-cars (.toLowerCase (slurp "examples/ford/cars-essay.txt")))
(def tokenize (nlp/make-tokenizer "models/EnglishTok.bin.gz"))
(println "pres.txt" (count (tokenize wiki-ford-pres)))
(println "cars.txt" (count (tokenize wiki-ford-cars)))
(println "epres.txt" (count (tokenize essay-ford-pres)))
(println "ecars.txt" (count (tokenize essay-ford-cars)))


;(def regular-searcher (cs/make-context-searcher "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))

;(println "++ Regular searcher, presidential essay ++")
;;(pprint (cs/rank regular-searcher "ford" essay-ford-pres))
;(println "Score: " (double (cs/score regular-searcher "ford" essay-ford-pres)))
;(println "-----------------------------------------------------------")
;(println "++ Regular searcher, motor company essay ++")
;;(pprint (cs/rank regular-searcher "ford" essay-ford-cars))
;(println "Score: " (double (cs/score regular-searcher "ford" essay-ford-cars)))



(println "===========================================================")

(def pres-seeded-searcher (scs/make-seeded-context-searcher "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))
(def cars-seeded-searcher (scs/make-seeded-context-searcher "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))

(scs/add-seed pres-seeded-searcher wiki-ford-pres)
(scs/add-seed cars-seeded-searcher wiki-ford-cars)
(println "Presidential score words")
(println (scs/score-words pres-seeded-searcher "ford"))
(println "Motor company score words")
(println (scs/score-words cars-seeded-searcher "ford"))
(println "===========================================================")

(println "++ Presidential seeded searcher, presidential essay ++")
(println (double (scs/score pres-seeded-searcher "ford" essay-ford-pres)))
(println "-----------------------------------------------------------")
(println "++ Motor company seeded searcher, presidential essay ++")
(println (double (scs/score cars-seeded-searcher "ford" essay-ford-pres)))
(println "===========================================================")

(println "++ Presidential seeded searcher, motor company essay ++")
(println (double (scs/score pres-seeded-searcher "ford" essay-ford-cars)))
(println "-----------------------------------------------------------")
(println "++ Motor company seeded searcher, motor company essay ++")
(println (double (scs/score cars-seeded-searcher "ford" essay-ford-cars)))
