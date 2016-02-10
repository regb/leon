package leon
package invariant.util

import purescala.Definitions._
import purescala.Expressions._
import purescala.ExprOps._
import ProgramUtil._
import invariant.structure.FunctionUtils._
import invariant.datastructure._

/**
 * This represents a call graph of the functions in the program
 */
class CallGraph {
  val graph = new DirectedGraph[FunDef]()
  lazy val reverseCG = graph.reverse

  def addFunction(fd: FunDef) = graph.addNode(fd)

  def addEdgeIfNotPresent(src: FunDef, callee: FunDef): Unit = {
    if (!graph.containsEdge(src, callee))
      graph.addEdge(src, callee)
  }

  def callees(src: FunDef): Set[FunDef] = {
    graph.getSuccessors(src)
  }

  def transitiveCallees(src: FunDef): Set[FunDef] = {
    graph.BFSReachables(Seq(src))
  }

  def transitiveCallers(dest: FunDef) : Set[FunDef] = {
    reverseCG.BFSReachables(Seq(dest))
  }

  def transitiveCallees(srcs: Seq[FunDef]): Set[FunDef] = {
    graph.BFSReachables(srcs)
  }

  def transitiveCallers(dests: Seq[FunDef]) : Set[FunDef] = {
    reverseCG.BFSReachables(dests)
  }

  def isRecursive(fd: FunDef): Boolean = {
    transitivelyCalls(fd, fd)
  }

  /**
   * Checks if the src transitively calls the procedure proc
   */
  def transitivelyCalls(src: FunDef, proc: FunDef): Boolean = {
    //important: We cannot say that src calls it self even though source is reachable from itself in the callgraph
    graph.BFSReach(src, proc, excludeSrc = true)
  }

  def calls(src: FunDef, proc: FunDef): Boolean = {
    graph.containsEdge(src, proc)
  }

  /**
   * sorting functions in ascending topological order
   */
  def topologicalOrder: Seq[FunDef] = {

    def insert(index: Int, l: Seq[FunDef], fd: FunDef): Seq[FunDef] = {
      var i = 0
      var head = Seq[FunDef]()
      l.foreach((elem) => {
        if (i == index)
          head :+= fd
        head :+= elem
        i += 1
      })
      head
    }

    var funcList = Seq[FunDef]()
    graph.getNodes.toList.foreach((f) => {
      var inserted = false
      var index = 0
      for (i <- funcList.indices) {
        if (!inserted && this.transitivelyCalls(funcList(i), f)) {
          index = i
          inserted = true
        }
      }
      if (!inserted)
        funcList :+= f
      else funcList = insert(index, funcList, f)
    })

    funcList
  }

  override def toString: String = {
    val procs = graph.getNodes
    procs.foldLeft("")((acc, proc) => {
      acc + proc.id + " --calls--> " +
        graph.getSuccessors(proc).foldLeft("")((acc, succ) => acc + "," + succ.id) + "\n"
    })
  }
}

object CallGraphUtil {

  def constructCallGraph(prog: Program,
      onlyBody: Boolean = false,
      withTemplates: Boolean = false,
      calleesFun: Expr => Set[FunDef] = getCallees): CallGraph = {

    val cg = new CallGraph()
    functionsWOFields(prog.definedFunctions).foreach((fd) => {
      cg.addFunction(fd)
      if (fd.hasBody) {
        var funExpr = fd.body.get
        if (!onlyBody) {
          if (fd.hasPrecondition)
            funExpr = Tuple(Seq(funExpr, fd.precondition.get))
          if (fd.hasPostcondition)
            funExpr = Tuple(Seq(funExpr, fd.postcondition.get))
        }
        if (withTemplates && fd.hasTemplate) {
          funExpr = Tuple(Seq(funExpr, fd.getTemplate))
        }
        //introduce a new edge for every callee
        calleesFun(funExpr).foreach(cg.addEdgeIfNotPresent(fd, _))
      }
    })
    cg
  }

  def getCallees(expr: Expr): Set[FunDef] = collect {
    case expr@FunctionInvocation(TypedFunDef(callee, _), _) if callee.isRealFunction =>
      Set(callee)
    case _ =>
      Set[FunDef]()
  }(expr)

}