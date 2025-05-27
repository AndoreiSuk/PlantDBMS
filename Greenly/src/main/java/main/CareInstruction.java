/* NAG ADD KOG CARE INSTRUCTION FOR DE PLANTS BECAUSE 
   for creating and managing each holding specific details like watering, sunlight, and soil needs. */

package main;

public class CareInstruction {
    private String careInstruID;
    private String wateringFreq;
    private String sunlightReq;
    private String soilType;
    private String fertilizingSched;
    private String toxicity;

    // Constructor
    public CareInstruction(String careInstruID, String wateringFreq, String sunlightReq, String soilType, String fertilizingSched, String toxicity) {
        this.careInstruID = careInstruID;
        this.wateringFreq = wateringFreq;
        this.sunlightReq = sunlightReq;
        this.soilType = soilType;
        this.fertilizingSched = fertilizingSched;
        this.toxicity = toxicity;
    }

    // Getters and Setters
    public String getCareInstruID() { return careInstruID; }
    public void setCareInstruID(String careInstruID) { this.careInstruID = careInstruID; }
    public String getWateringFreq() { return wateringFreq; }
    public void setWateringFreq(String wateringFreq) { this.wateringFreq = wateringFreq; }
    public String getSunlightReq() { return sunlightReq; }
    public void setSunlightReq(String sunlightReq) { this.sunlightReq = sunlightReq; }
    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }
    public String getFertilizingSched() { return fertilizingSched; }
    public void setFertilizingSched(String fertilizingSched) { this.fertilizingSched = fertilizingSched; }
    public String getToxicity() { return toxicity; }
    public void setToxicity(String toxicity) { this.toxicity = toxicity; }

    @Override
    public String toString() { // For CSV representation
        return String.join(",", careInstruID, wateringFreq, sunlightReq, soilType, fertilizingSched, toxicity);
    }
}