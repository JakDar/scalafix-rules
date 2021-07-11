package fix

// Version of :
// https://gist.github.com/xuwei-k/d1ae2f6cdd960326648fe23b5c0385c6
// With some parens removed. Might break but I always delete those parens anyway.

import scalafix.v1._
import scala.meta._

class Sbt0_13BuildSyntax extends SyntacticRule("Sbt0_13BuildSyntax") {
  private def maybeOldSyntax(tree: Tree): Boolean = {
    tree match {
      case t: Term.ApplyInfix if t.op.value == "in" && t.lhs.toString != "project" =>
        true
      case _ =>
        false
    }
  }

  override def fix(implicit doc: SyntacticDocument): Patch = {
    doc.tree.collect {
      case t: Term.ApplyInfix if maybeOldSyntax(t) =>
        t.lhs match {
          case t2: Term.ApplyInfix if maybeOldSyntax(t2) =>
            (t.args, t2.args) match {
              case (List(arg0), List(args1)) =>
                Patch.replaceTree(t, s"($arg0 / $args1 / ${t2.lhs})")
              case _ =>
                Patch.empty
            }

          case _ if t.parent.exists(_.isInstanceOf[Term.Select]) =>
            val select = t.parent.get.asInstanceOf[Term.Select]
            stringSlashify(t.lhs, t.args).fold(Patch.empty)(inner => Patch.replaceTree(select, s"($inner).${select.name.value}"))

          case _ if !t.parent.exists(maybeOldSyntax) =>
            slashify(t, t.lhs, t.args)
          case _ =>
            Patch.empty
        }
      case t @ Term.Apply(Term.Select(qual, Term.Name("in")), args) if qual.toString != "project" =>
        slashify(t, qual, args)
    }.asPatch
  }

  def slashify(t: Tree, lhs: Term, args: Seq[Term]): Patch =
    stringSlashify(lhs, args).fold(Patch.empty)(Patch.replaceTree(t, _))

  def stringSlashify(lhs: Term, args: Seq[Term]): Option[String] = {
    args match {
      case List(arg0)             => Some(s"$arg0 / $lhs")
      case List(arg0, arg1)       => Some(s"$arg0 / $arg1 / $lhs")
      case List(arg0, arg1, arg2) => Some(s"$arg0 / $arg1 / $arg2 / $lhs")
      case _                      => None
    }
  }
}
