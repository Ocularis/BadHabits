package model;
//import no.hiof.andrekar.badhabits.R;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Habit {
    //An ArrayList to contain all habits when they are created

    //TODO: implement Math for habits. Return progress values.

    //TODO: make this list create itself from stored files
    public static ArrayList<Habit> habits = new ArrayList<Habit>();


    //We need a title and description for our main class.
    private String title;
    private String description;
    //Favourite boolean?
    private boolean isFavourite;

    //Start date? - Maybe this is better as a String and cast it later?
    private Date startDate;


    //Constructors

    public static void fromJson(JSONObject jsonObject) {
        try {
            String title = jsonObject.getString("title");
            String description = jsonObject.getString("description");

            //Temporary
            Date startDate = new Date();
            Habit h = new Habit(title, description, startDate);
            habits.add(h);
        } catch (Exception e) {
            Log.d("fromJSON", e.getMessage());
        }
    }


    public Habit(String title, String description, Date startDate) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.isFavourite = false;

        habits.add(this);
    }

    //Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getStartDate() {
        return startDate;
    }


    public boolean getIsFavourite() {
        return isFavourite;
    }


    //Setters

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFavourite(boolean isFavourite) {
        //Might want to have this as a toggle instead.
        this.isFavourite = isFavourite;
    }
}
