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

package com.wolery
package fx
package control

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.input.KeyCombination

/**
 * Miscellaneous utility functions for manipulating menus.
 *
 * @author Jonathon Bell
 */
object menu
{
  /**
   *
   */
  implicit final
  class MenuItemBuilder(val x: MenuItem) extends AnyVal
  {
    def accelerator     (v: String)         = {x.setAccelerator(keyCombination(v));x}
    def accelerator     (v: KeyCombination) = {x.setAccelerator(v)       ;x}
    def disable         (v: Bool)           = {x.setDisable(v)           ;x}
    def enable          (v: Bool)           = {x.setDisable(!v)          ;x}
    def graphic         (v: Node)           = {x.setGraphic(v)           ;x}
    def id              (v: String)         = {x.setId(v)                ;x}
    def mnemonicParsing (v: Bool)           = {x.setMnemonicParsing(v)   ;x}
    def onAction        (v: ⇒ Unit)         = {x.setOnAction(_⇒v)        ;x}
    def onMenuValidation(v: ⇒ Unit)         = {x.setOnMenuValidation(_⇒v);x}
    def style           (v: String)         = {x.setStyle(v)             ;x}
    def text            (v: String)         = {x.setText(v)              ;x}
    def datum           (v: Any)            = {x.setUserData(v)          ;x}
    def visible         (v: Bool)           = {x.setVisible(v)           ;x}
  }

  /**
   *
   */
  implicit final
  class CheckMenuItemBuilder(val x: CheckMenuItem) extends AnyVal
  {
    def select          (v: Bool)           = {x.setSelected(v);    x}
  }

  /**
   *
   */
  implicit final
  class CustomMenuItemBuilder(val x: CustomMenuItem) extends AnyVal
  {
    def content         (v: Node)           = {x.setContent(v);     x}
    def hideOnClick     (v: Bool)           = {x.setHideOnClick(v); x}
  }

  /**
   *
   */
  implicit final
  class RadioMenuItemSyntax(val x: RadioMenuItem) extends AnyVal
  {
    def select          (v: Bool)           = {x.setSelected(v);    x}
    def toggleGroup     (v: ToggleGroup)    = {x.setToggleGroup(v); x}
  }

  /**
   *
   */
  implicit final
  class MenuBuilder(val x: Menu) extends AnyVal
  {
    def onHidden        (v: ⇒ Unit)         = {x.setOnHidden (_⇒v); x}
    def onHiding        (v: ⇒ Unit)         = {x.setOnHiding (_⇒v); x}
    def onShowing       (v: ⇒ Unit)         = {x.setOnShowing(_⇒v); x}
    def onShown         (v: ⇒ Unit)         = {x.setOnShown  (_⇒v); x}
  }

  /**
   * Parse the given string for an accelerator key combination.
   *
   * Extends the mini language recognized by the parser for the system defined
   * `KeyCombination` class to include the special characters '⌥' (alt/option),
   * '^' (control), '⇧' (shift), and '◆' (meta/command).
   *
   * @return The accelerator key combination described by the given string.
   */
  def keyCombination(name: String): KeyCombination =
  {
    var s = ""                                           // Translation string

    name.foreach                                         // For each character
    {
      case '⇧'     ⇒ s += "shift+"                       // ...shift   key
      case '^'     ⇒ s += "ctrl+"                        // ...control key
      case '⌥'     ⇒ s += "alt+"                         // ...option  key
      case '⌘'|'◆' ⇒ s += "meta+"                        // ...command key
      case  c      ⇒ s += c                              // ...anything else
    }

    KeyCombination.keyCombination(s)                     // Parse translation
  }

  /**
   * For those platforms that support a single 'system' menu bar (e.g. Mac OS)
   * attempt to move the given application menu up into the shared system menu
   * bar.
   *
   * Moreover,  if the platform provides a  special 'application' menu that is
   * pre-populated with default items,  merge the items from the first menu in
   * the given menu bar into this existing application menu.
   *
   * @param  menubar  The list of menus to move up into the system menu bar.
   */
  def setApplicationMenu(menubar: MenuBar): Unit =
  {
    import com.sun.glass.ui.{ Menu ⇒ gMenu, MenuItem ⇒ gMenuItem }
    import com.sun.glass.ui.Application.GetApplication
    import com.sun.glass.ui.MenuItem.{ Callback, Separator ⇒ gSeperator }
    import com.sun.javafx.PlatformUtil.isMac
    import com.wolery.util.reflect.method

    /**
     * Goodness, this took Ages to get right!  I've seen all sorts of attempts
     * out there online, including Jay Gassen's NSMenuFX, from which I learned
     * the trick of using reflection to  gain access to an otherwise protected
     * class, as well as out of date and conflicting documentation describing
     * the use of AWT or Swing desktop classes - all incompatible with JavaFX.
     * Why was this so difficult?
     *
     * The problem is to prepend items to an existing Apple menu while leaving
     * existing system generated items like 'Show All'  intact so that  things
     * continue to work correctly when a future version of the system adds new
     * default application menu items we have not yet thought of. By contrast,
     * the NSMenuFX approach discards the existing Apple menu entirely.
     *
     * Moreover,  Sun's implementation of the Apple menu is expressed in terms
     * of an entirely separate - but dual - set of Menu, MenuItem, and MenuBar
     * classes - I've no idea why - making the translation of key combinations
     * to accelerator modifiers difficult.
     */
    if (isMac && !menubar.getMenus.isEmpty)              // Something to do?
    {
      /**
       * Returns the Apple menu as a 'Glass' menu.
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
       * Creates a copy of the the given JavaFX menu item as a 'Glass' menu.
       *
       * Preserves any existing accelerator key modifiers and callback.
       *
       * Called once for every item in the first menu of the caller's menu bar
       * to create an object suitable for insertion into the Apple menu.
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
          case  c  ⇒ k  = k                              // ...anything else
        }

        GetApplication.createMenuItem(i.getText,c,k,m)   // Glass menu item
      }

      val a = appleMenu()                                // Get the Apple menu
      val n = a.getItems.size                            // Remember its size

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

//****************************************************************************
