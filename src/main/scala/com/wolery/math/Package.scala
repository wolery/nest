//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : The package object for the `math` package.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery

//****************************************************************************

/**
 * The package object for the `math` package.
 *
 * @author Jonathon Bell
 */
package object math
{
  /**
   * Extends the elements of a semigroup with additional methods.
   *
   * @tparam S  An instance of the `Semigroup` type class.
   * @param  s  An element of the semigroup `S`.
   * @param  t  An element of the semigroup `S`.
   */
  implicit
  class SemigroupSyntax[S](private val s: S) extends AnyVal
  {
    def ⋅      (t: S)(implicit ε: Semigroup[S]): S = ε.combine(s,t)
    def combine(t: S)(implicit ε: Semigroup[S]): S = ε.combine(s,t)
  }

  /**
   * Extends the elements of a group with additional methods.
   *
   * @tparam G  An instance of the `Group` type class.
   * @param  g  An element of the group `G`.
   */
  implicit
  class GroupSyntax[G](private val g: G) extends AnyVal
  {
    def unary_-(implicit ε: Group[G]): G = ε.inverse(g)
    def inverse(implicit ε: Group[G]): G = ε.inverse(g)
  }

  /**
   * Extends the elements of a carrier set for a group action with additional
   * methods.
   *
   * @tparam S  A set acted upon by the group `G`.
   * @tparam G  A group acting upon the set `S`.
   * @param  s  An element of the carrier set `S`.
   * @param  t  An element of the acting group `G`.
   */
  implicit
  class ActionSyntax[S,G](private val s: S) extends AnyVal
  {
    def apply(g: G)(implicit ε: Action[S,G]): S = ε(s,g)
    def +    (g: G)(implicit ε: Action[S,G]): S = ε(s,g)
    def -    (g: G)(implicit ε: Action[S,G]): S = ε(s,ε.group.inverse(g))
  }

  /**
   * Extends the elements of a carrier set for a torsor action with additional
   * methods.
   *
   * @tparam S  A set acted upon sharply transitively by the group `G`.
   * @tparam G  A group acting sharply transitively upon the set `S`.
   * @param  s  An element of the carrier set `S`.
   * @param  t  An element of the acting group `G`.
   */
  implicit
  class TorsorSyntax[S,G](private val s: S) extends AnyVal
  {
    def -    (t: S)(implicit ε: Torsor[S,G]): G = ε.delta(t,s)
    def delta(t: S)(implicit ε: Torsor[S,G]): G = ε.delta(t,s)
  }

  /**
   * Extends the values of a finite type with additional methods.
   *
   * @tparam α  A type inhabited by a finite set of values.
   * @param  a  A value of the finite type `α`.
   */
  implicit
  class FiniteSyntax[α](private val a: α) extends AnyVal
  {
    def toℕ(implicit ε: Finite[α]): ℕ = ε.toℕ(a)
  }

  /**
   * Extends the elements of the natural numbers `ℕ` with additional methods.
   *
   * @param  n  A natural number.
   */
  implicit
  class NaturalSyntax[α](private val n: ℕ) extends AnyVal
  {
    def fromℕ(implicit ε: Finite[α]): α = {assert(n >= 0);ε.fromℕ(n)}
  }

  /**
   * The integers `ℤ` form a group under addition.
   */
  implicit
  object `Group[ℤ]` extends Group[ℤ]
  {
    val e                 : ℤ = 0
    def inverse(i: ℤ)     : ℤ = -i
    def combine(i: ℤ,j: ℤ): ℤ = i + j
  }

  /**
   * The reals `ℝ` form a group under addition.
   */
  implicit
  object `Group[ℝ]` extends Group[ℝ]
  {
    val e                 : ℝ = 0
    def inverse(i: ℝ)     : ℝ = -i
    def combine(i: ℝ,j: ℝ): ℝ = i + j
  }

  /**
   * The type constructor `Set` is a `Scala` endo-functor.
   */
  implicit
  object `Functor[Set]` extends Functor[Set]
  {
    override
    def map[α,β](set: Set[α])(f: α ⇒ β): Set[β] = set.map(f)
  }

  /**
   * Sets are partially ordered by set inclusion.
   */
  implicit
  def `PartialOrdering[Set[α]]`[α,S[α] <: Set[α]]: PartialOrdering[S[α]] =
  {
    object instance extends PartialOrdering[Set[Any]]
    {
      def lteq(s: Set[Any],t: Set[Any]): Bool = s ⊆ t

      def tryCompare(s: Set[Any],t: Set[Any]) = (s ⊆ t,s ⊇ t) match
      {
        case (true, true)  ⇒ Some( 0)
        case (true, false) ⇒ Some(-1)
        case (false,true)  ⇒ Some(+1)
        case (false,false) ⇒ None
      }
    }

    instance.asInstanceOf[PartialOrdering[S[α]]]
  }
}

//****************************************************************************
