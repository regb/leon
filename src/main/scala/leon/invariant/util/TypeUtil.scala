package leon
package invariant.util

import purescala.Common._
import purescala.Definitions._
import purescala.Expressions._
import purescala.ExprOps._
import purescala.Extractors._
import purescala.Types._

object TypeUtil {
  def getTypeParameters(t: TypeTree): Seq[TypeParameter] = {
    t match {
      case tp @ TypeParameter(_) => Seq(tp)
      case NAryType(tps, _) =>
        (tps flatMap getTypeParameters).distinct
    }
  }

  def getTypeArguments(t: TypeTree) : Seq[TypeTree] =  t match {
    case ct: ClassType => ct.tps
    case NAryType(tps, _) =>
        (tps flatMap getTypeParameters).distinct
  }

  def typeNameWOParams(t: TypeTree): String = t match {
    case ct: ClassType => ct.id.name
    case TupleType(ts) => ts.map(typeNameWOParams).mkString("(", ",", ")")
    case ArrayType(t) => s"Array[${typeNameWOParams(t)}]"
    case SetType(t) => s"Set[${typeNameWOParams(t)}]"
    case MapType(from, to) => s"Map[${typeNameWOParams(from)}, ${typeNameWOParams(to)}]"
    case FunctionType(fts, tt) =>
      val ftstr = fts.map(typeNameWOParams).mkString("(", ",", ")")
      s"$ftstr => ${typeNameWOParams(tt)}"
    case t => t.toString
  }

  def instantiateTypeParameters(tpMap: Map[TypeParameter, TypeTree])(t: TypeTree): TypeTree = {
    t match {
      case tp: TypeParameter => tpMap.getOrElse(tp, tp)
      case NAryType(subtypes, tcons) =>
        tcons(subtypes map instantiateTypeParameters(tpMap) _)
    }
  }
}