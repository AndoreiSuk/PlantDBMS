// PARA SA VIEW PLANT DETAILS INFORMATION NA TAB

package main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ViewPlantDetailsController {
    @FXML private Label plantNameTitleLabel; 
    @FXML private ImageView plantImageView;
    @FXML private Label plantIdField;
    @FXML private Label gardenNameField;
    @FXML private Label datePlantedField;
    @FXML private Label growthStatusField;
    @FXML private Label healthStatusField;
    @FXML private Label wateringFreqField;
    @FXML private Label sunlightReqField;
    @FXML private Label soilTypeField;
    @FXML private Label fertilizingSchedField;
    @FXML private Label toxicityField;
    @FXML private Button closeButton;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, uuuu");
    private final String gardenFilePath = "src/main/resources/csv_files/garden.csv"; // Pathfile to garden.csv

    private String fetchGardenNameFromCsv(String gardenGid) {
        if (gardenGid == null || gardenGid.trim().isEmpty()) {
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(gardenFilePath))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    String currentGid = parts[0].trim();
                    String currentName = parts[1].trim();
                    if (currentGid.equals(gardenGid)) {
                        return currentName; 
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading garden.csv to fetch garden name: " + e.getMessage());
        }
        return null; 
    }

    // Formatting the Garden Name (e.g., Fruits Garden (G-0004))
    private String formatDisplayGardenName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "N/A"; 
        }
        String trimmedName = name.trim();
        if (!trimmedName.toLowerCase().endsWith(" garden") && !trimmedName.startsWith("G-")) { 
            return trimmedName + " Garden";
        }
        return trimmedName;
    }

    public void populateDetails(Plant plant, CareInstruction careInstruction) {
        if (plant != null) {
            plantNameTitleLabel.setText(plant.getPlantName() != null ? plant.getPlantName() : "Unknown Plant");
            plantIdField.setText((plant.getPlantID() != null ? plant.getPlantID() : "N/A")); 
            
            String gardenGid = plant.getGardenID();
            String finalGardenDisplay = "N/A";
            if (gardenGid != null && !gardenGid.isEmpty()) {
                String actualGardenName = fetchGardenNameFromCsv(gardenGid);
                if (actualGardenName != null && !actualGardenName.isEmpty()) {
                    finalGardenDisplay = formatDisplayGardenName(actualGardenName) + " (" + gardenGid + ")";
                } else {
                    finalGardenDisplay = formatDisplayGardenName(gardenGid) + " (ID Only)"; 
                }
            }
            gardenNameField.setText(finalGardenDisplay);

            if (plant.getDatePlanted() != null) {
                datePlantedField.setText(plant.getDatePlanted().format(dateFormatter));
            } else {
                datePlantedField.setText("N/A");
            }
            growthStatusField.setText(plant.getGrowthStatus() != null ? plant.getGrowthStatus() : "N/A");
            healthStatusField.setText(plant.getHealthStatus() != null ? plant.getHealthStatus() : "N/A");
            
            try {
                String imgPath = plant.getPlantImg();
                Image image = null;
                if (imgPath != null && !imgPath.trim().isEmpty()) {
                    if (imgPath.startsWith("/")) {
                        java.net.URL imgUrl = getClass().getResource(imgPath);
                        if (imgUrl != null) image = new Image(imgUrl.toExternalForm());
                    } else if (new java.io.File(imgPath).exists()){
                        image = new Image("file:" + imgPath);
                    }
                }
                if (image == null || image.isError()) { 
                     image = new Image(getClass().getResource("/img_files/photo1.jpg").toExternalForm());
                }
                plantImageView.setImage(image);
            } catch (Exception e) {
                // Simplified error handling for image load
                System.err.println("Error loading plant image for details view (Path: " + plant.getPlantImg() + "): " + e.getMessage());
                try {
                    plantImageView.setImage(new Image(getClass().getResource("/img_files/photo1.jpg").toExternalForm()));
                } catch (Exception ex) {
                    // Fallback image itself failed
                }
            }
        }

        if (careInstruction != null) {
            wateringFreqField.setText(careInstruction.getWateringFreq() != null ? careInstruction.getWateringFreq() : "N/A");
            sunlightReqField.setText(careInstruction.getSunlightReq() != null ? careInstruction.getSunlightReq() : "N/A");
            soilTypeField.setText(careInstruction.getSoilType() != null ? careInstruction.getSoilType() : "N/A");
            fertilizingSchedField.setText(careInstruction.getFertilizingSched() != null ? careInstruction.getFertilizingSched() : "N/A");
            toxicityField.setText(careInstruction.getToxicity() != null ? careInstruction.getToxicity() : "N/A");
        } else {
            wateringFreqField.setText("N/A");
            sunlightReqField.setText("N/A");
            soilTypeField.setText("N/A");
            fertilizingSchedField.setText("N/A");
            toxicityField.setText("N/A");
        }
    }
    
    @FXML
    private void handleCloseButtonAction() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
