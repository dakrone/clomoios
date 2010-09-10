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


(def regular-searcher (cs/make-context-searcher "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))

(def pres-seeded-searcher (scs/make-seeded-context-searcher "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))
(def cars-seeded-searcher (scs/make-seeded-context-searcher "models/EnglishSD.bin.gz" "models/EnglishTok.bin.gz" "models/tag.bin.gz"))

(scs/add-seed pres-seeded-searcher wiki-ford-pres)
(scs/add-seed cars-seeded-searcher wiki-ford-cars)

(println "===========================================================")
(println "++ Score words for motor company seeded searcher:")
(println "Word:" (scs/score-words cars-seeded-searcher "ford"))
(println "===========================================================")
(println "++ Score words for president seeded searcher:")
(println "Word:" (scs/score-words pres-seeded-searcher "ford"))
(println "===========================================================\n")

(println "===========================================================")
(println "++ Regular searcher, presidential essay ++")
(println "Score: " (double (cs/score regular-searcher "ford" essay-ford-pres)))
(println "\n++ Presidential seeded searcher, presidential essay ++")
(println "Score: " (double (scs/score pres-seeded-searcher "ford" essay-ford-pres)))
(println "\n++ Motor company seeded searcher, presidential essay ++")
(println "Score: " (double (scs/score cars-seeded-searcher "ford" essay-ford-pres)))
(println "===========================================================\n")


(println "===========================================================")
(println "++ Regular searcher, motor company essay ++")
(println "Score: " (double (cs/score regular-searcher "ford" essay-ford-cars)))
(println "\n++ Motor company seeded searcher, motor company essay ++")
(println "Score: " (double (scs/score cars-seeded-searcher "ford" essay-ford-cars)))
(println "\n++ Presidential seeded searcher, motor company essay ++")
(println "Score: " (double (scs/score pres-seeded-searcher "ford" essay-ford-cars)))
(println "===========================================================")

;(pprint (filter #(> (second %)) (take 4 (scs/rank cars-seeded-searcher "ford" essay-ford-cars))))

;(println "Word:" (scs/score-words cars-seeded-searcher "ford"))
;(pprint (filter #(> (second %) 0) (take 4 (scs/rank pres-seeded-searcher "ford" essay-ford-pres))))
;(println "Word:" (scs/score-words pres-seeded-searcher "ford"))
;(println "Word:" (scs/score-words pres-seeded-searcher "ford"))
;(pprint (filter #(> (second %) 0) (take 4 (cs/rank regular-searcher "ford" essay-ford-pres))))
;(pprint (filter #(> (second %)) (take 4 (cs/rank regular-searcher "ford" essay-ford-cars))))
