package main;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/homeScene.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setStage(primaryStage); 

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Greenly");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

//ELIA TO DO:
// fix the bug on row1 garden.csv (last priority)
// add functionality on search bar ---- line 244 on GardenSceneController.java
// create new scene for plant log 
// add time sa date added


//ANDREY TO DO:
// find a way unsaon pagsort sa plants
// create functionality to edit the plant
// add a plant card to the gridpane
// add move feature. if plant is moved to another garden, remove it from the old garden and add it to the new garden (ONLY IF MAKAYA)



//RANDOM NGA GIATAY
//ngano di madelete and gardenList.csv, plantList.csv, ug careInstructionsList.csv YAWA
//ignore sa na, ill fix that later
//use garden.csv, plant.csv, and plant_care_instructions.csv 
