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
import scala.util.Random

object Neo4jEvolutions extends App {

  val http = new Http
  val neo4jhost = "http://192.168.10.24:7474"
  val neo4jurl = url(neo4jhost + "/db/data/node")
  val neo4jroot = neo4jhost + "/db/data/node/0"
  val user_2_user_count = 300
  var usersList: List[String] = Nil
  var tweetsList: List[String] = Nil
  val random = new Random()

  // override library log level to info
  val hcLogger: Logger = LoggerFactory.getLogger("org.apache.http").asInstanceOf[Logger]
  hcLogger.setLevel(Level.INFO)
  val dispatchLogger: Logger = LoggerFactory.getLogger("dispatch").asInstanceOf[Logger]
  dispatchLogger.setLevel(Level.INFO)

  // create users
  val usersLocation = createNode(Map("category" -> "users"))
  createRelation(neo4jroot, usersLocation, "USERS")

  // create user
  val users = Source fromFile ("test/data/users_small.txt") getLines ()
  users foreach (user => {
    val userLocation = createNode(user)
    createRelation(usersLocation, userLocation, "USER")
    usersList ::= userLocation
  })

  // create tweets
  val tweetsLocation = createNode(Map("category" -> "tweets"))
  createRelation(neo4jroot, tweetsLocation, "TWEETS")

  // create tweet
  val tweets = Source fromFile ("test/data/tweets_small.txt") getLines ()
  tweets foreach (tweet => {
    val tweetLocation = createNode(tweet)
    createRelation(tweetsLocation, tweetLocation, "TWEET")
    tweetsList ::= tweetLocation
  })

  // create random user-user relation
  for (i <- 1 to user_2_user_count) {
    val user1 = usersList(random.nextInt(usersList.size))
    var user2 = ""
    do {
      user2 = usersList(random.nextInt(usersList.size))
    } while (user1 == user2)
    createRelation(user1, user2, "FOLLOWS")
  }

  // create random user-tweet relation
  tweetsList foreach (tweet => {
    val user = usersList(random.nextInt(usersList.size))
    createRelation(user, tweet, "TWEETED")
  })

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

  val find_active_users = """
START users=node:node_auto_index(category = "users")
MATCH users-[:USER]->user-[:FOLLOWS]->follows
RETURN user, count(follows) as followsCount
ORDER BY followsCount DESC;
    """

  val find_famous_users = """
START users=node:node_auto_index(category = "users")
MATCH users-[:USER]->user<-[:FOLLOWS]-someother
RETURN user, count(someother) as followedCount
ORDER BY followedCount DESC;
  """
}