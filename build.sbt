
import sbt.Keys.*

ThisBuild / scalaVersion := "3.3.4"

lazy val protocol = project

lazy val simulator = project
  .dependsOn(protocol)
  .settings(
    fork := true
  )

lazy val app = project
  .dependsOn(protocol)
  .settings(
    Compile / unmanagedJars +=
      (LocalRootProject / baseDirectory).value / "lib" / "phidget22.jar",

    libraryDependencies ++= Seq(
      "io.reactivex.rxjava3" % "rxjava" % "3.1.9",
      "org.apache.pekko" %% "pekko-actor-typed" % "1.1.2",
      "org.apache.pekko" %% "pekko-actor" % "1.1.2",
      "ch.qos.logback" % "logback-classic" % "1.5.21",
      "org.slf4j" % "slf4j-api" % "2.0.17",
      "io.github.cdimascio" % "java-dotenv" % "5.2.2"
    )
  )
