// THE MAIN CONTROLLER

package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class MainController {

    public String plantPath = "/fxml_files/plantScene.fxml";
    public String gardenPath = "/fxml_files/gardenScene.fxml";
    public String homePath = "/fxml_files/homeScene.fxml";
    public String plantFilePath = "src/main/resources/csv_files/plant.csv";
    public String gardenFilePath = "src/main/resources/csv_files/garden.csv";
    public String plantCareInstruFilePath = "src/main/resources/csv_files/plant_care_instructions.csv";
    public String plantCareLogFilePath = "src/main/resources/csv_files/plant_care_log.csv";

    protected Stage stage;
    @FXML public GridPane gridPane;
    protected final int maxColumn = 6;
    protected int row = 0;

    protected DateTimeFormatter csvDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static String currentGardenForPlantAdd;


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    protected void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected boolean inputExists(String textfieldString, String resourcePath, int columnIndex) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.out.println("Resource not found: " + resourcePath);
                return false;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (values.length > columnIndex && values[columnIndex].trim().equalsIgnoreCase(textfieldString)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected void loadScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.setStage(stage);
        stage.setScene(new Scene(root));
        stage.setTitle("Greenly");
        stage.show();
        System.out.println("Scene loaded: " + fxmlPath);
    }

    protected void showAlertAndRegister(
            String csvRow,
            String filePath,
            String successMessage,
            Runnable clearFormFunction) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Registration");
        confirmationAlert.setHeaderText("Are you sure you want to proceed?");
        confirmationAlert.setContentText("Click OK to proceed or Cancel to abort.");

        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
                    writer.append(csvRow);
                    showAlert(Alert.AlertType.CONFIRMATION, "Success", successMessage);
                    System.out.println(successMessage);
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not save data: " + e.getMessage());
                    System.out.println(e.getMessage());
                }
            } else {
                if (clearFormFunction != null) {
                    clearFormFunction.run();
                    System.out.println("form cleared");
                }
            }
        });
    }

    protected void populateTextField(TextField textField, String resourcePath, int columnIndex) {
        List<String> originalItems = new ArrayList<>();
        try (InputStream resource = getClass().getResourceAsStream(resourcePath)) {
            if (resource == null) {
                System.out.println("Resource not found: " + resourcePath);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(resource));
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > columnIndex && !values[columnIndex].isEmpty()) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    originalItems.add(values[columnIndex].trim());
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error reading resource: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "ERROR", "Failed to read " + resourcePath + ": " + e.getMessage());
            return;
        }
        ContextMenu suggestions = new ContextMenu();
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                suggestions.hide();
            } else {
                List<String> filteredList = originalItems.stream()
                        .filter(item -> item.toLowerCase().contains(newValue.toLowerCase()))
                        .collect(Collectors.toList());
                if (filteredList.isEmpty()) {
                    suggestions.hide();
                } else {
                    List<MenuItem> menuItems = new ArrayList<>();
                    for (String item : filteredList) {
                        MenuItem menuItem = new MenuItem(item);
                        menuItem.setOnAction(e -> {
                            textField.setText(item);
                            suggestions.hide();
                        });
                        menuItems.add(menuItem);
                    }
                    suggestions.getItems().clear();
                    suggestions.getItems().addAll(menuItems);
                    if (!suggestions.isShowing()) {
                        suggestions.show(textField, Side.BOTTOM, 0, 0);
                    }
                }
            }
        });
    }

    protected void updateCsvReferenceToNew(String csvFile, int columnIndex, String valueToReplace, String newValue) {
        try {
            Path path = Paths.get(csvFile);
            List<String> lines = Files.readAllLines(path);
            List<String> updatedLines = new ArrayList<>();

            boolean changed = false;
            boolean isHeader = true;

            for (String line : lines) {
                if (isHeader) {
                    updatedLines.add(line);
                    isHeader = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length > columnIndex && parts[columnIndex].trim().equals(valueToReplace)) {
                    parts[columnIndex] = newValue;
                    changed = true;
                }
                updatedLines.add(String.join(",", parts));
            }

            if (changed) {
                Files.write(path, updatedLines);
            }
        } catch (IOException e) {
            System.out.println("Error updating CSV file: " + e.getMessage());
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("File Update Error");
            errorAlert.setContentText("Failed to update references in " + csvFile);
            errorAlert.showAndWait();
        }
    }

    private void addPlantCardToGrid(String plantId, String plantName, String imagePath, String datePlanted) {
        ImageView imageViewer = new ImageView(new Image(imagePath));
        imageViewer.setFitWidth(150);
        imageViewer.setFitHeight(150);
        Label nameLabel = new Label("Plant Name: " + plantName);
        Label idLabel = new Label("Plant ID: " + plantId);
        Label datePlantedLabel = new Label("Date Planted: " + datePlanted);
        Button viewDetails = new Button("View Details");
        Button removePlant = new Button("Remove Plant");

        VBox plantCard = new VBox(imageViewer, nameLabel, idLabel, datePlantedLabel, viewDetails, removePlant);
        gridPane.add(plantCard, row % maxColumn, row / maxColumn);
    }

    protected void showAlertAndRegisterToTwoCSVs(
            String csvRow1,
            String filePath1,
            String csvRow2,
            String filePath2,
            String successMessage,
            Runnable clearFormFunction) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Registration");
        confirmationAlert.setHeaderText("Are you sure you want to proceed?");
        confirmationAlert.setContentText("Click OK to proceed or Cancel to abort.");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try (
                        PrintWriter writer1 = new PrintWriter(new FileWriter(filePath1, true));
                        PrintWriter writer2 = new PrintWriter(new FileWriter(filePath2, true))) {
                    writer1.append(csvRow1);
                    writer2.append(csvRow2);
                    showAlert(Alert.AlertType.CONFIRMATION, "Success", successMessage);
                    System.out.println("Saved to: " + filePath1 + " and " + filePath2);
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not save data: " + e.getMessage());
                    System.out.println(e.getMessage());
                }
            } else {
                if (clearFormFunction != null) {
                    clearFormFunction.run();
                    System.out.println("Form cleared");
                }
            }
        });
    }

    protected void populateComboBox(ComboBox<String> comboBox, String filePath, int columnIndex) {
        List<String> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] split = line.split(",");
                if (split.length > columnIndex) {
                    items.add(split[columnIndex].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        comboBox.setItems(FXCollections.observableArrayList(items));
    }

    protected String generateNewID(String filePath, int idColumnIndex, String prefix) {
        Set<Integer> existingNumbers = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String header = br.readLine(); 

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > idColumnIndex) {
                    String id = values[idColumnIndex].trim();
                    if (id.startsWith(prefix) && id.length() == prefix.length() + 4) {
                        try {
                            existingNumbers.add(Integer.parseInt(id.substring(prefix.length())));
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file for ID generation: " + filePath + " - " + e.getMessage());
             if (e instanceof java.nio.file.NoSuchFileException || existingNumbers.isEmpty()) {
                return String.format("%s%04d", prefix, 1);
            }
        }

        int nextNumber = 1;
        if (!existingNumbers.isEmpty()) {
            nextNumber = Collections.max(existingNumbers) + 1;
        }
        return String.format("%s%04d", prefix, nextNumber);
    }

    protected boolean updateCsvRecord(String filePath, String recordID, int idColumnIndex, String newCsvRow) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to read file: " + filePath);
            System.out.println("Error reading file for update: " + e.getMessage());
            return false;
        }

        boolean updated = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] values = line.split(",");
            if (values.length > idColumnIndex && values[idColumnIndex].trim().equals(recordID)) {
                lines.set(i, newCsvRow);
                updated = true;
                break;
            }
        }

        if (updated) {
            try {
                Files.write(Paths.get(filePath), lines);
                return true;
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to write updates to file: " + filePath);
                System.out.println("Error writing file for update: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    protected boolean deleteCsvRecord(String filePath, String recordID, int idColumnIndex) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to read file: " + filePath);
            System.out.println("Error reading file for delete: " + e.getMessage());
            return false;
        }

        List<String> updatedLines = new ArrayList<>();
        boolean deleted = false;
        boolean isHeader = true;
        for (String line : lines) {
            if (isHeader) {
                updatedLines.add(line);
                isHeader = false;
                continue;
            }
            String[] values = line.split(",");
            if (values.length > idColumnIndex && values[idColumnIndex].trim().equals(recordID)) {
                deleted = true;
            } else {
                updatedLines.add(line);
            }
        }

        if (deleted) {
            try {
                Files.write(Paths.get(filePath), updatedLines);
                return true;
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to write deletions to file: " + filePath);
                System.out.println("Error writing file for delete: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
}
