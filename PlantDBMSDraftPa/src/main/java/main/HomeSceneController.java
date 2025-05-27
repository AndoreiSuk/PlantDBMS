// THIS IS THE CONTROLLER FOR THE HOME PAGE


package main;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;


public class HomeSceneController extends MainController {

    private String title = "Greenly";
    @FXML private Button homeButton;
    @FXML private Button plantButton;
    @FXML private Button gardenButton;
    @FXML private Button startNowButton;

    @FXML public void openPlantScene() throws IOException{
        System.out.println("Opening plant scene...");
        loadScene(plantPath);
    }

    @FXML public void openGardenScene() throws IOException {
        System.out.println("Opening garden scene...");
        loadScene(gardenPath);
    }

    public void initialize() {
        this.stage = stage;
        System.out.println("Home scene initialized.");  
    }


}
