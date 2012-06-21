package controllers

import play.api._
import play.api.Play.current
import com.codahale.jerkson.Json._
import java.util.Date
import scala.util.Random
import scala.io.Source
import anorm._
import anorm.SqlParser._
import play.api.db._

object Rdbms {
  val random = new Random()
  val sampleUsersPath = Play.application.configuration.getString("sample.users").get
  val sampleTweetsPath = Play.application.configuration.getString("sample.tweets").get
  val tweetsQuery = """SELECT t.tweetid, 
       t.text, 
       t.link, 
       t.date 
FROM   tweet t, user2tweet m, user2user u
WHERE  t.tweetid = m.tweetid 
AND    m.userid = u.followerid
AND    u.userid = {userid} 
ORDER  BY t.date DESC
LIMIT  20"""
    
  def tweetsByUserid(userid: String) = {
    DB.withConnection { implicit c =>
      val startTime = System.nanoTime

      val tweets = SQL(tweetsQuery)
        .on("userid" -> userid)
        .as(str("tweetid") ~ str("text") ~ str("link") ~ int("date") *)
        .map {
          case tweetid ~ text ~ link ~ date => Seq(tweetid, text, link, date)
        } seq

      generate(Map("data" -> tweets, "elapsed" -> (System.nanoTime - startTime) / 1000))
    }
  }

  def pickRandomUser() = {
    DB.withConnection { implicit c =>
      val usercount: Long =
        SQL("SELECT count(*) FROM user").as(scalar[Long].single)

      val userid: String =
        SQL("""
SELECT userid 
FROM   (SELECT u.*, 
               rownum AS R 
        FROM   USER u) 
WHERE  r = {rownumber} 
            """)
          .on("rownumber" -> (random.nextInt(usercount.intValue) + 1))
          .as(scalar[String].single)

      userid
    }
  }

  def reset() = {
    // remove all nodes
    DB.withConnection { implicit c =>
      SQL("truncate table user").execute()
      SQL("truncate table tweet").execute()
      SQL("truncate table user2user").execute()
      SQL("truncate table user2tweet").execute()
    }

    // create user
    var usersList: List[String] = Nil
    DB.withConnection { implicit c =>
      val users = Source fromFile (sampleUsersPath) getLines ()
      users foreach (user => {
        val userid = parse[Map[String, String]](user).getOrElse("userid", "")

        SQL("insert into user (userid) values ({userid})")
          .on('userid -> userid)
          .executeInsert()
        usersList ::= userid
      })

      c.commit
    }

    // create tweet
    var tweetsList: List[String] = Nil
    DB.withConnection { implicit c =>
      val tweets = Source fromFile (sampleTweetsPath) getLines ()
      tweets foreach (tweets => {
        val tweetMap = parse[Map[String, String]](tweets)
        val tweetid = tweetMap.getOrElse("tweetid", "")

        SQL("""
            insert into tweet (tweetid, text, link, date) 
            values ({tweetid}, {text}, {link}, {date})
            """)
          .on('tweetid -> tweetMap.getOrElse("tweetid", ""),
            'text -> tweetMap.getOrElse("text", ""),
            'link -> tweetMap.getOrElse("link", ""),
            'date -> tweetMap.getOrElse("date", ""))
          .executeInsert()
        tweetsList ::= tweetid
      })

      c.commit
    }

    // create random user-user relation
    DB.withConnection { implicit c =>
      for (i <- 1 to usersList.size * 10) {
        val user1 = usersList(random.nextInt(usersList.size))
        var user2 = ""
        do {
          user2 = usersList(random.nextInt(usersList.size))
        } while (user1 == user2)
        SQL("""
            insert into user2user (userid, followerid) 
            values ({user1}, {user2})
            """)
          .on('user1 -> user1,
            'user2 -> user2)
          .executeInsert()
      }
      c.commit
    }

    // create random user-tweet relation
    DB.withConnection { implicit c =>
      tweetsList foreach (tweetid => {
        val userid = usersList(random.nextInt(usersList.size))
        SQL("""
            insert into user2tweet (userid, tweetid) 
            values ({userid}, {tweetid})
            """)
          .on('userid -> userid,
            'tweetid -> tweetid)
          .executeInsert()
      })
      c.commit
    }
  }
}