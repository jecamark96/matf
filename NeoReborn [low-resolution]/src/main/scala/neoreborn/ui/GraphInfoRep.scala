package neoreborn.ui

import javafx.beans.binding.Bindings
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import neoreborn.GraphControl
import neoreborn.base.Graph

import scala.collection.JavaConverters._

class GraphInfoRep(control: GraphControl, parent: AnchorPane, template: AnchorPane, base : Graph) extends AnchorPane {
  setOnMouseClicked(onMouseClicked)
  getChildren.addAll(template.getChildren)
  setMinHeight(template.getPrefHeight)
  setMinWidth(template.getPrefWidth)
  setPrefHeight(template.getPrefHeight)
  setPrefWidth(template.getPrefWidth)
  setMaxHeight(template.getPrefHeight)
  setMinWidth(template.getPrefWidth)
  setEffect(template.getEffect)
  lookup("#nameLb").asInstanceOf[Label].textProperty.bind(base.graphNameProperty)
  lookup("#verticesLb").asInstanceOf[Label].textProperty.bind(Bindings.concat("Vertices: ", Bindings.convert(base.vertexCountProperty)))
  lookup("#edgesLb").asInstanceOf[Label].textProperty.bind(Bindings.concat("Edges: ", Bindings.convert(base.edgeCountProperty)))
  lookup("#directLb").asInstanceOf[Label].setText(if (base.isDirected) "→" else "↔")
  lookup("#weightedLb").asInstanceOf[Label].setText(if (base.isWeighted) "W" else "")

  var active : Boolean = false

  def onMouseClicked(e: MouseEvent): Unit =
  {
    control.onInfoRepSelectRequested(this)
  }

  def setActive(act : Boolean)=
  {
    active = act;
    if (active)
      {
        setStyle("-fx-background-color: #00E000; -fx-border-color: #00E000; -fx-border-width: 3;")
        for (node : Node <- getChildren.asScala)
          node.setStyle("-fx-text-fill: #202020")
        setOpacity(1)
      }
    else
    {
      setStyle("-fx-background-color: #202020; -fx-border-color: #00E000; -fx-border-width: 3;")
      for (node : Node <- getChildren.asScala)
        node.setStyle("-fx-text-fill: #00E000")
      setOpacity(0.6)
    }
  }

  def getControl = control
}
