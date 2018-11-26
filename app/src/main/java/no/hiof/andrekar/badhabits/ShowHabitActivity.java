package no.hiof.andrekar.badhabits;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.SimpleTarget;
import org.threeten.bp.temporal.ChronoUnit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import model.DateHabit;
import model.EconomicHabit;
import model.Habit;
import model.SaveData;

public class ShowHabitActivity extends AppCompatActivity {

    public static int currentNumber;
    public ImageButton deleteButton;
    public ImageButton editButton;
    public ImageButton failedButton;
    LineChart chart;
    PieChart dateChart;

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
        // get user theme
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

        String currency = sharedPref.getString
                (GlobalConstants.KEY_PREF_CURRENCY, "");
        boolean onboarding = sharedPref.getBoolean(GlobalConstants.KEY_PREF_ONBOARDSHOWHABIT, false);
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
        //Sets up fields with information from the chosen habit
        if (habit instanceof DateHabit) {
            goalView.setText(((DateHabit)habit).getDaysSinceStart());
            progressView.setText(((DateHabit)habit).getDateGoal());
            Date date = new Date(habit.getStartDate());
            String dateText = df2.format(date);
            startView.setText(dateText);
            setDateData();
        } else {
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

        //alert for delete button
        deleteButton = findViewById(R.id.btn_habitDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowHabitActivity.this);
                builder.setMessage(getString(R.string.habit_cannot_be_reversed))
                        .setTitle(getString(R.string.delete_habit))
                        .setPositiveButton(getString(R.string.habit_yes_confirm), new DialogInterface.OnClickListener() {
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
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
            }
        });
        failedButton = findViewById(R.id.btn_habitFailed);
        failedButton.setOnClickListener(new FailedListener(currentNumber, failedButton.getContext(), true, this));


