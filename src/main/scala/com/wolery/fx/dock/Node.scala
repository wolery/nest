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
  private val DOCKED_PSEUDO_CLASS    = PseudoClass.getPseudoClass("docked");
  private val FLOATING_PSEUDO_CLASS  = PseudoClass.getPseudoClass("floating");
  private val MAXIMIZED_PSEUDO_CLASS = PseudoClass.getPseudoClass("maximized");
}

class DockNode
(
  contents  : Node,
  title     : String = "Dock",
  graphic   : Node   = null
)
extends VBox with EventHandler[MouseEvent]
{
  val graphicProperty           = new SimpleObjectProperty[Node](null,"graphic",graphic)
  val titleProperty             = new SimpleStringProperty      (null,"title",title)
  val closableProperty          = new SimpleBooleanProperty     (null,"closable",true)
  val floatableProperty         = new SimpleBooleanProperty     (null,"floatable",true)
  val stageResizableProperty    = new SimpleBooleanProperty     (null,"resizable",true)
  val customTitleBarProperty    = new SimpleBooleanProperty     (null,"customTitleBar",true)

  private
  val maximizedProperty = new SimpleBooleanProperty(null,"maximized",false)
  {
    override
    def invalidated: Unit =
    {
      pseudoClassStateChanged(DockNode.MAXIMIZED_PSEUDO_CLASS,get);

      if (borderPane != null)
      {
        borderPane.pseudoClassStateChanged(DockNode.MAXIMIZED_PSEUDO_CLASS,get);
      }

      stage.setMaximized(get);

      // TODO: This is a work around to fill the screen bounds and not overlap the task bar when
      // the window is undecorated as in Visual Studio. A similar work around needs applied for
      // JFrame in Swing. http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4737788
      // Bug report filed:
      // https://bugs.openjdk.java.net/browse/JDK-8133330
      if (get)
      {
        val bounds = Screen
            .getScreensForRectangle(stage.getX,stage.getY,stage.getWidth,stage.getHeight)
            .get(0)
            .getVisualBounds
        stage.setX     (bounds.getMinX)
        stage.setY     (bounds.getMinY)
        stage.setWidth (bounds.getWidth)
        stage.setHeight(bounds.getHeight)
      }
    }
  }

  val floatingProperty = new SimpleBooleanProperty(null,"floating",false)
  {
    override
    def invalidated() =
    {
      pseudoClassStateChanged(DockNode.FLOATING_PSEUDO_CLASS,get)

      if (borderPane != null)
      {
        borderPane.pseudoClassStateChanged(DockNode.FLOATING_PSEUDO_CLASS,get)
      }
    }
  }

  val dockedProperty = new SimpleBooleanProperty(null,"docked",false)
  {
    override
    def invalidated(): Unit =
    {
      if (get && dockTitleBar != null)
      {
        dockTitleBar.setVisible(true);
        dockTitleBar.setManaged(true);
      }

      pseudoClassStateChanged(DockNode.DOCKED_PSEUDO_CLASS,get);
    }
  }

  private var stageStyle             = StageStyle.TRANSPARENT;
  private var stage  : Stage         = _
  private var borderPane: BorderPane = _
  private var dockPane: DockPane     = _
  private var dockTitleBar           = new DockTitleBar(this);

  getChildren.addAll(dockTitleBar,contents)
  VBox.setVgrow(contents,Priority.ALWAYS)
  getStyleClass.add("dock-node")

  def setStageStyle(s: StageStyle): Unit =
  {
    stageStyle = s
  }

  def setDockTitleBar(dtb: DockTitleBar): Unit =
  {
    if (dtb != null)
    {
      if (dockTitleBar != null)
      {
        getChildren.set(getChildren.indexOf(dockTitleBar),dtb)
      }
      else
      {
        getChildren.add(0,dtb)
      }
    }
    else
    {
      getChildren.remove(dockTitleBar)
    }

    dockTitleBar = dtb
  }

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

      if (isDocked)
      {
        undock()
      }

      stage = new Stage();
      stage.titleProperty().bind(titleProperty)

      if (dockPane!=null && dockPane.getScene != null
       && dockPane.getScene.getWindow != null)
      {
        stage.initOwner(dockPane.getScene.getWindow)
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

        stagePosition = floatScene.add(new Point2D(owner.getX,owner.getY))
      }
      else
      {
        stagePosition = floatScreen
      }

      if (translation != null)
      {
        stagePosition = stagePosition.add(translation)
      }

      // the border pane allows the dock node to
      // have a drop shadow effect on the border
      // but also maintain the layout of contents
      // such as a tab that has no content
      borderPane = new BorderPane()
      borderPane.getStyleClass.add("dock-node-border")
      borderPane.setCenter(this)

      val scene = new Scene(borderPane)

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

      if (this.isStageResizable)
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
      floatingProperty.set(floating);
      stage.removeEventFilter(MouseEvent.MOUSE_PRESSED, this);
      stage.removeEventFilter(MouseEvent.MOUSE_MOVED,   this);
      stage.removeEventFilter(MouseEvent.MOUSE_DRAGGED, this);
      stage.close();
    }
  }

  def getDockPane(): DockPane         = dockPane
  def getDockTitleBar(): DockTitleBar = dockTitleBar
  def getStage() : Stage              = stage
  def getBorderPane() :BorderPane     = borderPane
  def getGraphic(): Node              = graphicProperty.get
  def getTitle(): String              = titleProperty.get
  def isCustomTitleBar(): Bool        = customTitleBarProperty.get
  def isFloating(): Bool              = floatingProperty.get
  def isFloatable(): Bool             = floatableProperty.get
  def isClosable(): Bool              = closableProperty.get
  def isDocked(): Bool                = dockedProperty.get
  def isStageResizable()              = stageResizableProperty.get
  def isMaximized() : Bool            = maximizedProperty.get
  def isDecorated(): Bool             = stageStyle!=StageStyle.TRANSPARENT && stageStyle!=StageStyle.UNDECORATED;

  def setTitle(title: String)            = titleProperty.setValue(title)
  def setStageResizable(resizable: Bool) = stageResizableProperty.set(resizable)
  def setClosable(closable: Bool):Unit   = closableProperty.set(closable)
  def setGraphic(graphic: Node): Unit    = graphicProperty.setValue(graphic)
  def setMaximized(maximized: Bool): Unit= maximizedProperty.set(maximized)

  def setUseCustomTitleBar(useCustomTitleBar: Bool): Unit =
  {
    if (this.isFloating)
    {
      dockTitleBar.setVisible(useCustomTitleBar);
      dockTitleBar.setManaged(useCustomTitleBar);
    }

    customTitleBarProperty.set(useCustomTitleBar);
  }

  def setFloatable(floatable: Bool): Unit =
  {
    if (!floatable && this.isFloating)
    {
      this.setFloating(false)
    }

    floatableProperty.set(floatable)
  }

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
    if (dockPane != null)
    {
      dockPane.undock(this);
    }

    dockedProperty.set(false);
  }

  def close() : Unit =
  {
    if (isFloating)
    {
      setFloating(false)
    }
    else
    if (isDocked)
    {
      undock()
    }
  }

  private var sizeLast: Point2D = _
  private var sizeWest  = false
  private var sizeEast  = false
  private var sizeNorth = false
  private var sizeSouth = false;

  def isMouseResizeZone(): Bool =
  {
    sizeWest || sizeEast || sizeNorth || sizeSouth;
  }

  def onMousePressed(e: MouseEvent): Unit =
  {
    sizeLast = new Point2D(e.getScreenX,e.getScreenY)
  }

  def onMouseMoved(e: MouseEvent): Unit =
  {
    var cursor = Cursor.DEFAULT

    val insets = borderPane.getPadding

    sizeWest  = e.getX < insets.getLeft
    sizeNorth = e.getY < insets.getTop
    sizeEast  = e.getX > borderPane.getWidth  - insets.getRight
    sizeSouth = e.getY > borderPane.getHeight - insets.getBottom

    if (sizeWest)
    {
      if (sizeNorth)
      {
        cursor = Cursor.NW_RESIZE;
      }
      else
      if (sizeSouth)
      {
        cursor = Cursor.SW_RESIZE;
      }
      else
      {
        cursor = Cursor.W_RESIZE;
      }
    }
    else
    if (sizeEast)
    {
      if (sizeNorth)
      {
        cursor = Cursor.NE_RESIZE;
      }
      else
      if (sizeSouth)
      {
        cursor = Cursor.SE_RESIZE;
      }
      else
      {
        cursor = Cursor.E_RESIZE;
      }
    }
    else
    if (sizeNorth)
    {
      cursor = Cursor.N_RESIZE;
    }
    else
    if (sizeSouth)
    {
      cursor = Cursor.S_RESIZE;
    }

    getScene.setCursor(cursor)
  }

  def onMouseDragged(e: MouseEvent): Unit =
  {
    val sizeCurrent = new Point2D(e.getScreenX,e.getScreenY)
    val sizeDelta   = sizeCurrent.subtract(sizeLast);

    var newX     = stage.getX
    var newY     = stage.getY
    var newWidth = stage.getWidth
    var newHeight= stage.getHeight

    if (sizeNorth)
    {
      newHeight -= sizeDelta.getY
      newY      += sizeDelta.getY
    }
    else
    if (sizeSouth)
    {
      newHeight += sizeDelta.getY
    }

    if (sizeWest)
    {
      newWidth -= sizeDelta.getX
      newX     += sizeDelta.getX
    }
    else
    if (sizeEast)
    {
      newWidth += sizeDelta.getX
    }

    // TODO: find a way to do this synchronously and eliminate the flickering of moving the stage
    // around, also file a bug report for this feature if a work around can not be found this
    // primarily occurs when dragging north/west but it also appears in native windows and Visual
    // Studio, so not that big of a concern.
    // Bug report filed:
    // https://bugs.openjdk.java.net/browse/JDK-8133332

    var currentX = sizeLast.getX
    var currentY = sizeLast.getY

    if (newWidth >= stage.getMinWidth)
    {
      stage.setX(newX);
      stage.setWidth(newWidth)
      currentX = sizeCurrent.getX
    }

    if (newHeight >= stage.getMinHeight)
    {
      stage.setY(newY);
      stage.setHeight(newHeight)
      currentY = sizeCurrent.getY
    }

    sizeLast = new Point2D(currentX,currentY)

    // we do not want the title bar getting these events
    // while we are actively resizing
    if (isMouseResizeZone)
    {
      e.consume()
    }
  }

  def handle(e: MouseEvent): Unit =
  {
    if (isFloating && isStageResizable)
    {
      e.getEventType match
      {
        case MouseEvent.MOUSE_PRESSED                      ⇒ onMousePressed(e)
        case MouseEvent.MOUSE_MOVED                        ⇒ onMouseMoved(e)
        case MouseEvent.MOUSE_DRAGGED if isMouseResizeZone ⇒ onMouseDragged(e)
        case _ ⇒
      }
    }
  }
}

//****************************************************************************
