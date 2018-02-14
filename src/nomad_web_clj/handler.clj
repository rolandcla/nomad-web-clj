(ns nomad-web-clj.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.handler.dump :refer [handle-dump]]
            )
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            )
  (:require [hiccup.core :refer :all]
            [hiccup.element :refer :all]))

;; NOMAD Storage --------------------------------------------------------------
(def NOMAD-DATA-DIR "/home/rolandcl/Projects/NOMAD/data")
(def NOMAD-HDF5-DIR (io/file NOMAD-DATA-DIR "hdf5"))

(defn read-cache [hdf5_lev]
  (let [cache-file (io/file NOMAD-HDF5-DIR hdf5_lev "cache.db")
        cache-db {:dbtype "sqlite"
                  :dbname cache-file}
        ]
    (->> (sql/format {:select [:*]
                      :from   [:files]
                      :order-by [:beg_dtime]})
         (jdbc/query cache-db))
    ))
;;-----------------------------------------------------------------------------

;; HTML rendering -------------------------------------------------------------
(defn render-in-table [infos]
  (html
   [:table {:style "width:100%"
            :border "1px solid black"
            }
    [:tr [:th "Path"]]
    (for [info infos]
      [:tr [:td (:path info)]])]
   ))

;;-----------------------------------------------------------------------------

(defn list-files [request]
  (-> (:uri request)
      (subs 1)
      read-cache
      render-in-table))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/hdf5_level_0p1c" [] list-files)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
