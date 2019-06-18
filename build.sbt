lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion    = "2.6.0-M3"
lazy val circeVersion = "0.9.3"


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "evolution.gaming",
      scalaVersion    := "2.12.7"
    )),
    name := "evolution_gaming",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion) 
  )
