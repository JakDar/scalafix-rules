lazy val V = _root_.scalafix.sbt.BuildInfo
scalaVersion := "2.12.11"

inThisBuild(
  List(
    organization := "com.geirsson",
    scalaVersion := "2.12.11",
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List(
      "-Yrangepos"
    )
    // classLoaderLayeringStrategy in Compile := ClassLoaderLayeringStrategy.Flat
  )
)

skip in publish := true

lazy val rules = project.settings(
  moduleName := "named-literal-arguments",
  libraryDependencies ++= Seq(
    "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
  ),
  scalacOptions ++= CompilerOpts.all
)

lazy val input = project.settings(
  skip in publish := true,
  libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.20.3",
  scalacOptions ++= CompilerOpts.all
)

lazy val output = project.settings(
  skip in publish := true,
  libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.20.3",
  scalacOptions ++= CompilerOpts.all
)

lazy val tests = project
  .settings(
    skip in publish := true,
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
