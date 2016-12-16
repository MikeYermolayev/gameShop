(ns contacts.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [contacts.core-test]
   [contacts.common-test]))

(enable-console-print!)

(doo-tests 'contacts.core-test
           'contacts.common-test)
