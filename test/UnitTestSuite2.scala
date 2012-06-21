import scala.collection.mutable.Stack
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import scala.util.Random
import dispatch._
import play.api.libs.json.Json

@RunWith(classOf[JUnitRunner])
class UnitTestSuite2 extends Specification {
  "'Hello world' 문자열" should {
    "11글자" in {
      "Hello world" must have size (11)
    }
    "'Hello'로 시작" in {
      "Hello world" must startWith("Hello")
    }
    "'world'로 끝" in {
      "Hello world" must endWith("world")
    }
  }

  "스택" should {
    "pop호출시 마지막 push한 값 반환" in {
      val stack = new Stack[Int]
      stack.push(1)
      stack.push(2)
      stack.pop() === 2
      stack.pop() === 1
      stack.size === 0
    }
  }

  "랜덤값" should {
    "일관되게 분배" in {
      val random = new Random(System.currentTimeMillis())
      val list = List(1, 2, 3, 4, 5)

      var sum = 0.0
      for (i <- 1 to 100) {
        val pick = list(random.nextInt(list.size))
        print(pick + ", ")
        sum += pick
      }

      val avg = sum / 100
      println("\n\navg : " + avg)
      avg must be >= 2.8
      avg must be <= 3.2
    }
  }
}