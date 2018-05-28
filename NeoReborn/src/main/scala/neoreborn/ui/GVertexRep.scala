package neoreborn.ui

import javafx.beans.binding.Bindings
import javafx.beans.property.{DoubleProperty, IntegerProperty, SimpleDoubleProperty, SimpleIntegerProperty}
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.effect.Glow

class GVertexRep(parent : GraphRep, x : Double, y : Double, idx : Int) extends Button{

  //vertex index
  private val index : IntegerProperty = new SimpleIntegerProperty(idx)
  def indexProperty = index
  def getIndex = index.get
  def setIndex(idx : Int) = index.set(idx)

  //select, deselect, init, delete
  def init =
  {
    setPrefWidth(35)
    setPrefHeight(35)
    setLayoutX(x - 17.5)
    setLayoutY(y - 17.5)
    XProperty.bind(Bindings.createDoubleBinding(() => getLayoutX + 17.5, layoutXProperty))
    YProperty.bind(Bindings.createDoubleBinding(() => getLayoutY + 17.5, layoutYProperty))
    setStyle("-fx-text-fill: #00E000; -fx-background-color: #202020; -fx-border-radius: 100; -fx-background-radius: 34;")
    setOnMouseClicked(e => parent.onVertexMouseClicked(this, e))
    setOnMouseMoved(e => parent.fireEvent(e))
    setAlignment(Pos.CENTER)
    textProperty.bind(Bindings.convert(indexProperty))
    parent.getChildren.add(this)

  }
  def select =
  {
    setStyle("-fx-background-color: #00E000; -fx-text-fill: #202020")
    setEffect(new Glow(0.3))
  }
  def deselect =
  {
    setStyle("-fx-background-color: #202020; -fx-text-fill: #00E000")
    setEffect(null)
  }
  def delete= parent.getChildren.remove(this)

  //position
  val XProperty : DoubleProperty = new SimpleDoubleProperty(x)
  val YProperty : DoubleProperty = new SimpleDoubleProperty(y)
  def getX = XProperty.get
  def getY = YProperty.get
  def setX(value : Double) = setLayoutX(value - 17.5)
  def setY(value : Double) = setLayoutY(value - 17.5)

  //rep color
  def setColor(color : String) = setStyle("-fx-background-color: #202020; -fx-border-color: " + color + "; -fx-text-fill: " + color + ";")
}
