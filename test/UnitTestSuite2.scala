import scala.collection.mutable.Stack
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import scala.util.Random
import dispatch._
import play.api.libs.json.Json

@RunWith(classOf[JUnitRunner])
class UnitTestSuite2 extends Specification {
  "The 'Hello world' string" should {
    "contain 11 characters" in {
      "Hello world" must have size (11)
    }
    "start with 'Hello'" in {
      "Hello world" must startWith("Hello")
    }
    "end with 'world'" in {
      "Hello world" must endWith("world")
    }
  }

  "the stack" should {
    "return last input value on pop invoke" in {
      val stack = new Stack[Int]
      stack.push(1)
      stack.push(2)
      stack.pop() === 2
      stack.pop() === 1
      stack.size === 0
    }
  }

  "the random" should {
    "distribute equally" in {
      val random = new Random(System.currentTimeMillis())
      val list = List(1, 2, 3, 4, 5)

      var sum = 0.0
      for (i <- 1 to 100) {
        val pick = list(random.nextInt(list.size))
        print(pick + ", ")
        sum += pick
      }

      val http = new Http

      http(url("http://192.168.10.24:7474/db/data/node/2/relationships")
        << (Json.toJson(Map("to" -> "http://192.168.10.24:7474/db/data/node/3", "type" -> "FOLLOWS")).toString, "application/json")
        >:> { header => header getOrElse ("Location", Set()) head
        })

      val avg = sum / 100
      println("\n\navg : " + avg)
      avg must be >= 2.8
      avg must be <= 3.2
    }
  }
}