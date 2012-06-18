import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "social_board_demo"
  val appVersion = "0.0.1"

  val appDependencies = Seq(
    "net.databinder" %% "dispatch-http" % "0.8.8")

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
  )
}
