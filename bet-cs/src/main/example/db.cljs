(ns example.db)

;; initial state of app-db
(defonce app-db {:counter 0
                 :counter-tappable? true
                 :teams {:furia {:image (js/require "../assets/furia.png") :score 1000}
                         :navi {:image (js/require "../assets/navi.png") :score 1500}
                         :sk {:image (js/require "../assets/sk.png") :score 3000}
                         :fuia {:image (js/require "../assets/furia.png") :score 1000}
                         :nai {:image (js/require "../assets/navi.png") :score 1500}
                         :s {:image (js/require "../assets/sk.png") :score 3000}}})

