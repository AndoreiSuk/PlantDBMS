// PARA SA GARDEN NA PAGE
package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GardenSceneController extends MainController implements javafx.fxml.Initializable {
    @FXML private Button homeButton;
    @FXML private Button gardenButton;
    @FXML private Button plantButton; 
    @FXML private Button addGardenButton;
    @FXML private Button updateGardenButton;
    @FXML private TabPane gardenTabPane;
    @FXML private Tab addGardenTab;
    @FXML private TextField gardenNameFieldForForm; 
    @FXML private DatePicker dateAddedFieldForForm;
    private String editingGardenID;
    private String originalEditingGardenName;
    private Map<String, Plant> plantCache = new HashMap<>();
    private Map<String, CareInstruction> careInstructionCache = new HashMap<>();
    private String currentGardenID;

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        System.out.println("Garden scene initialized.");
        refreshTabs(gardenFilePath);
        gardenTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getUserData() != null && newTab.getUserData() instanceof String) {
                currentGardenID = (String) newTab.getUserData();
                GridPane plantGrid = plantGridFromTab(newTab);
                TextField searchField = searchFieldFromTab(newTab);
                if (plantGrid != null) {
                    displayPlantsForGarden(currentGardenID, plantGrid, searchField != null ? searchField.getText() : "");
                }
            
            } else if (newTab != null && "addGardenTab".equals(newTab.getId())) {
                addGardenButton.setDisable(false);
                addGardenButton.setVisible(true);
                updateGardenButton.setDisable(true);
                updateGardenButton.setVisible(false);
                clearGardenForm();
            }
        });
    }

    private String formatDisplayGardenName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Unnamed Garden";
        }
        String trimmedName = name.trim();
        if (!trimmedName.toLowerCase().endsWith(" garden")) {
            return trimmedName + " Garden";
        }
        return trimmedName;
    }

    private GridPane plantGridFromTab(Tab tab) {
        if (tab != null && tab.getContent() instanceof AnchorPane) {
            AnchorPane rootAnchor = (AnchorPane) tab.getContent();
            if (!rootAnchor.getChildren().isEmpty() && rootAnchor.getChildren().get(0) instanceof VBox) {
                VBox mainVBox = (VBox) rootAnchor.getChildren().get(0);
                for (javafx.scene.Node childNode : mainVBox.getChildren()) {
                    if (childNode instanceof ScrollPane) {
                        ScrollPane scrollPane = (ScrollPane) childNode;
                        if (scrollPane.getContent() instanceof GridPane) {
                            return (GridPane) scrollPane.getContent();
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private TextField searchFieldFromTab(Tab tab) {
        if (tab != null && tab.getContent() instanceof AnchorPane) {
            AnchorPane rootAnchor = (AnchorPane) tab.getContent();
            if (!rootAnchor.getChildren().isEmpty() && rootAnchor.getChildren().get(0) instanceof VBox) {
                VBox mainVBox = (VBox) rootAnchor.getChildren().get(0);
                if (mainVBox.getChildren().size() > 0 && mainVBox.getChildren().get(0) instanceof VBox) {
                    VBox topSectionVBox = (VBox) mainVBox.getChildren().get(0);
                    if (topSectionVBox.getChildren().size() > 2 && topSectionVBox.getChildren().get(2) instanceof HBox) { 
                        HBox searchControlsHBox = (HBox) topSectionVBox.getChildren().get(2);
                        for(javafx.scene.Node node : searchControlsHBox.getChildren()){
                            if(node instanceof TextField && "gardenPlantSearchField".equals(node.getId())){
                                return (TextField) node;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private ComboBox<String> sortComboBoxFromTab(Tab tab) {
        if (tab != null && tab.getContent() instanceof AnchorPane) {
            AnchorPane rootAnchor = (AnchorPane) tab.getContent();
            if (!rootAnchor.getChildren().isEmpty() && rootAnchor.getChildren().get(0) instanceof VBox) {
                VBox mainVBox = (VBox) rootAnchor.getChildren().get(0);
                if (mainVBox.getChildren().size() > 0 && mainVBox.getChildren().get(0) instanceof VBox) {
                    VBox topSectionVBox = (VBox) mainVBox.getChildren().get(0);
                    if (topSectionVBox.getChildren().size() > 0 && topSectionVBox.getChildren().get(0) instanceof HBox) { 
                        HBox gardenInfoAndActionsHeader = (HBox) topSectionVBox.getChildren().get(0);
                        if (gardenInfoAndActionsHeader.getChildren().size() > 1 && gardenInfoAndActionsHeader.getChildren().get(1) instanceof HBox) {
                            HBox gardenAndPlantActionsHBox = (HBox) gardenInfoAndActionsHeader.getChildren().get(1);
                            for(javafx.scene.Node node : gardenAndPlantActionsHBox.getChildren()){
                                if(node instanceof ComboBox && "plantSortComboBox".equals(node.getId())){
                                    return (ComboBox<String>) node;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void loadAllPlantData() {
        plantCache.clear();
        careInstructionCache.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(plantCareInstruFilePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length >= 6) {
                    CareInstruction ci = new CareInstruction(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim(), data[4].trim(), data[5].trim());
                    careInstructionCache.put(ci.getCareInstruID(), ci);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading care instructions: " + e.getMessage());
        }
        try (BufferedReader br = new BufferedReader(new FileReader(plantFilePath))) {
            String line;
            String header = br.readLine(); 
            if (header == null || !header.trim().toLowerCase().equals("gardenid,plantid,careinstruid,plantname,plantimgfilepath,growthstatus,healthstatus,dateplanted")) {
                System.err.println("WARNING: plant.csv header is INCORRECT or MISSING! Expected: gardenId,plantId,careInstruId,plantName,plantImgFilePath,growthStatus,healthStatus,datePlanted");
            }
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] data = line.split(",", -1);
                if (data.length >= 8) {
                    try {
                        String csvGardenId = data[0].trim();
                        String csvPlantId = data[1].trim();
                        String csvCareInstruId = data[2].trim();
                        String csvPlantName = data[3].trim();
                        String csvPlantImgFilePath = data[4].trim();
                        String csvGrowthStatus = data[5].trim();
                        String csvHealthStatus = data[6].trim();
                        String csvDatePlantedStr = data[7].trim();
                        LocalDate plantedDate = null;
                        if (csvDatePlantedStr != null && !csvDatePlantedStr.isEmpty()) {
                            plantedDate = LocalDate.parse(csvDatePlantedStr, csvDateFormatter);
                        }
                        if (csvPlantId.isEmpty()) {
                            System.err.println("Skipping plant record at line " + lineNumber + " due to empty plantId: " + line);
                            continue;
                        }
                        
                        Plant plant = new Plant(csvPlantId, csvGardenId, csvCareInstruId, csvPlantName, csvPlantImgFilePath, csvGrowthStatus, csvHealthStatus, plantedDate);
                        plantCache.put(plant.getPlantID(), plant);
                        
                    } catch (DateTimeParseException e) {
                        System.err.println("Could not parse date for plant at line " + lineNumber + ": [" + (data.length > 7 ? data[7].trim() : "EMPTY_DATE_FIELD") + "] " + line + " - " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error processing plant record at line " + lineNumber + ": " + line + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed plant CSV line " + lineNumber + " (expected 8 columns, found " + data.length + "): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading plant data: " + e.getMessage());
        }
    }

    private List<Plant> getPlantsForGarden(String gardenID, String searchTerm) {
        if (plantCache.isEmpty() || careInstructionCache.isEmpty()) loadAllPlantData();
        Stream<Plant> plantStream = plantCache.values().stream()
                .filter(p -> gardenID.equals(p.getGardenID()));
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String lowerSearchTerm = searchTerm.trim().toLowerCase();
            plantStream = plantStream.filter(p -> (p.getPlantName() != null && p.getPlantName().toLowerCase().contains(lowerSearchTerm)) ||
                                                 (p.getPlantID() != null && p.getPlantID().toLowerCase().contains(lowerSearchTerm)) );
        }
        return plantStream.collect(Collectors.toList());
    }

    private void displayPlantsForGarden(String gardenID, GridPane targetPlantGrid, String searchTerm) {
        if (targetPlantGrid == null || gardenID == null) {
            return;
        }
        targetPlantGrid.getChildren().clear();
        List<Plant> plants = getPlantsForGarden(gardenID, searchTerm);
        String sortOption = null;
        ComboBox<String> sortComboBox = sortComboBoxFromTab(gardenTabPane.getSelectionModel().getSelectedItem());
        if (sortComboBox != null) {
            sortOption = sortComboBox.getValue();
        }
        // FOR SORTING FUNCTIONALITY
        if (sortOption != null) {
            switch (sortOption) {
                case "Sort by Name (A-Z)":
                    plants.sort(Comparator.comparing(Plant::getPlantName, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "Sort by Name (Z-A)":
                    plants.sort(Comparator.comparing(Plant::getPlantName, String.CASE_INSENSITIVE_ORDER).reversed());
                    break;
                case "Sort by Date Planted (Newest)":
                    plants.sort(Comparator.comparing(Plant::getDatePlanted, Comparator.nullsLast(LocalDate::compareTo)).reversed());
                    break;
                case "Sort by Date Planted (Oldest)":
                    plants.sort(Comparator.comparing(Plant::getDatePlanted, Comparator.nullsLast(LocalDate::compareTo)));
                    break;
            }
        }
        // Explicitly set maxCols to 4 for 4 plants per row
        int maxCols = 4;
        int col = 0;
        int row = 0;
        for (Plant plant : plants) {
            CareInstruction ci = careInstructionCache.get(plant.getCareInstruID());
            if (ci == null) {
                ci = new CareInstruction(plant.getCareInstruID(), "N/A", "N/A", "N/A", "N/A", "N/A");
            }
            VBox plantCard = createPlantCard(plant, ci, gardenID);
            targetPlantGrid.add(plantCard, col, row);
            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
    }

    // PLANT CARD
    private VBox createPlantCard(Plant plant, CareInstruction ci, String currentGardenGid) {
        if (plant == null) {
            System.err.println("Error in createPlantCard: plant object is null for garden " + currentGardenGid + ". Skipping card creation.");
            return new VBox();
        }
        VBox card = new VBox(5);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.1, 0, 1);");
        card.setPrefSize(158, 260);
        card.setAlignment(Pos.TOP_LEFT);
        ImageView imageView = new ImageView();
        try {
            String imgPath = plant.getPlantImg();
            Image imgToSet;
            if (imgPath != null && !imgPath.trim().isEmpty()) {
                if (imgPath.startsWith("/")) {
                    java.net.URL imgUrl = getClass().getResource(imgPath);
                    if (imgUrl != null) {
                        imgToSet = new Image(imgUrl.toExternalForm());
                        if(imgToSet.isError()) {
                            System.err.println("Error loading classpath image: " + imgPath + " for plant " + plant.getPlantName());
                            imgToSet = new Image(getClass().getResource("/img_files/photo1.jpg").toExternalForm());
                        }
                    } else {
                        System.err.println("Classpath resource not found: " + imgPath + " for plant " + plant.getPlantName());
                        imgToSet = new Image(getClass().getResource("/img_files/photo1.jpg").toExternalForm());
                    }
                } else if (new java.io.File(imgPath).exists()) {
                    imgToSet = new Image("file:" + imgPath);
                    if(imgToSet.isError()) {
                        System.err.println("Error loading filesystem image: " + imgPath + " for plant " + plant.getPlantName());
                        imgToSet = new Image(getClass().getResource("/img_files/photo1.jpg").toExternalForm());
                    }
                } else {
                    System.err.println("Image file not found (absolute/relative path): " + imgPath + " for plant " + plant.getPlantName());
                    imgToSet = new Image(getClass().getResource("/img_files/photo1.jpg").toExternalForm());
                }
            } else {
                imgToSet = new Image(getClass().getResource("/img_files/photo1.jpg").toExternalForm());
            }
            imageView.setImage(imgToSet);
        } catch (Exception e) {
            System.err.println("General exception during image loading for plant " + (plant.getPlantID() != null ? plant.getPlantID() : "UNKNOWN") + ": " + e.getMessage());
            imageView.setImage(new Image(getClass().getResource("/img_files/photo1.jpg").toExternalForm()));
        }
        imageView.setFitHeight(100);
        imageView.setFitWidth(148);
        imageView.setPreserveRatio(false);
        VBox imageContainer = new VBox(imageView);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefHeight(105);
        Label nameLabel = new Label(plant.getPlantName() != null && !plant.getPlantName().trim().isEmpty() ? plant.getPlantName() : "Unnamed Plant");
        nameLabel.setFont(Font.font("Century Gothic Bold", 15));
        nameLabel.setTextFill(Color.web("#333333"));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(142); 
        Label idLabel = new Label("ID: " + (plant.getPlantID() != null && !plant.getPlantID().trim().isEmpty() ? plant.getPlantID() : "N/A"));
        idLabel.setFont(Font.font("System", 10));
        idLabel.setTextFill(Color.web("#666666"));
        idLabel.setWrapText(true); 
        idLabel.setMaxWidth(142); 
        String datePlantedDisplay = "N/A";
        if (plant.getDatePlanted() != null) {
            try {
                datePlantedDisplay = plant.getDatePlanted().format(csvDateFormatter);
            } catch (Exception e) {
                System.err.println("Error formatting date for plant " + plant.getPlantID() + ": " + plant.getDatePlanted());
            }
        }
        Label dateLabel = new Label("Planted: " + datePlantedDisplay);
        dateLabel.setFont(Font.font("System", 10));
        dateLabel.setTextFill(Color.web("#666666"));
        dateLabel.setWrapText(true);
        dateLabel.setMaxWidth(142); 
        Label viewDetailsLabel = new Label("View Details");
        viewDetailsLabel.setUnderline(true);
        viewDetailsLabel.setTextFill(Color.SEAGREEN);
        viewDetailsLabel.setFont(Font.font("System", 11));
        viewDetailsLabel.setCursor(Cursor.HAND);
        
        VBox textContentBox = new VBox(2);
        textContentBox.getChildren().addAll(nameLabel, idLabel, dateLabel);
        VBox.setMargin(viewDetailsLabel, new Insets(4,0,4,0)); 
        viewDetailsLabel.setOnMouseClicked(event -> {
            if (plant == null) return;
            String fxmlPath = "/fxml_files/viewPlantDetails.fxml";
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                ViewPlantDetailsController controller = loader.getController();
                controller.populateDetails(plant, ci);
                Stage detailsStage = new Stage();
                detailsStage.initModality(Modality.APPLICATION_MODAL);
            
                detailsStage.initOwner(this.stage); 
                String titleName = (plant.getPlantName() != null && !plant.getPlantName().trim().isEmpty()) ? plant.getPlantName() : "Plant";
                detailsStage.setTitle("Plant Details: " + titleName);
                detailsStage.setScene(new Scene(root));
                detailsStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "FXML Loading Error", "Error loading plant details: " + e.getMessage());
            }
        });
        
        Button editButton = new Button("Edit");
        styleActionButton(editButton);
        editButton.setOnAction(e -> { 
            if (plant == null) return;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(plantPath));
                Parent root = loader.load();
                PlantSceneController controller = loader.getController();
                controller.setStage(this.stage);
                controller.populateFormForEdit(plant, ci);
                String titleName = (plant.getPlantName() != null && !plant.getPlantName().trim().isEmpty()) ? plant.getPlantName() : "Plant";
                this.stage.setTitle("Edit Plant: \n" + titleName); 
                this.stage.setScene(new Scene(root));
            } catch (IOException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "FXML Error", "Could not open the edit plant form: " + ex.getMessage());
            }
        });
        Button deleteButton = new Button("Delete");
        styleActionButton(deleteButton);
        deleteButton.setOnAction(e -> {
            if (plant == null || plant.getPlantID() == null) return;
            String plantNameToConfirm = (plant.getPlantName() != null && !plant.getPlantName().trim().isEmpty()) ? plant.getPlantName() : "this plant (ID: " + plant.getPlantID() + ")";
            Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION, "Delete '" + plantNameToConfirm + "'?", ButtonType.YES, ButtonType.NO);
            confirmDelete.setTitle("Confirm Delete");
            confirmDelete.setHeaderText(null);
            confirmDelete.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
            
                    boolean plantDeleted = deleteCsvRecord(plantFilePath, plant.getPlantID(), 1);
                    boolean careDeleted = false;
                    if (plant.getCareInstruID() != null && !plant.getCareInstruID().trim().isEmpty()){
                        careDeleted = deleteCsvRecord(plantCareInstruFilePath, plant.getCareInstruID(), 0);
                    } else {
                        careDeleted = true;
                    }
                    
                    if (plantDeleted) {
                        loadAllPlantData();
                        TextField currentSearchField = searchFieldFromTab(gardenTabPane.getSelectionModel().getSelectedItem());
                        String activeGardenId = (gardenTabPane.getSelectionModel().getSelectedItem() != null && 
                                                gardenTabPane.getSelectionModel().getSelectedItem().getUserData() != null) ?
                                                (String) gardenTabPane.getSelectionModel().getSelectedItem().getUserData() :
                                                null;
                        if (activeGardenId != null) {
                                displayPlantsForGarden(activeGardenId, plantGridFromTab(gardenTabPane.getSelectionModel().getSelectedItem()), currentSearchField != null ? currentSearchField.getText() : "");
                        } else {
                            refreshTabs(gardenFilePath);
                        }
                    }
                }
            });
        });
        
        Button moveButton = new Button("Move");
        styleActionButton(moveButton);
        moveButton.setOnAction(e -> {
            if (plant == null) return;
            handleMovePlant(plant);
        });
        HBox buttonBox = new HBox(5, editButton, deleteButton, moveButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        card.getChildren().addAll(imageContainer, textContentBox, viewDetailsLabel, buttonBox);
        return card;
    }

    private void styleActionButton(Button button) {
        button.setFont(Font.font("Century Gothic", 10));
        button.setPrefHeight(25);
        button.setMinWidth(45);
        button.setStyle("-fx-background-color: #E9E9E9; -fx-text-fill: #333; -fx-border-color: #D0D0D0; -fx-border-width: 1px; -fx-background-radius: 3; -fx-border-radius: 3;");
    }

    private void handleMovePlant(Plant plantToMove) {
        if (plantToMove == null) {
            showAlert(Alert.AlertType.ERROR, "Operation Error", "Cannot perform action: plant data is missing.");
            return;
        }
        if (plantToMove.getPlantID() == null){
            showAlert(Alert.AlertType.ERROR, "Operation Error", "Cannot move plant: plant ID is missing.");
            return;
        }
        List<String[]> allGardens = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(gardenFilePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                if (values.length >= 2) { 
                    allGardens.add(new String[]{values[0].trim(), values[1].trim()});
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load garden list for moving plant.");
            return;
        }
        List<String> displayGardenNames = new ArrayList<>();
        Map<String, String> displayNameToIdMap = new HashMap<>();
        String currentPlantGardenId = plantToMove.getGardenID();
        for(String[] gardenData : allGardens) {
            if (currentPlantGardenId != null && !gardenData[0].equals(currentPlantGardenId)) {
                String gardenDisplayName = gardenData[1];
                displayGardenNames.add(gardenDisplayName); 
                displayNameToIdMap.put(gardenDisplayName, gardenData[0]);
            } else if (currentPlantGardenId == null) {
                String gardenDisplayName = gardenData[1];
                displayGardenNames.add(gardenDisplayName);
                displayNameToIdMap.put(gardenDisplayName, gardenData[0]);
            }
        }
        if (displayGardenNames.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Move Plant", "No other gardens available to move this plant to.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(displayGardenNames.get(0), displayGardenNames);
        dialog.setTitle("Move Plant");
        String plantNameToDisplay = plantToMove.getPlantName();
        if (plantNameToDisplay == null || plantNameToDisplay.trim().isEmpty()) {
            plantNameToDisplay = "Plant ID: " + plantToMove.getPlantID();
        }
        dialog.setHeaderText("Move '" + plantNameToDisplay + "' to which garden?");
        dialog.setContentText("Choose garden:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedGardenDisplayName -> {
            String newGardenGid = displayNameToIdMap.get(selectedGardenDisplayName);
            if (newGardenGid == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not identify the selected garden ID.");
                return;
            }
            String plantID = plantToMove.getPlantID();
            String careInstruID = plantToMove.getCareInstruID() != null ? plantToMove.getCareInstruID() : "";
            String name = plantToMove.getPlantName() != null ? plantToMove.getPlantName() : "";
            String img = plantToMove.getPlantImg() 
                        != null ? plantToMove.getPlantImg() : "";
            String growth = plantToMove.getGrowthStatus() != null ? plantToMove.getGrowthStatus() : "";
            String health = plantToMove.getHealthStatus() != null ? plantToMove.getHealthStatus() : "";
            String datePlantedStr = plantToMove.getDatePlanted() != null ?
                                    plantToMove.getDatePlanted().format(csvDateFormatter) : "";
            String updatedPlantRow = String.join(",",
                    newGardenGid, 
                    plantID,
                    careInstruID,
                    name,
                    img,
                    growth,
                    health,
                    datePlantedStr);
            boolean success = updateCsvRecord(plantFilePath, plantID, 1, updatedPlantRow); 
            if (success) {
                plantToMove.setGardenID(newGardenGid);
                loadAllPlantData();
                Tab selectedTab = gardenTabPane.getSelectionModel().getSelectedItem();
                if (selectedTab != null && selectedTab.getUserData() != null) {
                    String activeGardenGid = (String) selectedTab.getUserData();
                    TextField currentSearchField = searchFieldFromTab(selectedTab);
                    displayPlantsForGarden(activeGardenGid, plantGridFromTab(selectedTab), currentSearchField != null ? currentSearchField.getText() : "");
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "'" + name + "' moved to " + selectedGardenDisplayName + ".");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to move plant.");
            }
        });
    }

    public void refreshTabs(String csvFilePath) {
        Tab addGardenTabInstance = null;
        for (Tab t : gardenTabPane.getTabs()) {
            if ("addGardenTab".equals(t.getId())) {
                addGardenTabInstance = t;
                break;
            }
        }
        List<Tab> gardenTabsToRemove = new ArrayList<>();
        for (Tab t : gardenTabPane.getTabs()) {
            if (!"addGardenTab".equals(t.getId())) {
                gardenTabsToRemove.add(t);
            }
        }
        gardenTabPane.getTabs().removeAll(gardenTabsToRemove);
        
        if (addGardenTabInstance != null && !gardenTabPane.getTabs().contains(addGardenTabInstance)) {
             gardenTabPane.getTabs().add(0, addGardenTabInstance);
        } else if (addGardenTabInstance != null && gardenTabPane.getTabs().contains(addGardenTabInstance)) {
            gardenTabPane.getTabs().remove(addGardenTabInstance);
            gardenTabPane.getTabs().add(0, addGardenTabInstance);
        }
        loadAllPlantData();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            String header = br.readLine(); // Read the header line
            if (header == null || !header.trim().toLowerCase().equals("gardenid,gardenname,dateadded")) {
                System.err.println("Warning: garden.csv header might be missing or incorrect. Expected 'gardenId,gardenName,dateAdded'");
            }
            while ((line = br.readLine()) != null) { 
                String[] values = line.split(",", -1);
                if (values.length >= 3) { 
                    String gardenId = values[0].trim();
                    String gardenNameVal = values[1].trim(); 
                    String dateAddedString = values[2].trim();
                    addAndPopulateTab(gardenId, gardenNameVal, dateAddedString);
                } else if (!line.trim().isEmpty()){ 
                    System.err.println("Skipping malformed garden CSV line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file for tabs: " + e.getMessage());
        }
        
        if (!gardenTabPane.getTabs().isEmpty()) {
            if (gardenTabPane.getTabs().size() > 1 && "addGardenTab".equals(gardenTabPane.getTabs().get(0).getId())) {
                gardenTabPane.getSelectionModel().select(1);
            } else if (gardenTabPane.getTabs().size() > 0 && !"addGardenTab".equals(gardenTabPane.getTabs().get(0).getId())){
                gardenTabPane.getSelectionModel().selectFirst();
            } else if (addGardenTabInstance != null) {
                gardenTabPane.getSelectionModel().select(addGardenTabInstance);
            }
        }
    }

    private void addAndPopulateTab(String gardenID, String gardenNameVal, String dateAddedString) { 
        String displayTabTitle = formatDisplayGardenName(gardenNameVal);
        Tab tab = new Tab(displayTabTitle);
        tab.setUserData(gardenID);
        AnchorPane rootAnchor = new AnchorPane();
        rootAnchor.setPrefSize(800, 520); // MODIFIED: Increased height from 489 to 520

        VBox topSectionVBox = new VBox(10);
        topSectionVBox.setPadding(new Insets(10, 10, 5, 10));
        
        HBox gardenInfoAndActionsHeader = new HBox(10);
        gardenInfoAndActionsHeader.setAlignment(Pos.CENTER_LEFT);
        
        VBox gardenInfoVBox = new VBox(2);
        HBox nameAndIdLine = new HBox(5); 
        nameAndIdLine.setAlignment(Pos.BASELINE_LEFT);
        Label gardenNameDisplayLabel = new Label(formatDisplayGardenName(gardenNameVal));
        gardenNameDisplayLabel.setFont(Font.font("System Bold", 20));
        Label gardenIdDisplayLabel = new Label("(" + gardenID + ")");
        gardenIdDisplayLabel.setFont(Font.font("System", 12));
        gardenIdDisplayLabel.setTextFill(Color.SLATEGRAY);
        nameAndIdLine.getChildren().addAll(gardenNameDisplayLabel, gardenIdDisplayLabel);

        Label dateAddedDisplayLbl = new Label("Date Added: " + dateAddedString);
        dateAddedDisplayLbl.setFont(Font.font("System", 14));
        
        gardenInfoVBox.getChildren().addAll(nameAndIdLine, dateAddedDisplayLbl);
        HBox.setHgrow(gardenInfoVBox, Priority.ALWAYS);
        
        HBox gardenAndPlantActionsHBox = new HBox(10);
        gardenAndPlantActionsHBox.setAlignment(Pos.CENTER_RIGHT);
        
        ComboBox<String> tabSortPlantsComboBox = new ComboBox<>();
        tabSortPlantsComboBox.setId("plantSortComboBox");
        tabSortPlantsComboBox.getItems().addAll("Sort by Name (A-Z)", "Sort by Name (Z-A)", "Sort by Date Planted (Newest)", "Sort by Date Planted (Oldest)");
        tabSortPlantsComboBox.setPromptText("Sort Plants By...");
        tabSortPlantsComboBox.setPrefHeight(30);
        tabSortPlantsComboBox.setPrefWidth(180); 
        
        Button addPlantToThisGardenBtn = new Button("+ Add Plant");
        addPlantToThisGardenBtn.setStyle("-fx-background-color: #8CC152; -fx-text-fill: white; -fx-font-weight: bold;");
        addPlantToThisGardenBtn.setFont(Font.font("Century Gothic", 12));
        addPlantToThisGardenBtn.setPrefHeight(30);
        
        Button editGardenBtn = new Button("Edit Garden");
        editGardenBtn.setStyle("-fx-background-color: #8CC152; -fx-text-fill: white; -fx-font-weight: bold;");
        editGardenBtn.setFont(Font.font("Century Gothic", 12));
        editGardenBtn.setPrefHeight(30);
        Button deleteGardenBtn = new Button("Delete Garden");
        deleteGardenBtn.setStyle("-fx-background-color: #DA4453; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteGardenBtn.setFont(Font.font("Century Gothic", 12));
        deleteGardenBtn.setPrefHeight(30);
        
        gardenAndPlantActionsHBox.getChildren().addAll(tabSortPlantsComboBox, addPlantToThisGardenBtn, editGardenBtn, deleteGardenBtn);

        editGardenBtn.setOnAction(edit -> {
            openAddGardenTab();
            gardenNameFieldForForm.setText(gardenNameVal);
            try {
                dateAddedFieldForForm.setValue(LocalDate.parse(dateAddedString, csvDateFormatter));
            } catch (DateTimeParseException e) {
                dateAddedFieldForForm.setValue(null);
            }
            editingGardenID = gardenID;
            originalEditingGardenName = gardenNameVal;
            addGardenButton.setDisable(true);
            addGardenButton.setVisible(false);
            updateGardenButton.setDisable(false);
            updateGardenButton.setVisible(true);
        });
        deleteGardenBtn.setOnAction(delete -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Garden");
            alert.setHeaderText("Delete '" + formatDisplayGardenName(gardenNameVal) + " (" + gardenID + ")'?");
            alert.setContentText("This will permanently remove the garden and all its plants. This action cannot be undone.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    deleteGardenFromCSV(gardenID);
                    List<Plant> plantsInGarden = getPlantsForGarden(gardenID, ""); 

                    for (Plant p : plantsInGarden) {
                        deleteCsvRecord(plantFilePath, p.getPlantID(), 1);
                        deleteCsvRecord(plantCareInstruFilePath, p.getCareInstruID(), 0);
                    }
                    gardenTabPane.getTabs().remove(tab);
                    loadAllPlantData();
                }
            });
        });
        addPlantToThisGardenBtn.setOnAction(addplant -> {
            try {
                MainController.currentGardenForPlantAdd = gardenID;
                loadScene(plantPath);
            } catch (IOException e) {
                System.err.println("Error opening plant scene for add: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "FXML Loading Error", "Could not open the 'Add Plant' form: " + e.getMessage());
            }
        });
        tabSortPlantsComboBox.setOnAction(event -> {
            if (tab.isSelected()) {
                GridPane currentGrid = plantGridFromTab(tab);
                TextField currentSearchField = searchFieldFromTab(tab);
                    if (currentGrid != null) {
                        displayPlantsForGarden(gardenID, currentGrid, currentSearchField != null ? currentSearchField.getText() : "");
                    }
            }
        });

        gardenInfoAndActionsHeader.getChildren().addAll(gardenInfoVBox, gardenAndPlantActionsHBox);
        HBox searchControlsHBox = new HBox(10);
        searchControlsHBox.setAlignment(Pos.CENTER_LEFT);
        searchControlsHBox.setPadding(new Insets(5,0,5,0));
        TextField searchPlantsField = new TextField();
        searchPlantsField.setId("gardenPlantSearchField");
        searchPlantsField.setPromptText("Search plants in " + formatDisplayGardenName(gardenNameVal) + "...");
        HBox.setHgrow(searchPlantsField, Priority.ALWAYS);
        
        Button searchPlantsButton = new Button("Search");
        searchPlantsButton.setStyle("-fx-background-color: #8CC152; -fx-text-fill: white; -fx-font-weight: bold;");
        searchPlantsButton.setFont(Font.font("Century Gothic", 12));
        searchPlantsButton.setPrefHeight(30);
        searchPlantsField.setOnAction(e -> searchPlantsButton.fire());
        searchPlantsButton.setOnAction(e -> {
            GridPane currentGrid = plantGridFromTab(tab);
            if (currentGrid != null) {
                displayPlantsForGarden(gardenID, currentGrid, searchPlantsField.getText());
            }
        });
        searchControlsHBox.getChildren().addAll(searchPlantsField, searchPlantsButton);
        
        Line divider1 = new Line(0, 0, 780, 0);
        divider1.setStroke(Color.web("#A0D468"));
        divider1.setStrokeWidth(1);
        topSectionVBox.getChildren().addAll(gardenInfoAndActionsHeader, divider1, searchControlsHBox);

        GridPane plantsDisplayGrid = new GridPane();
        plantsDisplayGrid.setPadding(new Insets(10));
        plantsDisplayGrid.setHgap(10);
        plantsDisplayGrid.setVgap(10);
        // plantsDisplayGrid.setPrefWidth(780); // This can be kept or removed if ScrollPane's fitToWidth is True

        plantsDisplayGrid.getColumnConstraints().clear(); 
        for (int i = 0; i < 4; i++) { 
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(25); 
            colConst.setHalignment(javafx.geometry.HPos.CENTER); 
            plantsDisplayGrid.getColumnConstraints().add(colConst);
        }

        ScrollPane scrollPane = new ScrollPane(plantsDisplayGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox mainContentVBox = new VBox(5, topSectionVBox, scrollPane); 
        mainContentVBox.setPadding(new Insets(10)); 

        AnchorPane.setTopAnchor(mainContentVBox, 0.0);
        AnchorPane.setBottomAnchor(mainContentVBox, 0.0);
        AnchorPane.setLeftAnchor(mainContentVBox, 0.0);
        AnchorPane.setRightAnchor(mainContentVBox, 0.0);
        rootAnchor.getChildren().add(mainContentVBox);
        tab.setContent(rootAnchor);
        
        gardenTabPane.getTabs().add(tab);
        if (gardenTabPane.getSelectionModel().getSelectedItem() == tab) {
            currentGardenID = gardenID;
            displayPlantsForGarden(gardenID, plantsDisplayGrid, searchPlantsField.getText());
        }
    }

    @FXML
    public void addGarden() throws IOException {
        String gardenNameFromForm = gardenNameFieldForForm.getText().trim();
        LocalDate dateAddedFromForm = dateAddedFieldForForm.getValue();
        if (gardenNameFromForm.isEmpty() || dateAddedFromForm == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(gardenFilePath))) {
            String line;
            br.readLine(); 
            while((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if(parts.length >= 2 && parts[1].trim().equalsIgnoreCase(gardenNameFromForm)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Garden name already exists.");
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking existing garden names or file not found: " + e.getMessage());
        }
        String newGardenGid = generateNewID(gardenFilePath, 0, "G-");
        String dateAddedStr = dateAddedFromForm.format(csvDateFormatter);
        showAlertAndRegister(
                String.format("%s,%s,%s\n", newGardenGid, gardenNameFromForm, dateAddedStr),
                gardenFilePath,
                "Garden added successfully!",
                this::clearGardenForm);
        refreshTabs(gardenFilePath); 
        for (Tab t : gardenTabPane.getTabs()) {
            if (t.getUserData() != null && t.getUserData().equals(newGardenGid)) {
                gardenTabPane.getSelectionModel().select(t);
                break;
            }
        }
    }

    @FXML
    public void updateGarden() throws IOException {
        String newGardenNameStr = gardenNameFieldForForm.getText().trim();
        LocalDate newDateAddedValue = dateAddedFieldForForm.getValue();
        if (newGardenNameStr.isEmpty() || newDateAddedValue == null || editingGardenID == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields or no garden selected for edit.");
            return;
        }
         try (BufferedReader br = new BufferedReader(new FileReader(gardenFilePath))) {
            String line;
            br.readLine(); 
            while((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if(parts.length >= 2 && !parts[0].trim().equals(editingGardenID) && parts[1].trim().equalsIgnoreCase(newGardenNameStr)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Another garden with this name already exists.");
                    return;
                }
            }
        } catch (IOException e) {
             System.err.println("Error checking existing garden names for update: " + e.getMessage());
        }
        
        String newDateAddedStr = newDateAddedValue.format(csvDateFormatter);
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Edit");
        confirmationAlert.setHeaderText("Update garden '" + formatDisplayGardenName(originalEditingGardenName) + " ("+editingGardenID+")' to '" + formatDisplayGardenName(newGardenNameStr) + "'?");
        confirmationAlert.setContentText("Click OK to proceed.");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                boolean updated = updateGardenInCSV(editingGardenID, newGardenNameStr, newDateAddedStr, gardenFilePath);
                if (updated) {
                    clearGardenForm();
                    refreshTabs(gardenFilePath);
                    for (Tab t : gardenTabPane.getTabs()) {
                        if (t.getUserData() != null && t.getUserData().equals(editingGardenID)) {
                        
                            gardenTabPane.getSelectionModel().select(t);
                            break;
                        }
                    }
                    addGardenButton.setDisable(false);
                    addGardenButton.setVisible(true);
                    updateGardenButton.setDisable(true);
                    updateGardenButton.setVisible(false);
                }
            }
        });
    }

    private boolean updateGardenInCSV(String gardenIDToUpdate, String newName, String newDateAdded, String filePath) {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String header = br.readLine();
            if (header != null) lines.add(header);
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",",-1);
                if (values.length >= 3 && values[0].trim().equals(gardenIDToUpdate)) { 
                    lines.add(String.format("%s,%s,%s", gardenIDToUpdate, newName, newDateAdded));
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (found) {
            try (FileWriter writer = new FileWriter(filePath, false)) {
                for (String l : lines) {
                    writer.write(l + "\n");
                }
                return true;
            } catch (IOException e) {
                 e.printStackTrace();
                 return false;
            }
        }
        return false;
    }

    private void deleteGardenFromCSV(String gardenIDToDelete) {
        List<String> lines = new ArrayList<>();
        boolean deleted = false;
        try (BufferedReader br = new BufferedReader(new FileReader(gardenFilePath))) {
            String line;
            String header = br.readLine();
            if (header != null) lines.add(header);
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",",-1);
                    if (parts.length >= 1) { 
                        String currentGardenGid = parts[0].trim();
                        if (!currentGardenGid.equals(gardenIDToDelete)) {
                            lines.add(line);
                        } else {
                            deleted = true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (deleted) {
            try (FileWriter fw = new FileWriter(gardenFilePath, false)) {
                for (String l : lines) {
                    fw.write(l + "\n");
                }
            } catch (IOException e) {
                 e.printStackTrace();
            }
        }
    }

    public void openAddGardenTab() {
        Tab currentAddGardenTab = null;
        for(Tab t : gardenTabPane.getTabs()){
            if("addGardenTab".equals(t.getId())){
                currentAddGardenTab = t;
                break;
            }
        }
        if (currentAddGardenTab != null) {
            gardenTabPane.getSelectionModel().select(currentAddGardenTab);
        }
    }

    private void clearGardenForm() {
        gardenNameFieldForForm.clear();
        dateAddedFieldForForm.setValue(null);
        editingGardenID = null;
        originalEditingGardenName = null;
    }

    @FXML
    public void openPlantScene() throws IOException {
        loadScene(plantPath);
    }

    @FXML
    public void openHomeScene() throws IOException {
        loadScene(homePath);
    }
}