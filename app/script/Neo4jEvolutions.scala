package script

import scala.io._
import dispatch._
import play.api.libs.json.Json
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.Level
import play.api.libs.json.JsValue
import play.api.libs.json.JsArray
import play.api.libs.json.JsString
import play.api.libs.json.JsString

object Neo4jEvolutions extends App {

  val http = new Http
  val neo4jhost = "http://192.168.10.24:7474"
  val neo4jurl = url(neo4jhost + "/db/data/node")
  val neo4jroot = neo4jhost + "/db/data/node/0"

  // override library log level to info
  val hcLogger: Logger = LoggerFactory.getLogger("org.apache.http").asInstanceOf[Logger]
  hcLogger.setLevel(Level.DEBUG)
  val dispatchLogger: Logger = LoggerFactory.getLogger("dispatch").asInstanceOf[Logger]
  dispatchLogger.setLevel(Level.DEBUG)

  // create users
  //  val usersLocation = createNode(Map("category" -> "users"))
  //  createRelation(neo4jroot, usersLocation, "USERS")
  val usersLocation = neo4jhost + "/db/data/node/10"

  // create user
  //  val users = Source fromFile ("test/data/users_fragment.txt") getLines ()
  //  users foreach (user => {
  //    val userLocation = createNode(user)
  //    createRelation(usersLocation, userLocation, "USER")
  //  })

  // create tweets
  //  val tweetsLocation = createNode(Map("category" -> "tweets"))
  //  createRelation(neo4jroot, tweetsLocation, "TWEETS")
  val tweetsLocation = neo4jhost + "/db/data/node/11"

  // create tweet
  //  val tweets = Source fromFile ("test/data/tweets.txt") getLines ()
  //  tweets foreach (tweet => {
  //    val tweetLocation = createNode(tweet)
  //    createRelation(tweetsLocation, tweetLocation, "TWEET")
  //  })

  deleteNodeByType(usersLocation, "USER")

  def createNode(data: Map[String, String]): String = createNode(Json.toJson(data).toString())

  def createNode(data: String): String = {
    http(neo4jurl
      << (data, "application/json")
      >:> { header => header getOrElse ("Location", Set()) head
      })
  }

  def createRelation(from: String, to: String, typeStr: String): String = {
    http(url(from + "/relationships")
      << (Json.toJson(Map("to" -> to, "type" -> typeStr)).toString, "application/json")
      >:> { header => header getOrElse ("Location", Set()) head
      })
  }

  def deleteNodeByType(from: String, typeStr: String) = {
    val targetNode = http(url(from + "/relationships/all/" + typeStr)
      >- { jsonStr =>
        {
          val jsonArray = Json.parse(jsonStr).as[List[JsValue]]
          for (json <- jsonArray) yield (json \ "end").as[String]
        }
      })
    targetNode map { target => println(target) }
  }

  def searchNode(twid: String) = {
    http(:/("192.168.10.24", 7474)
      / "db" / "data" / "index" / "auto" / "node" / ("?query=twid:" + twid)
      >\ "UTF-8"
      <:< (Map("Accept-Language" -> "ko-KR")) //set some headers
      >- { txt =>
        println(txt)
      })
  }

  def deleteNode(nodeLocation: String) = {
    http(url(nodeLocation)
      .DELETE >|)
  }
}