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

import scala.collection.mutable.Stack

import _root_.com.sun.javafx.css.StyleManager;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Animation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.util.Duration;
import collection.JavaConverters._

class DockPosButton
(
  val isDockRoot: Bool,
  val dockPos : DockPos
)
extends Button

object DockPane
{
  def initializeDefaultUserAgentStylesheet(): Unit =
  {
    val r = getClass.getResource("default.css")

    StyleManager.getInstance.addUserAgentStylesheet(r.toExternalForm)
  }
}

class DockPane extends StackPane with EventHandler[DockEvent]
{
  val dockPosIndicator        = new GridPane()
  val dockIndicatorPopup      = new Popup()
  val dockIndicatorOverlay    = new Popup()
  val dockRootPane            = new StackPane()
  val dockAreaIndicator       = new Rectangle()
  val dockAreaStrokeTimeline  = new Timeline()
  val dockCenter              = new DockPosButton(false,DockPos.CENTER)
  val dockTop                 = new DockPosButton(false,DockPos.TOP)
  val dockRight               = new DockPosButton(false,DockPos.RIGHT)
  val dockBottom              = new DockPosButton(false,DockPos.BOTTOM)
  val dockLeft                = new DockPosButton(false,DockPos.LEFT)
  val dockTopRoot             = new DockPosButton(true, DockPos.TOP)
  val dockRightRoot           = new DockPosButton(true, DockPos.RIGHT)
  val dockLeftRoot            = new DockPosButton(true, DockPos.LEFT)
  val dockBottomRoot          = new DockPosButton(true, DockPos.BOTTOM)

  this.addEventHandler(DockEvent.ANY,this)
  this.addEventFilter (DockEvent.DOCK_ENTER,(_:DockEvent) => receivedEnter = true)
  this.addEventFilter (DockEvent.DOCK_OVER ,(_:DockEvent) => dockNodeDrag = null)

  dockIndicatorPopup.setAutoFix(false)
  dockIndicatorOverlay.setAutoFix(false)
  dockRootPane.prefWidthProperty.bind(this.widthProperty())
  dockRootPane.prefHeightProperty.bind(this.heightProperty())
  dockAreaIndicator.setManaged(false)
  dockAreaIndicator.setMouseTransparent(true)
  dockAreaStrokeTimeline.setCycleCount(Animation.INDEFINITE)
  // 12 is the cumulative offset of the stroke dash array in the default.css style sheet
  // RFE filed for CSS styled timelines/animations:
  // https://bugs.openjdk.java.net/browse/JDK-8133837

  dockAreaStrokeTimeline.getKeyFrames.add(new KeyFrame(Duration.millis(500), new KeyValue(dockAreaIndicator.strokeDashOffsetProperty().asInstanceOf[javafx.beans.value.WritableValue[Any]], 12)))
  dockAreaStrokeTimeline.play()

  dockCenter    .getStyleClass.add("dock-center")
  dockTop       .getStyleClass.add("dock-top")
  dockRight     .getStyleClass.add("dock-right")
  dockBottom    .getStyleClass.add("dock-bottom")
  dockLeft      .getStyleClass.add("dock-left")
  dockTopRoot   .getStyleClass.add("dock-top-root")
  dockRightRoot .getStyleClass.add("dock-right-root")
  dockBottomRoot.getStyleClass.add("dock-bottom-root")
  dockLeftRoot  .getStyleClass.add("dock-left-root")

  StackPane.setAlignment(dockTopRoot,   Pos.TOP_CENTER)
  StackPane.setAlignment(dockRightRoot, Pos.CENTER_RIGHT)
  StackPane.setAlignment(dockBottomRoot,Pos.BOTTOM_CENTER)
  StackPane.setAlignment(dockLeftRoot, Pos.CENTER_LEFT)

  dockPosIndicator.add(dockTop,   1,0)
  dockPosIndicator.add(dockRight, 2,1)
  dockPosIndicator.add(dockBottom,1,2)
  dockPosIndicator.add(dockLeft,  0,1)
//dockPosIndicator.add(dockCenter,1,1)

