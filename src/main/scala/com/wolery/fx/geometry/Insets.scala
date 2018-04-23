//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : Header:
//*
//*
//*  Purpose : A companion object for class `Insets`.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery
package fx
package geometry

import javafx.geometry.{Insets ⇒ jInsets}

/**
 * A companion object for class `Insets`.
 *
 * @author Jonathon Bell
 */
object Insets
{
  /**
   * Returns an instance whose four insets are all initialized to `0`.
   *
   * @return An instance whose four insets are all initialized to `0`.
   */
  def apply(): jInsets =
  {
    jInsets.EMPTY
  }

  /**
   * Returns an instance whose four insets are initialized to the given value.
   *
   * @param  trbl  The size in pixels of all four insets.
   *
   * @return An instance whose four insets are initialized to the given value.
   */
  def apply(trbl: ℝ): jInsets =
  {
    new jInsets(trbl,trbl,trbl,trbl);
  }

  /**
   * Returns an instance whose top and bottom insets are initialized to `tb`,
   * and whose right and left insets are initialized to `rl`.
   *
   * @param  tb  The size in pixels of the top and bottom insets.
   * @param  rl  The size in pixels of the right and left insets.
   *
   * @return An instance whose top and bottom insets are initialized to `tb`,
   * and whose right and left insets are initialized to `rl`.
   */
  def apply(tb: ℝ,rl: ℝ): jInsets =
  {
    new jInsets(tb,rl,tb,rl);
  }

  /**
   * Returns an instance whose insets are initialized to the given values,
   *
   * @param  t  The size in pixels of the top inset.
   * @param  r  The size in pixels of the right inset.
   * @param  b  The size in pixels of the bottom inset.
   * @param  l  The size in pixels of the left inset.
   *
   * @return An instance whose insets are initialized to the given values,
   */
  def apply(t: ℝ,r: ℝ,b: ℝ,l: ℝ): jInsets =
  {
    new jInsets(t,r,b,l);
  }
}

//****************************************************************************
