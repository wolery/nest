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

import scala.collection.mutable.{Map,Stack}

import scala.collection.JavaConverters.asScalaBufferConverter

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
  private val label      : Label  = new Label("Dock Title Bar")
  private val closeButton: Button = new Button()
  private val stateButton: Button = new Button()
  private var isDragging:  Bool = false;

  private val dragNodes = Map[Window,Node]();
  private var dragStart: Point2D = null;

  label.textProperty.bind(dockNode.titleProperty);
  label.graphicProperty.bind(dockNode.graphicProperty);

  stateButton.setOnAction(e ⇒
  {
    if (dockNode.isFloating)
      dockNode.setMaximized(!dockNode.isMaximized)
    else
      dockNode.setFloating(true)
  })

  closeButton.setOnAction(e ⇒ dockNode.close())
  closeButton.visibleProperty.bind(dockNode.closableProperty)

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
//addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressed(_))
//addEventHandler(MouseEvent.DRAG_DETECTED, onDragDetected(_))
//addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDragged(_))
//addEventHandler(MouseEvent.MOUSE_RELEASED,onMouseReleased(_))

  this.       getStyleClass.add("dock-title-bar")
  label.      getStyleClass.add("dock-title-label")
  closeButton.getStyleClass.add("dock-close-button")
  stateButton.getStyleClass.add("dock-state-button")
 //private def isDragging()    : Bool     = isDragging
  private def getLabel()      : Label    = label
  private def getCloseButton(): Button   = closeButton
  private def getStateButton(): Button   = stateButton
  private def getDockNode()   : DockNode = dockNode

  private
  trait EventTask
  {
    var isReady: Bool = true;
    def run(node: Node,dragNode: Node): Unit
    def reset()        : Unit = {isReady = true}
  }

  def handle(e: MouseEvent): Unit = e.getEventType match
  {
    case MouseEvent.MOUSE_PRESSED  ⇒ onMousePressed(e)
    case MouseEvent.DRAG_DETECTED  ⇒ onDragDetected(e)
    case MouseEvent.MOUSE_DRAGGED  ⇒ onMouseDragged(e)
    case MouseEvent.MOUSE_RELEASED ⇒ onMouseReleased(e)
  }

  def onMousePressed(event: MouseEvent): Unit =
  {
    if (dockNode.isFloating && event.getClickCount==2 && event.getButton==MouseButton.PRIMARY)
    {
      dockNode.setMaximized(!dockNode.isMaximized);
    }
    else
    {
    // drag detected is used in place of mouse pressed so there is some
    // threshold for the dragging which is determined by the default drag
    // detection threshold
      dragStart = new Point2D(event.getX,event.getY);
    }
  }

  def onDragDetected(event: MouseEvent): Unit =
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
        dockNode.setFloating(true,new Point2D(0,DockTitleBar.this.getHeight));
      }
      else
      {
        dockNode.setFloating(true);
      }

      // TODO: Find a better solution.
      // Temporary work around for nodes losing the drag event when removed from
      // the scene graph.
      // A possible alternative is to use "ghost" panes in the DockPane layout
      // while making DockNode simply an overlay stage that is always shown.
      // However since flickering when popping out was already eliminated that would
      // be overkill and is not a suitable solution for native decorations.
      // Bug report open: https://bugs.openjdk.java.net/browse/JDK-8133335
      val dockPane = this.getDockNode().getDockPane();
      if (dockPane != null)
      {
        dockPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
        dockPane.addEventFilter(MouseEvent.MOUSE_RELEASED,this);
      }
    }
    else
    if (dockNode.isMaximized())
    {
      val ratioX = event.getX / this.getDockNode.getWidth();
      val ratioY = event.getY / this.getDockNode.getHeight();

      // Please note that setMaximized is ruined by width and height changes occurring on the
      // stage and there is currently a bug report filed for this though I did not give them an
      // accurate test case which I should and wish I would have. This was causing issues in the
      // original release requiring maximized behavior to be implemented manually by saving the
      // restored bounds. The problem was that the resize functionality in DockNode.java was
      // executing at the same time canceling the maximized change.
      // https://bugs.openjdk.java.net/browse/JDK-8133334

      // restore/minimize the window after we have obtained its dimensions
      dockNode.setMaximized(false);

      // scale the drag start location by our restored dimensions
      dragStart = new Point2D(ratioX * dockNode.getWidth,
                              ratioY * dockNode.getHeight);
    }

    isDragging = true;

    event.consume();
  }

  def onMouseDragged(event: MouseEvent): Unit =
  {
    if (dockNode.isFloating
     && event.getClickCount == 2
     && event.getButton == MouseButton.PRIMARY)
    {
      event.setDragDetect(false);
      event.consume();
    }
    else
    if (isDragging)
    {
      val stage       = dockNode.getStage;
      val insetsDelta = this.getDockNode.getBorderPane.getInsets;

      // dragging this way makes the interface more responsive in the event
      // the system is lagging as is the case with most current JavaFX
      // implementations on Linux
      stage.setX(event.getScreenX - dragStart.getX - insetsDelta.getLeft);
      stage.setY(event.getScreenY - dragStart.getY - insetsDelta.getTop);

      val dockEnterEvent = dockEvent(DockEvent.DOCK_ENTER,event)
      val dockOverEvent  = dockEvent(DockEvent.DOCK_OVER, event)
      val dockExitEvent =  dockEvent(DockEvent.DOCK_EXIT, event)

      val eventTask = new EventTask()
      {
        def run(node:Node,dragNode: Node): Unit =
        {
          isReady = false

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
      };

      pickEventTarget(event.getScreenX,event.getScreenY,eventTask,Some(dockExitEvent))
    }
  }

  def onMouseReleased(event: MouseEvent): Unit =
  {
    isDragging = false;

    val dockReleasedEvent = dockEvent(DockEvent.DOCK_RELEASED,event,Option(this.getDockNode()))

    val eventTask = new EventTask()
    {
      def run(node: Node,dragNode: Node): Unit =
      {
        isReady = false

        if (dragNode != node)
        {
          Event.fireEvent(node,dockReleasedEvent.copyFor(DockTitleBar.this,node))
        }

        Event.fireEvent(node,dockReleasedEvent.copyFor(DockTitleBar.this,node))
      }
    }

    pickEventTarget(event.getScreenX,event.getScreenY,eventTask)

    dragNodes.clear();

    // Remove temporary event handler for bug mentioned above.
    val dockPane = this.getDockNode.getDockPane

    if (dockPane != null)
    {
      dockPane.removeEventFilter(MouseEvent.MOUSE_DRAGGED ,this);
      dockPane.removeEventFilter(MouseEvent.MOUSE_RELEASED,this);
    }
  }

  private
  def dockEvent(etype: EventType[DockEvent],e:MouseEvent,contents: Option[DockNode] = None): DockEvent =
  {
    new DockEvent(this,Event.NULL_SOURCE_TARGET,etype,e.getScreenX,e.getScreenY,contents)
  }

  private
  def pickEventTarget(x: ℝ,y: ℝ,task: EventTask,explicit: Option[Event] = None): Unit =
  {
    def isCandidate(node: Node): Bool =
    {
      !node.isMouseTransparent && node.contains(node.screenToLocal(x,y))
    }

    for (targetStage ← stages)
    {
      task.reset();

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
              case n         ⇒ task.run(n,dragNode)
            }

            notFired = false;
          }
        }

        // if none of the children fired the event or there were no children
        // fire it with the parent as the target to receive the event
        if (notFired)
        {
          task.run(parent,dragNode);
        }
      }

      if (task.isReady && explicit.isDefined && dragNode!=null)
      {
        Event.fireEvent(dragNode,explicit.get.copyFor(this,dragNode))
        dragNodes(targetStage) = null
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
