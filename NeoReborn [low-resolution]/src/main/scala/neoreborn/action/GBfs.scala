package neoreborn.action

import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.{ColorPicker, ListView, TextField}

class GBfs(repPath : String) extends GAction(repPath) {
  val vertexLb : TextField = rep.lookup("#vertexLb").asInstanceOf[TextField]
  val visitedLw : ListView[Int] = rep.lookup("#visitedLw").asInstanceOf[ListView[Int]]
  val queuedLw : ListView[Int] = rep.lookup("#queuedLw").asInstanceOf[ListView[Int]]
  val visitedColorPick : ColorPicker = rep.lookup("#visitedColorPick").asInstanceOf[ColorPicker]
  val queuedColorPick : ColorPicker = rep.lookup("#queuedColorPick").asInstanceOf[ColorPicker]

  val visitedList : ObservableList[Int] = FXCollections.observableArrayList()
  visitedLw.setItems(visitedList)
  val queuedList : ObservableList[Int] = FXCollections.observableArrayList()
  queuedLw.setItems(queuedList)

  def getStartingVertex : Int =
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

  def getVisitedColor = "#" + visitedColorPick.getValue.toString.substring(2)
  def getQueuedColor = "#" + queuedColorPick.getValue.toString.substring(2)

  override protected def runAction: Unit =
  {
    Platform.runLater(() => {
      graphRep(getStartingVertex).setColor(getVisitedColor)
      queuedList.add(getStartingVertex)
      graphRep.addNavigator(getStartingVertex, getVisitedColor)
    })
    try {
      base.bfs(updateBfs, getStartingVertex)
    } catch {
      case _ : InterruptedException => clear
    }
  }

  private def updateBfs(idxBegin : Int, idxEnd : Int, edgeType: Int): Boolean =
  {
    val beginX = graphRep(idxBegin).getX
    val beginY = graphRep(idxBegin).getY
    val endX = graphRep(idxEnd).getX
    val endY = graphRep(idxEnd).getY
    if (edgeType == 2) {
      Platform.runLater(() => {
        visitedList.add(idxBegin)
        queuedList.remove(0)
        graphRep(idxBegin).setColor(getVisitedColor)
      })
      return true
    }
    if (edgeType == 0)
      Platform.runLater(() => {
      graphRep.addTempEdgeRep(idxBegin, getVisitedColor)
      })
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
        graphRep.commitTempEdgeRep(graphRep(idxEnd))
        queuedList.add(idxEnd)
        graphRep(idxEnd).setColor(getQueuedColor)
      }
      )
    if (getAutoPause) {
      pausedProperty.set(true)
      pause
      pausedProperty.set(false)
    }
    return true
  }

  override def clear: Unit =
  {
    Platform.runLater(() => {
      visitedList.clear
      queuedList.clear
      graphRep.removeTemporaryEdgeReps()
      graphRep.removeNavigator
      graphRep.repaintVertexReps
    })
    super.clear
  }

  override def toString = "Bfs"

  override def canRun: Boolean = getStartingVertex != -1
}
