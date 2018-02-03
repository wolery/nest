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

//****************************************************************************

import java.util.LinkedList

import javafx.collections.{ FXCollections, ListChangeListener, ObservableList }
import javafx.stage.{ Stage, Window };

object StageUtils
{
  private var stages:  ObservableList[Stage] = _
  private var windows: ObservableList[Window] = _

  def getStages2(): ObservableList[Stage] =
  {
    Window.getWindows
          .filtered(_.isInstanceOf[Stage])
          .asInstanceOf[ObservableList[Stage]]
  }

  def getStages(): ObservableList[Stage] =
  {
    if (stages == null)
    {
      stages = FXCollections.observableArrayList()
      windows = Window.getWindows()
      windows.addListener(new ListChangeListener[Window]()
      {
        def onChanged(c: javafx.collections.ListChangeListener.Change[_ <: Window]): Unit =
        {
          updateStages();
        }
      })

      updateStages();
    }

    return stages;
  }

  private def updateStages(): Unit =
  {
    val newStages = new LinkedList[Stage]();
    windows.forEach(w=>
    {
      if (w.isInstanceOf[Stage])
      {
        newStages.add(w.asInstanceOf[Stage])
      }
    })
    stages.setAll(newStages)
  }
}
//****************************************************************************
