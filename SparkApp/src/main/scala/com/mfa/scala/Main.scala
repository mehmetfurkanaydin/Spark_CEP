package com.mfa.scala


import kafka.serializer.StringDecoder
import org.apache.spark.{SparkConf}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils

import collection.mutable.{HashMap, ListBuffer, MultiMap, Set}
import com.mfa.{CalculateCosineSimilarity, KafkaProducerClass}

import scala.io.Source
import spray.json._


case class Event(ip: String, clientId: String, userId: String, viewId: String, productId: String, checkout: String, eventType: String)

case class Rule(eventObject: String, countType: String, operand: String, threshold: String, ceType: String, inform: String)

case class Alert(userId: String, itemList: String, rule: String, eventType: String, notification: String)

case class cfAlert(ip: String, currentItems: String, secondIp: String, secondItems: String, similarity: String, eventType: String, notification: String)

case class eventAlert(ip: String, user: String, product: String, checkout: String, eventType: String)

/**
  * Created by mfa on 14.12.2016.
  */
object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val alertFormat = jsonFormat5(Alert)
}

object MyCFProtocol extends DefaultJsonProtocol {
  implicit val cfAlertFormat = jsonFormat7(cfAlert)
}

object eventAlertProtocol extends DefaultJsonProtocol {
  implicit val cfAlertFormat = jsonFormat5(eventAlert)
}

object Main {
  def main(args: Array[String]) {


    val filename = "data/rules.txt"
    val cffile = "data/CF.txt"

    val ruleList = new ListBuffer[Rule]()
    var lineCount = 0
    for (line <- Source.fromFile(filename).getLines()) {
      if (lineCount > 1) {
        val splittedLine = line.split(",")
        val rule = new Rule(splittedLine(1), splittedLine(2), splittedLine(3), splittedLine(4), splittedLine(5), splittedLine(6))
        ruleList += rule
      }
      lineCount = lineCount + 1
    }

    ruleList.foreach(x => println(x))

    var cfSimilarity = 0.0

    for (line <- Source.fromFile(cffile).getLines()) {
      val splittedLine = line.split(":")
      cfSimilarity = splittedLine(1).toDouble
    }

    // Create the context with a 1 second batch size
    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val ssc = new StreamingContext(conf, Seconds(1))

    val kafkaParams = Map("metadata.broker.list" -> "localhost:9092")
    // List of topics you want to listen for from Kafka
    val topics = List("eventLogs").toSet
    // Create our Kafka stream, which will contain (topic,message) pairs. We tack a
    // map(_._2) at the end in order to only get the messages, which contain individual
    // lines of data.
    val lines = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaParams, topics).map(_._2)


    val currentLog = lines.map(x => (x, 1)).reduceByKeyAndWindow(_ + _, _ - _, Seconds(300), Seconds(1))

    val ips = currentLog.map(x => {
      val ip = Regex.ipPattern.findFirstIn(x.toString()) getOrElse "clientId=[error]"
      val clientId = Regex.clientId.findFirstIn(x.toString()) getOrElse "clientId=[error]"
      val client = clientId.split("=")
      val userId = Regex.userId.findFirstIn(x.toString()) getOrElse "userId=0"
      val user = userId.split("=")
      val viewId = Regex.viewId.findFirstIn(x.toString()) getOrElse "viewId=0"
      val view = viewId.split("=")
      val productId = Regex.productId.findFirstIn(x.toString()) getOrElse "productId=0"
      val product = productId.split("=")
      val checkoutId = Regex.checkOut.findFirstIn(x.toString()) getOrElse "checkout=0"
      val checkout = checkoutId.split("=")
      val eventTypeId = Regex.eventType.findFirstIn(x.toString()) getOrElse "eventType=0"
      val eventType = eventTypeId.split("=")
      val event: Event = new Event(ip, client(1), user(1), view(1), product(1), checkout(1), eventType(1))
      (event)
    })

