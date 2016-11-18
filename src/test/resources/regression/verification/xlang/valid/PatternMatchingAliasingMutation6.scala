package test.resources.regression.verification.xlang.valid

object PatternMatchingAliasingMutation6 {

  case class M(var value: Int)

  def bar(m: M): Unit = {
    m.value = m.value + 1
  }

  def foo(m: M): Unit = m match {
    case (n: M) => bar(n)
  }

}