  dockRootPane.getChildren.addAll(dockAreaIndicator,dockTopRoot,dockRightRoot,dockBottomRoot,dockLeftRoot)

  dockIndicatorOverlay.getContent.add(dockRootPane)
  dockIndicatorPopup  .getContent.addAll(dockPosIndicator)

  this.             getStyleClass.add("dock-pane")
  dockRootPane.     getStyleClass.add("dock-root-pane")
  dockPosIndicator. getStyleClass.add("dock-pos-indicator")
  dockAreaIndicator.getStyleClass.add("dock-area-indicator")

  /**
   * The current root node of this dock pane's layout.
   */
  private var root: Node = _

  /**
   * Whether a DOCK_ENTER event has been received by this dock pane since the last DOCK_EXIT event
   * was received.
   */
  private var receivedEnter = false

  /**
   * The current node in this dock pane that we may be dragging over.
   */
  private var dockNodeDrag: Node = _

  /**
   * The docking area of the current dock indicator button if any is selected. This is either the
   * root or equal to dock node drag.
   */
  private var dockAreaDrag: Node  = _

  /**
   * The docking position of the current dock indicator button if any is selected.
   */
  private var dockPosDrag : DockPos = _


  def getDockAreaStrokeTimeline(): Timeline = dockAreaStrokeTimeline;

  def getDefaultUserAgentStyleheet(): String =
  {
    getClass.getResource("default.css").toExternalForm;
  }

  private
  val dockNodeEventFilters: ObservableMap[Node, DockNodeEventHandler] =
  {
    FXCollections.observableHashMap()
  }

  private
  class DockNodeEventHandler(node: Node) extends EventHandler[DockEvent]
  {
    def handle(e: DockEvent): Unit=
    {
      DockPane.this.dockNodeDrag = node;
    }
  }

  def dock(node: Node,dockPos: DockPos,sibling: Node = root): Unit =
  {
    val dockNodeEventHandler = new DockNodeEventHandler(node)

    dockNodeEventFilters.put(node, dockNodeEventHandler)

    node.addEventFilter(DockEvent.DOCK_OVER, dockNodeEventHandler)

    var split = root.asInstanceOf[SplitPane]

    if (split == null)
    {
      split = new SplitPane()
      split.getItems.add(node)
      root = split
      this.getChildren.add(root)
      return
    }

    // find the parent of the sibling

    if (sibling!=null && sibling!=root)
    {
      val stack: Stack[Parent] = Stack()

      stack.push(root.asInstanceOf[Parent])

      while (!stack.isEmpty)
      {
        val parent = stack.pop()

        var children = parent.getChildrenUnmodifiable()

        if (parent.isInstanceOf[SplitPane])
        {
          val splitPane = parent.asInstanceOf[SplitPane];
          children = splitPane.getItems()
        }

        for (i ← 0 until children.size)
        {
          if (children.get(i) == sibling)
          {
            split = parent.asInstanceOf[SplitPane]
          }
          else
          if (children.get(i).isInstanceOf[Parent])
          {
            stack.push(children.get(i).asInstanceOf[Parent])
          }
        }
      }
    }

    val requestedOrientation = if (dockPos == DockPos.LEFT || dockPos == DockPos.RIGHT)
        Orientation.HORIZONTAL else Orientation.VERTICAL

    // if the orientation is different then reparent the split pane

    if (split.getOrientation != requestedOrientation)
    {
      if (split.getItems.size > 1)
      {
        val splitPane = new SplitPane()

        if (split==root && sibling==root)
        {
          this.getChildren.set(this.getChildren.indexOf(root),splitPane)
          splitPane.getItems.add(split)
          root = splitPane;
        }
        else
        {
          split.getItems.set(split.getItems.indexOf(sibling),splitPane)
          splitPane.getItems.add(sibling)
        }

        split = splitPane;
      }

      split.setOrientation(requestedOrientation)
    }

    // finally dock the node to the correct split pane
    val splitItems = split.getItems

    var magnitude = 0.0

    if (splitItems.size > 0)
    {
      if (split.getOrientation == Orientation.HORIZONTAL)
      {
        splitItems.forEach(splitItem ⇒ magnitude += splitItem.prefWidth(0))
      }
      else
      {
        splitItems.forEach(splitItem ⇒ magnitude += splitItem.prefHeight(0))
      }
    }

    if (dockPos==DockPos.LEFT || dockPos == DockPos.TOP)
    {
      var relativeIndex = 0

      if (sibling!=null && sibling!=root)
      {
        relativeIndex = splitItems.indexOf(sibling)
      }

      splitItems.add(relativeIndex,node)

      if (splitItems.size > 1)
      {
        if (split.getOrientation == Orientation.HORIZONTAL)
        {
          split.setDividerPosition(relativeIndex,node.prefWidth(0) / (magnitude + node.prefWidth(0)))
        }
        else
        {
          split.setDividerPosition(relativeIndex,node.prefHeight(0) / (magnitude + node.prefHeight(0)))
        }
      }
    }
    else
    if (dockPos==DockPos.RIGHT || dockPos==DockPos.BOTTOM)
    {
      var relativeIndex = splitItems.size

      if (sibling!=null && sibling!=root)
      {
        relativeIndex = splitItems.indexOf(sibling) + 1;
      }

      splitItems.add(relativeIndex, node)

      if (splitItems.size > 1)
      {
        if (split.getOrientation == Orientation.HORIZONTAL)
        {
          split.setDividerPosition(relativeIndex - 1,1 - node.prefWidth(0) / (magnitude + node.prefWidth(0)))
        }
        else
        {
          split.setDividerPosition(relativeIndex - 1,1 - node.prefHeight(0) / (magnitude + node.prefHeight(0)))
        }
      }
    }
  }

