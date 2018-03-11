package com.mfa.scala

/**
  * Created by mfa on 28.11.2016.
  */
object Regex {

  val ipPattern = """(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}""".r
  val clientId = """clientID.*?&""".r
  val productId = """productId.*?&""".r
  val userId = """userId.*?&""".r
  val viewId = """viewID.*?&""".r
  val checkOut = """checkout.*?&""".r
  val eventType = """eventType.*?&""".r
}
