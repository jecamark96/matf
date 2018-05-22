package neoreborn.ui

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import neoreborn.base.{GVertex, Graph}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class GraphRep(template : AnchorPane, base : Graph) extends AnchorPane {

  //constructor - set event handlers and basic graphic attributes
  setOnMouseClicked(onMouseClicked)
  setOnMouseMoved(onMouseMoved)
  setLayoutX(template.getLayoutX)
  setLayoutY(template.getLayoutY)
  setMinHeight(template.getHeight)
  setMinWidth(template.getWidth)
  setStyle(template.getStyle)

  //drawing edge flag and value
  private var isDrawingEdge = false
  private var currentDrawnEdge: GEdgeRep = _

  //default color of edges and vertices
  private var defaultColor : String = "#00E000"

  //reps of vertices and methods
  private val vertices : ArrayBuffer[GVertexRep] = new ArrayBuffer[GVertexRep]
  def apply(idx : Int) : GVertexRep = vertices(idx)                 //gets vertex rep at index
  def addVertexRep(x : Double, y : Double, idx : Int): Unit =       //adds vertex rep at coordinates
  {
    val vRep = new GVertexRep(this, x, y, idx)
    vertices += vRep
    vRep.init
  }
  def deleteVertexRep(idx : Int): Unit =                            //deletes vertex rep along with edge reps
  {
    val vRep = vertices(idx)
    val toDel = edges.filter(edge => edge.getSource == vRep || edge.getDestination == vRep)
    toDel.map(x => x.delete)
    edges --= toDel
    vertices -= vRep
    vertices.filter(v => v.getIndex > idx).map(v => v.setIndex(v.getIndex - 1))
    vRep.delete
    setCurrentVertex(null)
    if (getCurrentEdge != null && getCurrentEdge.isDeleted)
      setCurrentEdge(null)
  }
  def repaintVertexReps: Unit =  vertices.map(v => v.setColor(defaultColor))
  def selectVertex(idx : Int) : Unit = selectVertex(this(idx))      //selects vertex with index idx
  private def selectVertex(vRep : GVertexRep): Unit =                       //selects vertex rep
  {
    if (getCurrentVertex != null)
      getCurrentVertex.deselect
    vRep.select
    setCurrentVertex(vRep)
  }

  //reps of edges and methods
  private val edges : ListBuffer[GEdgeRep] = new ListBuffer[GEdgeRep]
  def addEdgeRep(idx_a : Int, idx_b : Int, weight : Int = 0) : Unit = {
    val rep = new GEdgeRep(this, this(idx_a), base.isWeighted, base.isDirected, 25)
    rep.setWeight(weight)
    rep.init("#00E000")
    rep.commit(this(idx_b))
    edges += rep
  }           //adds edge rep from vertex rep idx_a to vertex rep idx_b
  def removeCurrentEdgeRep: Unit =                                //removes rep of the selected edge
  {
    getCurrentEdge.delete
    setCurrentEdge(null)
  }

  //reps of temporary edges and methods
  private var tempEdges : ListBuffer[GEdgeRep] = new ListBuffer[GEdgeRep]
  def addTempEdgeRep(srcIdx : Int, color : String, userdata : Int = 0): Unit =
  {
    val rep = new GEdgeRep(this, this(srcIdx), false, base.isDirected, 25)
    rep.init(color)
    rep.setUserdata(userdata)
    rep.endXProperty.bind(navigator.centerXProperty)
    rep.endYProperty.bind(navigator.centerYProperty)
    rep.toFront
    tempEdges.+=:(rep)
  }
  def commitTempEdgeRep(vRep : GVertexRep) =                     //commits temp edge rep to its destination vertex reps
  {
    tempEdges.head.endXProperty.unbind
    tempEdges.head.endYProperty.unbind
    tempEdges.head.commit(vRep)
  }
  def removeTemporaryEdgeReps(userdata : Int = 0): Unit =                                //removes reps of all temporary edges
  {
    if (userdata == 0) {
      tempEdges.map(edge => edge.delete)
      tempEdges.clear
    }
    else {
      tempEdges.map(edge => if (edge.getUserdata == userdata) edge.delete)
      tempEdges = tempEdges.filter(edge => edge.getUserdata != userdata)
    }
  }

  //navigator and methods
  private var navigator : Circle = _
  def addNavigator(srcIdx : Int, color : String) =   //adds navigator at coordinates painted with color
  {
      navigator = new Circle
      navigator.setRadius(5)
      navigator.setFill(Paint.valueOf(color))
      navigator.setCenterX(this(srcIdx).getX)
      navigator.setCenterY(this(srcIdx).getY)
      getChildren.add(navigator)
  }
  def updateNavigator(x : Double, y : Double): Unit =                                                           //updates navigator position
  {
    navigator.setCenterX(x)
    navigator.setCenterY(y)
  }
  def setNavigatorColor(color : String) = navigator.setFill(Paint.valueOf(color))
  def removeNavigator: Unit =                                                                                 //removes navigator
    {
      getChildren.remove(navigator)
      navigator = null
    }

  //enable, deactivate
  def activate =
    {
      setMouseTransparent(false)
      if (getCurrentEdge != null)
        getCurrentEdge.select
      if (getCurrentVertex != null)
        getCurrentVertex.select
    }
  def deactivate =
  {
    setMouseTransparent(true)
    if (getCurrentEdge != null)
      getCurrentEdge.deselect
    if (getCurrentVertex != null)
      getCurrentVertex.deselect
  }

  //selected vertex rep property
  private val currentVertex : ObjectProperty[GVertexRep] = new SimpleObjectProperty[GVertexRep](null)
  def currentVertexProperty = currentVertex
  def getCurrentVertex = currentVertex.get
  def setCurrentVertex(v : GVertexRep) = currentVertex.set(v)

  //selected edge rep property
  private val currentEdge: ObjectProperty[GEdgeRep] = new SimpleObjectProperty[GEdgeRep](null)
  def currentEdgeProperty = currentEdge
  def getCurrentEdge = currentEdge.get
  def setCurrentEdge(e : GEdgeRep) = currentEdge.set(e)


  //events
  def onMouseClicked(me: MouseEvent): Unit =        //adds vertex if edge is not being drawn, deletes drawn edge otherwise
  {
    if (me.getButton == MouseButton.PRIMARY) {
      if (isDrawingEdge) {
        isDrawingEdge = false
        currentDrawnEdge.delete
        currentDrawnEdge = null
        return
      }
      if (me.getX < 25 || me.getY < 25 || Math.abs(me.getX - getWidth) < 25 || Math.abs(me.getY - getHeight) < 25) return
      val edgeMatches = edges.filter(e => getDistanceFromEdge(me.getX, me.getY, e) <= 50)
      if (!edgeMatches.isEmpty)
        {
          var closestEdge = edgeMatches.head
          edgeMatches.map(v => closestEdge = if (getDistanceFromEdge(me.getX, me.getY, closestEdge) < getDistanceFromEdge(me.getX, me.getY, v)) closestEdge else v)
          if (getCurrentEdge != null)
            getCurrentEdge.deselect
          setCurrentEdge(closestEdge)
          getCurrentEdge.select
          return
        }
      if (vertices.exists(v => getDistanceFromVertex(me.getX, me.getY, v) <= 50))
        return
      val vertex = new GVertex(base.getSize)
      addVertexRep(me.getX, me.getY, vertex.getIndex)
      base.addVertex(vertex)
    }
  }
  def onMouseMoved(me: MouseEvent): Unit =          //if edge is being drawn, updates its destination
  {
    if (!isDrawingEdge)
      return;
    currentDrawnEdge.update(me.getX, me.getY)
    }
  def onEdgeSelectRequested(rep : GEdgeRep, e : MouseEvent): Unit =       //selects edge rep
  {
    if (isDrawingEdge) return
    if (getCurrentEdge != null)
      getCurrentEdge.deselect
    setCurrentEdge(rep)
    rep.select
  }
  def onEdgeUpdateWeightRequested(rep : GEdgeRep, weight : Int): Unit =  base.updateEdgeWeight(rep.getSource.getIndex, rep.getDestination.getIndex, weight)       //updates edge weight
  def onVertexMouseClicked(rep : GVertexRep, me: MouseEvent): Unit =      //if edge is being drawn commits it to vertex, otherwise selects vertex on left click and starts drawing edge on right click
  {
    if (isDrawingEdge) {
      currentDrawnEdge.commit(rep)
      edges += currentDrawnEdge
      base.addEdge(currentDrawnEdge.getSource.getIndex, rep.getIndex)
      isDrawingEdge = false
    }
    else {
      if (me.getButton == MouseButton.PRIMARY)
        selectVertex(rep)
      else if (me.getButton == MouseButton.SECONDARY)
      {
        currentDrawnEdge = new GEdgeRep(this, rep, base.isWeighted, base.isDirected, 25)
        currentDrawnEdge.init("#00E000")
        isDrawingEdge = true
      }
    }
    me.consume
  }

  //distances
  private def getDistanceFromVertex(x : Double, y : Double, v : GVertexRep) = Math.sqrt(Math.pow(x - v.getX, 2) + Math.pow(y - v.getY, 2))    //gets distance of vertex rep v to (x, y) coordinates
  private def getDistanceFromEdge(x : Double, y : Double, e : GEdgeRep) : Double =                                                                    //gets distance of edge rep e to (x, y) coordinates
  {
    val x1 = e.getSource.getX
    val y1 = e.getSource.getY
    val x2 = e.getDestination.getX
    val y2 = e.getDestination.getY
    val p = Math.abs(((x1 - x) * (x2 - x1) + (y1 - y) * (y2 - y1)) / ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)))
    if (p >= 0 && p <= 1)
      {
        val x3 = x1 + (x2 - x1) * p
        val y3 = y1 + (y2 - y1) * p
        val res = Math.sqrt((x3 - x) * (x3 - x) + (y3 - y) * (y3 - y))
        return res
      }
    val res = Math.min(getDistanceFromVertex(x, y, e.getDestination), getDistanceFromVertex(x, y, e.getSource))
    return res
  }
}
