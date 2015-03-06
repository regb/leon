package leon.test
package synthesis

import leon.purescala.Trees._
import leon.purescala.TypeTrees._
import leon.purescala.TreeOps._
import leon.purescala.Common._
import leon.purescala.Definitions._
import leon.test.purescala.WithLikelyEq
import leon.evaluators._
import leon.LeonContext

import leon.synthesis.LinearEquations._


class LinearEquationsSuite extends LeonTestSuite with WithLikelyEq {


  def i(x: Int) = InfiniteIntegerLiteral(x)

  val xId = FreshIdentifier("x", IntegerType)
  val x = Variable(xId)
  val yId = FreshIdentifier("y", IntegerType)
  val y = Variable(yId)
  val zId = FreshIdentifier("z", IntegerType)
  val z = Variable(zId)

  val aId = FreshIdentifier("a", IntegerType)
  val a = Variable(aId)
  val bId = FreshIdentifier("b", IntegerType)
  val b = Variable(bId)

  def toSum(es: Seq[Expr]) = es.reduceLeft(Plus(_, _))
  
  def checkSameExpr(e1: Expr, e2: Expr, vs: Set[Identifier], prec: Expr, defaultMap: Map[Identifier, Expr] = Map()) {
    assert( //this outer assert should not be needed because of the nested one
      LikelyEq(e1, e2, vs, prec, (e1, e2) => {assert(e1 === e2); true}, defaultMap)
    )
  }


  def enumerate(nbValues: Int, app: (Array[Int] => Unit)) {
    val min = -5
    val max = 5
    val counters: Array[Int] = (1 to nbValues).map(_ => min).toArray
    var i = 0

    while(i < counters.size) {
      app(counters)
      if(counters(i) < max)
        counters(i) += 1
      else {
        while(i < counters.size && counters(i) >= max) {
          counters(i) = min
          i += 1
        }
        if(i < counters.size) {
          counters(i) += 1
          i = 0
        }
      }
    }

  }

  //TODO: automatic check result
  test("elimVariable") {
    val as = Set[Identifier](aId, bId)

    val evaluator = new DefaultEvaluator(testContext, Program.empty)

    def check(t: Expr, c: List[Expr], prec: Expr, witnesses: List[Expr], freshVars: List[Identifier]) {
      enumerate(freshVars.size, (vals: Array[Int]) => {
        val mapping: Map[Expr, Expr] = (freshVars.zip(vals.toList).map(t => (Variable(t._1), i(t._2))).toMap)
        val cWithVars: Expr = c.zip(witnesses).foldLeft[Expr](i(0)){ case (acc, (coef, wit)) => Plus(acc, Times(coef, replace(mapping, wit))) }
        checkSameExpr(Plus(t, cWithVars), i(0), as, prec)
      })
    }

    val t1 = Minus(Times(i(2), a), b)
    val c1 = List(i(3), i(4), i(8))
    val (pre1, wit1, f1) = elimVariable(evaluator, as, t1::c1)
    check(t1, c1, pre1, wit1, f1)

    val t2 = Plus(Plus(i(0), i(2)), Times(i(-1), i(3)))
    val c2 = List(i(1), i(-1))
    val (pre2, wit2, f2) = elimVariable(evaluator, Set(), t2::c2)
    check(t2, c2, pre2, wit2, f2)


    val t3 = Minus(Times(i(2), a), i(3))
    val c3 = List(i(2))
    val (pre3, wit3, f3) = elimVariable(evaluator, Set(aId), t3::c3)
    check(t3, c3, pre3, wit3, f3)

    val t4 = Times(i(2), a)
    val c4 = List(i(2), i(4))
    val (pre4, wit4, f4) = elimVariable(evaluator, Set(aId), t4::c4)
    check(t4, c4, pre4, wit4, f4)

    val t5 = Minus(a, b)
    val c5 = List(i(-60), i(-3600))
    val (pre5, wit5, f5) = elimVariable(evaluator, Set(aId, bId), t5::c5)
    check(t5, c5, pre5, wit5, f5)

  }

}
