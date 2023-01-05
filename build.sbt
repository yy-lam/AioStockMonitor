ThisBuild / scalaVersion := "2.13.8"

val akkaVersion = "2.7.0"
val akkaHttpVersion = "10.4.0"

lazy val root = (project in file("."))
  .aggregate(server, client, sharedJvm, sharedJs)

lazy val server = project.settings(
  scalaVersion := "2.13.4",
  scalaJSProjects := Seq(client),
  Assets / pipelineStages := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest),
  // Expose as sbt-web assets some files retrieved from the NPM packages of the `client` project
  npmAssets ++= NpmAssets.ofProject(client) { modules => (modules / "font-awesome").allPaths }.value,
  Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
  // The assets (js files, sourcemaps, etc.) are added to the classpath during development thanks to the following lines
  Assets / WebKeys.packagePrefix := "public/",
  Runtime / managedClasspath += (Assets / packageBin).value,
  Compile / mainClass := Some("app.AkkaHttpServer"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "org.scalatest" %% "scalatest" % "3.2.12",
    "edu.stanford.nlp" % "stanford-corenlp" % "4.4.0" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")),
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  ),
).enablePlugins(WebScalaJSBundlerPlugin)
  .dependsOn(sharedJvm)

lazy val client = project.settings(
  scalacOptions += "-Ymacro-annotations",
  scalaJSUseMainModuleInitializer := true,
  Compile / npmDependencies ++= Seq(
    "react" -> "17.0.2",
    "react-dom" -> "17.0.2"
  ),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "2.0.0",
    "com.lihaoyi" %%% "scalatags" % "0.11.1",
    "me.shadaj" %%% "slinky-core" % "0.7.0",
    "me.shadaj" %%% "slinky-web" % "0.7.0",
  ),
).enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(sharedJs)

def toPathMapping(f: File): (File, String) = f -> f.getName

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "scalatags" % "0.9.4",
      "com.lihaoyi" %%% "upickle" % "2.0.0",
      "com.lihaoyi" %%% "autowire" % "0.3.3",
    )
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js