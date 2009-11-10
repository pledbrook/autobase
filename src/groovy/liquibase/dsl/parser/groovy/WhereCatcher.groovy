package liquibase.dsl.parser.groovy

import groovy.util.Proxy

class WhereCatcher extends Proxy {

  WhereCatcher(toWrap) {
    if(!toWrap) { throw new IllegalArgumentException("Needs a change to wrap") }
    adaptee = toWrap
  }

  void where(String clause) {
    adaptee.whereClause = clause
  }

}
