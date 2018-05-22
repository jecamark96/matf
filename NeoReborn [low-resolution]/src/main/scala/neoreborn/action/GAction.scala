package neoreborn.action

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property._
import javafx.fxml.FXMLLoader
import javafx.scene.control._
import javafx.scene.layout.AnchorPane
import neoreborn.GraphControl
import neoreborn.base.Graph
import neoreborn.ui.GraphRep

abstract class GAction(repPath : String) extends Runnable{

  //control, graph and graph rep
  protected var ctrl : GraphControl = _
  protected var base : Graph = _
  protected var graphRep : GraphRep = _
  final def setGraph(newCtrl : GraphControl): Unit =
  {
    ctrl = newCtrl
    if (graphRep != null)
      clear
    base = ctrl.getGraph
    graphRep = ctrl.getRep
  }

  //algorithm pane
  protected val rep : AnchorPane = new FXMLLoader(getClass.getResource(repPath)).load[AnchorPane]

  //properties from action pane
  private val speedProperty : IntegerProperty = new SimpleIntegerProperty(0)
  final def getSpeed = speedProperty.get

  protected val autoPauseProperty : BooleanProperty = new SimpleBooleanProperty(false)
  def getAutoPause = autoPauseProperty.get

  protected val autoResetProperty : BooleanProperty = new SimpleBooleanProperty(false)
  def getAutoReset = autoResetProperty.get

  //execution flow flags
  protected var pauseFlag = false
  protected var interruptFlag = false

  //execution flow properties
  val pausedProperty : BooleanProperty = new SimpleBooleanProperty(false)
  def isPaused = pausedProperty.get
  val runningProperty : BooleanProperty = new SimpleBooleanProperty(false)
  def isRunning = runningProperty.get
  val finishedProperty =  new SimpleBooleanProperty(false)
  def isFinished = finishedProperty.get

  //execution flow methods
  def start =
    {
      val t : Thread = new Thread(this)
      t.setDaemon(true)
      t.start
    }
  def continue = this.synchronized
  {
    pauseFlag = false
    notify
  }
  def pause = this.synchronized { wait }
  def stop =
  {
    this.synchronized {
      if (isPaused)
        notify
    }
    interruptFlag = isRunning
  }

  //execution flag check
  final def checkState : Boolean =
  {
    if (interruptFlag)
      return false
    while (pauseFlag) {
      pausedProperty.set(true)
      pause
      if (interruptFlag)
      {
        pausedProperty.set(false)
        return false
      }
    }
    pausedProperty.set(false)
    return true
  }

  //base run and virtual run for subclasses, virtual parameter check
  override final def run: Unit =
  {
    if (!canRun) return
    Platform.runLater(() => setError(""))
    clear
    interruptFlag = false
    pauseFlag = false
    finishedProperty.set(false)
    runningProperty.set(true)
    Platform.runLater(() => ctrl.disableEditing)
    runAction
    if (getAutoReset)
      clear
    runningProperty.set(false)
    finishedProperty.set(true)
  }
  protected def runAction
  protected def canRun : Boolean

  //graphic clearing
  def clear = Platform.runLater(() => ctrl.enableEditing)

  //select and deselect as current action
  def select(parent : AnchorPane, actionPane : AnchorPane, executionPane : AnchorPane): Unit =
  {
    parent.getChildren.add(rep)
    val actionCb : ComboBox[GAction] = actionPane.lookup("#actionCb").asInstanceOf[ComboBox[GAction]]
    val speedSlider : Slider = actionPane.lookup("#speedSlider").asInstanceOf[Slider]
    val autoPauseCheck : CheckBox = actionPane.lookup("#autoPauseCheck").asInstanceOf[CheckBox]
    val autoResetCheck : CheckBox = actionPane.lookup("#autoResetCheck").asInstanceOf[CheckBox]
    actionCb.disableProperty.bind(runningProperty)
    speedProperty.bind(speedSlider.valueProperty)
    autoPauseCheck.selectedProperty.addListener((x, o, n) => autoPauseProperty.set(n))
    autoResetCheck.selectedProperty.addListener((x, o, n) => autoResetProperty.set(n))
    executionPane.setDisable(false)
    val startBtn : Button = executionPane.lookup("#startBtn").asInstanceOf[Button]
    val pauseBtn : Button  = executionPane.lookup("#pauseBtn").asInstanceOf[Button]
    val continueBtn : Button  = executionPane.lookup("#continueBtn").asInstanceOf[Button]
    val stopBtn : Button  = executionPane.lookup("#stopBtn").asInstanceOf[Button]
    val clearBtn : Button = executionPane.lookup("#clearBtn").asInstanceOf[Button]
    val errorLb : Label = executionPane.lookup("#executeErrorLb").asInstanceOf[Label]
    startBtn.disableProperty.bind(runningProperty)
    startBtn.setOnAction(e => start)
    pauseBtn.disableProperty.bind(Bindings.createBooleanBinding(() => isPaused || !isRunning, pausedProperty, runningProperty))
    pauseBtn.setOnAction(e => pauseFlag = true)
    continueBtn.disableProperty.bind(Bindings.createBooleanBinding(() => !isPaused, pausedProperty))
    continueBtn.setOnAction(e => continue)
    stopBtn.disableProperty.bind(Bindings.createBooleanBinding(() => !isRunning, runningProperty))
    stopBtn.setOnAction(e => stop)
    clearBtn.disableProperty.bind(runningProperty)
    clearBtn.setOnAction(e => clear)
    errorLb.textProperty.bind(errorProperty)
  }
  def deselect(parent : AnchorPane, actionPane : AnchorPane, executionPane : AnchorPane): Unit =
  {
    parent.getChildren.remove(rep)
    val actionCb : ComboBox[GAction] = actionPane.lookup("#actionCb").asInstanceOf[ComboBox[GAction]]
    actionCb.disableProperty.unbind
    executionPane.setDisable(true)
    val startBtn : Button = executionPane.lookup("#startBtn").asInstanceOf[Button]
    startBtn.setOnAction(null)
    val pauseBtn : Button  = executionPane.lookup("#pauseBtn").asInstanceOf[Button]
    pauseBtn.setOnAction(null)
    val continueBtn : Button  = executionPane.lookup("#continueBtn").asInstanceOf[Button]
    continueBtn.setOnAction(null)
    val stopBtn : Button  = executionPane.lookup("#stopBtn").asInstanceOf[Button]
    stopBtn.setOnAction(null)
    val clearBtn : Button = executionPane.lookup("#clearBtn").asInstanceOf[Button]
    clearBtn.setOnAction(null)
    val errorLb : Label = executionPane.lookup("#executeErrorLb").asInstanceOf[Label]
    startBtn.disableProperty.unbind
    pauseBtn.disableProperty.unbind
    continueBtn.disableProperty.unbind
    stopBtn.disableProperty.unbind
    clearBtn.disableProperty.unbind
    errorLb.textProperty.unbind
    clear
  }

  //error, incompatible parameters
  val errorProperty = new SimpleStringProperty("")
  def setError(err : String) = errorProperty.set(err)
  def getError = errorProperty.get
}
