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

import java.util.Stack;

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
  val dockPosIndicator = new GridPane();

  this.addEventHandler(DockEvent.ANY, this);
  this.addEventFilter (DockEvent.ANY, new EventHandler[DockEvent]()
  {
    def handle(event: DockEvent): Unit =
    {
      if (event.getEventType() == DockEvent.DOCK_ENTER) {
        DockPane.this.receivedEnter = true;
      } else if (event.getEventType() == DockEvent.DOCK_OVER) {
        DockPane.this.dockNodeDrag = null;
      }
    }
  })

  val dockIndicatorPopup = new Popup();
  dockIndicatorPopup.setAutoFix(false);

  val dockIndicatorOverlay = new Popup();
  dockIndicatorOverlay.setAutoFix(false);

  val dockRootPane = new StackPane();
  dockRootPane.prefWidthProperty().bind(this.widthProperty());
  dockRootPane.prefHeightProperty().bind(this.heightProperty());

  val dockAreaIndicator = new Rectangle();
  dockAreaIndicator.setManaged(false);
  dockAreaIndicator.setMouseTransparent(true);

  val dockAreaStrokeTimeline = new Timeline();
  dockAreaStrokeTimeline.setCycleCount(Animation.INDEFINITE);
  // 12 is the cumulative offset of the stroke dash array in the default.css style sheet
  // RFE filed for CSS styled timelines/animations:
  // https://bugs.openjdk.java.net/browse/JDK-8133837

  val xx:javafx.beans.value.WritableDoubleValue= dockAreaIndicator.strokeDashOffsetProperty()
  val kv:KeyValue = new KeyValue(xx.asInstanceOf[javafx.beans.value.WritableValue[Any]], 12);
  val kf = new KeyFrame(Duration.millis(500), kv);

  dockAreaStrokeTimeline.getKeyFrames().add(kf);
  dockAreaStrokeTimeline.play();

  val dockCenter = new DockPosButton(false, DockPos.CENTER);
  dockCenter.getStyleClass().add("dock-center");

  val dockTop = new DockPosButton(false, DockPos.TOP);
  dockTop.getStyleClass().add("dock-top");
  val dockRight = new DockPosButton(false, DockPos.RIGHT);
  dockRight.getStyleClass().add("dock-right");
  val dockBottom = new DockPosButton(false, DockPos.BOTTOM);
  dockBottom.getStyleClass().add("dock-bottom");
  val dockLeft = new DockPosButton(false, DockPos.LEFT);
  dockLeft.getStyleClass().add("dock-left");

  val dockTopRoot = new DockPosButton(true, DockPos.TOP);
  StackPane.setAlignment(dockTopRoot, Pos.TOP_CENTER);
  dockTopRoot.getStyleClass().add("dock-top-root");

  val dockRightRoot = new DockPosButton(true, DockPos.RIGHT);
  StackPane.setAlignment(dockRightRoot, Pos.CENTER_RIGHT);
  dockRightRoot.getStyleClass().add("dock-right-root");

  val dockBottomRoot = new DockPosButton(true, DockPos.BOTTOM);
  StackPane.setAlignment(dockBottomRoot, Pos.BOTTOM_CENTER);
  dockBottomRoot.getStyleClass().add("dock-bottom-root");

  val dockLeftRoot = new DockPosButton(true, DockPos.LEFT);
  StackPane.setAlignment(dockLeftRoot, Pos.CENTER_LEFT);
  dockLeftRoot.getStyleClass().add("dock-left-root");

  // TODO: dockCenter goes first when tabs are added in a future version
  val dockPosButtons = FXCollections.observableArrayList(dockTop, dockRight, dockBottom, dockLeft,
      dockTopRoot, dockRightRoot, dockBottomRoot, dockLeftRoot);

  dockPosIndicator.add(dockTop, 1, 0);
  dockPosIndicator.add(dockRight, 2, 1);
  dockPosIndicator.add(dockBottom, 1, 2);
  dockPosIndicator.add(dockLeft, 0, 1);
  // dockPosIndicator.add(dockCenter, 1, 1);

  dockRootPane.getChildren().addAll(dockAreaIndicator, dockTopRoot, dockRightRoot, dockBottomRoot,
      dockLeftRoot);

  dockIndicatorOverlay.getContent().add(dockRootPane);
  dockIndicatorPopup.getContent().addAll(dockPosIndicator);

  this.getStyleClass().add("dock-pane");
  dockRootPane.getStyleClass().add("dock-root-pane");
  dockPosIndicator.getStyleClass().add("dock-pos-indicator");
  dockAreaIndicator.getStyleClass().add("dock-area-indicator");

  /**
   * The current root node of this dock pane's layout.
   */
  private var root:Node = null

  /**
   * Whether a DOCK_ENTER event has been received by this dock pane since the last DOCK_EXIT event
   * was received.
   */
  private var receivedEnter = false

  /**
   * The current node in this dock pane that we may be dragging over.
   */
  private var dockNodeDrag:Node =null;
  /**
   * The docking area of the current dock indicator button if any is selected. This is either the
   * root or equal to dock node drag.
   */
  private var dockAreaDrag: Node  = _

  /**
   * The docking position of the current dock indicator button if any is selected.
   */
  private var dockPosDrag : DockPos = _

  /**
   * The timeline used to animate the borer of the docking area indicator shape. Because JavaFX has
   * no CSS styling for timelines/animations yet we will make this private and offer an accessor for
   * the user to programmatically modify the animation or disable it.
   */
 // private var dockAreaStrokeTimeline:Timeline =null
  /**
   * The popup used to display the root dock indicator buttons and the docking area indicator.
   */