  def undock(node: DockNode ): Unit =
  {
    val dockNodeEventHandler = dockNodeEventFilters.get(node)
    node.removeEventFilter(DockEvent.DOCK_OVER, dockNodeEventHandler)
    dockNodeEventFilters.remove(node)

    // depth first search to find the parent of the node
    val findStack = Stack[Parent]()
    findStack.push(root.asInstanceOf[Parent])

    while (!findStack.isEmpty)
    {
      var parent = findStack.pop()

      var children = parent.getChildrenUnmodifiable()

      if (parent.isInstanceOf[SplitPane])
      {
        val split = parent.asInstanceOf[SplitPane]
        children = split.getItems()
      }

      for (i ← 0 until children.size)
      {
        if (children.get(i) == node)
        {
          children.remove(i)

          // start from the root again and remove any SplitPane's with no children in them
          val clearStack = Stack[Parent]()
          clearStack.push(root.asInstanceOf[Parent])
          while (!clearStack.isEmpty)
          {
            parent = clearStack.pop()

            children = parent.getChildrenUnmodifiable()

            if (parent.isInstanceOf[SplitPane])
            {
              val split = parent.asInstanceOf[SplitPane]

              children = split.getItems()
            }

            for (i ← 0 until children.size)
            {
              if (children.get(i).isInstanceOf[SplitPane])
              {
                val split = children.get(i).asInstanceOf[SplitPane]

                if (split.getItems.size() < 1)
                {
                  children.remove(i)
                }
                else
                {
                  clearStack.push(split)
                }
              }
            }
          }

          return
        }
        else
        if (children.get(i).isInstanceOf[Parent])
        {
          findStack.push(children.get(i).asInstanceOf[Parent])
        }
      }
    }
  }

  private
  def onDockEnter(event: DockEvent): Unit =
  {
    if (!dockIndicatorOverlay.isShowing())
    {
      val topLeft = DockPane.this.localToScreen(0,0)

      dockIndicatorOverlay.show(DockPane.this,topLeft.getX,topLeft.getY)
    }
  }

