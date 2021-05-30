scalaVersion := "2.13.6"
semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:reflectiveCalls",
  "-language:postfixOps"
)

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

addCommandAlias("format", "; scalafmtSbt; scalafmt; test:scalafmt; scalafixAll")
addCommandAlias("checkFormat", "; scalafmtSbtCheck; scalafmtCheckAll; test:scalafmtCheckAll; scalafixAll --check")
