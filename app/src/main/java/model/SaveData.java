package model;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import no.hiof.andrekar.badhabits.MainActivity;
import no.hiof.andrekar.badhabits.MyAdapter;

public class SaveData {
    //Todo: Change this into internal storage, no need to use Downloads
    String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/TestGSon";
    String ecofile = filename+"Eco.txt";
    String datefile = filename+"Date.txt";
    ArrayList<EconomicHabit> ecohabits = new ArrayList<EconomicHabit>();
    ArrayList<DateHabit> datehabits = new ArrayList<DateHabit>();
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    private boolean habitexists = false;

    //DONE: Fix duplication problem.
    //TODO: Make Adapter refresh after sync


    public void readFromFile() {



        dbRef.child(fbAuth.getUid()).child("habits").keepSynced(true);
       // dbRef.child(fbAuth.getUid()).child("habits").child("DateHabits").addChildEventListener(dateEventListener);
       // dbRef.child(fbAuth.getUid()).child("habits").child("EcoHabits").addChildEventListener(ecoEventListener);

}

    public void saveToFile(Habit habit, int typeHabit) {
        if (typeHabit == 1) {
            dbRef.child(fbAuth.getUid()).child("habits").child("EcoHabits").child(habit.getUid()).setValue(habit);
        } else if (typeHabit == 2) {
            dbRef.child(fbAuth.getUid()).child("habits").child("DateHabits").child(habit.getUid()).setValue(habit);
        }
    };

    public void removeData(Habit habit, int typeHabit) {
        if (typeHabit == 1) {
            dbRef.child(fbAuth.getUid()).child("habits").child("EcoHabits").child(habit.getUid()).removeValue();
        } else if (typeHabit == 2) {
            dbRef.child(fbAuth.getUid()).child("habits").child("DateHabits").child(habit.getUid()).removeValue();
        }
    }

    public void saveToFile(Habit habit, int typeHabit, int habitIndex) {
        try {
            // Create a new Gson object
            Gson gson = new Gson();

            //convert the Java object to json
            if(typeHabit == 1) {
                Log.d("InstanceOF", "Got Eco Habit");

                Habit.habits.set(habitIndex, habit);
                ecohabits.clear();
                for ( Habit habitListed : Habit.habits ) {
                    Log.d("InstanceOF", "Looping through habits");
                    if (habitListed instanceof EconomicHabit) {
                        Log.d("InstanceOF", "EcoHabit");
                        ecohabits.add((EconomicHabit) habitListed);
                    }
                }
                String jsonString = gson.toJson(ecohabits);

                FileWriter fileWriter = new FileWriter(ecofile, false);
                fileWriter.write(jsonString);
                fileWriter.close();
                //EconomicHabit.ecohabits.clear();
            } else if (typeHabit == 2) {

                Log.d("InstanceOF", "Got Date Habit");

                Habit.habits.set(habitIndex, habit);
                datehabits.clear();
                for ( Habit habitListed : Habit.habits ) {
                    Log.d("InstanceOF", "Looping through habits");
                    if (habitListed instanceof DateHabit) {
                        Log.d("InstanceOF", "EcoHabit");
                        datehabits.add((DateHabit) habitListed);
                    }
                }
                String jsonString = gson.toJson(datehabits);
                FileWriter fileWriter = new FileWriter(datefile, false);
                fileWriter.write(jsonString);
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateData(Habit habit, int typeHabit) {
        if (typeHabit == 1) {
            dbRef.child(fbAuth.getUid()).child("habits").child("EcoHabits").child(habit.getUid()).setValue(habit);
        } else if (typeHabit == 2) {
            dbRef.child(fbAuth.getUid()).child("habits").child("DateHabits").child(habit.getUid()).setValue(habit);
        }
    }
}