    ips.print()

    val count = ips.count()
    count.print()


    val ipProduct = new HashMap[String, Set[String]] with MultiMap[String, String]
    val checkoutMap = new HashMap[String, Set[String]] with MultiMap[String, String]
    val checkoutProductMap = new HashMap[String, Set[String]] with MultiMap[String, String]

    val viewMap = new HashMap[String, Set[String]] with MultiMap[String, String]
    val visitMap = new HashMap[String, Set[String]] with MultiMap[String, String]
    val listMap = new HashMap[String, Set[String]] with MultiMap[String, String]


    ips.foreachRDD((rdd, time) => {

      if (rdd.count() > 0) {

        val elements = rdd.collect()
        for (element <- elements) {
          val ip = (element.ip).split("&")
          val user = (element.userId).split("&")
          val product = (element.productId).split("&")
          val checkout = (element.checkout).split("&")
          val eventType = (element.eventType).split("&")
          val event = returnFirstString(eventType).head

          import eventAlertProtocol._
          import spray.json._

          val jsonEvent = eventAlert(returnFirstString(ip).head, returnFirstString(user).head, returnFirstString(product).head, returnFirstString(checkout).head, event).toJson
          val jsonEventString = jsonEvent.toString()

          //sendEventMessageKafka("events", jsonEventString)


          event match {
            case "checkOut" => {

              if ((!returnFirstString(product).head.equals("0")) && (!returnFirstString(checkout).head.equals("0"))) {
                checkoutMap.addBinding(returnFirstString(user).head, returnFirstString(checkout).head)
                ipProduct.addBinding(returnFirstString(ip).head, returnFirstString(product).head)
                checkoutProductMap.addBinding(returnFirstString(user).head, returnFirstString(product).head)
              }

            }
            case "visit" => {
              if (!returnFirstString(product).head.equals("0")) {
                visitMap.addBinding(returnFirstString(user).head, returnFirstString(product).head)
              }

            }
            case "view" => {
              if (!returnFirstString(product).head.equals("0")) {
                viewMap.addBinding(returnFirstString(user).head, returnFirstString(product).head)
              }
            }
            case "list" => {

            }
          }

        }

        val ipProductMap = ipProduct.groupBy(e => e._1).mapValues(e => e.map(x => x._2).toList)
        val userCheckoutMap = checkoutMap.groupBy(e => e._1).mapValues(e => e.map(x => x._2).toList)
        val userProductCheckoutMap = checkoutProductMap.groupBy(e => e._1).mapValues(e => e.map(x => x._2).toList)


        val userViewMap = viewMap.groupBy(e => e._1).mapValues(e => e.map(x => x._2).toList)
        val userVisitMap = visitMap.groupBy(e => e._1).mapValues(e => e.map(x => x._2).toList)

        println(ipProductMap)


        for ((ipNo, productList) <- ipProductMap) {
          val currentIp = ipNo
          var currentProductCount = 0

          for (ListOfProducts <- productList) {
            var currentIpProduct = ""
            for (currentProduct <- ListOfProducts) {
              currentIpProduct = currentIpProduct + currentProduct + " "
              currentProductCount = currentProductCount + 1
            }

            for ((secondIpNo, secondProductList) <- ipProductMap) {
              val secondIp = secondIpNo
              var secondProductCount = 0

              for (secondListOfProducts <- secondProductList) {
                var secondIpProduct = ""
                for (secondCurrentProduct <- secondListOfProducts) {
                  secondIpProduct = secondIpProduct + secondCurrentProduct + " "
                  secondProductCount = secondProductCount + 1
                }

                if (currentProductCount > 2 && secondProductCount > 2 && (!currentIp.equals(secondIp))) {
                  val resultSim = calculateFunction(currentIpProduct, secondIpProduct)

                  if (resultSim > cfSimilarity) {

                    import MyCFProtocol._
                    import spray.json._

                    val jsonCF = cfAlert(currentIp, currentIpProduct, secondIp.toString, secondIpProduct, resultSim.toString, "cf", "BOTH").toJson
                    val jsonCFString = jsonCF.toString()

                    sendMessageKafka("cfAlerts", jsonCFString)
                    /* println("Alert => " + resultSim.toString)
                     println("Current Ip : " + currentIp + " Current Items => " + currentIpProduct)
                     println("Ip:" + secondIp.toString + " => " + secondIpProduct)
                     println("=================================================") */
                  }
                }

              }

            }

          }

        }



        ruleList.foreach(rule => {
          rule.eventObject match {
            case "checkout" => {
              doRule(userCheckoutMap, rule, "checkout")
            }
            case "visit" => {
              doRule(userVisitMap, rule, "visit")
            }
            case "view" => {
              doRule(userViewMap, rule, "view")
            }
            case "checkoutProduct" => {
              doRule(userProductCheckoutMap, rule, "checkoutProduct")
            }
          }
        })


      }
    })