        //Theme
        if (userTheme.equals("Light")){
            parentView.setBackgroundResource(R.color.colorPrimary);
            startViewImg.setColorFilter(ContextCompat.getColor(this, R.color.primaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);
            progressViewImg.setColorFilter(ContextCompat.getColor(this, R.color.primaryLightColor), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else if (userTheme.equals("Dark")){
            parentView.setBackgroundResource(R.color.colorPrimaryDark);
            deleteButton.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);
            editButton.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);
            failedButton.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);

            startViewImg.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);
            progressViewImg.setColorFilter(ContextCompat.getColor(this, R.color.primaryTextColorDark), android.graphics.PorterDuff.Mode.SRC_IN);
            goalView.setTextColor(getResources().getColor(R.color.primaryTextColorDark));

        }
    }
    //function for populating eco habit's line chart
    private void setEcoData() {
        dateChart.setVisibility(View.GONE);
        chart.setVisibility(View.VISIBLE);
        List<Entry> values = new ArrayList<>();
        List<Entry> values2 = new ArrayList<>();

        EconomicHabit habit = ((EconomicHabit) Habit.habits.get(currentNumber));

        //map for eco habit's line chart
        Map<String, Integer> amountFailed = habit.getMappedFail();
        float totalPrice = 0;
        for (int i = 0; i <= habit.getDaysFromStart(); i++) {
                float price = habit.getPrice();
                for(Map.Entry<String, Integer> entry : amountFailed.entrySet()) {
                    //Log.d("MAPS", "Found map entry");
                    long mapDate = Habit.convertMillisToDays(Long.parseLong(entry.getKey()));
                    float mapAmount = entry.getValue();
                    //Log.d("MAPS", Long.toString(mapDate));
                    long startDateInDays = Habit.convertMillisToDays(habit.getStartDate());
                    long dateToCheck = startDateInDays + i;
                    //Log.d("MAPS", Long.toString(dateToCheck));
                    if( (mapDate) == dateToCheck) {
                        //Log.d("MAPS", "Found matching entry");
                        price = price - mapAmount;
                    }
                }
                totalPrice = totalPrice + price;
                values.add(new Entry(i, totalPrice,
                        getResources().getDrawable(R.drawable.star_on)));
                values2.add(new Entry(i, habit.getAlternativePrice() * i,
                    getResources().getDrawable(R.drawable.star_on)));
            }

        LineDataSet set1;
        LineDataSet set2;

        //Filling eco habit's line chart
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) chart.getData().getDataSetByIndex(1);
            if(values.size() > 10) {
                set1.setValues(values.subList(values.size()-10, values.size()));
                set2.setValues(values2.subList(values.size()-10, values.size()));
            } else {
                set1.setValues(values);
                set2.setValues(values2);
            }
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            if (values.size() > 10) {
                set1 = new LineDataSet(values.subList(values.size() - 10, values.size()), getString(R.string.habit_would_have_used));
                set2 = new LineDataSet(values2.subList(values.size() - 10, values.size()), getString(R.string.habit_alternative_would_have_cost));
            } else {
                set1 = new LineDataSet(values, getString(R.string.habit_would_have_used));
                set2 = new LineDataSet(values2, getString(R.string.habit_alternative_would_have_cost));
            }
            set1.setColor(ColorTemplate.MATERIAL_COLORS[0]);
            set2.setColor(ColorTemplate.MATERIAL_COLORS[1]);
            set1.setCircleColor(ColorTemplate.MATERIAL_COLORS[0]);
            set2.setCircleColor(ColorTemplate.MATERIAL_COLORS[1]);
            set1.setDrawIcons(false);
            set2.setDrawIcons(false);
            set1.setLineWidth(5f);
            set2.setLineWidth(5f);
            set1.setDrawCircleHole(true);
            set2.setDrawCircleHole(true);
            set1.setCircleHoleRadius(3f);
            set2.setCircleHoleRadius(3f);
            set1.setCircleRadius(7f);
            set2.setCircleRadius(7f);

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            dataSets.add(set2);

            LineData data = new LineData(dataSets);
            data.setValueTextSize(10f);
            XAxis xAxis = chart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setDrawLabels(false);
            xAxis.setDrawAxisLine(false);

            YAxis yAxis1 = chart.getAxisLeft();
            yAxis1.setDrawGridLines(false);
            yAxis1.setDrawLabels(false);
            yAxis1.setDrawAxisLine(false);

            YAxis yAxis2 = chart.getAxisRight();
            yAxis2.setDrawGridLines(false);
            yAxis2.setDrawLabels(false);
            yAxis2.setDrawAxisLine(false);

            chart.getDescription().setEnabled(false);
            chart.setDoubleTapToZoomEnabled(false);
            chart.setPinchZoom(false);

            Legend legend = chart.getLegend();
            legend.setTextSize(16f);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);

            chart.setData(data);
            chart.animateXY(1000, 1000);
        }
    }
    //function for populating date habit's pie chart
    public void setDateData(){
        chart.setVisibility(View.INVISIBLE);
        DateHabit habit = ((DateHabit) Habit.habits.get(currentNumber));
        //Log.d("failureTimes", Integer.toString(habit.getFailureTimes()));
        //Log.d("failureTimes", Float.toString(habit.getDaysFromStart()));
        ArrayList<PieEntry> entriesDate = new ArrayList<>();

        PieDataSet dataSetDate = new PieDataSet(entriesDate, "");
        PieData dataDate = new PieData(dataSetDate);
        ArrayList<Integer> colors = new ArrayList<>();
        //DONE: uses failure times not days, so if a a fail happens twice in a day it would subtract an addition day.
        if((habit.getDaysFromStart() - habit.getFailureTimes())< 0){
            entriesDate.add(new PieEntry(( 0 ), getString(R.string.chart_successful_days)));
        } else {
            entriesDate.add(new PieEntry((  habit.getDaysFromStart() - habit.getFailureTimes() ), getString(R.string.chart_successful_days)));
        }
        entriesDate.add(new PieEntry(( habit.getFailureTimes() ), getString(R.string.chart_failed_days)));

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

    //On boarding function
    public void onBoard(final View view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                SimpleTarget firstTarget = new SimpleTarget.Builder(ShowHabitActivity.this)
                        .setPoint(findViewById(R.id.btn_habitDelete))
                        .setShape(new Circle(50f))
                        .setTitle(getString(R.string.tutorial_habit_title_delete))
                        .setDescription(getString(R.string.tutorial_habit_desc_delete))
                        .build();

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
                                //Toast.makeText(ShowHabitActivity.this, "spotlight is started", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onEnded() {
                                //Toast.makeText(ShowHabitActivity.this, "spotlight is ended", Toast.LENGTH_SHORT).show();
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ShowHabitActivity.this);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean(GlobalConstants.KEY_PREF_ONBOARDSHOWHABIT, true);
                                editor.commit();
                            }
                        })
                        .start();
            }
        });
    }
}
