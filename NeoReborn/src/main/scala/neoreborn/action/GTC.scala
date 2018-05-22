package neoreborn.action

import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.{ColorPicker, ListView, TextField}

class GTC(repPath : String) extends GAction(repPath) {
  val vertexLb : TextField = rep.lookup("#vertexLb").asInstanceOf[TextField]
  val reachableLw : ListView[Int] = rep.lookup("#reachableLw").asInstanceOf[ListView[Int]]
  val tcColorPick : ColorPicker = rep.lookup("#tcColorPick").asInstanceOf[ColorPicker]
  val dfsColorPick : ColorPicker = rep.lookup("#dfsColorPick").asInstanceOf[ColorPicker]

  def getDfsColor = "#" + dfsColorPick.getValue.toString.substring(2)
  def getTcColor = "#" + tcColorPick.getValue.toString.substring(2)
  def getColor(edgeType : Int): String = edgeType match {
    case 0 => getDfsColor
    case 1 => getDfsColor
    case -1 => getTcColor
    case _ => ""
  }

  val reachableList : ObservableList[Int] = FXCollections.observableArrayList()
  reachableLw.setItems(reachableList)

  def updateTc(idxBegin : Int, idxEnd : Int, edgeType: Int): Boolean =
  {
    val beginX = graphRep(idxBegin).getX
    val beginY = graphRep(idxBegin).getY
    val endX = graphRep(idxEnd).getX
    val endY = graphRep(idxEnd).getY
    if (edgeType == 255) {
      Platform.runLater(() =>graphRep.removeTemporaryEdgeReps(1))
      return true
    }
    if (edgeType == 256) {
      Platform.runLater(() => {
        vertexLb.setText(idxBegin.toString)
        graphRep.repaintVertexReps
        graphRep(idxBegin).setColor(getTcColor)
        reachableList.clear
      })
      return true
    }
    if (edgeType < 1) Platform.runLater(() => graphRep.addTempEdgeRep(idxBegin, getColor(edgeType), if (edgeType == 0) 1 else 0))
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
      if (edgeType < 1)
        graphRep.commitTempEdgeRep(graphRep(idxEnd))
      if (edgeType == 0)
        reachableList.add(idxEnd)
      if (edgeType == 0)
        graphRep(idxEnd).setColor(getColor(edgeType))
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
    try {
      base.transitiveClosure(updateTc)
      Platform.runLater(() => {
        graphRep.addNavigator(0, getDfsColor)
        graphRep.repaintVertexReps
        reachableList.clear
        vertexLb.setText("")
      })
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
      reachableList.clear
      vertexLb.setText("")
    })
    super.clear
  }

  override def toString: String = "Transitive closure"

  override def canRun: Boolean = true
}