    ssc.checkpoint("home/Desktop/checkpoint/")
    ssc.start()
    ssc.awaitTermination()
  }

  def returnFirstString(a: Array[String]): Option[String] = a.headOption

  def calculateFunction(event: String, currentEvent: String): Double = {
    val calculateSimilarity: CalculateCosineSimilarity = new CalculateCosineSimilarity()
    val sim = calculateSimilarity.CalculateSimilarity(event, currentEvent)
    return sim
  }

  def sendMessageKafka(topic: String, message: String): String = {
    val kafkaProducerClass: KafkaProducerClass = new KafkaProducerClass()
    kafkaProducerClass.sendMessage(topic, message)
    return "Kafka Producer Run"
  }


  def doRule(map: Map[String, scala.List[Set[String]]], rule: Rule, eventType: String): Int = {

    rule.countType match {
      case "total" => {


        for ((userNo, itemList) <- map) {
          var totalItem = 0
          for (ListOfItems <- itemList) {
            for (currentItem <- ListOfItems) {
              totalItem = totalItem + Integer.valueOf(currentItem)
            }
          }
          val result = calculateRule(totalItem, rule)
          if (result == true) {

            /* println("Alert ==> " + rule)
             println(userNo)
             println(itemList) */

            if (!userNo.equals("0")) {

              import MyJsonProtocol._
              import spray.json._
              val alertJson = Alert(userNo, itemList.toString(), rule.toString, rule.ceType, rule.inform).toJson
              val jsonString = alertJson.toString()
              sendMessageKafka("ruleAlerts", jsonString)
            }
          }
        }

      }
      case "count" => {

        for ((userNo, itemList) <- map) {
          var countItem = 0
          for (ListOfItems <- itemList) {
            for (currentItem <- ListOfItems) {
              countItem = countItem + 1
            }

          }

          val result = calculateRule(countItem, rule)
          if (result == true) {
            /* println("Alert ==> " + rule)
             println(userNo)
             println(itemList) */

            if (!userNo.equals("0")) {

              import MyJsonProtocol._
              import spray.json._

              val alertJson = Alert(userNo, itemList.toString(), rule.toString, rule.ceType, rule.inform).toJson
              val jsonString = alertJson.toString()
              sendMessageKafka("ruleAlerts", jsonString)
            }
          }
        }

      }
    }

    return 0
  }

  def calculateRule(value: Int, rule: Rule): Boolean = {
    var result = false;
    rule.operand match {
      case ">" => {
        if (value > Integer.valueOf(rule.threshold)) {
          result = true
        }
        return result
      }
      case "<" => {
        if (value < Integer.valueOf(rule.threshold)) {
          result = true
        }
        return result
      }
      case "=" => {
        if (value == Integer.valueOf(rule.threshold)) {
          result = true
        }
        return result
      }
    }

  }

}