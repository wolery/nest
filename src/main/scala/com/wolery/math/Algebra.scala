//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Defines type classes for the various algebraic constructions we
//*            shall be leveraging throughout the library.
//*
//*            Our principle interest is in the action of the ring of integers
//*            upon such sets as the (even tempered) pitches and notes,  hence
//*            our choice of an additive notation for groups and the like, and
//*            our singling out of transposing and intervallic sets as special
//*            instances of these more general constructs.
//*
//*            Implicit wrappers that provide  syntactic sugar for the classes
//*            in the form of overloaded arithmetic operators are also defined
//*            within the core package object.
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery
package math

/**
 * Describes the operations that endow the data type `S` with the structure of
 * a semigroup.
 *
 * Instances satisfy the axiom:
 * {{{
 *     (s₁ ⋅ s₂) ⋅ s₃ = s₁ ⋅ (s₂ ⋅ s₃)                   associativity
 * }}}
 * for all `sᵢ` in `S`, where `⋅` denotes the binary function `combine`.
 *
 * In other words, `S` is an associative magma.
 *
 * @tparam S  The carrier set on which the binary operator `⋅` acts.
 *
 * @see    [[https://en.wikipedia.org/wiki/Semigroup Semigroup (Wikipedia)]]
 * @see    [[SemigroupSyntax]]
 *
 * @author Jonathon Bell
 */
trait Semigroup[S]
{
  /**
   * Returns the product of the given pair of elements, whatever this may mean
   * for the specific algebraic structure in question.
   *
   * The only requirement is that this mapping be ''associative''; that is:
   * {{{
   *     (s₁ ⋅ s₂) ⋅ s₃ = s₁ ⋅ (s₂ ⋅ s₃)                 associativity
   * }}}
   * for all `sᵢ` in `S`, where `⋅` denotes the binary function `combine`.
   *
   * @param  s  An element of the carrier set `S`.
   * @param  t  An element of the carrier set `S`.
   *
   * @return An element of the carrier set `S`.
   *
   * @see    [[https://en.wikipedia.org/wiki/Associative_property Associative
   *         property (Wikipedia)]]
   */
  def combine(s: S,t: S): S
}

/**
 * Describes the operations that endow the data type `M` with the structure of
 * a monoid.
 *
 * Instances satisfy the axioms:
 * {{{
 *            e ⋅ m = m = m ⋅ e                          identity
 *     (m₁ ⋅ m₂) ⋅ m₃ = m₁ ⋅ (m₂ ⋅ m₃)                   associativity
 * }}}
 * for all `mᵢ` in `M`, where `⋅` denotes the binary function `combine`.
 *
 * In other words, `M` is a [[Semigroup]] with an identity element.
 *
 * @tparam M  The carrier set on which the binary operator `⋅` acts.
 *
 * @see    [[https://en.wikipedia.org/wiki/Monoid Monoid (Wikipedia)]]
 *
 * @author Jonathon Bell
 */
trait Monoid[M] extends Semigroup[M]
{
  /**
   * The identity element of the monoid `M`;  that is,  the unique element `e`
   * in `M` such that `e ⋅ m = m = m ⋅ e` for all `m` in `M`, where `⋅`denotes
   * the binary function `combine`.
   */
  val e: M
}

/**
 * Describes the operations that endow the data type `G` with the structure of
 * a group.
 *
 * Instances satisfy the axioms:
 * {{{
 *            e ⋅ g = g = g ⋅ e                          identity
 *     (g₁ ⋅ g₂) ⋅ g₃ = g₁ ⋅ (g₂ ⋅ g₃)                   associativity
 *           g ⋅ -g = e = -g ⋅ g                         invertability
 * }}}
 * for all `gᵢ` in `G`, where `⋅` and  `-` denote the  functions `combine` and
 * `inverse` respectively.
 *
 * In other words, `G` is a [[Monoid]] in which every element has an inverse.
 *
 * @tparam G  The carrier set on which the binary operator `⋅` acts.
 *
 * @see    [[http://en.wikipedia.org/wiki/Group_(mathematics) Group (Wikipedia)]]
 * @see    [[GroupSyntax]]
 *
 * @author Jonathon Bell
 */
