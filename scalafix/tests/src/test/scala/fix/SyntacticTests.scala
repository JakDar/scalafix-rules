package fix

import org.scalatest.FunSuiteLike
import scalafix.testkit.AbstractSyntacticRuleSuite
import scalafix.v1.SyntacticDocument
import scala.meta.inputs.Input
import scalafix.internal.v1.Rules

class SyntacticTests extends AbstractSyntacticRuleSuite() with FunSuiteLike {

  val original = """daemonUser in Docker := "root"
                |
                |lazy val `my-project` = project
                |  .in(file("."))
                |  .settings(resolvers ++= Dependencies.additionalResolvers)
                |  .settings(scalacOptions ++= CompilerOpts.all)
                |  .settings(PB.targets in Compile := Seq(
                |    scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
                |  ))
                |  .settings(
                |    publishArtifact in (Compile, packageDoc) := false,
                |    publishArtifact in packageDoc := false,
                |    sources in (Compile, doc) := Seq.empty
                |  )
                |
                |PB.protoSources in Compile := Seq(
                |  baseDirectory.value /  "proto" / "ala"
                |)""".stripMargin

  val expectedOutput =
    """Docker / daemonUser := "root"
      |
      |lazy val `my-project` = project
      |  .in(file("."))
      |  .settings(resolvers ++= Dependencies.additionalResolvers)
      |  .settings(scalacOptions ++= CompilerOpts.all)
      |  .settings(Compile / PB.targets := Seq(
      |    scalapb.gen(flatPackage = true) -> (Compile / sourceManaged).value
      |  ))
      |  .settings(
      |    Compile / packageDoc / publishArtifact := false,
      |    packageDoc / publishArtifact := false,
      |    Compile / doc / sources := Seq.empty
      |  )
      |
      |Compile / PB.protoSources := Seq(
      |  baseDirectory.value /  "proto" / "ala"
      |)""".stripMargin

  // val r = new Sbt0_13BuildSyntax().fix

  registerTest("Works") {
    val scalaVersion = scala.meta.dialects.Sbt0136
    val doc =
      SyntacticDocument.fromInput(Input.String(original), scalaVersion)
    val rules             = Rules(List(new Sbt0_13BuildSyntax()))
    val resultWithContext = rules.syntacticPatch(doc, suppress = true)
    val obtained          = resultWithContext.fixed
    assertNoDiff(obtained, expectedOutput)
  }

}
