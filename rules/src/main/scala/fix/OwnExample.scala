package fix

import scalafix.v1._
import scala.meta._

class OwnExample extends SemanticRule("OwnExample") {

  def name(a: Any)    = a.getClass().getName()
  def present(a: Any) = println(a + " | " + name(a))

  override def fix(implicit doc: SemanticDocument): Patch = {

    doc.tree
      .collect {
        case Term.Apply(f, _) if f.symbol.owner.value == "reactivemongo/api/collections/GenericCollection#" =>
          f.symbol.displayName match {
            case "find" => Nil
            case "update" =>
              f.symbol.info.map(_.signature) match {
                case Some(m: MethodSignature) => present(m)
                case other                    => present(other)

              }
              List(Patch.addRight(f, ".one"))

            case "remove" => Nil
            case _        => Nil // FIXME:bcm insert
          }
        // case other => find what is that second update

      }
      .flatten
      .asPatch
  }
}
