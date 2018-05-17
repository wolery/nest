//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Unit tests for some piecewise functions.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery
package math

//****************************************************************************

import org.scalacheck.Arbitrary
import Double.{PositiveInfinity ⇒ ∞}

//****************************************************************************

class PiecewiseTest extends test.Suite with piecewise
{
  def equal[α: Arbitrary,β](f: α ⇒ β,g: α ⇒ β): Unit =
  {
    forAll("x")((x: α) ⇒ assert(f(x) == g(x),            "[f(x) = g(x)]"))
  }

  test("boxcar")
  {
    equal(boxcar(0.0,∞,1),heaviside)

    forAll("a","b","y"){(a: ℝ,b: ℝ,y: ℝ) ⇒ whenever (a <= b)
    {
      equal(boxcar(a,b,y),step(0.0,(a,y),(b,0.0)))
    }}
  }

  test("heaviside")
  {
    assert(heaviside(-1) == 0,                           "[heaviside(-1) = 0]")
    assert(heaviside( 0) == 1,                           "[heaviside(0)  = 1]")
    assert(heaviside(+1) == 1,                           "[heaviside(+1) = 1]")

    equal(heaviside,step(0,(0.0,1)))
  }

  test("abs")
  {
    assert(abs(-1) == 1,                                 "[|-1| = 1]")
    assert(abs( 0) == 0,                                 "[| 0| = 0]")
    assert(abs(+1) == 1,                                 "[|+1| = 1]")

    equal(abs,piecewise((x: ℝ) ⇒ -x,(0.0,(x: ℝ) ⇒ x)))
  }

  test("sgn")
  {
    assert(sgn(-1) == -1,                                "[sgn(-1) = -1]")
    assert(sgn( 0) ==  0,                                "[sgn(0)  =  0]")
    assert(sgn(+1) == +1,                                "[sgn(+1) = +1]")

    equal(sgn,(x: ℝ) ⇒ x / abs(x))
  }
}

//****************************************************************************
