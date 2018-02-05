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

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable.{ Map, Stack }

import javafx.collections.ObservableList
import javafx.event.{ Event, EventHandler, EventType }
import javafx.geometry.Point2D
import javafx.scene.{ Node, Parent }
import javafx.scene.control.{ Button, Label }
import javafx.scene.input.{ MouseButton, MouseEvent }
import javafx.scene.layout.{ HBox, Pane, Priority }
import javafx.stage.{ Stage, Window }

class DockTitleBar
(
  dockNode: DockNode
)
extends HBox with EventHandler[MouseEvent]
{
  private val label      : Label            = new Label("Dock Title FRED")
  private val closeButton: Button           = new Button()
  private val stateButton: Button           = new Button()
  private var isDragging : Bool             = false
  private val dragNodes  : Map[Window,Node] = Map()
  private var dragStart  : Point2D          = null

  label.textProperty.bind(dockNode.titleProperty);
  label.graphicProperty.bind(dockNode.graphicProperty);

  stateButton.setOnAction(onStateButton(_))
  closeButton.setOnAction(e ⇒ dockNode.close())
  closeButton.visibleProperty.bind(dockNode.closableProperty)

  this.       getStyleClass.add("dock-title-bar")
  label.      getStyleClass.add("dock-title-label")
  closeButton.getStyleClass.add("dock-close-button")
  stateButton.getStyleClass.add("dock-state-button")

  {
  // create a pane that will stretch to make the buttons right aligned
    val p = new Pane();
    HBox.setHgrow(p,Priority.ALWAYS);
    getChildren.addAll(label,p,stateButton,closeButton);
  }

  addEventHandler(MouseEvent.MOUSE_PRESSED, this)
  addEventHandler(MouseEvent.DRAG_DETECTED, this)
  addEventHandler(MouseEvent.MOUSE_DRAGGED, this)
  addEventHandler(MouseEvent.MOUSE_RELEASED,this)

  def onStateButton(e: Event): Unit =
  {
    if (dockNode.isFloating)
      dockNode.setMaximized(!dockNode.isMaximized)
    else
      dockNode.setFloating(true)
  }

  def handle(e: MouseEvent): Unit = e.getEventType match
  {
    case MouseEvent.MOUSE_PRESSED  ⇒ onMousePressed(e)
    case MouseEvent.DRAG_DETECTED  ⇒ onDragDetected(e)
    case MouseEvent.MOUSE_DRAGGED  ⇒ onMouseDragged(e)
    case MouseEvent.MOUSE_RELEASED ⇒ onMouseReleased(e)
  }

  private
  def onMousePressed(e: MouseEvent): Unit =
  {
    if (dockNode.isFloating && e.getClickCount==2 && e.getButton==MouseButton.PRIMARY)
    {
      dockNode.setMaximized(!dockNode.isMaximized)
    }
    else
    {
    // drag detected is used in place of mouse pressed so there is some
    // threshold for the dragging which is determined by the default drag
    // detection threshold
      dragStart = new Point2D(e.getX,e.getY)
    }
  }

  private
  def onDragDetected(e: MouseEvent): Unit =
  {
    if (!dockNode.isFloating)
    {
      // if we are not using a custom title bar and the user
      // is not forcing the default one for floating and
      // the dock node does have native window decorations
      // then we need to offset the stage position by
      // the height of this title bar

      if (!dockNode.isCustomTitleBar && dockNode.isDecorated)
      {
        dockNode.setFloating(true,new Point2D(0,getHeight))
      }
      else
      {
        dockNode.setFloating(true)
      }

      enableFilters(true)
    }
    else
    if (dockNode.isMaximized)
    {
      val ratioX = e.getX / dockNode.getWidth
      val ratioY = e.getY / dockNode.getHeight

      // Please note that setMaximized is ruined by width and height changes occurring on the
      // stage and there is currently a bug report filed for this though I did not give them an
      // accurate test case which I should and wish I would have. This was causing issues in the
      // original release requiring maximized behavior to be implemented manually by saving the
      // restored bounds. The problem was that the resize functionality in DockNode.java was
      // executing at the same time canceling the maximized change.
      // https://bugs.openjdk.java.net/browse/JDK-8133334

      // restore/minimize the window after we have obtained its dimensions
      dockNode.setMaximized(false)

      // scale the drag start location by our restored dimensions
      dragStart = new Point2D(ratioX * dockNode.getWidth,
                              ratioY * dockNode.getHeight)
    }

    isDragging = true
    e.consume();
  }

  private
  def onMouseDragged(e: MouseEvent): Unit =
  {
    if (dockNode.isFloating
     && e.getClickCount == 2
     && e.getButton == MouseButton.PRIMARY)
    {
      e.setDragDetect(false);
      e.consume();
    }
    else
    if (isDragging)
    {
      val stage       = dockNode.getStage;
      val insetsDelta = dockNode.getBorderPane.getInsets;

      // dragging this way makes the interface more responsive in the event
      // the system is lagging as is the case with most current JavaFX
      // implementations on Linux
      stage.setX(e.getScreenX - dragStart.getX - insetsDelta.getLeft);
      stage.setY(e.getScreenY - dragStart.getY - insetsDelta.getTop);

      val dockEnterEvent = dockEvent(DockEvent.DOCK_ENTER,e)
      val dockOverEvent  = dockEvent(DockEvent.DOCK_OVER, e)
      val dockExitEvent =  dockEvent(DockEvent.DOCK_EXIT, e)

      def fire(node: Node,dragNode: Node): Unit =
      {
        if (dragNode != node)
        {
          Event.fireEvent(node,dockEnterEvent.copyFor(DockTitleBar.this, node));

          if (dragNode != null)
          {
            // fire the dock exit first so listeners
            // can actually keep track of the node we
            // are currently over and know when we
            // aren't over any which DOCK_OVER
            // does not provide
            Event.fireEvent(dragNode,dockExitEvent.copyFor(DockTitleBar.this,dragNode))
          }

          dragNodes(node.getScene.getWindow) = node
        }

        Event.fireEvent(node,dockOverEvent.copyFor(DockTitleBar.this,node));
      }

      pickTarget(e,fire _,Some(dockExitEvent))
    }
  }

  private
  def onMouseReleased(e: MouseEvent): Unit =
  {
    isDragging = false;

    val dockReleasedEvent = dockEvent(DockEvent.DOCK_RELEASED,e,Option(dockNode))

    def fire(node: Node,dragNode: Node): Unit =
    {
      if (dragNode != node)
      {
        Event.fireEvent(node,dockReleasedEvent.copyFor(DockTitleBar.this,node))
      }

      Event.fireEvent(node,dockReleasedEvent.copyFor(DockTitleBar.this,node))
    }

    pickTarget(e,fire _)

    dragNodes.clear();

    enableFilters(false)
  }

  private
  def dockEvent(etype: EventType[DockEvent],e:MouseEvent,contents: Option[DockNode] = None): DockEvent =
  {
    new DockEvent(this,Event.NULL_SOURCE_TARGET,etype,e.getScreenX,e.getScreenY,contents)
  }

  private
  def pickTarget(event: MouseEvent,fire: (Node,Node)=>Unit,explicit: Option[Event] = None): Unit =
  {
    def isCandidate(node: Node): Bool =
    {
      !node.isMouseTransparent &&
       node.contains(node.screenToLocal(event.getScreenX,event.getScreenY))
    }

    for (targetStage ← stages)
    {
      var ready = true
      var notFired = true

      val stack    = Stack[Parent]();
      val dragNode = dragNodes.getOrElse(targetStage,null)
      val root     = targetStage.getScene.getRoot;

      if (isCandidate(root))
      {
        stack.push(root)
      }

      // depth first traversal to find the deepest node or parent with no
      // children that intersects the point of interest

      while (!stack.isEmpty)
      {
        var notFired = true
        val parent   = stack.pop()

        for (node ← parent.getChildrenUnmodifiable.asScala if notFired)
        {
          if (isCandidate(node))
          {
            node match
            {
              case n: Parent ⇒ stack.push(n)
              case n         ⇒ ready = false;fire(n,dragNode)
            }

            notFired = false;
          }
        }

        // if none of the children fired the event or there were no children
        // fire it with the parent as the target to receive the event
        if (notFired)
        {
          ready = false
          fire(parent,dragNode);
        }
      }

      if (ready && explicit.isDefined && dragNode!=null)
      {
        Event.fireEvent(dragNode,explicit.get.copyFor(this,dragNode))
        dragNodes(targetStage) = null
      }
    }
  }

  // TODO: Find a better solution.
  // Temporary work around for nodes losing the drag event when removed from
  // the scene graph.
  // A possible alternative is to use "ghost" panes in the DockPane layout
  // while making DockNode simply an overlay stage that is always shown.
  // However since flickering when popping out was already eliminated that would
  // be overkill and is not a suitable solution for native decorations.
  // Bug report open: https://bugs.openjdk.java.net/browse/JDK-8133335
  private
  def enableFilters(enable: Bool):Unit =
  {
    val dockPane = dockNode.getDockPane

    if (dockPane != null)
    {
      if (enable)
      {
        dockPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this)
        dockPane.addEventFilter(MouseEvent.MOUSE_RELEASED,this)
      }
    // Remove temporary event handler for bug mentioned above.
      else
      {
        dockPane.removeEventFilter(MouseEvent.MOUSE_DRAGGED, this)
        dockPane.removeEventFilter(MouseEvent.MOUSE_RELEASED,this)
      }
    }
  }

  private
  def stages: Seq[Stage] =
  {
    val s = this.dockNode.getStage

    Window.getWindows
          .filtered(w ⇒ w.isInstanceOf[Stage] && w!=s)
          .asInstanceOf[ObservableList[Stage]]
          .asScala
  }
}

//****************************************************************************
