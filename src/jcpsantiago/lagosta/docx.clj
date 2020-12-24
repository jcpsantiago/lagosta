(ns jcpsantiago.lagosta.docx
  "Functions to fill a docx template"
  (:require [stencil.api :refer [prepare render!]]
            [clojure.java.io :as io]))


(def output-path (or (System/getenv "LETTER_OUTPUT_DIR")
                     ; FIXME should this be /tmp/output instead?
                     "output/output.docx"))


(def letter-template
  (prepare (io/resource "docx/letter_template.docx")))


(defn render-template [template output-path data]
  (render! template data :output output-path))


(def render-letter! (partial render-template letter-template output-path))
 
; (render-letter! 
;   {:rows [{:name "foo" :address "bar 23 Berlin"} 
;           {:name "me" :address "stallschreiber 29"}]})
