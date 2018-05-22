package neoreborn.ui.dialog

import java.io.File
import java.util.Scanner

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, TextArea}
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import neoreborn.base.{DirectedGraph, GVertex, Graph, UndirectedGraph}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class LoadGraphPrompt extends DialogController[(Graph, ArrayBuffer[(Double, Double)])]{
  var gResult : Graph = _
  var cResult : ArrayBuffer[(Double, Double)] = new ArrayBuffer[(Double, Double)]

  @FXML
  var mainTa : TextArea = _
  @FXML
  var okBtn : Button = _
  @FXML
  var cancelBtn : Button = _
  @FXML
  var mainAp : AnchorPane = _
  @FXML
  var errorLb : Label = _

  @FXML
  def onCreateRequested(e : ActionEvent) = {
    cResult.clear
    gResult = null
    val sr: Scanner = new Scanner(mainTa.getText)
    try {
      val name: String = sr.nextLine
      val flags: String = sr.nextLine
      val directed = flags.contains('D')
      val weighted = flags.contains('W')
      val layout = flags.contains('C')
      val graph: Graph = if (directed) new DirectedGraph(name, weighted) else new UndirectedGraph(name, weighted)
      val vCount = sr.nextInt
      for (i <- 1 to vCount) {
        graph.addVertex(new GVertex(i - 1))
        cResult += (if (layout) (sr.nextDouble, sr.nextDouble) else (0.025 + Random.nextDouble * 0.95, 0.025 + Random.nextDouble * 0.95))
      }
      val eCount = sr.nextInt
      for (i <- 1 to eCount)
          graph.addEdge(sr.nextInt, sr.nextInt, if (weighted) sr.nextInt else 0)
      gResult = graph
      mainAp.getScene.getWindow.hide
    } catch
    {
      case ex : IllegalArgumentException => errorLb.setText("Loops not supported. Remove any edges between same nodes.")
      case _ : Exception => errorLb.setText("Bad format.")
    }
    sr.close
  }
  @FXML
  def onCancelRequested(e : ActionEvent) ={
    gResult = null
    mainAp.getScene.getWindow.hide
  }
  @FXML
  def onLoadRequested(e : ActionEvent) : Unit ={
    val fc : FileChooser = new FileChooser
    fc.getExtensionFilters.addAll(new ExtensionFilter("Text files (*.txt)", "*.txt"), new ExtensionFilter("All files (*.*)", "*.*"))
    fc.setTitle("Load graph file")
    val file : File = fc.showOpenDialog(mainAp.getScene.getWindow)
    if (file == null)
      return
    if (!file.canRead)
      errorLb.setText("Cannot read from a file, make sure it's not open in another program and try again.")
    mainTa.clear
    val sr : Scanner = new Scanner(file)
    while (sr.hasNextLine)
      mainTa.appendText(sr.nextLine + System.lineSeparator)
    sr.close
  }

  override def getResult = (gResult, cResult)
}
