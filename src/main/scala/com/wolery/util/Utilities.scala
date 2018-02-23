//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Miscellaneous utility functions used throughout the wolery.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery
package util

/**
 * Miscellaneous utility functions used throughout the wolery.
 *
 * @author Jonathon Bell
 */
trait utilities
{
  /**
   * Returns a subscripted version of the given character if such a code point
   * is present in the Unicode standard, and the given character otherwise.
   *
   * Subscripted versions exist for the full set of Arabic numerals.
   *
   * @param  c  An arbitrary character.
   *
   * @return The subscripted version of `c`, if defined by Unicode,  otherwise
   *         `c`.
   *
   * @see    [[https://en.wikipedia.org/wiki/Unicode_subscripts_and_superscripts
   *         Unicode subscripts and superscripts (Wikipedia)]]
   */
  final
  def subscript(c: Char): Char = c match
  {
    case '-'              ⇒ '₋'
    case '+'              ⇒ '₊'
    case '='              ⇒ '₌'
    case '('              ⇒ '₍'
    case ')'              ⇒ '₎'
    case  c if c.isDigit  ⇒ ('₀' + c - '0').toChar
    case  _               ⇒ c
  }

  /**
   * Returns a superscripted version of the given character if such a code point
   * is present in the Unicode standard, and the given character otherwise.
   *
   * Superscripted versions exist for the full set of Arabic numerals.
   *
   * @param  c  An arbitrary character.
   *
   * @return The superscripted version of the character `c`, if defined by Unicode,
   *         otherwise `c`.
   *
   * @see    [[https://en.wikipedia.org/wiki/Unicode_subscripts_and_superscripts
   *         Unicode subscripts and superscripts (Wikipedia)]]
   */
  final
  def superscript(c: Char): Char = c match
  {
    case '1'              ⇒ '¹'
    case '2'              ⇒ '²'
    case '3'              ⇒ '³'
    case '-'              ⇒ '⁻'
    case '+'              ⇒ '⁺'
    case '='              ⇒ '⁼'
    case '('              ⇒ '⁽'
    case ')'              ⇒ '⁾'
    case  c if c.isDigit  ⇒ ('⁰' + c - '0').toChar
    case  _               ⇒ c
  }

  /**
   * Returns a copy of the given string in which  characters with  subscripted
   * versions defined by Unicode are replaced with their subscripted variants.
   *
   * For example:
   * {{{
   *    subscript("A13")  =  "A₁₃"
   * }}}
   *
   * @param  s  A string of characters.
   *
   * @return A superscripted version of the character string `s`.
   */
  final
  def subscript(s: String): String = s.map(subscript)

  /**
   * Returns a copy of the given string in which characters with superscripted
   * versions defined by Unicode are replaced with their superscripted variants.
   *
   * For example:
   * {{{
   *    superscript("A13")  =  "A¹³"
   * }}}
   *
   * @param  s  A string of characters.
   *
   * @return A superscripted version of the character string `s`.
   */
  final
  def superscript(s: String): String = s.map(superscript)

  /**
   * Returns true if the value `v` lies within the closed interval `[lo, hi]`.
   *
   * By 'closed' we mean that the interval includes its own bounds.
   *
   * @param  v   The value to test for inclusion in the closed interval.
   * @param  lo  The lower bound of the closed interval.
   * @param  hi  The upper bound of the closed interval.
   *
   * @return `true` if `v` lies within the closed interval `[lo, hi]`.
   */
  final
  def isBetween[α: Ordering](v: α,lo: α,hi: α): Bool =
  {
    assert(lo <= hi)                                     // Validate arguments

    lo<=v && v<=hi                                       // Test for inclusion
  }

