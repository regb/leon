/* Copyright 2009-2015 EPFL, Lausanne */

package leon
package optimization

import solvers.z3._

import purescala.ExprOps._
import purescala.Expressions._
import purescala.Common._
import purescala.ScalaPrinter
import purescala.Definitions.{Program, FunDef}
import leon.utils.ASCIIHelpers

object OptimizationPhase extends TransformationPhase {
  val name        = "Optimization"
  val description = "Optimize Scala program"

  def apply(ctx: LeonContext, p: Program): Program = {
    ctx.reporter.info("Performing optimization on input program...")

    val pipeline = DeadCodeEliminationPhase

    pipeline.run(ctx)(p)
  }


}
