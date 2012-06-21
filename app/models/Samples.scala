package models

import play.api._
import play.api.Play.current
import scala.util.Random
import java.util.UUID

case class User(userid: String)

case class Tweet(tweetid: String, text: String, link: String, date: Int)

object Sample {
  val random = new Random
  val sampleUsersCount = Play.application.configuration.getString("sample.users.count").get.toInt
  val sampleTweetsCount = Play.application.configuration.getString("sample.tweets.count").get.toInt
  val users = generateUser(sampleUsersCount)
  val tweets = generateTweet(sampleTweetsCount)

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
