//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : Header:
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
package util

//****************************************************************************

import java.time.Duration

import System.{nanoTime ⇒ now}

//****************************************************************************

/**
 *
 *
 * @author Jonathon Bell
 */
final class Stopwatch
{
  private var m_started: Long = 0                        //
  private var m_elapsed: Long = 0                        //

  /**
   * Returns true if the stop watch is currently stopped; in other words, it
   * is not currently accumulating elapsed time.
   */
  def isStopped: Boolean =
  {
    m_started <= 0                                       // Are we stopped?
  }

  /**
   * Start the stop watch running (if it is not already).
   */
  def start() =
  {
    if (isStopped)                                       // Not yet started?
    {
      m_started = now                                    // ...save the time
    }
  }

  /**
   * Stop the stop watch (if it is currently started).
   */
  def stop() =
  {
    if (!isStopped)                                      // Is it started?
    {
      m_elapsed+= delta
      m_started = 0
    }
  }

  def elapsed: Duration =
  {
    Duration.ofNanos(m_elapsed + delta)
  }

  def reset() =
  {
    m_started = 0
    m_elapsed = 0
  }

  override
  def toString: String =
  {
    elapsed.toString
  }

  private
  def delta: Long =
  {
    if (isStopped)
    {
      0
    }
    else
    {
      now - m_started
    }
  }
}

//****************************************************************************
