//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Common definitions used throughout the Wolery.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com

//****************************************************************************

/**
 * Common definitions used throughout the Wolery.
 *
 * @author Jonathon Bell
 */
package object wolery
{
  /**
   * A non-negative integer, represented as a native 32 bit signed integer.
   */
  type ℕ = Int

  /**
   * An integer, represented as a native 32 bit signed integer.
   */
  type ℤ = Int

  /**
   * A real number, represented as a double precision floating point number.
   */
  type ℝ = Double

  /**
   * A truth value, represented as a native boolean.
   */
  type Bool = Boolean

  /**
   * A name for some entity, represented as a native string.
   */
  type Name = String

  /**
   * Extends the boolean type with additional methods.
   *
   * @param  a  A truth value.
   * @param  b  A truth value.
   */
  implicit
  class BoolSyntax(private val a: Bool) extends AnyVal
  {
    def iff    (b:   Bool): Bool =  a == b
    def implies(b: ⇒ Bool): Bool = !a || b
  }

  /**
   * Extends the elements of a partially ordered type with additional methods.
   *
   * @tparam α  An instance of the `PartialOrdering` type class.
   *
   * @param  a  An element of the partially ordered type `α`.
   * @param  b  An element of the partially ordered type `α`.
   */
  implicit
  class PartialOrderingSyntax[α](private val a: α) extends AnyVal
  {
    def <    (b: α)(implicit ε: PartialOrdering[α]): Bool = ε.lt(a,b)
    def <=   (b: α)(implicit ε: PartialOrdering[α]): Bool = ε.lteq(a,b)
    def >    (b: α)(implicit ε: PartialOrdering[α]): Bool = ε.gt(a,b)
    def >=   (b: α)(implicit ε: PartialOrdering[α]): Bool = ε.gteq(a,b)
    def ≡    (b: α)(implicit ε: PartialOrdering[α]): Bool = ε.equiv(a,b)
    def equiv(b: α)(implicit ε: PartialOrdering[α]): Bool = ε.equiv(a,b)
  }

  /**
   * Extends the elements of an ordered type with additional methods.
   *
   * @tparam α  An instance of the `Ordering` type class.
   *
   * @param  a  An element of the ordered type `α`.
   * @param  b  An element of the ordered type `α`.
   */
  implicit
  class OrderingSyntax[α](private val a: α) extends AnyVal
  {
    def max      (b: α)     (implicit ε: Ordering[α]): α    = ε.max(a,b)
    def min      (b: α)     (implicit ε: Ordering[α]): α    = ε.min(a,b)
    def clamp    (l: α,h: α)(implicit ε: Ordering[α]): α    = util.utilities.clamp    (a,l,h)
    def isBetween(l: α,h: α)(implicit ε: Ordering[α]): Bool = util.utilities.isBetween(a,l,h)
  }

  /**
   * Extends sequences with additional methods.
   *
   * Enables us to refer to the methods of class `Seq[α]` by their traditional
   * symbolic names:
   *
   *  - `\`  set difference
   *  - `∪`  set union
   *  - `∩`  set intersection
   *  - `⊖`  symmetric difference
   *  - `⊂`  set inclusion (strict)
   *  - `⊆`  set inclusion (non-strict)
   *  - `∈`  set membership
   *
   * @tparam α  The type of an element.
   *
   * @param  s  A sequence of elements of type `α`.
   * @param  t  A sequence of elements of type `α`.
   * @param  e  A (candidate) set element of type `α`.
   *
   * @see    [[ElementSyntax]]
   */
  implicit
  class SeqSyntax[α](private val s: Seq[α]) extends AnyVal
  {
    def \ (t: Seq[α]): Seq[α] =  s.diff(t)
    def ∪ (t: Seq[α]): Seq[α] =  s.union(t)
    def ∩ (t: Seq[α]): Seq[α] =  s.intersect(t)
    def ⊖ (t: Seq[α]): Seq[α] =  s.union(t) diff s.intersect(t)
    def ⊂ (t: Seq[α]): Bool   =  s.containsSlice(t) && !t.containsSlice(s)
    def ⊃ (t: Seq[α]): Bool   =  t.containsSlice(s) && !s.containsSlice(t)
    def ⊄ (t: Seq[α]): Bool   = !s.containsSlice(t) ||  t.containsSlice(s)
    def ⊅ (t: Seq[α]): Bool   = !t.containsSlice(s) ||  s.containsSlice(t)
    def ⊆ (t: Seq[α]): Bool   =  s.containsSlice(t)
    def ⊇ (t: Seq[α]): Bool   =  t.containsSlice(s)
    def ⊈ (t: Seq[α]): Bool   = !s.containsSlice(t)
    def ⊉ (t: Seq[α]): Bool   = !t.containsSlice(s)
    def ∋ (e: α)     : Bool   =  s.contains(e)
    def ∌ (e: α)     : Bool   = !s.contains(e)
  }

  /**
   * Extends sets with additional methods.
   *
   * Enables us to refer to the methods of class `Set[α]` by their traditional
   * symbolic names:
   *
   *  - `\`  set difference
   *  - `∪`  set union
   *  - `∩`  set intersection
   *  - `⊖`  symmetric difference
   *  - `⊂`  set inclusion (strict)
   *  - `⊆`  set inclusion (non-strict)
   *  - `∈`  set membership
   *  - `∅`  the empty set
   *
   * @tparam α  The type of an element.
   *
   * @param  s  A set of elements of type `α`.
   * @param  t  A set of elements of type `α`.
   * @param  e  A (candidate) set element of type `α`.
   *
   * @see    [[ElementSyntax]]
   */
  implicit
  class SetSyntax[α](private val s: Set[α]) extends AnyVal
  {
    def \ (t: Set[α]): Set[α] =  s.diff(t)
    def ∪ (t: Set[α]): Set[α] =  s.union(t)
    def ∩ (t: Set[α]): Set[α] =  s.intersect(t)
    def ⊖ (t: Set[α]): Set[α] =  s.union(t) diff s.intersect(t)
    def ⊂ (t: Set[α]): Bool   =  s.subsetOf(t) && !t.subsetOf(s)
    def ⊃ (t: Set[α]): Bool   =  t.subsetOf(s) && !s.subsetOf(t)
    def ⊄ (t: Set[α]): Bool   = !s.subsetOf(t) ||  t.subsetOf(s)
    def ⊅ (t: Set[α]): Bool   = !t.subsetOf(s) ||  s.subsetOf(t)
    def ⊆ (t: Set[α]): Bool   =  s.subsetOf(t)
    def ⊇ (t: Set[α]): Bool   =  t.subsetOf(s)
    def ⊈ (t: Set[α]): Bool   = !s.subsetOf(t)
    def ⊉ (t: Set[α]): Bool   = !t.subsetOf(s)
    def ∋ (e: α)     : Bool   =  s.contains(e)
    def ∌ (e: α)     : Bool   = !s.contains(e)
  }

  /**
   * Extends the element type `α` with additional methods.
   *
   * @tparam α  The type of an element.
   *
   * @param  s  A collection of elements of type `α`.
   * @param  e  A (candidate) set element of type `α`.
   *
   * @see    [[SeqSyntax]] and [[SetSyntax]]
   */
  implicit
  class ElementSyntax[α](private val e: α) extends AnyVal
  {
    def ∈ (s: Seq[α])  : Bool =  s.contains(e)
    def ∈ (s: Set[α])  : Bool =  s.contains(e)
    def ∈ (s: Map[α,_]): Bool =  s.contains(e)
    def ∉ (s: Seq[α])  : Bool = !s.contains(e)
    def ∉ (s: Set[α])  : Bool = !s.contains(e)
    def ∉ (s: Map[α,_]): Bool = !s.contains(e)
  }

  /**
   * The polymorphic empty set.
   *
   * @tparam α  The type of an element.
   *
   * @return An empty set of type `Set[α]`.
   *
   * @see    [[SetSyntax]]
   */
  def ∅[α]: Set[α] = Set[α]()
}

//****************************************************************************
