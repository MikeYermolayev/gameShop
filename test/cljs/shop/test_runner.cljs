(ns shop.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [shop.core-test]
   [shop.common-test]))

(enable-console-print!)

(doo-tests 'shop.core-test
           'shop.common-test)
