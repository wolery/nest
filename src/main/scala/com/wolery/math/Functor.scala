//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Describes the operations that allow the type constructor 'F[_]'
//*            to act as a (covariant endo-) functor on the category 'Scala'.
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery
package math

/**
 * Describes the operations that allow the type constructor `F[_]` to act as a
 * (covariant endo-) functor on the category `Scala`.
 *
 * Loosely speaking, a functor is a unary type constructor whose instances can
 * be 'mapped over' in some sense;  examples include `Seq`, `Set`, and `List`,
 * to name just a few.
 *
 * The term originates from category theory, where it refers to a homomorphism
 * between categories. For us, the relevant category is `Scala`, whose objects
 * are types and whose morphisms are the computable functions between them.
 *
 * Instances satisfy the axioms:
 * {{{
 *     map(Fa)(id,,α,,) = Fa                             preserve identities
 *     map(Fa)(f ∘ g)   = map(map(Fa)(g))(f)             preserve compositions
 * }}}
 * or, equivalently:
 * {{{
 *     lift(id,,α,,) = id,,F[α],,                        preserve identities
 *     lift(f ∘ g)   = (lift f) ∘ (lift g)               preserve compositions
 * }}}
 * for all types `α`, values `Fa` in `F[α]`, and composable functions `f` and
 * `g`, where `id,,α,,` denotes the identity function at type `α`.
 *
 * That is, `lift` preserves identity functions and function compositions, and
 * is thus a homomorphism from `Scala` to the sub-category `F[Scala]`.
 *
 * @tparam F  A unary type constructor whose instances can be 'mapped over' in
 *            some sense.
 *
 * @see    [[https://en.wikipedia.org/wiki/Functor Functor (Wikipedia)]]
 *
 * @author Jonathon Bell
 */
trait Functor[F[_]]
{
  /**
   * Returns the result of 'mapping' the given function over the given value,
   * whatever this may mean for the specific functor in question.
   *
   * The only requirement is that this `mapping` must preserve both identities
   * and function compositions; that is:
   * {{{
   *     map(Fa)(id,,α,,) = Fa                           preserve identities
   *       map(Fa)(f ∘ g) = map(map(Fa)(g))(f)           preserve compositions
   * }}}
   * for all types `α`, values `Fa` in `F[α]`, and composable functions `f` and
   * `g`, where `id,,α,,` denotes the identity function at type `α`.
   *
   * @param  Fa  A value of type `F[α]`.
   * @param  f   A function from type `α` to type `β`.
   *
   * @return The result of 'mapping' the function `f` over `Fa`, whatever this
   *         may mean for the specific functor in question.
   */
  def map[α,β](Fa: F[α])(f: α ⇒ β): F[β] =
  {
    lift(f)(Fa)                                          // Delegate to lift
  }

  /**
   * Returns the result of 'mapping' the given function over the given value,
   * whatever this may mean for the specific functor in question.
   *
   * The only requirement is that this `mapping` must preserve both identities
   * and function compositions; that is:
   * {{{
   *     lift(id,,α,,) = id,,F[α],,                      preserve identities
   *       lift(f ∘ g) = (lift f) ∘ (lift g)             preserve compositions
   * }}}
   * for all types `α`, values `Fa` in `F[α]`, and composable functions `f` and
   * `g`, where `id,,α,,` denotes the identity function at type `α`.
   *
   * In other words, `lift` is a homomorphism from the category `Scala` to the
   * sub-category `F[Scala]`.
   *
   * @param  f   A function from type `α` to type `β`.
   * @param  Fa  A value of type `F[α]`.
   *
   * @return The result of 'mapping' the function `f` over `Fa`, whatever this
   *         may mean for the specific functor in question.
   */
  def lift[α,β](f: α ⇒ β)(Fa: F[α]): F[β] =
  {
    map(Fa)(f)                                            // Delegate to map
  }
}

//****************************************************************************
