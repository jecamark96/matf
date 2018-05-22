package neoreborn

import java.io.PrintWriter
import java.net.URL
import java.util.{ArrayList, ResourceBundle}

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader, Initializable}
import javafx.scene.Scene
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.{FileChooser, Modality, Stage}
import neoreborn.action._
import neoreborn.base.Graph
import neoreborn.ui.dialog.DialogController
import neoreborn.ui.GraphInfoRep

import scala.collection.mutable.ArrayBuffer

class MainWindow extends Initializable {

  //FXML fields
  @FXML
  var newButton : Button = _
  @FXML
  var repTemplate : AnchorPane = _
  @FXML
  var mainAP : AnchorPane = _
  @FXML
  var testLabel : Label = _
  @FXML
  var infoContainerAP : AnchorPane = _
  @FXML
  var advNameLb : Label = _
  @FXML
  var vertexInfoAp : AnchorPane = _
  @FXML
  var edgeInfoAp : AnchorPane = _
  @FXML
  var vertexInfoAdjListLw : ListView[Int] = _
  @FXML
  var actionCb : ComboBox[GAction] = _
  @FXML
  var actionAp : AnchorPane = _
  @FXML
  var actionInfoAp : AnchorPane = _
  @FXML
  var executionAp : AnchorPane = _
  @FXML
  var mainButtonAp : AnchorPane = _


  //selected graph control
  var currentGraphProperty : ObjectProperty[GraphControl] = new SimpleObjectProperty[GraphControl](null)
  def getCurrentGraph = currentGraphProperty.get
  def setCurrentGraph(ctrl : GraphControl) = currentGraphProperty.set(ctrl)

  //list of available graphs
  val graphs : ArrayList[GraphControl] = new ArrayList[GraphControl]
  def addGraph(graph : Graph) : GraphControl =
  {
    val control = new GraphControl(this, graphs.size, graph)
    control.setRep(mainAP, repTemplate)
    control.setInfoRep(infoContainerAP, new FXMLLoader(getClass.getResource("/fxml/ActiveInfoRepTemplate.fxml")).load.asInstanceOf[AnchorPane])
    control.setStaticReps(vertexInfoAp, edgeInfoAp)
    graphs.add(control)
    setCurrentGraph(control)
    return control
  }

  //action
  def getSelectedAction = actionCb.getSelectionModel.getSelectedItem

  //events
  @FXML
  def onNewRequested(e: ActionEvent): Unit =
  {
    if (graphs.size == 13)
      {
        val alert = new Alert(AlertType.INFORMATION)
        alert.setTitle("Info")
        alert.setHeaderText(null)
        alert.setContentText("Maximum number of graphs reached.")
        alert.getDialogPane.getStylesheets.add(getClass.getResource("src/main/resources/dialog.css").toExternalForm)
        alert.showAndWait
        return
      }
    val graph = createDialog[Graph]("New Graph", "/fxml/NewGraphPrompt.fxml")
    if (graph == null)
      return
    addGraph(graph)
  }
  @FXML
  def onLoadRequested(e : ActionEvent): Unit =
  {
    val res : (Graph, ArrayBuffer[(Double, Double)]) = createDialog("Load Graph", "/fxml/LoadGraphPrompt.fxml")
    if (res._1 == null)
      return
    val control = addGraph(res._1)
    control.deserializeGraph(res._2)
  }
  @FXML
  def onSaveRequested(e : ActionEvent): Unit =
  {
    if (getCurrentGraph == null) return
    val fc: FileChooser = new FileChooser
    fc.getExtensionFilters.addAll(new ExtensionFilter("Text files (*.txt)", "*.txt"), new ExtensionFilter("All files (*.*)", "*.*"))
    val file = fc.showSaveDialog(mainAP.getScene.getWindow)
    if (file == null) return
    val pw = new PrintWriter(file)
    pw.print(getCurrentGraph.serializeGraph)
    pw.close
  }
  @FXML
  def onDeleteRequested(e : ActionEvent): Unit =
  {
    if (getCurrentGraph == null) return
    var gIdx = graphs.indexOf(getCurrentGraph)
    graphs.remove(getCurrentGraph)
    getCurrentGraph.delete(mainAP, infoContainerAP)
    for (i <- 1 to graphs.size)
      graphs.get(i - 1).setIndex(i - 1)
    if (graphs.size == 0)
      {
        setCurrentGraph(null)
        return
      }
    gIdx = if (gIdx > 0) gIdx - 1 else 0
    setCurrentGraph(graphs.get(gIdx))
  }
  @FXML
  def onInfoRepSelectRequested(src : GraphInfoRep): Unit = {
    if (src.getControl == getCurrentGraph) return
    getCurrentGraph.deselect
    setCurrentGraph(src.getControl)
    getCurrentGraph.select
  }
  @FXML
  def onDeleteVertexRequested(e : ActionEvent): Unit =
  {
    if (getCurrentGraph != null)
      getCurrentGraph.deleteSelectedVertex
  }
  @FXML
  def onDeleteEdgeRequested(e : ActionEvent) : Unit =
  {
    if (getCurrentGraph != null)
      getCurrentGraph.deleteSelectedEdge
  }

  //dialog
  def createDialog[T](title : String, fxml : String): T =
  {
    val loader : FXMLLoader = new FXMLLoader(getClass.getResource(fxml))
    val stage : Stage = new Stage
    val scene : Scene = new Scene(loader.load.asInstanceOf[AnchorPane])
    val controller : DialogController[T] = loader.getController.asInstanceOf[DialogController[T]]
    stage.setTitle(title)
    stage.initModality(Modality.WINDOW_MODAL)
    stage.initOwner(mainAP.getScene.getWindow)
    stage.setScene(scene)
    stage.showAndWait
    return controller.getResult
  }

  //activate, deactivate
  def activate: Unit =
  {
    infoContainerAP.setDisable(false)
    mainButtonAp.setDisable(false)
  }
  def deactivate : Unit =
  {
    infoContainerAP.setDisable(true)
    mainButtonAp.setDisable(true)
  }

  //init
  def initialize(url: URL, rb: ResourceBundle): Unit = {
    val actionList : ObservableList[GAction] = FXCollections.observableArrayList[GAction](new GDfs("/fxml/DfsInfoRep.fxml"),
      new GBfs("/fxml/BfsInfoRep.fxml"), new GTC("/fxml/TCInfoRep.fxml"), new GDijkstra("/fxml/DijkstraInfoRep.fxml"))
    actionCb.setItems(actionList)
    actionCb.getSelectionModel.selectedItemProperty.addListener((x, o, n) => {
      if (o != null)
        o.deselect(actionInfoAp, actionAp, executionAp)
      n.select(actionInfoAp, actionAp, executionAp)
      n.setGraph(getCurrentGraph)
    })
    currentGraphProperty.addListener((x, o, n) =>
    {
      if (o != null)
        o.deselect
      if (n != null) {
        n.select
        actionInfoAp.setDisable(false)
        actionAp.setDisable(false)
        if (getSelectedAction != null)
          getSelectedAction.setGraph(n)
      } else
        {
          actionInfoAp.setDisable(true)
          actionAp.setDisable(true)
          executionAp.setDisable(true)
          if (getSelectedAction != null) {
            getSelectedAction.stop
            getSelectedAction.deselect(actionInfoAp, actionAp, executionAp)
          }
        }
    })
  }
}