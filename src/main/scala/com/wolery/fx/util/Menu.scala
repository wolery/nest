//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : Header:
//*
//*
//*  Purpose : Miscellaneous utility functions for manipulating menus.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*
//*
//****************************************************************************

package com.wolery.fx.util

import javafx.event.{ActionEvent,EventHandler}
import javafx.scene.control.{MenuBar,MenuItem,SeparatorMenuItem}

import com.sun.glass.ui.{Menu ⇒ gMenu,MenuItem ⇒ gMenuItem}
import com.sun.glass.ui.Application.GetApplication
import com.sun.glass.ui.MenuItem.{Callback,Separator ⇒ gSeperator}
import com.sun.javafx.PlatformUtil.isMac

import com.wolery.util.reflect.method

/**
 * Miscellaneous utility functions for manipulating menus.
 *
 * @author JOnathon Bell
 */
trait menu
{
  /**
   * @param  menubar
   */
  def setApplicationMenu(menubar: MenuBar): Unit =
  {
    if (isMac && !menubar.getMenus.isEmpty)
    {
      def appleMenu(): gMenu =
      {
        method(GetApplication,"getAppleMenu").invoke(GetApplication).asInstanceOf[gMenu]
      }

      def gMenuItem(i: MenuItem): gMenuItem =
      {
        val h = Option(i.getOnAction).map(h ⇒ new Callback
        {
          def action():   Unit = {h.handle(new ActionEvent)}
          def validate(): Unit = {}
        }).getOrElse(null)

        val s = Option(i.getAccelerator).map(_.getDisplayText).getOrElse("")
        var m = 0
        var c = 0

        s.foreach
        {
          case '⇧' => m |= 1
          case '⌃' => m |= 4
          case '⌥' => m |= 5
          case '⌘' => m |= 16
          case  k  => c  = k
        }

        GetApplication.createMenuItem(i.getText,h,c,m)
      }

      val a = appleMenu()                                //
      val n = a.getItems.size                            //

      menubar.getMenus.get(0).getItems.forEach           //
      {
        case s: SeparatorMenuItem ⇒ a.add(gSeperator)    //
        case i: MenuItem          ⇒ a.add(gMenuItem(i))  //
      }

      for (i ← 0 until n)                                //
      {
        val i = a.getItems.get(0)                        //
        a.remove(0)                                      //
        a.add(i.asInstanceOf[gMenuItem])                 //
      }

      menubar.getMenus.remove(0)                         //
      menubar.setUseSystemMenuBar(true)                  //
    }
  }
}

/**
 * Miscellaneous utility functions for manipulating menus.
 *
 * @author Jonathon Bell
 */
object menu extends menu

//****************************************************************************