  /**
   * Returns true if every  consecutive pair of elements in the given sequence
   * satisfies the given binary predicate.
   *
   * Generalizes testing that a sequence is sorted for an arbitrary definition
   * of 'order'.
   *
   * @param  iterable  The collection of elements to examine.
   * @param  compare   The binary predicate with which to compare consecutive
   *                   pairs of elements.
   *
   * @return `true` if the elements of `iterable` are in order with respect to
   *         the predicate `compare`.
   */
  final
  def isMonotonic[α](iterable: Iterable[α])(compare: (α,α) ⇒ Bool): Bool =
  {
    val (i,j) = iterable.iterator.duplicate              // Get two iterators

    def loop(): Bool =                                   // For each element
    {
      !j.hasNext || compare(i.next,j.next) && loop()     // ...check next pair
    }

    !j.hasNext || {j.next;loop()}                        // Skip first element
  }

  /**
   * Returns true if each element of the given sequence is less than or equal
   * to its successor.
   *
   * @param  iterable  The collection of elements to examine.
   *
   * @return `true` if the elements `iterable` are non-decreasing.
   */
  final
  def isIncreasing[α: Ordering](sequence: α*): Bool =
  {
    isMonotonic(sequence)(_ <= _)                        // Is non decreasing?
  }

  /**
   * Returns true if each element of the given sequence is greater than or
   * equal to its successor.
   *
   * @param  iterable  The collection of elements to examine.
   *
   * @return `true` if the elements `iterable` are non-increasing.
   */
  final
  def isDecreasing[α: Ordering](sequence: α*): Bool =
  {
    isMonotonic(sequence)(_ >= _)                        // Is non increasing?
  }

  /**
   * Returns true if the integer `i` is a natural power of 2.
   *
   * @param  i  An arbitrary integer.
   *
   * @return `true` if the integer `i` is a natural power of 2.
   */
  final
  def isPowerOf2(i: ℤ): Bool =
  {
    i > 0 && (i & (i-1)) == 0                            // Mask off with i-1
  }

  /**
   * Clamp the given value to lie within the closed interval `[lo, hi]`.
   *
   * By 'closed' we mean that the interval includes its own bounds.
   *
   * @param  lo  The lower bound of the range to which `v` is clamped.
   * @param  v   The value to be clamped.
   * @param  hi  The upper bound of the range to which `v` is clamped.
   *
   * @return The value `v`, clamped to lie within the closed interval `[lo, hi]`.
   */
  final
  def clamp[α: Ordering](lo: α,v: α,hi: α): α =
  {
    assert(lo <= hi);                                    // Validate arguments

    if (v < lo)                                          // Less than 'lo'?
      lo                                                 // ...clamp to 'lo'
    else
    if (v > hi)                                          // Greater than 'hi'?
      hi                                                 // ...clamp to 'hi'
    else                                                 // Within interval
      v                                                  // ...nothing to do
  }

  /**
   * Returns the non-negative remainder of the integer `i` upon division by
   * the positive integer `n`.
   *
   * The result is the unique integer `0 ≤ r < n` such that `i = n⋅q + r` for
   * some `q` in `ℤ`.
   *
   * @param  i  An integer.
   * @param  n  A positive integer.
   *
   * @return The non-negative remainder of `i` upon division by `n`.
   *
   * @see    [[https://en.wikipedia.org/wiki/Modulo_operation Modulo operation
   *         (Wikipedia)]]
   */
  final
  def mod(i: ℤ,n: ℕ): ℕ =
  {
    assert(n > 0,"non-positive modulus")                 // Validate argument

    val r = i % n;                                       // Compute remainder

    if (r < 0) r + n else r                              // Check non-negative
  }

  /**
    * Emits an audible 'beep' that depends upon the native system settings and
    * hardware capabilities.
    */
  final
  def beep(): Unit =
  {
    java.awt.Toolkit.getDefaultToolkit.beep()            // Delegate to AWT
  }
}

/**
 * Miscellaneous utility functions used throughout wolery.
 *
 * @author Jonathon Bell
 */
object utilities extends utilities

//****************************************************************************
