package com.company.rest.calculation

trait Model[X, P, R] {
  def calculate(x: X)(params: P): R
}

object Models {
    object Linear extends Model[Int, (Int, Int), Double] {
      override def calculate(x: Int)(params: (Int, Int)): Double = params match { case (a, b) =>
        x*a + b
      }
    }

    object Second extends Model[(Int), (Int, Int, Int), Double] {
      override def calculate(x: Int)(params: (Int, Int, Int)): Double = params match {case (a, b, c) =>
          x*x*a + x*b + c
      }
    }
}