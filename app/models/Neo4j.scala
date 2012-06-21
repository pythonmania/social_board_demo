package models

import play.api._
import play.api.Play.current
import com.codahale.jerkson.Json._
import dispatch._
import java.util.Date
import scala.util.Random
import scala.io.Source

object Neo4j {

  val neo4jhost = Play.application.configuration.getString("neo4j.host").get
  val neo4jUrl = url(neo4jhost + "/db/data/node")
  val neo4jCypherUrl = url(neo4jhost + "/db/data/cypher")
  val neo4jRootNode = neo4jhost + "/db/data/node/0"
  val random = new Random()
  val sampleUsersPath = Play.application.configuration.getString("sample.users").get
  val sampleTweetsPath = Play.application.configuration.getString("sample.tweets").get
 
  val tweetsQuery = """START user=node:node_auto_index(userid = {userid})
MATCH user-[:FOLLOWS]->follower-[:TWEETED]->tweet
RETURN tweet.tweetid as id, tweet.text as text, tweet.link as link, tweet.date as date 
ORDER BY tweet.date DESC
LIMIT 20"""

  def tweetsByUserid(userid: String) = {
    val restReq = generate(
      Map("query" -> tweetsQuery,
        "params" -> Map("userid" -> userid))).toString

    val startTime = System.nanoTime

    val res = Http(neo4jCypherUrl
      << (restReq, "application/json")
      >- { jsonStr => jsonStr })

    "{\"elapsed\":" + (System.nanoTime - startTime) / 1000 + ", " + res.toString().drop(1)
  }

  def pickRandomUser() = {
    val usercountReq = generate(
      Map("query" -> """
	START users=node:node_auto_index(category = "users")
	MATCH users-[:USER]->user
	RETURN count(user) as usersCount
      """, "params" -> Map())).toString

    val usercountJsonStr = Http(neo4jCypherUrl
      << (usercountReq, "application/json")
      >- { jsonStr => jsonStr })

    val usercount = parse[Map[String, Object]](usercountJsonStr)
      .getOrElse("data", "")
      .toString()
      .drop(2)
      .dropRight(2)
      .toInt

    val useridReq = generate(
      Map("query" -> """
    	START users=node:node_auto_index(category = "users")
    	MATCH users-[:USER]->user
    	RETURN user.userid as userid
    	SKIP {skipcount}
    	LIMIT 1
          """, "params" -> Map("skipcount" -> random.nextInt(usercount.intValue)))).toString

    val useridJsonStr = Http(neo4jCypherUrl
      << (useridReq, "application/json")
      >- { jsonStr =>
        jsonStr
      })

    val userid = parse[Map[String, Object]](useridJsonStr)
      .getOrElse("data", "")
      .toString()
      .drop(2)
      .dropRight(2)

    userid
  }

  def reset() = {
    // remove all nodes
    removeAll

    // create users node
    val usersLocation = createNode(Map("category" -> "users"))
    createRelation(neo4jRootNode, usersLocation, "USERS")

    // create tweets node
    val tweetsLocation = createNode(Map("category" -> "tweets"))
    createRelation(neo4jRootNode, tweetsLocation, "TWEETS")

    // create user node
    var usersList: List[String] = Nil
    // val users = Source fromFile (sampleUsersPath) getLines ()
    val users = Sample.users
  
    users foreach (user => {
      // val userLocation = createNode(user)
	  val userLocation = createNode(Map("userid" -> user.userid))
      createRelation(usersLocation, userLocation, "USER")
      usersList ::= userLocation
    })

    // create tweet node
    var tweetsList: List[String] = Nil
    // val tweets = Source fromFile (sampleTweetsPath) getLines ()
	val tweets = Sample.tweets
	
    tweets foreach (tweet => {
      // val tweetLocation = createNode(tweet)
	  val tweetLocation = createNode(
        Map("tweetid" -> tweet.tweetid,
          "text" -> tweet.text,
          "link" -> tweet.link,
          "date" -> tweet.date.toString))
      createRelation(tweetsLocation, tweetLocation, "TWEET")
      tweetsList ::= tweetLocation
    })

    // create random user-user relation
    for (i <- 1 to usersList.size * 10) {
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
  }

  def createNode(data: Map[String, String]): String = createNode(generate(data).toString())

  def createNode(data: String): String = {
    Http(neo4jUrl
      << (data, "application/json")
      >:> { header => header getOrElse ("Location", Set()) head
      })
  }

  def createRelation(from: String, to: String, typeStr: String): String = {
    Http(url(from + "/relationships")
      << (generate(Map("to" -> to, "type" -> typeStr)).toString, "application/json")
      >:> { header => header getOrElse ("Location", Set()) head
      })
  }

  def removeAll() = {
    // remove all nodes
    val removeReq = generate(
      Map("query" -> """
START n=node(*)
MATCH n-[r?]-()
WHERE ID(n) <> 0
DELETE n,r
    """, "params" -> Map())).toString

    Http(neo4jCypherUrl
      << (removeReq, "application/json") >|)
  }
}