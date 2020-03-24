package fix

import metaconfig.Configured
import scalafix.v1._
import scala.meta._

class OwnExample extends SemanticRule("OwnExample") {

  def name(a:Any) = a.getClass().getName()
  def present(a:Any) = println(a + " | " + name(a))

  override def fix(implicit doc: SemanticDocument): Patch = {

    doc.tree.collect{
      case Term.Apply(f,_) =>
        present(f.symbol.owner.value)
        println(f.symbol.displayName)

    }
    Patch.empty
  }
}

