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

//****************************************************************************

import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.input.KeyCombination

//****************************************************************************

object menu
{
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

  implicit final
  class CheckMenuItemBuilder(val x: CheckMenuItem) extends AnyVal
  {
    def select          (v: Bool)           = {x.setSelected(v);    x}
  }

  implicit final
  class CustomMenuItemBuilder(val x: CustomMenuItem) extends AnyVal
  {
    def content         (v: Node)           = {x.setContent(v);     x}
    def hideOnClick     (v: Bool)           = {x.setHideOnClick(v); x}
  }

  implicit final
  class RadioMenuItemSyntax(val x: RadioMenuItem) extends AnyVal
  {
    def select          (v: Bool)           = {x.setSelected(v);    x}
    def toggleGroup     (v: ToggleGroup)    = {x.setToggleGroup(v); x}
  }

  implicit final
  class MenuBuilder(val x: Menu) extends AnyVal
  {
    def onHidden        (v: ⇒ Unit)         = {x.setOnHidden (_⇒v); x}
    def onHiding        (v: ⇒ Unit)         = {x.setOnHiding (_⇒v); x}
    def onShowing       (v: ⇒ Unit)         = {x.setOnShowing(_⇒v); x}
    def onShown         (v: ⇒ Unit)         = {x.setOnShown  (_⇒v); x}
  }

  def keyCombination(name: String): KeyCombination =
  {
    var s = ""

    name.foreach
    {
      case '⌥' ⇒ s += "alt+"
      case '^' ⇒ s += "ctrl+"
      case '◆' ⇒ s += "meta+"
      case '⇧' ⇒ s += "shift+"
      case key ⇒ s += key
    }

    KeyCombination.keyCombination(s)
  }
}

//****************************************************************************