//  private var dockIndicatorOverlay:Popup =null

  /**
   * The grid pane used to lay out the local dock indicator buttons. This is the grid used to lay
   * out the buttons in the circular indicator.
   */
  //private var dockPosIndicator :GridPane =null
  /**
   * The popup used to display the local dock indicator buttons. This allows these indicator buttons
   * to be displayed outside the window of this dock pane.
   */
  //private var dockIndicatorPopup:Popup =null

  class DockPosButton(var dockRoot: Bool,var dockPos: DockPos) extends Button
  {
    def setDockRoot(dockRoot: Bool): Unit = this.dockRoot = dockRoot
    def setDockPos(dockPos:DockPos): Unit = this.dockPos = dockPos
    def getDockPos() = dockPos
    def isDockRoot() = dockRoot
  }

  /**
   * A collection used to manage the indicator buttons and automate hit detection during DOCK_OVER
   * events.
   */
//  private var dockPosButtons: ObservableList[DockPosButton] = null

  def getDockAreaStrokeTimeline() = dockAreaStrokeTimeline;

  def getDefaultUserAgentStyleheet(): String =
    getClass.getResource("default.css").toExternalForm();


  private
  val dockNodeEventFilters:ObservableMap[Node, DockNodeEventHandler] = FXCollections.observableHashMap();

  private
  class DockNodeEventHandler(node: Node) extends EventHandler[DockEvent]
  {
    def handle(e:DockEvent): Unit=
    {
      DockPane.this.dockNodeDrag = node;
    }
  }

  def dock(node: Node,dockPos: DockPos,sibling: Node): Unit =
  {
    System.out.println("DOCKING");
    val dockNodeEventHandler = new DockNodeEventHandler(node);
    dockNodeEventFilters.put(node, dockNodeEventHandler);
    node.addEventFilter(DockEvent.DOCK_OVER, dockNodeEventHandler);

    var split = root.asInstanceOf[SplitPane]

    if (split == null)
    {
      split = new SplitPane();
      split.getItems().add(node);
      root = split;
      this.getChildren().add(root);
      return;
    }

    // find the parent of the sibling
    if (sibling != null && sibling != root)
    {
      val stack = new Stack[Parent]();
      stack.push(root.asInstanceOf[Parent])

      while (!stack.isEmpty())
      {
        val parent = stack.pop();

        var children = parent.getChildrenUnmodifiable();

        if (parent.isInstanceOf[SplitPane])
        {
          val splitPane = parent.asInstanceOf[SplitPane];
          children = splitPane.getItems();
        }

        for (i <- 0 until children.size())
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
    if (split.getOrientation() != requestedOrientation)
    {
      if (split.getItems().size() > 1)
      {
        val splitPane = new SplitPane();
        if (split == root && sibling == root)
        {
          this.getChildren().set(this.getChildren().indexOf(root), splitPane);
          splitPane.getItems().add(split);
          root = splitPane;
        }
        else
        {
          split.getItems().set(split.getItems().indexOf(sibling), splitPane);
          splitPane.getItems().add(sibling);
        }

        split = splitPane;
      }
      split.setOrientation(requestedOrientation);
    }

    // finally dock the node to the correct split pane
    val splitItems = split.getItems();

    var magnitude = 0.0;

    if (splitItems.size() > 0)
    {
      if (split.getOrientation() == Orientation.HORIZONTAL)
      {
        splitItems.forEach(splitItem=>
          magnitude += splitItem.prefWidth(0)
        )
      }
      else
      {
        splitItems.forEach(splitItem=>
          magnitude += splitItem.prefHeight(0))
      }
    }

    if (dockPos == DockPos.LEFT || dockPos == DockPos.TOP)
    {
      var relativeIndex = 0
      if (sibling != null && sibling != root)
      {
        relativeIndex = splitItems.indexOf(sibling);
      }

      splitItems.add(relativeIndex, node);

      if (splitItems.size() > 1)
      {
        if (split.getOrientation() == Orientation.HORIZONTAL)
        {
          split.setDividerPosition(relativeIndex,node.prefWidth(0) / (magnitude + node.prefWidth(0)));
        }
        else
        {
          split.setDividerPosition(relativeIndex,node.prefHeight(0) / (magnitude + node.prefHeight(0)));
        }
      }
    }
    else
      if (dockPos == DockPos.RIGHT || dockPos == DockPos.BOTTOM)
      {
      var relativeIndex = splitItems.size();
      if (sibling != null && sibling != root)
      {
        relativeIndex = splitItems.indexOf(sibling) + 1;
      }

      splitItems.add(relativeIndex, node);
      if (splitItems.size() > 1) {
        if (split.getOrientation() == Orientation.HORIZONTAL) {
          split.setDividerPosition(relativeIndex - 1,
              1 - node.prefWidth(0) / (magnitude + node.prefWidth(0)));
        } else {
          split.setDividerPosition(relativeIndex - 1,
              1 - node.prefHeight(0) / (magnitude + node.prefHeight(0)));
        }
      }
    }
  }

  def dock(node: Node,dockPos: DockPos): Unit =
  {
    dock(node,dockPos,root);
  }

  def undock(node: DockNode ): Unit =
  {
    val dockNodeEventHandler = dockNodeEventFilters.get(node);
    node.removeEventFilter(DockEvent.DOCK_OVER, dockNodeEventHandler);
    dockNodeEventFilters.remove(node);

    // depth first search to find the parent of the node
    val findStack = new Stack[Parent]();
    findStack.push(root.asInstanceOf[Parent])

    while (!findStack.isEmpty())
    {
      var parent = findStack.pop();

      var children = parent.getChildrenUnmodifiable();

      if (parent.isInstanceOf[SplitPane])
      {
        val split = parent.asInstanceOf[SplitPane]
        children = split.getItems()
      }

      for (i <- 0 until children.size())
      {
        if (children.get(i) == node) {
          children.remove(i);

          // start from the root again and remove any SplitPane's with no children in them
          val clearStack = new Stack[Parent]();
          clearStack.push(root.asInstanceOf[Parent]);
          while (!clearStack.isEmpty())
          {
            parent = clearStack.pop();

            children = parent.getChildrenUnmodifiable();

            if (parent.isInstanceOf[SplitPane])
            {
              val split = parent.asInstanceOf[SplitPane]
              children = split.getItems();
            }

            for (i <- 0 until children.size())
            {
              if (children.get(i).isInstanceOf[SplitPane])
              {
                val split = children.get(i).asInstanceOf[SplitPane]
                if (split.getItems().size() < 1)
                {
                  children.remove(i);
                  //continue;
                }
                else
                {
                  clearStack.push(split);
                }
              }
            }
          }

          return;
        }
        else
        if (children.get(i).isInstanceOf[Parent])
        {
          findStack.push(children.get(i).asInstanceOf[Parent])
        }
      }
    }
  }

  def handle(event:DockEvent ):Unit ={
    if (event.getEventType() == DockEvent.DOCK_ENTER) {
      if (!dockIndicatorOverlay.isShowing()) {
        val topLeft = DockPane.this.localToScreen(0, 0);
        dockIndicatorOverlay.show(DockPane.this, topLeft.getX(), topLeft.getY());
      }
    } else if (event.getEventType() == DockEvent.DOCK_OVER) {
      System.out.println("OVER");
      this.receivedEnter = false;

      dockPosDrag = null;
      dockAreaDrag = dockNodeDrag;
var FLAG = true
      for (dockIndicatorButton <- dockPosButtons.asScala if FLAG) {
        if (dockIndicatorButton
            .contains(dockIndicatorButton.screenToLocal(event.getScreenX, event.getScreenY))) {
          dockPosDrag = dockIndicatorButton.getDockPos();
          if (dockIndicatorButton.isDockRoot()) {
            dockAreaDrag = root;
          }
          dockIndicatorButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
          FLAG=false
          //break;
        } else {
          dockIndicatorButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), false);
        }
      }

      if (dockPosDrag != null)
      {
        val originToScene = dockAreaDrag.localToScene(0, 0);

        dockAreaIndicator.setVisible(true);
        dockAreaIndicator.relocate(originToScene.getX, originToScene.getY)

        if (dockPosDrag == DockPos.RIGHT)
        {
          dockAreaIndicator.setTranslateX(dockAreaDrag.getLayoutBounds().getWidth() / 2);
        }
        else
        {
          dockAreaIndicator.setTranslateX(0);
        }

        if (dockPosDrag == DockPos.BOTTOM)
        {
          dockAreaIndicator.setTranslateY(dockAreaDrag.getLayoutBounds().getHeight() / 2);
        }
        else
        {
          dockAreaIndicator.setTranslateY(0);
        }

        if (dockPosDrag == DockPos.LEFT || dockPosDrag == DockPos.RIGHT)
        {
          dockAreaIndicator.setWidth(dockAreaDrag.getLayoutBounds().getWidth() / 2);
        }
        else
        {
          dockAreaIndicator.setWidth(dockAreaDrag.getLayoutBounds().getWidth())
        }

        if (dockPosDrag == DockPos.TOP || dockPosDrag == DockPos.BOTTOM)
        {
          dockAreaIndicator.setHeight(dockAreaDrag.getLayoutBounds().getHeight() / 2);
        }
        else
        {
          dockAreaIndicator.setHeight(dockAreaDrag.getLayoutBounds().getHeight());
        }
      }
      else
      {
        dockAreaIndicator.setVisible(false);
      }

      if (dockNodeDrag != null) {
        val originToScreen = dockNodeDrag.localToScreen(0, 0);

        val posX = originToScreen.getX() + dockNodeDrag.getLayoutBounds().getWidth() / 2
            - dockPosIndicator.getWidth() / 2;
        val posY = originToScreen.getY() + dockNodeDrag.getLayoutBounds().getHeight() / 2
            - dockPosIndicator.getHeight() / 2;

        if (!dockIndicatorPopup.isShowing()) {
          dockIndicatorPopup.show(DockPane.this, posX, posY);
        } else {
          dockIndicatorPopup.setX(posX);
          dockIndicatorPopup.setY(posY);
        }

        // set visible after moving the popup
        dockPosIndicator.setVisible(true);
      } else {
        dockPosIndicator.setVisible(false);
      }
    }

    if (event.getEventType() == DockEvent.DOCK_RELEASED && event.getContents != null) {
      if (dockPosDrag != null && dockIndicatorOverlay.isShowing()) {
        val dockNode = event.getContents.asInstanceOf[DockNode];
        System.out.println("FOUND AND DOCK");
        dockNode.dock(this, dockPosDrag, dockAreaDrag);
      }
    }

    if ((event.getEventType() == DockEvent.DOCK_EXIT && !this.receivedEnter)
        || event.getEventType() == DockEvent.DOCK_RELEASED) {
      if (dockIndicatorPopup.isShowing()) {
        dockIndicatorOverlay.hide();
        dockIndicatorPopup.hide();
      }
    }
  }

}

//****************************************************************************
