package no.hiof.andrekar.badhabits;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.SimpleTarget;

import org.threeten.bp.temporal.ChronoUnit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import model.DateHabit;
import model.EconomicHabit;
import model.Habit;
import model.SaveData;

import static model.Habit.habits;

public class ShowHabitActivity extends AppCompatActivity {

    public static int currentNumber;
    private String failedAmount = "";
    public ImageButton deleteButton;
    public ImageButton editButton;
    public ImageButton failedButton;
    LineChart chart;
    PieChart dateChart;
    //BarChart dateChart;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && resultCode == RESULT_OK) {
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);

        String userTheme = sharedPref.getString("key_theme", "");
        if (userTheme.equals("Light")){
            setTheme(R.style.LightTheme);
        }
        else if (userTheme.equals("Dark")){
            setTheme(R.style.DarkTheme);
        }

        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_show_habit);
        EconomicHabit ecohabit;
        DateHabit dateHabit;


        String currency = sharedPref.getString
                (SettingsActivity.KEY_PREF_CURRENCY, "");

        boolean onboarding = sharedPref.getBoolean(SettingsActivity.KEY_PREF_ONBOARDSHOWHABIT, false);
        if (onboarding == false) {
            onBoard(findViewById(R.id.getStartTextView));
        }




        TextView goalView = findViewById(R.id.getGoalTextView);
        TextView progressView = findViewById(R.id.getProgressTextView);
        TextView startView = findViewById(R.id.getStartTextView);
        TextView failedView = findViewById(R.id.getFailTextView);
        View parentView = findViewById(R.id.show_habit_parent);
        chart = findViewById(R.id.detailChart);
        dateChart = findViewById(R.id.detailChartDate);

        ImageView startViewImg = findViewById(R.id.startTextView);
        ImageView progressViewImg = findViewById(R.id.progressTextView);


        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
        Habit habit = Habit.habits.get(currentNumber);

        if (habit instanceof DateHabit) {
            goalView.setText(((DateHabit)habit).getDaysSinceStart());
            progressView.setText(((DateHabit)habit).getDateGoal());
            //temp code?
            Date date = new Date(habit.getStartDate());
            String dateText = df2.format(date);
            startView.setText(dateText);
            setDateData();
        }
        else {
            goalView.setText(String.valueOf(((EconomicHabit) habit).getProgress()) + " " + currency);
            progressView.setText(Float.toString(((EconomicHabit) habit).getGoalValue()) + " " +currency);
            Date date=new Date(habit.getStartDate());
            String dateText = df2.format(date);
            startView.setText(dateText);
            setEcoData();
        }

        if (habit.getFailDate() == 0) {
            ImageView failText = findViewById(R.id.failTextView);
            failedView.setVisibility(View.GONE);
            failText.setVisibility(View.GONE);
        } else {
            //DONE: Format this as "Days since last fail, maybe?"
            failedView.setText(Long.toString(Habit.getDateDiff(habit.getFailDate(), new Date().getTime(), ChronoUnit.DAYS)) + " " + getString(R.string.ecohabitFail_trailing));
        }

        setTitle(Habit.habits.get(currentNumber).getTitle());


        TextView descriptionView = findViewById(R.id.getDescriptionTextView);
        descriptionView.setText(Habit.habits.get(currentNumber).getDescription());


        deleteButton = findViewById(R.id.btn_habitDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowHabitActivity.this);
                builder.setMessage("This can not be reversed!")
                        .setTitle("Delete habit?")
                        .setPositiveButton("Yes, I Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //DONE can this be done without looping through all habits?
                                Habit habit = Habit.habits.get(currentNumber);
                                SaveData saveData = new SaveData();
                                //DONE: Fix deletion after implementing firebase. We can null out habit to firebase.
                                if (habit instanceof EconomicHabit) {
                                    saveData.removeData(Habit.habits.get(currentNumber), 1);
                                    Habit.habits.remove(currentNumber);
                                } else if (habit instanceof DateHabit) {
                                    saveData.removeData(Habit.habits.get(currentNumber), 2);
                                    Habit.habits.remove(currentNumber);
                                }


                                Intent intent = new Intent(ShowHabitActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // CANCEL AND DO NOTHING
                            }
                        });
                // Create the AlertDialog object and return it
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        editButton = findViewById(R.id.btn_habitEdit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //DONE: editButton onclick
                Intent intent = new Intent(getBaseContext(), HabitActivity.class);
                intent.putExtra("TITLE", "Editing: " + Habit.habits.get(currentNumber).getTitle());
                intent.putExtra("CURRENT_HABIT_INDEX", currentNumber);
                startActivityForResult(intent, 500);
                //Snackbar.make(findViewById(android.R.id.content), "Not yet implemented", Snackbar.LENGTH_LONG).show();
            }
        });



        failedButton = findViewById(R.id.btn_habitFailed);
        failedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Habit habit = Habit.habits.get(currentNumber);

                if (habit instanceof EconomicHabit) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowHabitActivity.this);
                    builder.setTitle("Don't worry, even if you fail, you can still do this! How much did you spend?");
                    final EditText input = new EditText(ShowHabitActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SaveData saveData = new SaveData();
                            failedAmount = input.getText().toString();
                            ((EconomicHabit) habit).increaseFailedTotal(Integer.parseInt(failedAmount));
                            habit.setFailDate(new Date().getTime());
                            saveData.saveData(Habit.habits.get(currentNumber), 1);
                            recreate();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog ecoAlert = builder.create();
                    ecoAlert.show();

                } else if (habit instanceof DateHabit) {


                    //DONE: editButton onclick
                    final AlertDialog.Builder failedBuilder = new AlertDialog.Builder(ShowHabitActivity.this);
                    failedBuilder.setMessage("Don't worry, even if you fail, you can still do this! Do you want to reset the days since last fail?").setTitle("Failed habit?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Date currentTime = Calendar.getInstance().getTime();
                            habit.setFailDate(currentTime.getTime());
                            SaveData saveData = new SaveData();
                            saveData.saveData(Habit.habits.get(currentNumber), 2);

                            recreate();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // CANCEL AND DO NOTHING
                        }
                    });
                    // Create the AlertDialog object and return it
                    AlertDialog dialog = failedBuilder.create();
                    //ecoAlert.show();
                    dialog.show();
                    //dialog.show();
                }
            }
        });


        //Theme
        if (userTheme.equals("Light")){

            parentView.setBackgroundResource(R.color.colorPrimary);
            //deleteButton.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
            //editButton.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
            //failedButton.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);

            startViewImg.setColorFilter(ContextCompat.getColor(this, R.color.primaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);
            progressViewImg.setColorFilter(ContextCompat.getColor(this, R.color.primaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);
            //goalView.setTextColor(getResources().getColor(R.color.colorPrimary));

        }
        else if (userTheme.equals("Dark")){
            //goalView.setBackgroundResource(R.color.colorPrimaryDark);
            parentView.setBackgroundResource(R.color.colorPrimaryDark);
            deleteButton.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);
            editButton.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);
            failedButton.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);

            startViewImg.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);
            progressViewImg.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);
            goalView.setTextColor(getResources().getColor(R.color.primaryTextColorDark));

        }
    }

    private void setEcoData() {

        List<Entry> values = new ArrayList<>();
        List<Entry> values2 = new ArrayList<>();

        EconomicHabit habit = ((EconomicHabit) Habit.habits.get(currentNumber));

        Map<String, Integer> amountFailed = habit.getMappedFail();

        Log.d("setEcoData", Integer.toString(amountFailed.size()));


        if (amountFailed.size() > 0) {
            // TODO: Check if loop can be done better
            // TODO: Map is empty if app is restarted and a new element is not added to list.
            for (int i = 1; i <= habit.getDaysFromStart() + 1; i++) {

                for (Map.Entry<String, Integer> entry : amountFailed.entrySet()) {
                    // Get values from failed map
                    long mapDate = habit.convertMillisToDays(Long.parseLong(entry.getKey()));
                    float mapAmount = entry.getValue();

                    // Gets the current date to check up against failed map.
                    long startDateInDays = habit.convertMillisToDays(habit.getStartDate());
                    long dateToCheck = startDateInDays + i;

                    // Check if failed date and the current date to plot is the same
                    if (mapDate == dateToCheck) {
                        // Holds the amount from previous day, so it can be subtracted.
                        float previousAmount = (i-1) * habit.getPrice();

                        // Subtract amount the user failed with
                        values.add(new Entry(i, previousAmount - mapAmount,
                                getResources().getDrawable(R.drawable.star_on)));
                    } else {
                        // Add normally.
                        values.add(new Entry(i, habit.getPrice() * i,
                                getResources().getDrawable(R.drawable.star_on)));

                        values2.add(new Entry(i, habit.getAlternativePrice() * i,
                                getResources().getDrawable(R.drawable.star_on)));
                    }
                }
            }
        } else {
            for (int i = 0; i <= habit.getDaysFromStart(); i++) {
                values.add(new Entry(i, habit.getPrice() * i,
                        getResources().getDrawable(R.drawable.star_on)));

                values2.add(new Entry(i, habit.getAlternativePrice() * i,
                    getResources().getDrawable(R.drawable.star_on)));
            }
        }



        LineDataSet set1;
        LineDataSet set2;


        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) chart.getData().getDataSetByIndex(1);
            set1.setValues(values);
            set2.setValues(values2);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, "Would have used");
            set2 = new LineDataSet(values2, "The alternative would have cost");
            set1.setColor(R.color.chartsGreen1);
            set2.setColor(R.color.chartsBrown1);

            set1.setDrawIcons(false);
            set2.setDrawIcons(false);


            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            dataSets.add(set2);

            LineData data = new LineData(dataSets);
            data.setValueTextSize(10f);
            chart.setData(data);
            chart.animateXY(1000, 1000);
        }



    }

    public void setDateData(){
        chart.setVisibility(View.INVISIBLE);
        DateHabit habit = ((DateHabit) Habit.habits.get(currentNumber));
        Log.d("failureTimes", Integer.toString(habit.getFailureTimes()));
        Log.d("failureTimes", Float.toString(habit.getDaysFromStart()));
        ArrayList<PieEntry> entriesDate = new ArrayList<>();

        PieDataSet dataSetDate = new PieDataSet(entriesDate, "");
        PieData dataDate = new PieData(dataSetDate);
        ArrayList<Integer> colors = new ArrayList<>();
        //TODO: uses failure times not days, so if a a fail happens twice in a day it would subtract an addition day.
        if((habit.getDaysFromStart() - habit.getFailureTimes())< 0){
            entriesDate.add(new PieEntry(( 0 ), "Successful days"));
        } else {
            entriesDate.add(new PieEntry((  habit.getDaysFromStart() - habit.getFailureTimes() ), "Successful days"));
        }

        entriesDate.add(new PieEntry(( habit.getFailureTimes() ), "Failed days"));

        for (int c : ColorTemplate.MATERIAL_COLORS)
        colors.add(c);

        dataSetDate.setColors(colors);
        dataSetDate.setDrawIcons(false);
        dataSetDate.setValueTextSize(15f);
        dataSetDate.setValueTextColor(Color.BLACK);
        dateChart.setVisibility(View.VISIBLE);
        dateChart.setDrawHoleEnabled(false);
        dateChart.getDescription().setEnabled(false);
        dateChart.setDrawCenterText(false);
        dateChart.getLegend().setWordWrapEnabled(true);
        dateChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        dateChart.getLegend().setTextSize(15f);
        dateChart.setData(dataDate);
        dateChart.setExtraOffsets(0,0,0,0);
        dateChart.highlightValue(null);
        dateChart.setDrawEntryLabels(false);
        dateChart.invalidate();
        dateChart.animateXY(500, 500);

    }
    public static void setCurrentNumber(int number) {
        currentNumber = number;
    }

    public void onBoard(final View view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // make an
                SimpleTarget firstTarget = new SimpleTarget.Builder(ShowHabitActivity.this)
                        .setPoint(findViewById(R.id.btn_habitDelete))
                        .setShape(new Circle(50f))
                        .setTitle(getString(R.string.tutorial_habit_title_delete))
                        .setDescription(getString(R.string.tutorial_habit_desc_delete))
                        .build();

                //View two = findViewById(R.id.favorite_recycler_view);


                SimpleTarget secondTarget = new SimpleTarget.Builder(ShowHabitActivity.this)
                        .setPoint(findViewById(R.id.btn_habitEdit))
                        .setShape(new Circle(50f))
                        .setTitle(getString(R.string.tutorial_habit_title_edit))
                        .setDescription(getString(R.string.tutorial_habit_desc_edit))
                        .build();

                SimpleTarget thirdTarget = new SimpleTarget.Builder(ShowHabitActivity.this).setPoint(findViewById(R.id.btn_habitFailed))
                        .setShape(new Circle(50f))
                        .setTitle(getString(R.string.tutorial_habit_title_failed))
                        .setDescription(getString(R.string.tutorial_habit_desc_failed))
                        .build();



                Spotlight.with(ShowHabitActivity.this)
                        .setOverlayColor(R.color.background)
                        .setDuration(100L)
                        .setAnimation(new DecelerateInterpolator(2f))
                        .setTargets(firstTarget, secondTarget, thirdTarget)
                        .setClosedOnTouchedOutside(true)
                        .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                            @Override
                            public void onStarted() {
                                Toast.makeText(ShowHabitActivity.this, "spotlight is started", Toast.LENGTH_SHORT)
                                        .show();
                                //populateData();
                            }

                            @Override
                            public void onEnded() {
                                Toast.makeText(ShowHabitActivity.this, "spotlight is ended", Toast.LENGTH_SHORT).show();
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ShowHabitActivity.this);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean(SettingsActivity.KEY_PREF_ONBOARDSHOWHABIT, true);
                                editor.commit();
                            }
                        })
                        .start();
            }
        });
    }
}
