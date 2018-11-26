package model;

import com.google.firebase.firestore.Exclude;

import org.threeten.bp.temporal.ChronoUnit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//DONe: Implement maths in class? IE: getters for progress?
//DONE: Rename variables? ie. Price and InitialValue / GoalValue? - DO we need both or should ie. initialValue be renamed to price for alternative?
//DONE: Clean up unused functions.

public class EconomicHabit extends Habit {
    static private String currency;
    private float goalValue;
    private float price;
    private float alternativePrice;
    private int failedTotal;
    private Map<String, Integer> failedMap = new HashMap<>();
    private String habitType = "ECO_HABIT";

    // Constructor
    public EconomicHabit(String title, String description, long startDate, float alternativePrice, float goalValue, float price, Boolean isFavourite) {
        super(title, description, startDate, isFavourite, "ECO_HABIT");
        this.alternativePrice = alternativePrice;
        this.goalValue = goalValue;
        this.price = price;
    }

    //Empty constructor for firestore.
    public EconomicHabit() {}

    //Getters
    public float getPrice() { return price; }

    public String getHabitType() {
        return habitType;
    }

    public void setHabitType(String habitType) {
        this.habitType = habitType;
    }

    public float getGoalValue() { return goalValue; }

    public float getAlternativePrice() {
        return alternativePrice;
    }

    //Setters
    public void setAlternativePrice(float alternativePrice) {
        this.alternativePrice = alternativePrice;
    }

    //Setters
    public void setPrice(float price) {
        this.price = price;
    }

    public void setGoalValue(float goalValue) {
        this.goalValue = goalValue;
    }

    //Map date to fails
    public Map<String, Integer> getMappedFail() {
        return failedMap;
    }

    public void setMappedFail(Map<String, Integer> failedMap) {
        this.failedMap = failedMap;
    }

    @Exclude
    public float getProgress() {
        //Return progress towards goal
        float dateGoalL = Habit.getDateDiff(this.getStartDate(), new Date().getTime(), ChronoUnit.DAYS);
        //Maths
        float saved = (( - this.getGoalValue() - (dateGoalL*this.getAlternativePrice()) ) + (dateGoalL*this.getPrice()) - this.getFailedTotal());
        int temp = (int)(100*saved);
        saved = ((float)temp/100);
        if (saved < 0) {
            return saved;
        }
        else {
            return saved;
        }

    }

    public int getFailedTotal() {
        return failedTotal;
    }

    public void setFailedTotal(int failedTotal) {
        this.failedTotal = failedTotal;
    }
    
    public void increaseFailedTotal(int failedAmout){
        //Put the fail into a Map with the current date.
        failedMap.put(Long.toString(new Date().getTime()), failedAmout);
        this.failedTotal += failedAmout;
    }
}
