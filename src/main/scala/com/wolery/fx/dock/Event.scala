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
//*
//*
//****************************************************************************

package com.wolery
package fx
package dock

import javafx.event.{Event,EventTarget,EventType}

/**
 *
 */
object DockEvent
{
  /**
   * Common supertype for all dock event types.
   *
   * Unlike a `DragEvent` the dock over event is handed to all stages that may
   * be interested in receiving the dock pane.
   */
  val ANY: EventType[DockEvent] = new EventType(Event.ANY,"DOCK")

  /**
   * Occurs when a dock window is being dragged by its title bar and the mouse
   * enters a node's bounds.
   */
  val DOCK_ENTER: EventType[DockEvent] = new EventType(ANY,"DOCK_ENTER")

  /**
   * Occurs when a dock window is being dragged by its title bar and the mouse i
   * is contained in a node's bounds.
   */
  val DOCK_OVER: EventType[DockEvent] = new EventType(ANY,"DOCK_OVER")

  /**
   * Occurs when a dock window is being dragged by its title bar and the mouse
   * exits a node's bounds.
   */
  val DOCK_EXIT: EventType[DockEvent] = new EventType(ANY,"DOCK_EXIT")

  /**
   * Occurs when a dock window is being dragged by its title bar and the mouse
   * is released over a node's bounds.
   */
  val DOCK_RELEASED: EventType[DockEvent] = new EventType(ANY,"DOCK_RELEASED")
}

/**
 *
 */
class DockEvent
(
      source    : Object,
      target    : EventTarget,
      eventType : EventType[DockEvent],
  val screenX   : ℝ,
  val screenY   : ℝ,
  val contents  : Option[DockNode] = None
)
extends Event(source,target,eventType)
{}

//****************************************************************************