  private
  def onDockOver(event: DockEvent): Unit =
  {
    this.receivedEnter = false;

    dockPosDrag = null;
    dockAreaDrag = dockNodeDrag;
    var FLAG = true

    val dockPosButtons = Seq(dockTop,dockRight,dockBottom,dockLeft,dockTopRoot,dockRightRoot,dockBottomRoot,dockLeftRoot)

    for (dockIndicatorButton ← dockPosButtons if FLAG)
    {
      if (dockIndicatorButton.contains(dockIndicatorButton.screenToLocal(event.screenX,event.screenY)))
      {
        dockPosDrag = dockIndicatorButton.dockPos

        if (dockIndicatorButton.isDockRoot)
        {
          dockAreaDrag = root;
        }

        dockIndicatorButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true)
        FLAG = false
        //break;
      }
      else
      {
        dockIndicatorButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), false)
      }
    }

    if (dockPosDrag != null)
    {
      val originToScene = dockAreaDrag.localToScene(0,0)

      dockAreaIndicator.setVisible(true)
      dockAreaIndicator.relocate(originToScene.getX, originToScene.getY)

      if (dockPosDrag == DockPos.RIGHT)
      {
        dockAreaIndicator.setTranslateX(dockAreaDrag.getLayoutBounds.getWidth() / 2)
      }
      else
      {
        dockAreaIndicator.setTranslateX(0)
      }

      if (dockPosDrag == DockPos.BOTTOM)
      {
        dockAreaIndicator.setTranslateY(dockAreaDrag.getLayoutBounds.getHeight() / 2)
      }
      else
      {
        dockAreaIndicator.setTranslateY(0)
      }

      if (dockPosDrag == DockPos.LEFT || dockPosDrag == DockPos.RIGHT)
      {
        dockAreaIndicator.setWidth(dockAreaDrag.getLayoutBounds.getWidth() / 2)
      }
      else
      {
        dockAreaIndicator.setWidth(dockAreaDrag.getLayoutBounds.getWidth())
      }

      if (dockPosDrag == DockPos.TOP || dockPosDrag == DockPos.BOTTOM)
      {
        dockAreaIndicator.setHeight(dockAreaDrag.getLayoutBounds.getHeight() / 2)
      }
      else
      {
        dockAreaIndicator.setHeight(dockAreaDrag.getLayoutBounds.getHeight())
      }
    }
    else
    {
      dockAreaIndicator.setVisible(false)
    }

    if (dockNodeDrag != null)
    {
      val originToScreen = dockNodeDrag.localToScreen(0,0)

      val posX = originToScreen.getX + dockNodeDrag.getLayoutBounds.getWidth()  / 2 - dockPosIndicator.getWidth / 2
      val posY = originToScreen.getY + dockNodeDrag.getLayoutBounds.getHeight() / 2 - dockPosIndicator.getHeight/ 2

      if (!dockIndicatorPopup.isShowing)
      {
        dockIndicatorPopup.show(DockPane.this,posX,posY)
      }
      else
      {
        dockIndicatorPopup.setX(posX)
        dockIndicatorPopup.setY(posY)
      }

      // set visible after moving the popup
      dockPosIndicator.setVisible(true)
    }
    else
    {
      dockPosIndicator.setVisible(false)
    }
  }

  private
  def onDockReleased(event: DockEvent): Unit =
  {
    if (event.contents.isDefined && dockPosDrag!=null && dockIndicatorOverlay.isShowing)
    {
      event.contents.get.dock(this,dockPosDrag,dockAreaDrag)
    }

    if (dockIndicatorPopup.isShowing)
    {
      dockIndicatorOverlay.hide()
      dockIndicatorPopup.hide()
    }
  }

  private
  def onDockExit(event: DockEvent): Unit =
  {
    if (!receivedEnter && dockIndicatorPopup.isShowing)
    {
      dockIndicatorOverlay.hide()
      dockIndicatorPopup.hide()
    }
  }

  def handle(e: DockEvent): Unit = e.getEventType match
  {
    case DockEvent.DOCK_ENTER    ⇒ onDockEnter(e)
    case DockEvent.DOCK_OVER     ⇒ onDockOver(e)
    case DockEvent.DOCK_EXIT     ⇒ onDockExit(e)
    case DockEvent.DOCK_RELEASED ⇒ onDockReleased(e)
  }
}

//****************************************************************************
