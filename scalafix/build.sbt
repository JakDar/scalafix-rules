lazy val V = _root_.scalafix.sbt.BuildInfo
scalaVersion := "2.13.7"

inThisBuild(
  List(
    organization := "com.github.jakdar",
    scalaVersion := "2.13.7",
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List("-Yrangepos")
    // classLoaderLayeringStrategy in Compile := ClassLoaderLayeringStrategy.Flat
  )
)

publish / skip := true

lazy val rules = project.settings(
  moduleName := "named-literal-arguments",
  libraryDependencies ++= Seq(
    "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
  ),
  scalacOptions ++= CompilerOpts.all
)

lazy val input = project.settings(
  publish / skip := true,
  libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.20.13",
  scalacOptions ++= CompilerOpts.all
)

lazy val output = project.settings(
  publish / skip := true,
  libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.20.13",
  scalacOptions ++= CompilerOpts.all
)

lazy val tests = project
  .settings(
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    scalafixTestkitOutputSourceDirectories :=
      sourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories :=
      sourceDirectories.in(input, Compile).value,
    scalafixTestkitInputClasspath :=
      fullClasspath.in(input, Compile).value
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
