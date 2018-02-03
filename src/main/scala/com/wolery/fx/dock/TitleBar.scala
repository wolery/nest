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

import java.util.HashMap;
import java.util.Stack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Window;
import collection.JavaConverters._

class DockTitleBar
(
  dockNode: DockNode
)
extends HBox with EventHandler[MouseEvent]
{
  private val label      : Label  = new Label("Dock Title Bar")
  private val closeButton: Button = new Button()
  private val stateButton: Button = new Button()

  label.textProperty().bind(dockNode.titleProperty);
  label.graphicProperty().bind(dockNode.graphicProperty);

  stateButton.setOnAction(new EventHandler[ActionEvent]()
  {
    def handle(event: ActionEvent): Unit =
    {
      if (dockNode.isFloating)
      {
        dockNode.setMaximized(!dockNode.isMaximized);
      }
      else
      {
        dockNode.setFloating(true);
      }
    }
  })

  closeButton.setOnAction(new EventHandler[ActionEvent]()
  {
    def handle(e: ActionEvent): Unit =
    {
      dockNode.close();
    }
  })
  closeButton.visibleProperty().bind(dockNode.closableProperty);

  // create a pane that will stretch to make the buttons right aligned
  val fillPane = new Pane();
  HBox.setHgrow(fillPane,Priority.ALWAYS);

  getChildren().addAll(label,fillPane,stateButton,closeButton);

  this.addEventHandler(MouseEvent.MOUSE_PRESSED, this);
  this.addEventHandler(MouseEvent.DRAG_DETECTED, this);
  this.addEventHandler(MouseEvent.MOUSE_DRAGGED, this);
  this.addEventHandler(MouseEvent.MOUSE_RELEASED, this);

  label.      getStyleClass().add("dock-title-label");
  closeButton.getStyleClass().add("dock-close-button");
  stateButton.getStyleClass().add("dock-state-button");
  this.       getStyleClass().add("dock-title-bar");

  def isDragging()    : Bool   = dragging
  def getLabel()      : Label  = label
  def getCloseButton(): Button = closeButton
  def getStateButton(): Button = stateButton
  def getDockNode()   : DockNode = dockNode

  private var dragStart: Point2D = null;
  private var dragging: Bool = false;
  private val dragNodes = new HashMap[Window,Node]();

  private abstract class EventTask
  {
    protected var executions: Int = 0;

    def run(node:Node,dragNode: Node): Unit
    def getExecutions(): Int  = executions
    def reset()        : Unit = {executions = 0}
  }

  private
  def pickEventTarget(location:Point2D,eventTask: EventTask,explicit: Event): Unit =
  {
    // RFE for public scene graph traversal API filed but closed:
    // https://bugs.openjdk.java.net/browse/JDK-8133331

    val stages  =
        FXCollections.unmodifiableObservableList(StageUtils.getStages()).asScala;

    for (targetStage <- stages if targetStage != this.dockNode.getStage())
    {
      // obviously this title bar does not need to receive its own events
      // though users of this library may want to know when their
      // dock node is being dragged by subclassing it or attaching
      // an event listener in which case a new event can be defined or
      // this continue behavior can be removed
//      if (targetStage == this.dockNode.getStage())
//        continue;

      eventTask.reset();

      val dragNode = dragNodes.get(targetStage);

      val root = targetStage.getScene().getRoot();
      val stack = new Stack[Parent]();
      if (root.contains(root.screenToLocal(location.getX(), location.getY()))
          && !root.isMouseTransparent()) {
        stack.push(root);
      }
      // depth first traversal to find the deepest node or parent with no children
      // that intersects the point of interest
      while (!stack.isEmpty()) {
        val parent = stack.pop();
        // if this parent contains the mouse click in screen coordinates in its local bounds
        // then traverse its children
        var notFired = true;
        for (node <- parent.getChildrenUnmodifiable().asScala if notFired) {
          if (node.contains(node.screenToLocal(location.getX(), location.getY()))
              && !node.isMouseTransparent()) {
            if (node.isInstanceOf[Parent]) {
              stack.push(node.asInstanceOf[Parent]);
            } else {
              eventTask.run(node, dragNode);
            }
            notFired = false;
          //  break;
          }
        }
        // if none of the children fired the event or there were no children
        // fire it with the parent as the target to receive the event
        if (notFired) {
        System.out.println("FIRING");
          eventTask.run(parent, dragNode);
        }
      }

      if (explicit != null && dragNode != null && eventTask.getExecutions() < 1) {
        Event.fireEvent(dragNode, explicit.copyFor(this, dragNode));
        dragNodes.put(targetStage, null);
      }
    }
  }

  def handle(event : MouseEvent): Unit = {
    if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
      if (dockNode.isFloating() && event.getClickCount() == 2
          && event.getButton() == MouseButton.PRIMARY) {
        dockNode.setMaximized(!dockNode.isMaximized());
      } else {
        // drag detected is used in place of mouse pressed so there is some threshold for the
        // dragging which is determined by the default drag detection threshold
        dragStart = new Point2D(event.getX(), event.getY());
      }
    } else if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
      if (!dockNode.isFloating()) {
        // if we are not using a custom title bar and the user
        // is not forcing the default one for floating and
        // the dock node does have native window decorations
        // then we need to offset the stage position by
        // the height of this title bar
        if (!dockNode.isCustomTitleBar() && dockNode.isDecorated()) {
          dockNode.setFloating(true, new Point2D(0, DockTitleBar.this.getHeight()));
        } else {
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
        if (dockPane != null) {
          dockPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
          dockPane.addEventFilter(MouseEvent.MOUSE_RELEASED, this);
        }
      } else if (dockNode.isMaximized()) {
        val ratioX = event.getX() / this.getDockNode().getWidth();
        val ratioY = event.getY() / this.getDockNode().getHeight();

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
        dragStart = new Point2D(ratioX * dockNode.getWidth(), ratioY * dockNode.getHeight());
      }
      dragging = true;
      event.consume();
    } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
      if (dockNode.isFloating() && event.getClickCount() == 2
          && event.getButton() == MouseButton.PRIMARY) {
        event.setDragDetect(false);
        event.consume();
        return;
      }

      if (!dragging)
        return;

      val stage = dockNode.getStage();
      val insetsDelta = this.getDockNode().getBorderPane().getInsets();

      // dragging this way makes the interface more responsive in the event
      // the system is lagging as is the case with most current JavaFX
      // implementations on Linux
      stage.setX(event.getScreenX() - dragStart.getX() - insetsDelta.getLeft());
      stage.setY(event.getScreenY() - dragStart.getY() - insetsDelta.getTop());

      // TODO: change the pick result by adding a copyForPick()
      val dockEnterEvent =
          new DockEvent(this, Event.NULL_SOURCE_TARGET, DockEvent.DOCK_ENTER, event.getX(),
              event.getY(), event.getScreenX(), event.getScreenY(), null,null);
      val dockOverEvent =
          new DockEvent(this, Event.NULL_SOURCE_TARGET, DockEvent.DOCK_OVER, event.getX(),
              event.getY(), event.getScreenX(), event.getScreenY(), null,null);
      val dockExitEvent =
          new DockEvent(this, Event.NULL_SOURCE_TARGET, DockEvent.DOCK_EXIT, event.getX(),
              event.getY(), event.getScreenX(), event.getScreenY(), null,null);

      val eventTask = new EventTask() {
        @Override
        def run(node:Node , dragNode:Node )  ={
          executions+= 1;

          if (dragNode != node) {
            Event.fireEvent(node, dockEnterEvent.copyFor(DockTitleBar.this, node));

            if (dragNode != null) {
              // fire the dock exit first so listeners
              // can actually keep track of the node we
              // are currently over and know when we
              // aren't over any which DOCK_OVER
              // does not provide
              Event.fireEvent(dragNode, dockExitEvent.copyFor(DockTitleBar.this, dragNode));
            }

            dragNodes.put(node.getScene().getWindow(), node);
          }
          Event.fireEvent(node, dockOverEvent.copyFor(DockTitleBar.this, node));
        }
      };

      this.pickEventTarget(new Point2D(event.getScreenX(), event.getScreenY()), eventTask,
          dockExitEvent);
    } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
      dragging = false;

      val dockReleasedEvent =
          new DockEvent(this, Event.NULL_SOURCE_TARGET, DockEvent.DOCK_RELEASED, event.getX(),
              event.getY(), event.getScreenX(), event.getScreenY(), null, this.getDockNode());

      val eventTask = new EventTask() {
        def run(node :Node, dragNode:Node ) ={
          executions+=1;
          if (dragNode != node) {
            Event.fireEvent(node, dockReleasedEvent.copyFor(DockTitleBar.this, node));
          }
          Event.fireEvent(node, dockReleasedEvent.copyFor(DockTitleBar.this, node));
        }
      };

      this.pickEventTarget(new Point2D(event.getScreenX(), event.getScreenY()), eventTask, null);

      dragNodes.clear();

      // Remove temporary event handler for bug mentioned above.
      val dockPane = this.getDockNode().getDockPane();
      if (dockPane != null) {
        dockPane.removeEventFilter(MouseEvent.MOUSE_DRAGGED, this);
        dockPane.removeEventFilter(MouseEvent.MOUSE_RELEASED, this);
      }
    }
  }
}

//****************************************************************************
