package neoreborn.action

import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.{ColorPicker, ListView, TextField}

import scala.collection.mutable

class GDijkstra(repPath : String) extends GAction(repPath) {
  val vertexLb : TextField = rep.lookup("#vertexLb").asInstanceOf[TextField]
  val queueLw : ListView[(Int, Int)] = rep.lookup("#queueLw").asInstanceOf[ListView[(Int, Int)]]
  val distancesLw : ListView[Int] = rep.lookup("#distancesLw").asInstanceOf[ListView[Int]]
  val indexesLw : ListView[Int] = rep.lookup("#indexesLw").asInstanceOf[ListView[Int]]
  val traverseColorPick : ColorPicker = rep.lookup("#traverseColorPick").asInstanceOf[ColorPicker]

  val distances : ObservableList[Int] = FXCollections.observableArrayList()
  val indexes : ObservableList[Int] = FXCollections.observableArrayList()
  val queue : ObservableList[(Int, Int)] = FXCollections.observableArrayList()
  distancesLw.setItems(distances)
  indexesLw.setItems(indexes)
  queueLw.setItems(queue)

  def setQueue(pq : List[(Int, Int)]): Unit =
  {
    queue.clear
    pq.map(x => queue.add(x))
  }

  def getStartingVertex: Int =
  {
    try {
      val idx = vertexLb.getText.toInt
      if (idx >= 0 && base.getSize > idx)
        return idx
    } catch {
      case _ : Exception =>
    }
    Platform.runLater(() => setError("Invalid starting vertex."))
    return -1
  }

  def getColor = "#" + traverseColorPick.getValue.toString.substring(2)

  def updateDijkstra(idxBegin : Int, idxEnd : Int, edgeType: Int, weight : Int, pq : mutable.PriorityQueue[(Int, Int)]): Boolean =
  {
    val beginX = graphRep(idxBegin).getX
    val beginY = graphRep(idxBegin).getY
    val endX = graphRep(idxEnd).getX
    val endY = graphRep(idxEnd).getY
    val queueList = pq.toList
    if (edgeType == -1) {
      Platform.runLater(() => setQueue(queueList))  //update queue popped
      return true
    }
    if (edgeType == -2) {                   //update closest vertex chosen, set final weight
      Platform.runLater(() =>{
        setQueue(queueList)
        distances.set(idxBegin, weight)
        distancesLw.refresh
        graphRep(idxBegin).setColor(getColor)
    })
      return true
    }
    if (edgeType == 0) Platform.runLater(() => graphRep.addTempEdgeRep(idxBegin, getColor))
    for (i <- 1 to 200) {
      Platform.runLater(() =>{
        graphRep.updateNavigator(beginX + (endX - beginX) * i.toDouble / 200, beginY + (endY - beginY) * i.toDouble / 200)
        graphRep(idxBegin).toFront
        graphRep(idxEnd).toFront
      })
      Thread.sleep(10 - getSpeed)
      if (!checkState) return false
    }
    if (edgeType == 0)
    Platform.runLater(() => {
        setQueue(queueList)
        graphRep.commitTempEdgeRep(graphRep(idxEnd))
        distances.set(idxEnd, base.getEdgeWeight(idxBegin, idxEnd) + weight)
        distancesLw.refresh
        graphRep(idxEnd).setColor(getColor)
      })
    if (getAutoPause) {
      pausedProperty.set(true)
      pause
      pausedProperty.set(false)
    }
    return true
  }

  override def runAction: Unit =
  {
    Platform.runLater(() => {
      graphRep.addNavigator(getStartingVertex, getColor)
      for (i <- 1 to base.getSize) {
        indexes.add(i - 1)
        distances.add(-1)
      }
    })
    try {
      base.dijkstra(updateDijkstra, getStartingVertex)
    } catch {
      case _ : InterruptedException => clear
    }
  }

  override def clear: Unit =
  {
    Platform.runLater(() => {
      graphRep.removeTemporaryEdgeReps()
      graphRep.removeNavigator
      graphRep.repaintVertexReps
      queue.clear
      distances.clear
      indexes.clear
    })
    super.clear
  }

  override def toString: String = "Dijkstra"

  override def canRun: Boolean =
    {
      if (!base.isWeighted) {
        Platform.runLater(() => setError("Dijkstra can only be run on weighted graphs."))
        return false
      }
      getStartingVertex!= -1
    }
}
