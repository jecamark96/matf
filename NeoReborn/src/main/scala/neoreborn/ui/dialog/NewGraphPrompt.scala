package neoreborn.ui.dialog

import java.net.URL
import java.util.ResourceBundle

import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control._
import javafx.scene.layout.AnchorPane
import neoreborn.base.{DirectedGraph, Graph, UndirectedGraph}


class NewGraphPrompt extends Initializable with DialogController[Graph] {

  var result : Graph = null

  @FXML
  var directedCb: ComboBox[String] = _
  @FXML
  var weightedCb : ComboBox[String] = _
  @FXML
  var mainAp: AnchorPane = _
  @FXML
  var okBtn : Button = _
  @FXML
  var cancelBtn : Button = _
  @FXML
  var nameField : TextField = _
  @FXML
  var errorLb: Label = _

  @FXML
  def onCancelRequested(e: ActionEvent): Unit =
  {
    result = null
    mainAp.getScene.getWindow.hide
  }
  @FXML
  def onCreateRequested(e: ActionEvent): Unit =
  {
    if (directedCb.getSelectionModel.getSelectedIndex == -1 || weightedCb.getSelectionModel.getSelectedIndex == -1) {
      errorLb.setText("Graph types must be picked.")
      errorLb.setUserData(0)
      return
    }
    if ((nameField.getText.length <= 0))
      nameField.setText("Graph")
    val weighted = weightedCb.getSelectionModel.getSelectedIndex != 0
    result = if (directedCb.getSelectionModel.getSelectedIndex == 0) new UndirectedGraph(nameField.getText, weighted) else new DirectedGraph(nameField.getText, weighted)
    mainAp.getScene.getWindow.hide
  }

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    directedCb.setItems(FXCollections.observableArrayList("Undirected", "Directed"))
    weightedCb.setItems(FXCollections.observableArrayList("Unweighted", "Weighted"))
    errorLb.setUserData(0)
    nameField.textProperty.addListener((x, o, n) => {
      if (n.length > 8)
        errorLb.setText("Name of the graph should be no longer than 8 characters.")
      else if (n.length <= 0)
        errorLb.setText("Name of the graph must be set.")
      else if (errorLb.getUserData == 1)
        errorLb.setText("")
      errorLb.setUserData(1)
    })
  }

  override def getResult: Graph =
  {
    return result
  }
}
