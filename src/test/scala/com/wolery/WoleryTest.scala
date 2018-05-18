//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Unit tests for the Wolery package object.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery

//****************************************************************************

class WoleryTest extends test.Suite
{
  test("BoolSyntax")
  {
    assert(true  iff true,            "[⊤ ⇔ ⊤]")
    reject(true  iff false,           "[⊤ ⇔ ⊥]")
    reject(false iff true,            "[⊥ ⇔ ⊤]")
    assert(false iff false,           "[⊥ ⇔ ⊥]")

    assert(true  implies true,        "[⊤ ⇒ ⊤]")
    reject(true  implies false,       "[⊤ ⇒ ⊥]")
    assert(false implies true,        "[⊥ ⇒ ⊤]")
    assert(false implies false,       "[⊥ ⇒ ⊥]")
  }

  test("OrderingSyntax")
  {
    assert(0  .max(1)   == 1,         "[max 1]")
    assert(0.0.max(1.0) == 1.0,       "[max 2]")
    assert('a'.max('b') == 'b',       "[max 3]")

    assert(0  .min(1)   == 0,         "[min 1]")
    assert(0.0.min(1.0) == 0.0,       "[min 2]")
    assert('a'.min('b') == 'a',       "[min 3]")

    assert(0.isBetween(0,1),          "[isBetween 1]")
    assert(1.isBetween(0,1),          "[isBetween 2]")
    reject(2.isBetween(0,1),          "[isBetween 3]")
    assert(0.0.isBetween(0.0,1.0),    "[isBetween 4]")
    assert(1.0.isBetween(0.0,1.0),    "[isBetween 5]")
    reject(2.0.isBetween(0.0,1.0),    "[isBetween 6]")
  }

  test("SeqSyntax")
  {
    val s = Seq(1,2,3)
    val t = Seq(  2,3,4)

    assert(∅[ℕ].isEmpty,              "[∅[ℕ]]")
    assert(∅[ℤ].isEmpty,              "[∅[ℤ]]")
    assert(∅[ℝ].isEmpty,              "[∅[ℝ]]")

    assert(1 ∈ s,                     "[∈]")
    assert(1 ∉ t,                     "[∉]")
    assert(s ∋ 1,                     "[∋]")
    assert(t ∌ 1,                     "[∌]")

    assert(s \ t == Seq(1),           "[\\]")
    assert(s ∪ t == Seq(1,2,3,2,3,4), "[∪]")
    assert(s ∩ t == Seq(2,3),         "[∩]")
    assert(s ⊖ t == Seq(1,2,3,4),     "[⊖]")
    reject(s ⊂ t,                     "[⊂]")
    reject(s ⊃ t,                     "[⊃]")
    assert(s ⊄ t,                     "[⊄]")
    assert(s ⊅ t,                     "[⊅]")
    reject(s ⊆ t,                     "[⊆]")
    reject(s ⊇ t,                     "[⊇]")
    assert(s ⊈ t,                     "[⊈]")
    assert(s ⊉ t,                     "[⊉]")
  }

  test("SetSyntax")
  {
    val s = Set(1,2,3)
    val t = Set(  2,3,4)

    assert(∅[ℕ].isEmpty,              "[∅[ℕ]]")
    assert(∅[ℤ].isEmpty,              "[∅[ℤ]]")
    assert(∅[ℝ].isEmpty,              "[∅[ℝ]]")

    assert(1 ∈ s,                     "[∈]")
    assert(1 ∉ t,                     "[∉]")
    assert(s ∋ 1,                     "[∋]")
    assert(t ∌ 1,                     "[∌]")

    assert(s \ t == Set(1),           "[\\]")
    assert(s ∪ t == Set(1,2,3,4),     "[∪]")
    assert(s ∩ t == Set(2,3),         "[∩]")
    assert(s ⊖ t == Set(1,4),         "[⊖]")
    reject(s ⊂ t,                     "[⊂]")
    reject(s ⊃ t,                     "[⊃]")
    assert(s ⊄ t,                     "[⊄]")
    assert(s ⊅ t,                     "[⊅]")
    reject(s ⊆ t,                     "[⊆]")
    reject(s ⊇ t,                     "[⊇]")
    assert(s ⊈ t,                     "[⊈]")
    assert(s ⊉ t,                     "[⊉]")
  }

  test("ElementSyntax")
  {
    assert(1 ∈ Seq(1,2),              "[∈]")
    assert(1 ∈ Set(1,2),              "[∈]")
    assert(1 ∈ Map(1→1,2→2),          "[∈]")
    assert(1 ∈ (1 to 2),              "[∈]")
    assert(0 ∉ Seq(1,2),              "[∉]")
    assert(0 ∉ Set(1,2),              "[∉]")
    assert(0 ∉ Map(1→1,2→2),          "[∉]")
    assert(0 ∉ (1 to 2),              "[∉]")
  }
}

//****************************************************************************
