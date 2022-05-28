ThisBuild / scalaVersion := "2.13.8"


lazy val root = (project in file("."))
  .aggregate(app.js, app.jvm)

val akkaVersion = "2.6.19"
lazy val app = crossProject(JSPlatform, JVMPlatform).in(file(".")).
  settings(
    name := "app",
    version := "0.1-SNAPSHOT",
  ).
  jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.2.12",
      "edu.stanford.nlp" % "stanford-corenlp" % "4.4.0" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")),
      "com.lihaoyi" %% "autowire" % "0.3.3",
      "com.lihaoyi" %% "upickle" % "1.6.0",
    )
  ).
  jsSettings(
    // Add JS-specific settings here
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.0.0",
      "com.lihaoyi" %%% "scalatags" % "0.9.4",
      "com.lihaoyi" %%% "autowire" % "0.3.3",
      "com.lihaoyi" %%% "upickle" % "1.6.0",
    )
  )
