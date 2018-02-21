//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Describes the operations that allow the type constructor `F[_]`
//*            to act as a (covariant endo-) functor on the category `Scala`.
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
 * are types and whose morphisms the computable functions between them.
 *
 * Instances satisfy the axioms:
 * {{{
 *     map f id,,α,, = id,,F[α],,                        preserve identities
 *     map (f ∘ g) = (map f) ∘ (map g)                   preserve compositions
 * }}}
 * for all types `α` and composable functions `f` and `g`, where `id,,α,,`
 * denotes the identity function at type `α`.
 *
 * In other words the function `map` preserves identity functions and function
 * compositions.
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
   * @param Fa  .
   * @param f   .
   *
   * @return
   */
  def map[α,β](Fa: F[α])(f: α ⇒ β): F[β] =
  {
    lift(f)(Fa)
  }

  /**
   * @param f   .
   * @param Fa  .
   *
   * @return
   */
  def lift[α,β](f: α ⇒ β)(Fa: F[α]): F[β] =
  {
    map(Fa)(f)
  }
}

//****************************************************************************
