ThisBuild / scalaVersion := "2.13.8"


lazy val root = (project in file("."))
  .aggregate(app.js, app.jvm)

val akkaVersion = "2.6.19"
val akkaHttpVersion = "10.2.9"
lazy val app = crossProject(JSPlatform, JVMPlatform).in(file(".")).
  settings(
    name := "app",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalatags" % "0.9.4",
      "com.lihaoyi" %%% "upickle" % "2.0.0",
      "com.lihaoyi" %%% "autowire" % "0.3.3",
    )
  ).
  jvmSettings(
    // Add JVM-specific settings here
    Compile / mainClass := Some("app.AkkaHttpServer"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "org.scalatest" %% "scalatest" % "3.2.12",
      "edu.stanford.nlp" % "stanford-corenlp" % "4.4.0" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")),
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
    )
  ).
  jsSettings(
    // Add JS-specific settings here
//    scalaJSUseMainModuleInitializer := true,
    Compile / mainClass := Some("app.Client"),
    Compile / fastOptJS / artifactPath := baseDirectory.value / "../jvm/src/main/resources/main.js",
    Compile / fullOptJS / artifactPath := baseDirectory.value / "../jvm/src/main/resources/main.js",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.0.0",
      "com.lihaoyi" %%% "scalatags" % "0.9.4",
    )
  )