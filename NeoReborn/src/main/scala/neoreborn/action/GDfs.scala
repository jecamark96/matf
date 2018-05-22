package neoreborn.action

import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.{ColorPicker, ListView, TextField}

class GDfs(graphRepPath : String) extends GAction(graphRepPath){
  val vertexLb : TextField = rep.lookup("#vertexLb").asInstanceOf[TextField]
  val visitedLw : ListView[Int] = rep.lookup("#visitedLw").asInstanceOf[ListView[Int]]
  val treeColorPick : ColorPicker = rep.lookup("#treeColorPick").asInstanceOf[ColorPicker]

  val visitedList : ObservableList[Int] = FXCollections.observableArrayList()
  visitedLw.setItems(visitedList)

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

  def getColor = "#" + treeColorPick.getValue.toString.substring(2)

  def updateDfs(idxBegin : Int, idxEnd : Int, edgeType: Int): Boolean =
  {
    val beginX = graphRep(idxBegin).getX
    val beginY = graphRep(idxBegin).getY
    val endX = graphRep(idxEnd).getX
    val endY = graphRep(idxEnd).getY
    Platform.runLater(() => graphRep(idxBegin).setColor(getColor))
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
    Platform.runLater(() => {
      if (edgeType == 0) {
        graphRep.commitTempEdgeRep(graphRep(idxEnd))
        visitedList.add(idxEnd)
      }
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
        graphRep(getStartingVertex).setColor(getColor)
        graphRep.addNavigator(getStartingVertex, getColor)
        visitedList.add(getStartingVertex)
      })
      try {
        base.dfs(updateDfs, getStartingVertex)
      } catch {
        case _ : InterruptedException => clear
      }
    }

  override def clear: Unit =
  {
    Platform.runLater(() => {
      visitedList.clear
      graphRep.removeTemporaryEdgeReps()
      graphRep.removeNavigator
      graphRep.repaintVertexReps
    })
    super.clear
  }

  override def toString: String = "Dfs"

  override def canRun: Boolean = getStartingVertex != -1
}
