//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Unit tests for the core utilities.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery
package util

//****************************************************************************

class UtiltiesTest extends test.Suite with utilities
{
  test("subscript")
  {
    assert(subscript("A(0123-+)Z")   == "A₍₀₁₂₃₋₊₎Z",    "[1]")
    assert(subscript("A C")          == "A C",           "[2]")
    assert(subscript("ABC")          == "ABC",           "[3]")
  }

  test("superscript")
  {
    assert(superscript("A(0123-+)Z") == "A⁽⁰¹²³⁻⁺⁾Z",    "[1]")
    assert(superscript("A C")        == "A C",           "[2]")
    assert(superscript("ABC")        == "ABC",           "[3]")
  }

  test("isBetween")
  {
    assert((-8 to +8).map(_.isBetween(-2,+2)) ==
           "FFFFFFTTTTTFFFFFF".map(_ == 'T'),            "[1]")
  }

  test("isIncreasing")
  {
    assert( isIncreasing(0),                             "[1]")
    assert( isIncreasing(0,0),                           "[2]")
    assert( isIncreasing(0,1,2),                         "[3]")
    assert( isIncreasing(0,1,2,2),                       "[4]")
    reject( isIncreasing(1,0),                           "[5]")

    assert( isIncreasing(0.0),                           "[6]")
    assert( isIncreasing(0.0,0.0),                       "[7]")
    assert( isIncreasing(0.0,1.0,2.0),                   "[8]")
    assert( isIncreasing(0.0,1.0,2.0,2.0),               "[9]")
    reject( isIncreasing(1.0,0.0),                       "[10]")
  }

  test("isDecreasing")
  {
    assert( isDecreasing(0),                             "[1]")
    assert( isDecreasing(0,0),                           "[2]")
    assert( isDecreasing(2,1,1),                         "[3]")
    assert( isDecreasing(2,2,1,0),                       "[4]")
    reject( isDecreasing(0,1),                           "[5]")

    assert( isDecreasing(0.0),                           "[6]")
    assert( isDecreasing(0.0,0.0),                       "[7]")
    assert( isDecreasing(2.0,1.0,1.0),                   "[8]")
    assert( isDecreasing(2.0,2.0,1.0,0.0),               "[9]")
    reject( isDecreasing(0.0,1.0),                       "[10]")
  }

  test("isPowerOf2")
  {
    assert((-8 to +8).map(isPowerOf2) ==
           "FFFFFFFFFTTFTFFFT".map(_=='T'),              "[1]")
  }

  test("clamp")
  {
    assert(clamp(-1,-2,+1) == -1,                        "[1]")
    assert(clamp(-1,-1,+1) == -1,                        "[2]")
    assert(clamp(-1, 0,+1) ==  0,                        "[3]")
    assert(clamp(-1,+1,+1) == +1,                        "[4]")
    assert(clamp(-1,+2,+1) == +1,                        "[5]")
  }

  test("mod")
  {
    forAll("i","n") {(i: ℤ,n: ℕ) ⇒ whenever {n > 0}
    {
      val r = mod(i,n)

      assert(0<=r && r<n,                                "[0 ≤ r < n]")
      assert(0<=i implies i == n * (i/n) + r,            "[i = n⋅q + r]")
    }}
  }
}

//****************************************************************************
