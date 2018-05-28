package neoreborn

import java.util

import javafx.beans.binding.Bindings
import javafx.beans.property._
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.{CheckBox, Label, ListView}
import javafx.scene.layout.AnchorPane
import neoreborn.base.Graph
import neoreborn.ui.{GraphInfoRep, GraphRep}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class GraphControl(parent: MainWindow, _idx: Int, base : Graph) extends Object
{
  //control index and methods
  private val indexProperty : IntegerProperty = new SimpleIntegerProperty(_idx)
  def getIndex = indexProperty.get
  def setIndex(newIdx : Int): Unit = indexProperty.set(newIdx)

  //graph
  def getGraph = base

  //graph rep
  private var rep : GraphRep = _
  def getRep = rep
  def setRep(repParent: AnchorPane, repTemplate : AnchorPane) =
  {
    rep = new GraphRep(repTemplate, base)
    repParent.getChildren.add(rep)
  }

  //info rep
  private var info : GraphInfoRep = _
  def getInfoRep = info
  def setInfoRep(infoParent : AnchorPane, infoTemplate : AnchorPane) =
  {
    info = new GraphInfoRep(this, infoParent, infoTemplate, base)
    info.setLayoutX(1)
    infoParent.getChildren.add(info)
    info.translateYProperty.bind(Bindings.createDoubleBinding(() => getIndex * (infoTemplate.getPrefHeight - 1) + 3, indexProperty))
  }

  //vertex info rep and edge info rep
  private var vertexInfoRep : AnchorPane = _
  private var edgeInfoRep : AnchorPane = _
  def setStaticReps(_vertexInfoRep : AnchorPane, _edgeInfoRep : AnchorPane): Unit =
  {
    vertexInfoRep = _vertexInfoRep
    edgeInfoRep = _edgeInfoRep
    selectedVertexProperty.bind(Bindings.createIntegerBinding(() => if (rep.getCurrentVertex == null) -1 else rep.getCurrentVertex.getIndex, rep.currentVertexProperty, base.edgeCountProperty))
    selectedVertexProperty.addListener((x, o, n) => if (n.intValue != -1) setSelectedVertexAdjList(base.getAdjacentEdges(n.intValue).asJavaCollection))
    base.edgeCountProperty.addListener((x, o, n) => if (getSelectedVertex != -1) setSelectedVertexAdjList(base.getAdjacentEdges(getSelectedVertex).asJavaCollection))
    selectedEdgeProperty.bind(Bindings.createObjectBinding[(Integer, Integer)](() => if (rep.getCurrentEdge == null) (-1, -1) else (rep.getCurrentEdge.getSource.getIndex, rep.getCurrentEdge.getDestination.getIndex), rep.currentEdgeProperty))
  }
  def onInfoRepSelectRequested(infoRep: GraphInfoRep) = parent.onInfoRepSelectRequested(infoRep)      //selects info rep

  //select, deselect, bind, unbind, delete
  def select: Unit =              //selects control
  {
    bind
    rep.setVisible(true)
    info.setActive(true)
  }
  def deselect =            //deselects control
  {
    unbind
    rep.setVisible(false)
    info.setActive(false)
  }
  def delete(repParent : AnchorPane, infoParent: AnchorPane) =    //deletes this control, removes info rep and graph rep
  {
    deselect
    repParent.getChildren.remove(rep)
    infoParent.getChildren.remove(info)
  }
  private def bind: Unit =      //binds static info reps to this control
  {
    val vertexLb : Label = vertexInfoRep.lookup("#vertexInfoIndexLb").asInstanceOf[Label]
    val adjListView : ListView[Int] = vertexInfoRep.lookup("#vertexInfoAdjListLw").asInstanceOf[ListView[Int]]
    vertexLb.textProperty.bind(Bindings.createStringBinding(() => if (getSelectedVertex == -1) "No vertex selected" else "Vertex " + getSelectedVertex, selectedVertexProperty))
    adjListView.setItems(selectedVertexAdjList)
    vertexInfoRep.disableProperty.bind(Bindings.createBooleanBinding(() => getSelectedVertex == -1, selectedVertexProperty))

    val edgeLb : Label = edgeInfoRep.lookup("#edgeInfoLb").asInstanceOf[Label]
    edgeLb.textProperty.bind(Bindings.createStringBinding(() => if (getSelectedEdge._1 == -1) "No edge selected" else "Edge: [" + getSelectedEdge._1 + (if (base.isDirected) " → " else " ↔ ")  + getSelectedEdge._2 + "]", selectedEdgeProperty))
    edgeInfoRep.disableProperty.bind(Bindings.createBooleanBinding(() => getSelectedEdge._1 == -1, selectedEdgeProperty))
  }
  private def unbind: Unit =    //unbinds static info reps from this control
  {
    val vertexLb : Label = vertexInfoRep.lookup("#vertexInfoIndexLb").asInstanceOf[Label]
    val adjListView : ListView[Int] = vertexInfoRep.lookup("#vertexInfoAdjListLw").asInstanceOf[ListView[Int]]
    vertexLb.textProperty.unbind
    adjListView.setItems(null)
    vertexInfoRep.disableProperty.unbind
    vertexLb.setText("No vertex selected")
    vertexInfoRep.setDisable(true)

    val edgeLb : Label = edgeInfoRep.lookup("#edgeInfoLb").asInstanceOf[Label]
    edgeLb.textProperty.unbind
    edgeInfoRep.disableProperty.unbind
    edgeLb.setText("No edge selected")
    edgeInfoRep.setDisable(true)
  }

  //enable editing, disable editing
  def enableEditing =
  {
    bind
    rep.activate
    parent.activate
  }
  def disableEditing: Unit =
  {
    unbind
    rep.deactivate
    parent.deactivate
  }

  //selected vertex and methods
  private val selectedVertex: IntegerProperty = new SimpleIntegerProperty(-1)
  def selectedVertexProperty = selectedVertex
  def getSelectedVertex : Integer = selectedVertex.get
  def deleteSelectedVertex =
  {
    base.removeVertex(getSelectedVertex)
    rep.deleteVertexRep(getSelectedVertex)
  }

  //adjacency list of selected vertex and methods
  private val selectedVertexAdjList : ObservableList[Int] = FXCollections.observableArrayList[Int]
  final def getSelectedVertexAdjList = selectedVertexAdjList
  final def setSelectedVertexAdjList(adjList : util.Collection[Int]) =
  {
    selectedVertexAdjList.clear
    selectedVertexAdjList.addAll(adjList)
  }

  //selected edge and methods
  private val selectedEdge : ObjectProperty[(Integer, Integer)] = new SimpleObjectProperty[(Integer, Integer)]((-1, -1))
  def selectedEdgeProperty = selectedEdge
  def getSelectedEdge : (Integer, Integer) = selectedEdge.get
  def deleteSelectedEdge =
  {
    base.removeEdge(getSelectedEdge._1, getSelectedEdge._2)
    rep.removeCurrentEdgeRep
  }

  //deserialize and serialize graph
  def deserializeGraph(layout : ArrayBuffer[(Double, Double)]) ={
    var idx = 0
    layout.map { case (x, y) => {
      rep.addVertexRep(x * rep.getMinWidth, y * rep.getMinHeight, idx)
      idx += 1
    } }
    base.getEdges.map{case(idx_a, idx_b) => rep.addEdgeRep(idx_a, idx_b, if (base.isWeighted) base.getEdgeWeight(idx_a, idx_b) else -1)}
  }
  def serializeGraph : String =
  {
    val sb : StringBuilder = new StringBuilder
    sb ++= base.getName + System.lineSeparator
    if (base.isDirected)
      sb += 'D'
    if (base.isWeighted)
      sb += 'W'
    sb ++= 'C' + System.lineSeparator
    sb ++= base.vertexCountProperty.get + System.lineSeparator
    for (i <- 1 to base.vertexCountProperty.get)
      sb ++= rep(i - 1).getX / rep.getWidth + " " + rep(i - 1).getY / rep.getHeight + System.lineSeparator
    sb ++= base.edgeCountProperty.get + System.lineSeparator
    base.getEdges.map(idx => sb ++= (idx._1 + " " + idx._2 + (if (base.isWeighted) " " + base.getEdgeWeight(idx._1, idx._2).toString else "") + System.lineSeparator))
    return sb.toString
  }

}
