package neoreborn

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage

class Main extends Application {
  override def start(primaryStage: Stage) {
    primaryStage.setTitle("NeoReborn")
    val root : AnchorPane = new FXMLLoader(getClass.getResource("/fxml/MainWindow.fxml")).load()
    primaryStage.setScene(new Scene(root))
    primaryStage.setResizable(false)
    primaryStage.sizeToScene()
    primaryStage.show()
  }
}

object Main {
  def main(args: Array[String]) {
    Application.launch(classOf[Main], args: _*)
  }
}