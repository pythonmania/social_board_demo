package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._
import com.codahale.jerkson.Json._
import play.api.Play.current
import dispatch._
import dispatch.json.JsHttp._
import java.util.Date
import scala.util.Random

object Application extends Controller {

  val neo4jhost = Play.application.configuration.getString("neo4j.host").get
  val neo4jurl = url(neo4jhost + "/db/data/cypher")
  val neo4jquery = """
	START user=node:node_auto_index(userid = {userid})
	MATCH user-[:FOLLOWS]->follower-[:TWEETED]->tweet
	RETURN tweet.tweetid as id, tweet.text as text, tweet.link as link, tweet.date as date 
	ORDER BY tweet.date DESC
	LIMIT 20
      """

  val random = new Random()

  def index = Action {
    Ok(views.html.main())
  }

  def neo4j(userid: String) = Action {
    val startTime = System.currentTimeMillis

    val neo4jQuery = generate(
      Map("query" -> neo4jquery,
        "params" -> Map("userid" -> userid))).toString

    val req = neo4jurl << (neo4jQuery, "application/json")

    val res = Http(
      req
        ># { json => json
        }).asInstanceOf[dispatch.json.JsObject]

    val resWithElapsed = "{\"elapsed\":" + (System.currentTimeMillis - startTime) + ", " + res.toString().drop(1)
    Ok(resWithElapsed).as(JSON)
  }

  def reset() = Action {
    println("reset clicked")
    Ok
  }

  def pick() = Action {
    val usercountQuery = generate(
      Map("query" -> """
	START users=node:node_auto_index(category = "users")
	MATCH users-[:USER]->user
	RETURN count(user) as usersCount
      """, "params" -> Map())).toString

    val usercount = Http(neo4jurl
      << (usercountQuery, "application/json")
      ># { json =>
        json
          .asInstanceOf[dispatch.json.JsObject].self
          .apply(dispatch.json.JsString("data"))
          .asInstanceOf[dispatch.json.JsArray].self
          .head
          .asInstanceOf[dispatch.json.JsArray].self
          .head
      }).asInstanceOf[dispatch.json.JsNumber].self

    val userQuery = generate(
      Map("query" -> """
	START users=node:node_auto_index(category = "users")
	MATCH users-[:USER]->user
	RETURN user.userid as userid
	SKIP {skipcount}
	LIMIT 1
      """, "params" -> Map("skipcount" -> random.nextInt(usercount.intValue)))).toString

    val userid = Http(neo4jurl
      << (userQuery, "application/json")
      ># { json =>
        json
          .asInstanceOf[dispatch.json.JsObject].self
          .apply(dispatch.json.JsString("data"))
          .asInstanceOf[dispatch.json.JsArray].self
          .head
          .asInstanceOf[dispatch.json.JsArray].self
          .head
          .asInstanceOf[dispatch.json.JsString].self
      }).self

    Ok(toJson(Map("userid" -> userid)))
  }
}