//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Miscellaneous utility functions for working with reflection.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*                                                                     0-0
//*                                                                   (| v |)
//**********************************************************************w*w***

package com.wolery
package util

import java.lang.invoke.{MethodHandle,MethodHandles}

/**
 * Miscellaneous utility functions for working with reflection.
 *
 * @author Jonathon Bell
 */
trait reflect
{
  /**
   * Returns a handle to the 'get' member function associated with the given
   * field of an object.
   *
   * @param  any   The object whose field is to be bound to.
   * @param  name  The name of the field to bind to.
   *
   * @return A handle to the specified method.
   */
  def fieldGet(any: AnyRef,name: String): MethodHandle =
  {
    val f = any.getClass.getDeclaredField(name)          // Get field by name

    f.setAccessible(true)                                // Bypass protection

    MethodHandles.lookup.unreflectGetter(f)              // Bind to the method
  }

  /**
   * Returns a handle to the 'set' member function associated with the given
   * field of an object.
   *
   * @param  any   The object whose field is to be bound to.
   * @param  name  The name of the field to bind to.
   *
   * @return A handle to the specified method.
   */
  def fieldSet(any: AnyRef,name: String): MethodHandle =
  {
    val f = any.getClass.getDeclaredField(name)          // Get field by name

    f.setAccessible(true)                                // Bypass protection

    MethodHandles.lookup.unreflectSetter(f)              // Bind to the method
  }

  /**
   * @param  any   The object whose method is to be bound to.
   * @param  name  The name of the method to bind to.
   * @param  args  The argument types for the method to bind to.
   *
   * @return A handle to the specified method.
   */
  def method(any: AnyRef,name: String,args: Class[_]*): MethodHandle =
  {
    val m = any.getClass.getDeclaredMethod(name,args:_*) // Get method by name

    m.setAccessible(true)                                // Bypass protection

    MethodHandles.lookup.unreflect(m)                    // Bind to the method
  }
}

/**
 * Miscellaneous utility functions for working with reflection.
 *
 * @author Jonathon Bell
 */
object reflect extends reflect

//****************************************************************************
