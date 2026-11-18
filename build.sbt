import sbt.Keys.*

ThisBuild / scalaVersion := "3.9.0"

val tapirVersion = "1.14.1"
val pekkoVersion = "1.7.1"
val pekkoHttpVersion = "1.4.2"
val rxJavaVersion = "3.1.9"
val logBackVersion = "1.5.21"
val slf4jVersion = "2.0.17"
val javaDotEnvVersion = "5.2.2"

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
      "io.reactivex.rxjava3"        % "rxjava"                   % rxJavaVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-pekko-http-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % tapirVersion,
      "org.apache.pekko"            %% "pekko-actor-typed"       % pekkoVersion,
      "org.apache.pekko"            %% "pekko-actor"             % pekkoVersion,
      "org.apache.pekko"            %% "pekko-stream"            % pekkoVersion,
      "org.apache.pekko"            %% "pekko-http"              % pekkoHttpVersion,
      "ch.qos.logback"              % "logback-classic"          % logBackVersion,
      "org.slf4j"                   % "slf4j-api"                % slf4jVersion,
      "io.github.cdimascio"         % "java-dotenv"              % javaDotEnvVersion
    ),
  )
