package neoreborn.ui

import javafx.beans.binding.Bindings
import javafx.beans.property.{DoubleProperty, IntegerProperty, SimpleDoubleProperty, SimpleIntegerProperty}
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.TextField
import javafx.scene.effect.Glow
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Paint
import javafx.scene.shape.Line
import javafx.util.converter.NumberStringConverter

class GEdgeRep(parent: GraphRep, vSrc : GVertexRep, weighted : Boolean = false, dir: Boolean = false, angleDeg: Int = 30) extends Object {

  //static radius of vertex rep
  val vertexRadius = 17.5

  //arrow shaft and its angle
  private val shaftRep : Line = new Line
  private val shaftAngle = Bindings.createDoubleBinding(() => getShaftAngle, shaftRep.startXProperty, shaftRep.endXProperty, shaftRep.startYProperty, shaftRep.startYProperty)
  private def getShaftAngle: Double =         //gets main angle
  {
    if (shaftRep.startXProperty.get == shaftRep.endXProperty.get)
      return Math.PI / 2
    return Math.atan((shaftRep.endYProperty.get - shaftRep.startYProperty.get) / (shaftRep.endXProperty.get - shaftRep.startXProperty.get))
  }

  //arrow head for directed graphs and its angle, length
  private val headLeftRep : Line = new Line
  private val headRightRep : Line = new Line
  private val angle : Double = Math.toRadians(angleDeg)
  private val headLength = Bindings.createDoubleBinding(() => Math.signum(shaftRep.getStartX - shaftRep.getEndX) * 12, shaftRep.startXProperty, shaftRep.endXProperty)

  //weight rep and properties
  private val weightRep : TextField = new TextField
  private val weight : IntegerProperty = new SimpleIntegerProperty(0)
  def weightProperty = weight
  def getWeight = weight.get
  def setWeight(value : Int) = weight.set(value)

  //source and destination vertices
  private val src : GVertexRep = vSrc
  private var dest : GVertexRep = _
  def getSource = src
  def getDestination = dest

  //select, deselect, init, delete
  def init(color : String) {
    shaftRep.setStartX(src.getX)
    shaftRep.setStartY(src.getY)
    src.toFront
    update(src.getX, src.getY)
    shaftRep.setStrokeWidth(2.0)
    shaftRep.setStroke(Paint.valueOf(color))
    if (dir)
      {
        headLeftRep.startXProperty.bind(shaftRep.endXProperty)
        headLeftRep.startYProperty.bind(shaftRep.endYProperty)
        headRightRep.startXProperty.bind(shaftRep.endXProperty)
        headRightRep.startYProperty.bind(shaftRep.endYProperty)
        headLeftRep.setStrokeWidth(2.0)
        headRightRep.setStrokeWidth(2.0)
        headLeftRep.setStroke(Paint.valueOf(color))
        headRightRep.setStroke(Paint.valueOf(color))
        parent.getChildren.add(headLeftRep)
        parent.getChildren.add(headRightRep)
        headLeftRep.endXProperty.bind(Bindings.add(Bindings.createDoubleBinding(() => headLength.get * Math.cos(shaftAngle.get + angle), headLength, shaftAngle), shaftRep.endXProperty))
        headLeftRep.endYProperty.bind(Bindings.add(Bindings.createDoubleBinding(() => headLength.get * Math.sin(shaftAngle.get + angle), headLength, shaftAngle), shaftRep.endYProperty))
        headRightRep.endXProperty.bind(Bindings.add(Bindings.createDoubleBinding(() => headLength.get * Math.cos(shaftAngle.get - angle), headLength, shaftAngle), shaftRep.endXProperty))
        headRightRep.endYProperty.bind(Bindings.add(Bindings.createDoubleBinding(() => headLength.get * Math.sin(shaftAngle.get - angle), headLength, shaftAngle), shaftRep.endYProperty))
        headLeftRep.toBack
        headRightRep.toBack
      }
    if (weighted) {
      weightRep.setStyle("-fx-font-size: 12; -fx-text-fill: #00E000;")
      weightRep.setPrefHeight(20)
      weightRep.setPrefWidth(50)
      weightRep.setAlignment(Pos.CENTER)//Paint.valueOf("#00E000"))
      weightRep.setText(weightProperty.get.toString)
      weightProperty.bind(Bindings.createIntegerBinding(() => try { weightRep.getText.toInt } catch { case _ : Exception => 0}, weightRep.textProperty))
      weightProperty.addListener((x, o, n) => parent.onEdgeUpdateWeightRequested(this, n.intValue))
      weightRep.setEditable(true)
      weightRep.setCursor(Cursor.DEFAULT)
      weightRep.setEffect(new Glow(0.3))
      weightRep.setVisible(false)
      parent.getChildren.add(weightRep)
    }
    shaftRep.setOnMouseClicked(onMouseClicked)
    parent.getChildren.add(shaftRep)
    shaftRep.toBack
  }
  def select: Unit =
  {
    shaftRep.setStrokeWidth(4.0)
    shaftRep.setEffect(new Glow(0.9))
    if (weighted) {
      weightProperty.bind(Bindings.createIntegerBinding(() => try { weightRep.getText.toInt } catch { case _ : Exception => 0}, weightRep.textProperty))
      //weightRep.setVisible(true)
    }
  }
  def deselect =
  {
    shaftRep.setStrokeWidth(2.0)
    shaftRep.setEffect(null)
    if (weighted) {
      weightProperty.unbind
      weightRep.setText(weightProperty.get.toString)
    }
  }
  var isDeleted = false
  def delete =
  {
    parent.getChildren.removeAll(shaftRep, headLeftRep, headRightRep)
    if (weighted)
      parent.getChildren.remove(weightRep)
    isDeleted = true
  }

  //coordinates, update and commit
  def endXProperty = shaftRep.endXProperty
  def endYProperty = shaftRep.endYProperty
  def update(x: Double, y: Double) =
  {
    shaftRep.setEndX(x)
    shaftRep.setEndY(y)
  }
  def commit(destVertex : GVertexRep): Unit =
  {
    dest = destVertex
    val x = destVertex.getX
    val y = destVertex.getY
    val diff_x = x - shaftRep.getStartX
    val diff_y = y - shaftRep.getStartY
    val diff = Math.sqrt(diff_x * diff_x + diff_y * diff_y)
    update(x - diff_x * vertexRadius / diff, y - diff_y * vertexRadius / diff)
    if (weighted)
    {
      weightRep.setLayoutX((getDestination.getX + getSource.getX - weightRep.getPrefWidth - Math.sin(getShaftAngle) * 30) / 2)
      weightRep.setLayoutY((getDestination.getY + getSource.getY - weightRep.getPrefHeight + Math.cos(getShaftAngle) * 30) / 2)
      weightRep.setRotate(getShaftAngle.toDegrees)
      weightRep.setVisible(true)
    }
  }

  //to front and to back
  def toFront =
  {
    shaftRep.toFront
    headLeftRep.toFront
    headRightRep.toFront
    weightRep.toFront
  }
  def toBack =
  {
    shaftRep.toBack
    headLeftRep.toBack
    headRightRep.toBack
    weightRep.toBack
  }

  //custom userdata
  private var userdata : Int = 0
  def getUserdata = userdata
  def setUserdata(value : Int) = userdata = value

  //events
  def onMouseClicked(e : MouseEvent): Unit = parent.onEdgeSelectRequested(this, e)
}
