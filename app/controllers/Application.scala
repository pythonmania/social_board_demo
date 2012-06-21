package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import models._

object Application extends Controller {

  def index = Action {
    Ok(views.html.main(Neo4j.tweetsQuery, Rdbms.tweetsQuery))
  }

  def neo4j(userid: String) = Action {
    Ok(Neo4j.tweetsByUserid(userid)).as(JSON)
  }

  def rdbms(userid: String) = Action {
    Ok(Rdbms.tweetsByUserid(userid)).as(JSON)
  }

  def reset() = Action {
    Neo4j.reset()
    Rdbms.reset()
    Ok
  }

  def pick() = Action {
    Ok(toJson(Map("userid" ->
       Neo4j.pickRandomUser())))
//      Rdbms.pickRandomUser())))
  }
}