trait Group[G] extends Monoid[G]
{
  /**
   * Returns the inverse of the element `g`;  that is, the unique element `-g`
   * in  `G` such  that  `g ⋅ -g = e = -g ⋅ g`,  where `⋅` denotes  the binary
   * function `combine`.
   *
   * @param  g  An element of the carrier set `G`.
   *
   * @return The unique element `-g` in `G` such  that  `g ⋅ -g = e = -g ⋅ g`,
   * where `⋅` denotes  the binary function `combine`.
   */
  def inverse(g: G): G

  /**
   * Derives the regular (right) action of the group `G` upon itself.
   *
   * Every group `G` is isomorphic to a subgroup of the symmetric group acting
   * on `G`, a result known as ''Cayley's Theorem''.
   *
   * Equivalently, and rephrased in the language of group actions, `G` acts on
   * itself regularly by (right) multiplication.  This action is known as  the
   * ''regular action'' of `G` upon itself.
   *
   * @return An instance of the [[Torsor]] type class implementing the regular
   *         action of `G` upon itself.
   *
   * @see    [[https://en.wikipedia.org/wiki/Cayley%27s_theorem Cayley's Theorem
   *         (Wikipedia)]]
   */
  def regularAction = new Torsor[G,G]()(this)
  {
    def apply(f: G,g: G): G   = combine(f,g)             // That is ,f ⋅  g
    def delta(g: G,f: G): G   = combine(f,inverse(g))    // That is, f ⋅ -g
  }
}

/**
 * Describes a (right) action of the group `G` upon the carrier set `S`.
 *
 * Instances satisfy the axioms:
 * {{{
 *            s + e = s                                  identity
 *    s + (g₁ ⋅ g₂) = (s + g₁) + g₂                      compatability
 * }}}
 * for all `sᵢ` in `S` and `g` in `G`, where `⋅` and  `+` denote the functions
 * `combine` and `apply` respectively.
 *
 * In other words, `+` is a homomorphism from `G` into `Sym(S)`, the symmetric
 * group consisting of all permutations of `S`,  regarded as a group under the
 * composition of mappings.
 *
 * Group actions, especially those of `(ℤ,+)`, the set of integers regarded as
 * an additive group, are of special interest to us in Owl because they enable
 * us to model permutations of pitches, notes, scales, and so on, using simple
 * integer arithmetic.
 *
 * Notice than when `G` is cyclically generated by some element `g`, an action
 * of `G` upon `S` is completely determined by the permutation  `_ + g`. Thus,
 * in particular, a `ℤ`-set is uniquely determined by the function `_ + 1`.
 *
 * @tparam G      A group that acts upon the carrier set  `S` via the mapping
 *                `apply`.
 * @tparam S      A non-empty set acted upon by the group `G` via the mapping
 *                `apply`.
 * @param  group  Evidence of the fact that `G` is a group.
 *
 * @see    [[http://en.wikipedia.org/wiki/Group_action Group action (Wikipedia)]]
 * @see    [[ActionSyntax]]
 *
 * @author Jonathon Bell
 */
abstract class Action[S,G](implicit val group: Group[G])
{ε ⇒
  /**
   * Applies an element of the group `G` to an element of the carrier set `S`,
   * whatever this may mean for the specific algebraic structure in question.
   *
   * It follows from the axioms that the function `_ + g` is a  permutation of
   * the carrier set `S`,  and furthermore that `apply` is a homomorphism from
   * `G` into `Sym(S)`,  the symmetric group consisting of all of permutations
   * of `S`, regarded as a group under the composition of mappings.
   *
   * @param  s  An element of the carrier set `S`.
   * @param  f  An element of the group `G` acting upon `S`.
   *
   * @return The result of applying the permutation denoted by `g` to the set
   *         element `s`.
   */
  def apply(s: S,g: G): S

