package models

import play.api._
import play.api.Play.current
import com.codahale.jerkson.Json._
import java.util.Date
import scala.util.Random
import scala.io.Source
import anorm._
import anorm.SqlParser._
import play.api.db._
import java.util.UUID

case class User(userid: String)

case class Tweet(tweetid: String, text: String, link: String, date: Int)

object Sample {
  val random = new Random
  val users = generateUser(10)
  val tweets = generateTweet(100)
  
  def generateUser(count: Int): Seq[User] = {
    for (i <- 1 to count) yield User(generateRandomString(15))
  }

  def generateTweet(count: Int): Seq[Tweet] = {
    for (i <- 1 to count)
      yield Tweet(generateRandomString(20), generateRandomString(50), generateRandomString(30), random.nextInt())
  }
  
  def generateRandomString(length: Int) = {
	var randomString = UUID.randomUUID.toString
	while (randomString.length < length) {
		randomString += UUID.randomUUID
	}
	randomString.take(length)
  }
}
