package fix

import scalafix.v1._
import scala.meta._

class OwnExample extends SemanticRule("OwnExample") {

  def name(a: Any)    = a.getClass().getName()
  def present(a: Any) = println(a + " | " + name(a))

  override def fix(implicit doc: SemanticDocument): Patch = {

    doc.tree
      .collect {
        case ap @ Term.Apply(f @ Term.Select((_, name)), params) if f.symbol.owner.value == "reactivemongo/api/collections/GenericCollection#" =>
          f.symbol.displayName match {
            case "find" =>
              f.symbol.info.map(_.signature) match {
                case Some(_: MethodSignature) =>
                  ap.parent.flatMap(_.parent).flatMap(_.children.lastOption) match {
                    case Some(Type.Name(typeName)) =>
                      if (params.size == 1) {
                        Patch.addRight(params.head, s", projection = Option.empty[$typeName]") :: Nil
                      } else {
                        Patch.empty :: Nil
                      }
                    case _ => Patch.empty :: Nil
                  }

                case _ => Patch.empty :: Nil
              }

            case "update" =>
              List(Patch.addRight(f, ".one"))

            case "remove" =>
              Patch.replaceTree(name, "delete") :: Patch.addRight(f, ".one") :: Nil

            case _ => Nil
          }

      }
      .flatten
      .asPatch
  }
}
