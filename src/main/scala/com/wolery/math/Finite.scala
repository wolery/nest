//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Describes a type that is inhabited by a finite set of values.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*
//*
//****************************************************************************

package com.wolery
package math

//****************************************************************************

/**
 * Describes a type that is inhabited by a finite set of values.
 *
 * Every finite type is isomorphic to some initial segment `[0, .. , n)` of ℕ,
 * the set of natural numbers, although the bijection is of course not unique,
 * there being one for each of the `n!` permutations of the type's `n` values.
 *
 * Any such mapping is sufficient to uniquely encode the values of the type as
 * natural numbers, however,  thus enabling us to easily enumerate them, store
 * them in bit sets, and so forth.
 *
 * Class `Finite` represents a type class whose instances effect such mappings
 * for 'small' types with, say, just a few hundred values or less.
 *
 * See class `FiniteSet` for an example of how we can exploit this capability.
 *
 * Instances satisfy the axioms:
 * {{{
 *           size  >  0                                  α is inhabited
 *         toℕ(a)  ∈  [0, size)                          to initial segment
 *    fromℕ ∘ toℕ  =  identity[α]                        toℕ   is injective
 *    toℕ ∘ fromℕ  =  identity[ℕ]                        fromℕ is injective
 * }}}
 * for all `a` in `α`, where `∘` denotes function composition.
 *
 * @tparam α  A type inhabited by a finite set of values.
 *
 * @author Jonathon Bell
 */
trait Finite[α]
{
  /**
   * The number of values inhabiting the type α. The size must be greater than
   * 0; that is, the type `α` is ''inhabited''.
   */
  val size: ℕ

  /**
   * Encodes the given value as a natural number in the range `[0, size)'.
   *
   * Implements a bijection from the values of type `α` onto the initial `size`
   * natural numbers.
   *
   * @param  a  Any value of type α.
   *
   * @return A natural number in the range `[0, size)'.
   */
  def toℕ(a: α): ℕ

  /**
   * Recovers a value of type `α` from its encoding as a natural number in the
   * range `[0, size)`.
   *
   * Undefined for values outside of this interval.
   *
   * Effects a bijection from the initial `size` natural numbers onto the type
   * `α`.
   *
   * @param  n  A natural number in the range `[0, size)`.
   *
   * @return A value of type `α`. Undefined when `n ∉ [0, size)`.
   */
  def fromℕ(n: ℕ): α
}

/**
 * Companion object for class `Finite`.
 *
 * @author Jonathon Bell
 */
object Finite
{
  /**
   * An instance of the type class `Finite` for the type `Byte`.
   */
  implicit
  object `Finite[Byte]` extends Finite[Byte]
  {
    val size: ℕ                  = 256
    def toℕ(b: Byte): ℕ          = (if (b <   0) 127 - b else b).toInt
    def fromℕ(n: ℕ): Byte        = (if (n > 127) 127 - n else n).toByte
  }

  /**
   * Derives an instance of the type class `Finite` for the given enumeration.
   *
   * Assumes the enumeration 'begins at 0'; that is, the value of 'id' for the
   * first element of the enumeration is `0`.
   *
   * @param  enum  An arbitrary enumeration.
   *
   * @return Evidence that the given enumeration is indeed finite.
   */
  def apply(enum : Enumeration): Finite[enum.Value] = new Finite[enum.Value]
  {
    val size: ℕ                  = enum.values.size      // Number of values
    def toℕ(e: enum.Value): ℕ    = e.id                  // Return the id
    def fromℕ(n: ℕ): enum.Value  = enum(n)               // Search for id
  }
}

//****************************************************************************
