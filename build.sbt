enablePlugins(GitVersioning, GitBranchPrompt)

scalaVersion := "2.13.8"
semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:reflectiveCalls",
  "-language:postfixOps",
  "-Wunused"
)

ThisBuild / scalafixScalaBinaryVersion := "2.13"
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

addCommandAlias("format", "; scalafmtSbt; scalafmt; Test / scalafmt; scalafixAll")
addCommandAlias("checkFormat", "; scalafmtSbtCheck; scalafmtCheckAll; Test / scalafmtCheckAll; scalafixAll --check")

val scalatestVersion    = "3.2.12"
val circeVersion        = "0.14.1"
val akkaVersion         = "2.6.19"
val akkaHttpVersion     = "10.2.9"
val akkaHttpJsonVersion = "1.39.2"
val logbackVersion      = "1.2.11"

libraryDependencies ++= Seq(
  "io.circe"          %% "circe-core"                  % circeVersion,
  "io.circe"          %% "circe-parser"                % circeVersion,
  "io.circe"          %% "circe-generic"               % circeVersion,
  "com.typesafe.akka" %% "akka-actor-typed"            % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"                  % akkaVersion,
  "ch.qos.logback"    % "logback-classic"              % logbackVersion,
  "com.typesafe.akka" %% "akka-stream"                 % akkaVersion,
  "com.typesafe.akka" %% "akka-http"                   % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe"             % akkaHttpJsonVersion,
  "org.scalatest"     %% "scalatest"                   % scalatestVersion % Test
)
