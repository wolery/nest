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

import javafx.beans.property.{ ObjectProperty, SimpleBooleanProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty }
import javafx.css.PseudoClass
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.{ Cursor, Node, Scene }
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{ BorderPane, Priority, VBox }
import javafx.stage.{ Screen, Stage, StageStyle };

object DockNode
{
  val FLOATING_PSEUDO_CLASS   = PseudoClass.getPseudoClass("floating");
  val DOCKED_PSEUDO_CLASS     = PseudoClass.getPseudoClass("docked");
  val MAXIMIZED_PSEUDO_CLASS  = PseudoClass.getPseudoClass("maximized");
}

class DockNode
(
var contents: Node,
title   : String = null,
graphic : Node  = null
)
extends VBox with EventHandler[MouseEvent]
{
  val graphicProperty:ObjectProperty[Node] = new SimpleObjectProperty[Node](null,"graphic",null)
  val titleProperty: StringProperty = new SimpleStringProperty(null,"title","Dock")
  val closableProperty = new SimpleBooleanProperty(null,"closable",true)
  val customTitleBarProperty = new SimpleBooleanProperty(null,"customTitleBar",true)
  val floatableProperty = new SimpleBooleanProperty(null,"floatable",true)
  private val maximizedProperty = new SimpleBooleanProperty(null,"maximized",false)
  {
    override
    def invalidated: Unit =
    {
      DockNode.this.pseudoClassStateChanged(DockNode.MAXIMIZED_PSEUDO_CLASS,get);

      if (borderPane != null)
      {
        borderPane.pseudoClassStateChanged(DockNode.MAXIMIZED_PSEUDO_CLASS,get);
      }

      stage.setMaximized(get());

      // TODO: This is a work around to fill the screen bounds and not overlap the task bar when
      // the window is undecorated as in Visual Studio. A similar work around needs applied for
      // JFrame in Swing. http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4737788
      // Bug report filed:
      // https://bugs.openjdk.java.net/browse/JDK-8133330
      if (this.get())
      {
        val screen = Screen
            .getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
            .get(0)
        val bounds = screen.getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
      }
    }
  };
  val dockedProperty = new SimpleBooleanProperty(null,"docked",false)
  {
   override
   def invalidated(): Unit =
    {
      if (get())
      {
        if (dockTitleBar != null)
        {
          dockTitleBar.setVisible(true);
          dockTitleBar.setManaged(true);
        }
      }

      DockNode.this.pseudoClassStateChanged(DockNode.DOCKED_PSEUDO_CLASS, get());
    }
  };

  private var stageStyle = StageStyle.TRANSPARENT;
  private var stage: Stage = null
  private var borderPane: BorderPane = null
  private var dockPane: DockPane = null


  var dockTitleBar = new DockTitleBar(this);

  getChildren().addAll(dockTitleBar,contents);
  VBox.setVgrow(contents,Priority.ALWAYS);

  this.getStyleClass().add("dock-node");




  def setStageStyle(s: StageStyle): Unit = stageStyle = stageStyle
  def setContents(c: Node) : Unit=
  {
    this.getChildren().set(this.getChildren().indexOf(this.contents), c);
    this.contents = c;
  }

  def setDockTitleBar(dockTitleBar: DockTitleBar):Unit = {
    if (dockTitleBar != null) {
      if (this.dockTitleBar != null) {
        this.getChildren().set(this.getChildren().indexOf(this.dockTitleBar), dockTitleBar);
      } else {
        this.getChildren().add(0, dockTitleBar);
      }
    } else {
      this.getChildren().remove(this.dockTitleBar);
    }

    this.dockTitleBar = dockTitleBar;
  }

  def setMaximized(maximized: Bool): Unit = maximizedProperty.set(maximized)

  def setFloating(floating: Bool,translation: Point2D = null): Unit =
  {
    if (floating && !this.isFloating)
    {
      // position the new stage relative to the old scene offset
      val floatScene = this.localToScene(0, 0);
      val floatScreen = this.localToScreen(0, 0);

      // setup window stage
      dockTitleBar.setVisible(this.isCustomTitleBar());
      dockTitleBar.setManaged(this.isCustomTitleBar());

      if (this.isDocked())
      {
        this.undock();
      }

      stage = new Stage();
      stage.titleProperty().bind(titleProperty);
      if (dockPane != null && dockPane.getScene() != null
          && dockPane.getScene().getWindow() != null) {
        stage.initOwner(dockPane.getScene().getWindow());
      }

      stage.initStyle(stageStyle);

      // offset the new stage to cover exactly the area the dock was local to
      // the scene
      // this is useful for when the user presses the + sign and we have no
      // information on where the mouse was clicked
      var stagePosition: Point2D = null

      if (this.isDecorated)
      {
        val owner = stage.getOwner

        stagePosition = floatScene.add(new Point2D(owner.getX(),owner.getY()));
      }
      else
      {
        stagePosition = floatScreen;
      }

      if (translation != null)
      {
        stagePosition = stagePosition.add(translation);
      }

      // the border pane allows the dock node to
      // have a drop shadow effect on the border
      // but also maintain the layout of contents
      // such as a tab that has no content
      borderPane = new BorderPane();
      borderPane.getStyleClass.add("dock-node-border");
      borderPane.setCenter(this);

      val scene = new Scene(borderPane);

      // apply the floating property so we can get its padding size
      // while it is floating to offset it by the drop shadow
      // this way it pops out above exactly where it was when docked
      this.floatingProperty.set(floating);
      this.applyCss();

      // apply the border pane css so that we can get the insets and
      // position the stage properly
      borderPane.applyCss();
      val insetsDelta = borderPane.getInsets();

      val insetsWidth = insetsDelta.getLeft() + insetsDelta.getRight();
      val insetsHeight = insetsDelta.getTop() + insetsDelta.getBottom();

      stage.setX(stagePosition.getX() - insetsDelta.getLeft());
      stage.setY(stagePosition.getY() - insetsDelta.getTop());

      stage.setMinWidth (borderPane.minWidth (this.getHeight) + insetsWidth);
      stage.setMinHeight(borderPane.minHeight(this.getWidth)  + insetsHeight);

      borderPane.setPrefSize(this.getWidth + insetsWidth,this.getHeight + insetsHeight);

      stage.setScene(scene);

      if (stageStyle == StageStyle.TRANSPARENT)
      {
        scene.setFill(null);
      }

      stage.setResizable(this.isStageResizable())

      if (this.isStageResizable())
      {
        stage.addEventFilter(MouseEvent.MOUSE_PRESSED, this);
        stage.addEventFilter(MouseEvent.MOUSE_MOVED,   this);
        stage.addEventFilter(MouseEvent.MOUSE_DRAGGED, this);
      }

      // we want to set the client area size
      // without this it subtracts the native border sizes from the scene
      // size
      stage.sizeToScene();

      stage.show();
    }
    else
    if (!floating && this.isFloating)
    {
      this.floatingProperty.set(floating);
      stage.removeEventFilter(MouseEvent.MOUSE_PRESSED, this);
      stage.removeEventFilter(MouseEvent.MOUSE_MOVED, this);
      stage.removeEventFilter(MouseEvent.MOUSE_DRAGGED, this);
      stage.close();
    }
  }

  def getDockPane() = dockPane
  def getDockTitleBar(): DockTitleBar = this.dockTitleBar
  def getStage() : Stage = stage
  def getBorderPane() :BorderPane = borderPane
  def getContents() : Node = contents

  def getGraphic(): Node = graphicProperty.get();
  def setGraphic(graphic: Node): Unit = this.graphicProperty.setValue(graphic)

  def getTitle():String               = titleProperty.get
  def setTitle(title :String)        = this.titleProperty.setValue(title)


  def isCustomTitleBar(): Bool =customTitleBarProperty.get();
  def setUseCustomTitleBar(useCustomTitleBar: Bool): Unit =
  {
    if (this.isFloating())
    {
      dockTitleBar.setVisible(useCustomTitleBar);
      dockTitleBar.setManaged(useCustomTitleBar);
    }
    this.customTitleBarProperty.set(useCustomTitleBar);
  }

  val floatingProperty = new SimpleBooleanProperty(null,"floating",false)
  {
    override
    def invalidated() =
    {
      DockNode.this.pseudoClassStateChanged(DockNode.FLOATING_PSEUDO_CLASS, get());
      if (borderPane != null)
      {
        borderPane.pseudoClassStateChanged(DockNode.FLOATING_PSEUDO_CLASS, get());
      }
    }
  }

  def isFloating(): Bool = floatingProperty.get


  def isFloatable() = floatableProperty.get
  def setFloatable(floatable: Bool) =
  {
    if (!floatable && this.isFloating)
    {
      this.setFloating(false);
    }
    this.floatableProperty.set(floatable);
  }

  def isClosable() = closableProperty.get()
  def setClosable(closable: Bool) = this.closableProperty.set(closable)

  val stageResizableProperty = new SimpleBooleanProperty(null,"resizable",true)
  def isStageResizable() = stageResizableProperty.get
  def setStageResizable(resizable: Bool) = stageResizableProperty.set(resizable)


  def isDocked() = dockedProperty.get

//  public final BooleanProperty maximizedProperty() {
//    return maximizedProperty;
//  }

  def isMaximized() : Bool = maximizedProperty.get

  def isDecorated(): Bool = stageStyle != StageStyle.TRANSPARENT && stageStyle != StageStyle.UNDECORATED;

  def dock(dockPane: DockPane,dockPos: DockPos,sibling: Node) =
  {
    dockImpl(dockPane);
    dockPane.dock(this, dockPos, sibling);
  }

  def dock(dockPane: DockPane,dockPos: DockPos) =
  {
    dockImpl(dockPane);
    dockPane.dock(this,dockPos);
  }

 private def dockImpl(dockPane: DockPane) =
 {
    if (isFloating())
    {
      setFloating(false);
    }
    this.dockPane = dockPane;
    this.dockedProperty.set(true);
  }

  def undock(): Unit =
  {
    if (dockPane != null) {
      dockPane.undock(this);
    }
    this.dockedProperty.set(false);
  }

  def close() : Unit =
  {
    if (isFloating())
    {
      setFloating(false);
    }
    else
    if (isDocked())
    {
      undock();
    }
  }

  /**
   * The last position of the mouse that was within the minimum layout bounds.
   */
  private var sizeLast: Point2D = null
  /**
   * Whether we are currently resizing in a given direction.
   */
  private var sizeWest = false
  private var sizeEast = false
  private var sizeNorth = false
  private var sizeSouth = false;

  def isMouseResizeZone() = sizeWest || sizeEast || sizeNorth || sizeSouth;

  def handle(event: MouseEvent): Unit =
  {
    var cursor = Cursor.DEFAULT;

    // TODO: use escape to cancel resize/drag operation like visual studio

    if (!this.isFloating() || !this.isStageResizable())
    {
      return;
    }

    if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
    {
      sizeLast = new Point2D(event.getScreenX(), event.getScreenY());
    }
    else
    if (event.getEventType() == MouseEvent.MOUSE_MOVED)
    {
      val insets = borderPane.getPadding();

      sizeWest = event.getX() < insets.getLeft();
      sizeEast = event.getX() > borderPane.getWidth() - insets.getRight();
      sizeNorth = event.getY() < insets.getTop();
      sizeSouth = event.getY() > borderPane.getHeight() - insets.getBottom();

      if (sizeWest) {
        if (sizeNorth) {
          cursor = Cursor.NW_RESIZE;
        } else if (sizeSouth) {
          cursor = Cursor.SW_RESIZE;
        } else {
          cursor = Cursor.W_RESIZE;
        }
      } else if (sizeEast) {
        if (sizeNorth) {
          cursor = Cursor.NE_RESIZE;
        } else if (sizeSouth) {
          cursor = Cursor.SE_RESIZE;
        } else {
          cursor = Cursor.E_RESIZE;
        }
      } else if (sizeNorth) {
        cursor = Cursor.N_RESIZE;
      } else if (sizeSouth) {
        cursor = Cursor.S_RESIZE;
      }

      this.getScene().setCursor(cursor);
    }
    else
    if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && this.isMouseResizeZone()) {
      val sizeCurrent = new Point2D(event.getScreenX(), event.getScreenY());
      val sizeDelta = sizeCurrent.subtract(sizeLast);

      var newX = stage.getX()
      var newY = stage.getY()
      var newWidth = stage.getWidth()
      var newHeight = stage.getHeight();

      if (sizeNorth) {
        newHeight -= sizeDelta.getY();
        newY += sizeDelta.getY();
      } else if (sizeSouth) {
        newHeight += sizeDelta.getY();
      }

      if (sizeWest) {
        newWidth -= sizeDelta.getX();
        newX += sizeDelta.getX();
      } else if (sizeEast) {
        newWidth += sizeDelta.getX();
      }

      // TODO: find a way to do this synchronously and eliminate the flickering of moving the stage
      // around, also file a bug report for this feature if a work around can not be found this
      // primarily occurs when dragging north/west but it also appears in native windows and Visual
      // Studio, so not that big of a concern.
      // Bug report filed:
      // https://bugs.openjdk.java.net/browse/JDK-8133332
      var currentX = sizeLast.getX()
      var currentY = sizeLast.getY()

      if (newWidth >= stage.getMinWidth()) {
        stage.setX(newX);
        stage.setWidth(newWidth);
        currentX = sizeCurrent.getX();
      }

      if (newHeight >= stage.getMinHeight()) {
        stage.setY(newY);
        stage.setHeight(newHeight);
        currentY = sizeCurrent.getY();
      }
      sizeLast = new Point2D(currentX, currentY);
      // we do not want the title bar getting these events
      // while we are actively resizing
      if (sizeNorth || sizeSouth || sizeWest || sizeEast) {
        event.consume();
      }
    }
  }
  this.titleProperty.setValue(title);
  this.graphicProperty.setValue(graphic);
}
