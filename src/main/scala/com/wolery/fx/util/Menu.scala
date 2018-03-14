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

import javafx.event.ActionEvent
import javafx.scene.control.{MenuBar,MenuItem,SeparatorMenuItem}

/**
 * Miscellaneous utility functions for manipulating menus.
 *
 * @author Jonathon Bell
 */
trait menu
{
  /**
   * For those platforms that support a single 'system' menu bar (e.g. Mac OS)
   * attempt to move the application menus up into the shared system menu bar.
   *
   * Moreover,  if the platform provides a  special 'application' menu that is
   * pre-populated with menu items created by the system, merge the items from
   * the first menu in the given menu bar into the existing application menu.
   *
   * @param  menubar  The list of menus to move up into the system menu bar.
   */
  def setApplicationMenu(menubar: MenuBar): Unit =
  {
    import com.sun.glass.ui.{Menu ⇒ gMenu, MenuItem ⇒ gMenuItem}
    import com.sun.glass.ui.Application.GetApplication
    import com.sun.glass.ui.MenuItem.{Callback, Separator ⇒ gSeperator}
    import com.sun.javafx.PlatformUtil.isMac
    import com.wolery.util.reflect.method
    /**
     * Goodness, this took Ages to get right!  I've seen all sorts of attempts
     * out there online, including Jay Gassen's NSMenuFX, from which I learned
     * the trick of using reflection to gain access to an otherwise  protected
     * class, as well out of date and conflicting documentation describing the
     * use of AWT or Swing desktop classes - all incompatible with JavaFX. Why
     * was this so hard?
     *
     * The problem is to 'add' items to the existing Apple menu  while leaving
     * the existing system generated items for things like 'Show All' intact,
     * so that things continue to work correctly when future versions of the
     * OS add new default application menu items we have not yet thoought of.
     * Jay, by contrast, discards the existing Apple menu entirely.
     *
     * Moreover, Sun's implementation of the Apple menu is expressed in terms
     * of an entirely different, but dual, set of Menu, MenuItem, and MenuBar
     * classes - i've no idea why - making the translation of key combinations
     * to accelerator modifiers tricky.
     */
    if (isMac && !menubar.getMenus.isEmpty)              // Something to do?
    {
      /**
       * Returns the 'Glass' Apple menu.
       *
       * Uses reflection to work around the fact that, at least on JVM9, this
       * public method belongs to a subclass of 'com.sun.glass.ui.Application'
       * that is not itself public.
       */
      def appleMenu(): gMenu =
      {
        method(GetApplication,"getAppleMenu")            // Get method handle
         .invoke(GetApplication)                         // Invoke the method
         .asInstanceOf[gMenu]                            // Return Glass menu
      }

      /**
       * Creates a 'Glass' menu copy of the the given javafx menu item.
       *
       * Preserves any existing accelerator key modifiers and callback.
       */
      def gMenuItem(i: MenuItem): gMenuItem =
      {
        val c = Option(i.getOnAction).map(h ⇒ new Callback
        {
          def action()   = {h.handle(new ActionEvent)}   // Call old handler
          def validate() = {}                            // Nothing to do
        }).getOrElse(null)

        var m = 0                                        // Shortcut key mods
        var k = 0                                        // Shortcut base key

     /* Parse the optional accelerator key combination, if present...*/

        Option(i.getAccelerator).map(_.getDisplayText).getOrElse("").foreach
        {
          case '⇧' ⇒ m |= 1                              // ...shift   key
          case '⌃' ⇒ m |= 4                              // ...control key
          case '⌥' ⇒ m |= 5                              // ...option  key
          case '⌘' ⇒ m |= 16                             // ...command key
          case  c  ⇒ k  = k                              // ...base    key
        }

        GetApplication.createMenuItem(i.getText,c,k,m)   // Glass menu item
      }

      val a = appleMenu()                                // Fetch Apple menu
      val n = a.getItems.size                            // Record its size

   /* Append (a 'Glass' copy of) each item from the first menu of the caller's
      menu bar to the end of the existing Apple menu...*/

      menubar.getMenus.get(0).getItems.forEach           // For each new menu
      {
        case s: SeparatorMenuItem ⇒ a.add(gSeperator)    // ...add seperator
        case i: MenuItem          ⇒ a.add(gMenuItem(i))  // ...add glass copy
      }

   /* ...now move the 'n' original items from the top of the Apple menu to
      the bottom,  effectively rotating the contents of the menu 'n' times.
      Why this odd way of doing things? Why not simply insert the items at
      the top of the menu, where we actually want them?  Answer: the Glass
      method gMenu.insert(item,index) does not appear to handle separators
      correctly...*/

      for (i ← 0 until n)                                // For each original
      {
        val i = a.getItems.get(0)                        // ...get first item
        a.remove(0)                                      // ...yank from menu
        a.add(i.asInstanceOf[gMenuItem])                 // ...append to menu
      }

      menubar.getMenus.remove(0)                         // Discard first menu
    }

    menubar.setUseSystemMenuBar(true)                    // Move to system bar
  }
}

/**
 * Miscellaneous utility functions for manipulating menus.
 *
 * @author Jonathon Bell
 */
object menu extends menu

//****************************************************************************
