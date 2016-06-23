/* Copyright 2009-2016 EPFL, Lausanne */

package leon.unit.xlang

import org.scalatest._

import leon.test._
import leon.purescala.Common._
import leon.purescala.Expressions._
import leon.purescala.Types._
import leon.purescala.TypeOps.isSubtypeOf
import leon.purescala.Definitions._
import leon.xlang.AliasAnalysis
import leon.xlang.Expressions._
import leon.xlang.ExprOps._

class AliasAnalysisSuite extends FunSuite with helpers.ExpressionsDSL {

  private val fd1 = new FunDef(FreshIdentifier("f1"), Seq(), Seq(ValDef(x.id)), IntegerType)
  fd1.body = Some(x)

  private val fd2 = new FunDef(FreshIdentifier("f2"), Seq(), Seq(ValDef(x.id)), IntegerType)
  fd2.body = Some(FunctionInvocation(fd1.typed, List(bi(1))))

  private val rec1 = new FunDef(FreshIdentifier("rec1"), Seq(), Seq(ValDef(x.id)), IntegerType)
  rec1.body = Some(FunctionInvocation(rec1.typed, List(x)))

  private val classA = new CaseClassDef(FreshIdentifier("A"), Seq(), None, false)
  private val classAField = FreshIdentifier("x", IntegerType)
  classA.setFields(Seq(ValDef(classAField).setIsVar(true)))
  private val classAInstance1Id = FreshIdentifier("a1", classA.typed)
  private val classAInstance1 = classAInstance1Id.toVariable
  private val classAInstance2Id = FreshIdentifier("a2", classA.typed)
  private val classAInstance2 = classAInstance2Id.toVariable
  private val classAInstance3Id = FreshIdentifier("a3", classA.typed)
  private val classAInstance3 = classAInstance3Id.toVariable


  test("expression with no variable has no alias") {
    val aliasAnalysis = new AliasAnalysis
    assert(aliasAnalysis.expressionAliasing(bi(42)) === Set())
    assert(aliasAnalysis.expressionAliasing(i(1)) === Set())
    assert(aliasAnalysis.expressionAliasing(BooleanLiteral(true)) === Set())
  }

  test("Lets in expression are aliases") {
    val aliasAnalysis = new AliasAnalysis
    assert(aliasAnalysis.expressionAliasing(Let(x.id, y, x)) === Set(x.id, y.id))
    assert(aliasAnalysis.expressionAliasing(Let(x.id, y, y)) === Set(x.id, y.id))
    assert(aliasAnalysis.expressionAliasing(Let(x.id, bi(12), x)) === Set(x.id))
    assert(aliasAnalysis.expressionAliasing(Let(x.id, bi(12), Let(y.id, x, y))) === Set(x.id, y.id))
    assert(aliasAnalysis.expressionAliasing(Let(x.id, bi(12), Let(y.id, x, x))) === Set(x.id, y.id))

    assert(aliasAnalysis.expressionAliasing(Let(x.id, y, Let(z.id, x, z))) === Set(x.id, y.id, z.id))
    assert(aliasAnalysis.expressionAliasing(Let(x.id, y, Let(z.id, x, y))) === Set(x.id, y.id, z.id))
  }

  test("Lets that are not returned are not aliases") {
    val aliasAnalysis = new AliasAnalysis
    assert(aliasAnalysis.expressionAliasing(Let(x.id, bi(12), bi(10))) === Set())
    assert(aliasAnalysis.expressionAliasing(Let(x.id, bi(12), y)) === Set(y.id))
    assert(aliasAnalysis.expressionAliasing(Let(x.id, y, Let(z.id, bi(12), x))) === Set(x.id, y.id))
    assert(aliasAnalysis.expressionAliasing(Let(x.id, y, Let(z.id, bi(12), y))) === Set(x.id, y.id))
  }

  test("asInstanceOf does not remove aliasing") {
    val aliasAnalysis = new AliasAnalysis
    assert(aliasAnalysis.expressionAliasing(AsInstanceOf(classAInstance1, classA.typed)) === Set(classAInstance1Id))
    assert(aliasAnalysis.expressionAliasing(AsInstanceOf(AsInstanceOf(classAInstance1, classA.typed), classA.typed)) === Set(classAInstance1Id))
    assert(aliasAnalysis.expressionAliasing(Let(classAInstance2Id, AsInstanceOf(classAInstance1, classA.typed), AsInstanceOf(classAInstance2, classA.typed))) === Set(classAInstance1Id, classAInstance2Id))
  }

  test("Pattern matching introduce aliases") {
    val aliasAnalysis = new AliasAnalysis
    val m1 = MatchExpr(x, Seq(MatchCase(WildcardPattern(Some(y.id)), None, y)))
    assert(aliasAnalysis.expressionAliasing(m1) === Set(x.id, y.id))
    val m2 = MatchExpr(x, Seq(MatchCase(WildcardPattern(Some(y.id)), None, x)))
    assert(aliasAnalysis.expressionAliasing(m2) === Set(x.id, y.id))
    val m3 = MatchExpr(x, Seq(MatchCase(WildcardPattern(Some(y.id)), None, bi(12))))
    assert(aliasAnalysis.expressionAliasing(m3) === Set())

    //TODO: we probably need to distinguish aliases per type, and not keep the abstraction all aliases to some part of an object graph
    val m4 = MatchExpr(classAInstance1, Seq(MatchCase(CaseClassPattern(None, classA.typed, Seq(WildcardPattern(Some(y.id)))), None, y)))
    assert(aliasAnalysis.expressionAliasing(m4) === Set(y.id, classAInstance1Id))
    val m5 = MatchExpr(classAInstance1, Seq(MatchCase(CaseClassPattern(Some(classAInstance2Id), classA.typed, Seq(WildcardPattern(Some(x.id)))), None, x)))
    assert(aliasAnalysis.expressionAliasing(m5) === Set(x.id, classAInstance1Id, classAInstance2Id))

    val m6 = MatchExpr(classAInstance1, Seq(
      MatchCase(CaseClassPattern(Some(classAInstance2Id), classA.typed, Seq(WildcardPattern(Some(x.id)))), None, x),
      MatchCase(CaseClassPattern(Some(classAInstance3Id), classA.typed, Seq(WildcardPattern(Some(y.id)))), None, y)
    ))
    assert(aliasAnalysis.expressionAliasing(m6) === Set(x.id, y.id, classAInstance1Id, classAInstance2Id, classAInstance3Id))
  }

}
