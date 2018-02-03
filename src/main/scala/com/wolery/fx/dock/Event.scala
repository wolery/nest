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

import com.sun.javafx.scene.input.InputEventUtils

import javafx.scene.Node
import javafx.scene.input.PickResult
import javafx.event.{Event,EventTarget,EventType }

//****************************************************************************

object DockEvent
{
  /**
   * Common supertype for all dock event types.
   */
  val ANY: EventType[DockEvent] = new EventType(Event.ANY,"DOCK");

  /**
   * This event occurs when a dock window is being dragged by its title bar and the mouse enters a
   * node's bounds. Unlike a {@code DragEvent} the dock over event is handed to all stages that may
   * be interested in receiving the dock pane.
   */
  val DOCK_ENTER: EventType[DockEvent] = new EventType(DockEvent.ANY,"DOCK_ENTER");

  /**
   * This event occurs when a dock window is being dragged by its title bar and the mouse is
   * contained in a node's bounds. Unlike a {@code DragEvent} the dock over event is handed to all
   * stages that may be interested in receiving the dock pane.
   */
  val DOCK_OVER: EventType[DockEvent] = new EventType(DockEvent.ANY,"DOCK_OVER");

  /**
   * This event occurs when a dock window is being dragged by its title bar and the mouse exits a
   * node's bounds. Unlike a {@code DragEvent} the dock over event is handed to all stages that may
   * be interested in receiving the dock pane.
   */
  val DOCK_EXIT: EventType[DockEvent] = new EventType(DockEvent.ANY,"DOCK_EXIT");

  /**
   * This event occurs when a dock window is being dragged by its title bar and the mouse is
   * released over a node's bounds. Unlike a {@code DragEvent} the dock over event is handed to all
   * stages that may be interested in receiving the dock pane.
   */
  val DOCK_RELEASED: EventType[DockEvent] = new EventType(DockEvent.ANY,"DOCK_RELEASED");
}

final class DockEvent
(
  source      : Object,
  target      : EventTarget,
  eventType   : EventType[ _ <: DockEvent],
  x           : ℝ,
  y           : ℝ,
  screenX     : ℝ,
  screenY     : ℝ,
  pickresult  : PickResult,
  contents    : Node
)
extends Event(source,target,eventType)
{
  val pickResult = if (pickresult != null) pickresult
                   else new PickResult(target,x,y)
  val p          = InputEventUtils.recomputeCoordinates(pickResult,null)

  def getX          = p.getX
  def getY          = p.getY
  def getZ          = p.getZ
  def getScreenX    = screenX
  def getScreenY    = screenY
  def getSceneX     = x
  def getSceneY     = y
  def getPickResult = pickResult
  def getContents   = contents

  def this
  (
    eventType   : EventType[ _ <: DockEvent],
    x           : ℝ,
    y           : ℝ,
    screenX     : ℝ,
    screenY     : ℝ,
    pickresult  : PickResult,
    contents    : Node = null) =
  {
    this(null,null,eventType,x,y,screenX,screenY,pickresult,contents)
  }
}

//****************************************************************************
