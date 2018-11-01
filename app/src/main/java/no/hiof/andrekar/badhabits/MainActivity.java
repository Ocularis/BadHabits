package no.hiof.andrekar.badhabits;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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


    public static MyAdapter adapter;
    public static MyFavoriteAdapter favAdapter;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Habit.habits.clear();
        SaveData saveData = new SaveData();
        saveData.readFromFile();





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
        Collections.sort(Habit.habits, Habit.HabitComparator);

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
        if (id == R.id.action_populate) {
            populateData();
            return true;
        }
        if (id == R.id.action_remove) {
            removeData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void  initRecyclerView(){

        RecyclerView favoriteRecyclerView = findViewById(R.id.favorite_recycler_view);

        favAdapter = new MyFavoriteAdapter(this);
        favoriteRecyclerView.setAdapter(favAdapter);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        adapter = new MyAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void populateData() {
        ArrayList<Habit> testHabits = new ArrayList<Habit>();
        Habit gumHabit = new EconomicHabit("Gum", "Stop with gum", new Date(), "kr", 10, 100, 10);
        Habit sodaHabit = new DateHabit("Soda", "Stop drinking soda", new Date(), 10);
        Habit poop = new EconomicHabit("Smokes", "Stop with smoking", new Date(), "kr", 10, 100, 10);
        Habit scoop = new DateHabit("Having fun", "Stop having fun", new Date(), 10);
        gumHabit.setFavourite(true);
        scoop.setFavourite(true);

        testHabits.add((EconomicHabit) gumHabit);
        testHabits.add((DateHabit) sodaHabit);
        testHabits.add((EconomicHabit) poop);
        testHabits.add((DateHabit) scoop);
        SaveData saveData = new SaveData();

        for (Habit habit : testHabits) {
            if (habit instanceof DateHabit) {
                saveData.saveToFile(habit, 2);
            } else if (habit instanceof EconomicHabit) {
                saveData.saveToFile(habit, 1);
            }
        }
        testHabits.clear();
        initRecyclerView();
        Collections.sort(Habit.habits, Habit.HabitComparator);
        }

        private void removeData() {
            Habit.habits.clear();
            SaveData saveData = new SaveData();
            saveData.updateData(1);
            saveData.updateData(2);
            initRecyclerView();
        }

        public static void updateRecyclerView(){
            adapter.notifyDataSetChanged();
            favAdapter.notifyDataSetChanged();
        }

}

