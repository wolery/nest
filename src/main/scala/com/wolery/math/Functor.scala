//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose :
//*
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
 * Loosely speaking, a functor is a unary type constructor whose instances can,
 * in some sense, be 'mapped over'; examples include `Seq`, `List`, and `Option`,
 * to name just a few.
 *
 * The term originates from category theory, where it refers to a homomorphism
 * between categories. For us, however, the relevant category is `Scala`, whose
 * objects are types and morphisms the computable functions between them.
 *
 * Instances satisfy the axioms:
 * {{{
 *     map f id__α__ = id__F[α]__                        identity
 *     map (f ∘ g) = (map f) ∘ (map g)                   compositionality
 * }}}
 * for all types `α` and composable functions `f` and `g`, where `id__α__`
 * denotes the identity function at type `α`.
 *
 * In other words, `map` preserves identity functions and function compositions.
 *
 * @tparam F  A unary type constructor.
 *
 * @see    [[https://en.wikipedia.org/wiki/Functor Functor (Wikipedia)]]
 *
 * @author Jonathon Bell
 */
trait Functor[F[_]]
{
  def map[α,β](fa: F[α])(f: α ⇒ β): F[β]   = lift(f)(fa)

  def lift[α,β](f: α ⇒ β)(fa: F[α]): F[β]  = map(fa)(f)
}

//****************************************************************************
