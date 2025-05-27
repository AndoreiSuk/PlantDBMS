// THIS IS THE CONTROLLER FOR THE PLANT PAGE
package main;

import java.io.File;
import java.io.BufferedReader; 
import java.io.FileReader;   
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class PlantSceneController extends MainController {

    @FXML private Text pageTitle;
    @FXML private Button homeButton;
    @FXML private Button gardenButton;
    @FXML private Button plantButton; 

    //PLANT INFO 
    @FXML private ComboBox<String> gardenName;
    @FXML private TextField plantImgFilePath;
    @FXML private Button browseImageButton; 
    @FXML private TextField plantName;
    @FXML private TextField growthStatus;
    @FXML private TextField healthStatus; 
    @FXML private DatePicker datePlanted;

    //PLANT CARE INSTRUCTION INFO
    @FXML private TextField wateringFreq;
    @FXML private TextField sunlightReq;
    @FXML private TextField soilType;
    @FXML private TextField fertilizingSched;
    @FXML private TextField toxicity;

    @FXML private Button addPlantButton;
    @FXML private Button updatePlantButton; 


    private Plant currentPlantToEdit;
    private CareInstruction currentCareInstructionToEdit;
    private boolean editMode = false;
    private String originalPlantIDForEdit; // To store the ID of the plant when BEING edited
    private String originalCareInstruIDForEdit; // To store the ID of the care instruction WHEN being edited
    
    private Map<String, String> gardenDisplayNameToIdMap; 

    public void initialize() {
        System.out.println("Plant scene initialized.");

        populateGardenComboBox();
        
        // If a garden was pre-selected (e.g., from GardenSceneController when clicking "+ Add Plant" in a specific garden tab)
        if (MainController.currentGardenForPlantAdd != null && !MainController.currentGardenForPlantAdd.isEmpty()) {
            String targetGid = MainController.currentGardenForPlantAdd;
            if (gardenDisplayNameToIdMap != null) { 
                for (Map.Entry<String, String> entry : gardenDisplayNameToIdMap.entrySet()) {
                    if (entry.getValue().equals(targetGid)) {
                        gardenName.setValue(entry.getKey()); 
                        break;
                    }
                }
            }
            MainController.currentGardenForPlantAdd = null; 
        }
        switchToMode(editMode); 
    }

    private void populateGardenComboBox() {
        gardenDisplayNameToIdMap = new HashMap<>();
        List<String> gardenDisplayList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gardenFilePath))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) { 
                    String gId = parts[0].trim();
                    String gName = parts[1].trim();
                    String displayName = gName; 
                    gardenDisplayList.add(displayName);
                    gardenDisplayNameToIdMap.put(displayName, gId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load garden list for plant form.");
        }
        gardenName.setItems(FXCollections.observableArrayList(gardenDisplayList));
    }


    private void switchToMode(boolean isEditMode) {
        this.editMode = isEditMode;
        if (isEditMode) {
            if (pageTitle != null) pageTitle.setText("Edit Plant");
            addPlantButton.setVisible(false);
            addPlantButton.setManaged(false);
            updatePlantButton.setVisible(true);
            updatePlantButton.setManaged(true);
        } else {
            if (pageTitle != null) pageTitle.setText("Add New Plant");
            addPlantButton.setVisible(true);
            addPlantButton.setManaged(true);
            updatePlantButton.setVisible(false);
            updatePlantButton.setManaged(false);
            gardenName.setDisable(false);
            clearPlantFormInternal(); 
        }
    }
    
    // This method is called from GardenSceneController when an "Edit" button on a plant card is clicked
    public void populateFormForEdit(Plant plant, CareInstruction careInstruction) {
        this.currentPlantToEdit = plant;
        this.currentCareInstructionToEdit = careInstruction;
        this.originalPlantIDForEdit = plant.getPlantID();
        this.originalCareInstruIDForEdit = careInstruction.getCareInstruID();
        String gardenGid = plant.getGardenID();
        String displayGardenName = "";
        if (gardenDisplayNameToIdMap != null) { 
            for(Map.Entry<String, String> entry : gardenDisplayNameToIdMap.entrySet()){
                if(entry.getValue().equals(gardenGid)){
                    displayGardenName = entry.getKey();
                    break;
                }
            }
        }
        gardenName.setValue(displayGardenName); 

        plantName.setText(plant.getPlantName());
        plantImgFilePath.setText(plant.getPlantImg());
        growthStatus.setText(plant.getGrowthStatus());
        healthStatus.setText(plant.getHealthStatus());
        if (plant.getDatePlanted() != null) {
            datePlanted.setValue(plant.getDatePlanted());
        } else {
            datePlanted.setValue(null);
        }

        if (careInstruction != null) {
            wateringFreq.setText(careInstruction.getWateringFreq());
            sunlightReq.setText(careInstruction.getSunlightReq());
            soilType.setText(careInstruction.getSoilType());
            fertilizingSched.setText(careInstruction.getFertilizingSched());
            toxicity.setText(careInstruction.getToxicity());
        }
        switchToMode(true); 
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Plant Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPEG Images (*.jpg, *.jpeg)", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(this.stage); 

        if (selectedFile != null) {
            plantImgFilePath.setText(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML 
    private void handleAddPlantAction(){ 
        addNewPlantDetails();
    }
    
    @FXML
    private void handleUpdatePlantAction() { 
        if (editMode) { 
            updateExistingPlant(); 
        } else {
            showAlert(Alert.AlertType.WARNING, "Mode Error", "Update button clicked, but not in edit mode. This shouldn't happen if UI is correct."); 
        }
    }
    
    // Handles the logic for adding a brand new plant for a specific garden
    private void addNewPlantDetails(){ 
        String selectedGardenDisplayName = gardenName.getValue(); // Get the selected garden
        String actualGardenGid = null;
        if (gardenDisplayNameToIdMap != null && selectedGardenDisplayName != null) {
            actualGardenGid = gardenDisplayNameToIdMap.get(selectedGardenDisplayName);
        }


        if (actualGardenGid == null || actualGardenGid.isEmpty()) { 
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a valid garden for the new plant."); 
            return; 
        }

        String plantImgFilePathString = plantImgFilePath.getText();  
        String plantNameString = plantName.getText();
        String growthStatusString = growthStatus.getText();
        String healthStatusString = healthStatus.getText();
        LocalDate datePlantedValue = datePlanted.getValue();
        String wateringFreqString = wateringFreq.getText();
        String sunlightReqString = sunlightReq.getText();
        String soilTypeString = soilType.getText();
        String fertilizingSchedString = fertilizingSched.getText();
        String toxicityString = toxicity.getText(); 
        
        // Validate all fields ^-^
        if (plantNameString.isEmpty() || 
            growthStatusString.isEmpty() || healthStatusString.isEmpty() || datePlantedValue == null 
            || wateringFreqString.isEmpty() || sunlightReqString.isEmpty() || soilTypeString.isEmpty() 
            || fertilizingSchedString.isEmpty() || toxicityString.isEmpty()) { 
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required plant details (except image, which is optional)."); 
            return; 
        }
        String datePlantedString = datePlantedValue.format(csvDateFormatter); 
        
        // Generating the unique IDs for the new plant and its care instructions
        String newPlantID = generateNewID(plantFilePath, 1, "P-"); 
        String newCareInstruID = generateNewID(plantCareInstruFilePath, 0, "C-");  

        /* Base from the care_instructions.csv format: 
           careInstruID,wateringFreq,sunlightReq,soilType,fertilizingSched,toxicity
           CSV Order for plant.csv: gardenId(G-XXXX),plantId(P-XXXX),careInstruId(C-XXXX),plantName,plantImgFilePath,growthStatus,healthStatus,datePlanted */
        String plantInfo = String.join(",", 
                actualGardenGid, 
                newPlantID, 
                newCareInstruID, 
                plantNameString,
                plantImgFilePathString, 
                growthStatusString, 
                healthStatusString, 
                datePlantedString);
        
        String plantCareInfo = String.join(",", 
                newCareInstruID, 
                wateringFreqString, 
                sunlightReqString,
                soilTypeString, 
                fertilizingSchedString, 
                toxicityString);
        
        // Save to the CSV files
        showAlertAndRegisterToTwoCSVs(
            plantInfo + "\n", plantFilePath, 
            plantCareInfo + "\n", plantCareInstruFilePath,  
            "Plant '" + plantNameString + "' added successfully to " + selectedGardenDisplayName + "!", 
            () -> { 
                try {
                    loadScene(gardenPath); 
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to garden view.");
                }
            } 
        );
    }
    
    // Handles the logic for updating the existing plant
    private void updateExistingPlant() { 
        if (currentPlantToEdit == null || currentCareInstructionToEdit == null || originalPlantIDForEdit == null || originalCareInstruIDForEdit == null) { 
            showAlert(Alert.AlertType.ERROR, "Error", "No plant data to update. Editing context might have been lost."); 
            return; 
        }
        
        // Get current values from the form fields like from the My Garden Tab
        String selectedGardenDisplayName = gardenName.getValue(); 
        String actualGardenGid = null;
        if (gardenDisplayNameToIdMap != null && selectedGardenDisplayName != null) {
            actualGardenGid = gardenDisplayNameToIdMap.get(selectedGardenDisplayName);
        }

        if (actualGardenGid == null || actualGardenGid.isEmpty()) { 
             showAlert(Alert.AlertType.ERROR, "Error", "Garden Name cannot be empty or invalid for update."); 
             return; 
        }

        String plantImgFilePathString = plantImgFilePath.getText();   
        String plantNameString = plantName.getText();
        String growthStatusString = growthStatus.getText();
        String healthStatusString = healthStatus.getText();
        LocalDate datePlantedValue = datePlanted.getValue();
        String wateringFreqString = wateringFreq.getText();
        String sunlightReqString = sunlightReq.getText();
        String soilTypeString = soilType.getText();
        String fertilizingSchedString = fertilizingSched.getText();
        String toxicityString = toxicity.getText();
        
        // Validate all fields
        if (plantNameString.isEmpty() || 
            growthStatusString.isEmpty() || healthStatusString.isEmpty() || datePlantedValue == null 
            || wateringFreqString.isEmpty() || sunlightReqString.isEmpty() || soilTypeString.isEmpty() 
            || fertilizingSchedString.isEmpty() || toxicityString.isEmpty()) { 
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields for update (except image, which is optional)."); 
            return; 
        }
        String datePlantedString = datePlantedValue.format(csvDateFormatter);

        String updatedPlantInfo = String.join(",", 
                actualGardenGid, 
                originalPlantIDForEdit, 
                originalCareInstruIDForEdit, 
                plantNameString,
                plantImgFilePathString, 
                growthStatusString, 
                healthStatusString, 
                datePlantedString);
        
        String updatedCareInfo = String.join(",", 
                originalCareInstruIDForEdit, 
                wateringFreqString, 
                sunlightReqString,
                soilTypeString, 
                fertilizingSchedString, 
                toxicityString);
        
        // Update the records in CSV files
        boolean plantUpdated = updateCsvRecord(plantFilePath, originalPlantIDForEdit, 1, updatedPlantInfo); // Assuming plantID is at index 1 in plant.csv
        boolean careUpdated = updateCsvRecord(plantCareInstruFilePath, originalCareInstruIDForEdit, 0, updatedCareInfo); // Assuming careInstruID is at index 0
        
        if (plantUpdated && careUpdated) { 
            showAlert(Alert.AlertType.INFORMATION, "Success", "Plant '" + plantNameString + "' updated successfully!"); 
            try {
                loadScene(gardenPath); 
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to garden view.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update plant details. One or more records not found or write error."); 
        }
        // Clear all the text fields and reset the form 
        switchToMode(false); 
    }
        
    private void clearPlantFormInternal() { 
        gardenName.setValue(null);
        plantImgFilePath.clear();
        plantName.clear();
        growthStatus.clear();
        healthStatus.clear();
        datePlanted.setValue(null);
        wateringFreq.clear();
        sunlightReq.clear();
        soilType.clear();
        fertilizingSched.clear();
        toxicity.clear();

        currentPlantToEdit = null; 
        currentCareInstructionToEdit = null;
        originalPlantIDForEdit = null;
        originalCareInstruIDForEdit = null;
    }

    private void idFormat(TextField textfield, String idType) { 
        textfield.textProperty().addListener((observable, oldValue, newValue) -> { 
            if (newValue == null || newValue.isEmpty() || !newValue.matches(idType+"-\\d{4}")) { 
                textfield.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-radius: 2px;"); 
            } else {
                textfield.setStyle(""); 
            }
        });
    }

    // checks for duplicates, this might be useful if you want to ensure unique plant names, for example.
    // but the thing here is the primary key uniqueness is handled by generated IDs.
    private boolean isValueTakenInCSV(String valueToCheck, int columnIndex, String filePath) { 
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) { 
            String line; 
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) { 
                String[] values = line.split(","); 
                if (values.length > columnIndex && values[columnIndex].trim().equalsIgnoreCase(valueToCheck.trim())) { 
                    return true; 
                }
            }
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
        return false; 
    }

    // Navigation method
    @FXML 
    public void openGardenScene() throws IOException { 
        System.out.println("Opening garden scene..."); 
        loadScene(gardenPath); 
    }
    
    // Navigation method
    @FXML 
    public void openHomeScene() throws IOException { 
        System.out.println("Opening home scene..."); // Corrected print statement
        loadScene(homePath); 
    }

    @FXML
    public void openPlantScene() throws IOException {
        System.out.println("My Plants (PlantScene) is already active or re-initializing.");
        if (this.editMode) { 
            switchToMode(false);
        }
    }
}
