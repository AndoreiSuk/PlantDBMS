/* FOR THE PLANT ERS CRUCIAL NI SYA
   blueprint siya for creating and managing Plant entities, 
   like each holding specific data like ID, name, and associated garden/care instruction details. */
package main;

import java.time.LocalDate;

public class Plant {
    private String plantID;
    private String gardenID;
    private String careInstruID;
    private String plantName;
    private String plantImg;
    private String growthStatus;
    private String healthStatus;
    private LocalDate datePlanted;

    public Plant(String plantID, String gardenID, String careInstruID, String plantName, String plantImg, String growthStatus, String healthStatus, LocalDate datePlanted) {
        this.plantID = plantID;
        this.gardenID = gardenID;
        this.careInstruID = careInstruID;
        this.plantName = plantName;
        this.plantImg = plantImg;
        this.growthStatus = growthStatus;
        this.healthStatus = healthStatus;
        this.datePlanted = datePlanted;
    }

    public String getPlantID() { return plantID; }
    public void setPlantID(String plantID) { this.plantID = plantID; }
    public String getGardenID() { return gardenID; }
    public void setGardenID(String gardenID) { this.gardenID = gardenID; }
    public String getCareInstruID() { return careInstruID; }
    public void setCareInstruID(String careInstruID) { this.careInstruID = careInstruID; }
    public String getPlantName() { return plantName; }
    public void setPlantName(String plantName) { this.plantName = plantName; }
    public String getPlantImg() { return plantImg; }
    public void setPlantImg(String plantImg) { this.plantImg = plantImg; }
    public String getGrowthStatus() { return growthStatus; }
    public void setGrowthStatus(String growthStatus) { this.growthStatus = growthStatus; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public LocalDate getDatePlanted() { return datePlanted; }
    public void setDatePlanted(LocalDate datePlanted) { this.datePlanted = datePlanted; }

    @Override
    public String toString() {
        return String.join(",", plantID, gardenID, careInstruID, plantName, plantImg, growthStatus, healthStatus, datePlanted.toString());
    }
}