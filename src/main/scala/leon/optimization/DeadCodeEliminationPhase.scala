/* Copyright 2009-2015 EPFL, Lausanne */

package leon
package optimization

import leon.TransformationPhase
import leon.LeonContext
import leon.purescala.Common._
import leon.purescala.Definitions._
import leon.purescala.Expressions._
import leon.purescala.Extractors._
import leon.purescala.Constructors._
import leon.purescala.Types._
import leon.purescala.TypeOps._

import leon.purescala.TransformerWithPC
import leon.purescala.SimplifierWithPaths
import leon.purescala.ExprOps._
import leon.purescala.DefOps._

import solvers._

object DeadCodeEliminationPhase extends TransformationPhase {

  val name = "dead-code-elimination"
  val description = "Eliminate dead code"

  def apply(ctx: LeonContext, pgm: Program): Program = {
    val entrySolver = SolverFactory.getFromSettings(ctx, pgm)

    val mainSolver = entrySolver
    //timeout match {
    //  case Some(sec) =>
    //    new TimeoutSolverFactory(entrySolver, sec*1000L)
    //  case None =>
    //    entrySolver
    //}


    eliminateDeadCode(pgm)(mainSolver)
  }

  class DeadCodeEliminationTransformer(sf: SolverFactory[TimeoutSolver]) 
    extends SimplifierWithPaths(sf) {

    //protected override def rec(e: Expr, path: C) = e match {
    //  case GreaterThan(e1, e2) => {
    //    val re1 = rec(e1, path)
    //    val re2 = rec(e2, path)
    //    println(s"proving that $re1 is less than $re2")
    //    if(impliedBy(GreaterThan(re1, re2), path))
    //      BooleanLiteral(true)
    //    else if(impliedBy(LessEquals(re1, re2), path))
    //      BooleanLiteral(false)
    //    else
    //      GreaterThan(re1, re2)
    //  }
    //  case _ => super.rec(e, path)
    //}

  }

  private def eliminateDeadCode(pgm: Program)(sf: SolverFactory[TimeoutSolver]): Program = {

    val transformer = new DeadCodeEliminationTransformer(sf)

    funDefsFromMain(pgm).foreach(funDef => {
      funDef.fullBody = transformer.transform(funDef.fullBody)
    })

    pgm
  }


}