  /**
   * Derives the natural action of `G` upon `F[S]` for the given functor `F`.
   *
   * For any functor `F` there is a natural action of `G` upon `F[S]` obtained
   * by 'mapping' the action `+ : G ⇒ Sym(S)` across the members of `F[S]`.
   *
   * To see this, observe that:
   *
   *  1. `G` may be regarded as a single object category,  all of whose arrows
   *  are isomorphisms.
   *
   *  2. `S` may be regarded as a single object category whose arrows are the
   *  permutations of `S`.
   *
   *  3. From this point of view, the action `+` is just a functor from `G` to
   *  `S`.
   *
   *  4. The composition of functors `F` and `+` is itself a functor, and thus
   *  an action of `G` upon `F[S]`.
   *
   * @tparam F  A functor.
   * @param  φ  Evidence of the fact that `F` is a functor.
   *
   * @return Evidence of the fact that the action `+` extends naturally to an
   *         action of `G` upon `F[S]`.
   *
   * @see    [[http://en.wikipedia.org/wiki/Group_action#Variants_and_generalizations
   *         Group action (Wikipedia)]] - actions as functors
   * @see    [[https://typelevel.org/cats/typeclasses/functor.html Functor (Cats)]]
   */
  def lift[F[_]](implicit φ: Functor[F]) = new Action[F[S],G]
  {
    def apply(fs: F[S],g: G): F[S] = φ.map(fs)(ε(_,g))   // Lift '+' to F[S]
  }
}

/**
 * Describes a regular (right) action of the group `G` upon the carrier set
 * `S`.
 *
 * By ''regular'' we mean that the action is ''sharply transitive'';  that is,
 * for every pair of elements  `s₁` and `s₂` in `S`  there exists a ``unique``
 * element `s₂ - s₁` in `G` such that `s₁ + (s₂ - s₁) = s₂`, where `+` and `-`
 * denote the functions `apply` and `delta` respectively.
 *
 * Instances satisfy the axioms:
 * {{{
 *            s + e = s                                  identity
 *    s + (g₁ ⋅ g₂) = (s + g₁) + g₂                      compatability
 *         s₁ + (s₂ - s₁) = s₂                           regularity
 * }}}
 * for all `sᵢ` in `S`,  where `+` and `-` denote the binary functions `apply`
 * and `delta` respectively.
 *
 * We say that `S` is a ''torsor'' for the group `G`,  or simply that `S` is a
 * ''`G`-torsor''.
 *
 * Torsors,  especially those of `(ℤ,+)`, the integers regarded as an additive
 * group, and ℤ/''n''ℤ,  the integers modulo ''n'', are of special interest to
 * us in Owl  because they make precise the musical notion of the ''interval''
 * between two notes, pitches, frequencies, and so on.
 *
 * @tparam G  A group that acts regularly upon the carrier set  `S` via the
 *            mapping `apply`.
 * @tparam S  A non-empty set acted upon regularly by the group `G` via the
 *            mapping `apply`.
 *
 * @see    [[http://en.wikipedia.org/wiki/Principal_homogeneous_space Torsor (Wikipedia)]]
 * @see    [[http://math.ucr.edu/home/baez/torsors.html Torsors Made Easy (John Baez)]]
 * @see    [[TorsorSyntax]]
 *
 * @author Jonathon Bell
 */
abstract class Torsor[S,G: Group] extends Action[S,G]
{
  /**
   * Returns the 'delta' between the given pair of elements of the carrier set
   * `S`; that is, the unique element `g` in `G` such that `s + g = t`,  where
   * `+` denotes the binary function `apply`.
   *
   * @param  s  An element of the carrier set `S`.
   * @param  t  An element of the carrier set `S`.
   *
   * @return The unique element of `G` that maps `s` to `t`.
   */
  def delta(s: S,t: S): G
}

//****************************************************************************
