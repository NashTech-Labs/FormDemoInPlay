import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "FormDemoInPlay"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
       "com.novus" %% "salat" % "1.9.1",
       "org.scalatest" %% "scalatest" % "1.7.2"       
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
