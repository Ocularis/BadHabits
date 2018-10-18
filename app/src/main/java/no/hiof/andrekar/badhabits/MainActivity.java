package no.hiof.andrekar.badhabits;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONObject;

import model.SaveData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import model.DateHabit;
import model.EconomicHabit;
import model.Habit;

import model.SaveData;

public class MainActivity extends AppCompatActivity {

    static boolean firstRun = false;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Habit.habits.clear();
        SaveData saveData = new SaveData();
        saveData.readFromFile();

        /*
        if (!firstRun) {
            //temp: adding some habits - will be replaced with stored files
            //Habit.habits.clear();
            Habit gumHabit = new EconomicHabit("Gum", "Stop with gum", new Date(), "kr", 10, 100, 10);
            Habit sodaHabit = new DateHabit("Soda", "Stop drinking soda", new Date(), 10);
            Habit poop = new EconomicHabit("Poop", "Stop with poop", new Date(), "kr", 10, 100, 10);
            Habit scoop = new DateHabit("Scoop", "Stop drinking scoop", new Date(), 10);
            gumHabit.setFavourite(true);
            scoop.setFavourite(true);
            firstRun = true;
        }
        */

        //code to ask user for permission to store data.
        int REQUEST_CODE=1;
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_addHabit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), HabitActivity.class);
                startActivity(intent);
            }
        });

        //TODO: Implement this into habits model? 
        Collections.sort(Habit.habits, new Comparator<Habit>() {
            @Override
            public int compare(Habit h2, Habit h1) {
            int result = Boolean.compare(h1.getIsFavourite(), h2.getIsFavourite());
                if (result == 0) {
                    // boolean values the same
                    result = h2.getTitle().compareTo(h1.getTitle());
                }
            return result;
        }});
        initRecyclerView();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.t
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Snackbar.make(findViewById(android.R.id.content), "Not yet implemented", Snackbar.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView(){

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        MyAdapter adapter = new MyAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
