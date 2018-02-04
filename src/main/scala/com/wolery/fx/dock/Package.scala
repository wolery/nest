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

package com.wolery.fx

//****************************************************************************
import scala.language.implicitConversions
import javafx.event.{Event,EventTarget,EventType,EventHandler}

package object dock
{
  val DockPos = Position
  type DockPos = Position

  implicit
  def asEventHandler[E <: Event](lambda: E ⇒ Unit): EventHandler[E] = new EventHandler[E]
  {
    def handle(e: E) = lambda(e)
  }

//  implicit
//  def asRunnable(lambda: ⇒ Unit): Runnable =  new Runnable
//  {
//    def run(): Unit = lambda
//  }

//  def point(x: ℝ,y: ℝ): Point2D = new Point2D(x,y)
}

//****************************************************************************
