package no.hiof.andrekar.badhabits;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.takusemba.spotlight.OnSpotlightStateChangedListener;
import com.takusemba.spotlight.Spotlight;
import com.takusemba.spotlight.shape.Circle;
import com.takusemba.spotlight.target.SimpleTarget;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import model.SaveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import model.DateHabit;
import model.EconomicHabit;
import model.Habit;

import static java.lang.Math.abs;
import static model.Habit.habits;

//TODO: Verify days till finished maths (-65 with test dataset)
//TODO: Verify days till finnished maths (44 with dataset, should be 51)


public class MainActivity extends AppCompatActivity implements rec_SwipeDelete.RecyclerItemTouchHelperListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public static MyAdapter adapter;
    public static MyFavoriteAdapter favAdapter;
    private static float totalSaved, totalDays, failedTotal, daysTillFinishedDate,daysTillFinishedEco,longestDateHabit;
    private static long longestStreakEco,longestStreakDate;
    private static TextView ecoBottomText, dateBottomText, longestStreakEcoText,longestStreakDateText,failedTotalText, daysTillFinishedDateText,daysTillFinishedEcoText,longestDateHabitText;
    private static SwipeRefreshLayout swipeContainer;
    private static String longestStreakName,longestDateName;
    private static PieChart bottomSheetPieEco, bottomSheetPieDate;
    public static View mainLayout;
    private static RecyclerView recyclerView;
    private static RecyclerView favoriteRecyclerView;
    private static Context context;
    private SaveData saveData = new SaveData();


    public static SharedPreferences preferences;
    public static AlarmManager mAlarmManager;
    public static NotificationChannel notificationChannel = null;

    //Checking if network is available
    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = preferences.getString("key_theme", "");
        AndroidThreeTen.init(this);

        //Setting theme depending on what is stored in shared preferences
        if (userTheme.equals("Light")){
            setTheme(R.style.LightTheme);
        }
        else if (userTheme.equals("Dark")){
            setTheme(R.style.DarkTheme);
        }
        else
            setTheme(R.style.AppTheme);

        GlobalConstants.update(this);

        super.onCreate(savedInstanceState);


        if(mDatabase == null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
            mDatabase = database.getReference();
            database.getReference(mAuth.getUid()).keepSynced(true);
        }

        mAuth = FirebaseAuth.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Login Main", "signInAnonymously:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Login Main", "signInAnonymously:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }



                            // ...
                        }
                    });
        } else {
            FirebaseUser currentUser = mAuth.getCurrentUser();
        }

        if (!isNetworkAvailable(this) && (mAuth.getCurrentUser() == null)) {
            Log.d("Internet", "This is false to has Internet");
            setContentView(R.layout.content_main_nointernet);
        } else {
            if (mDatabase == null) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.setPersistenceEnabled(true);
                mDatabase = database.getReference();
                database.getReference(mAuth.getUid()).keepSynced(true);
            }
            themefunc();
            setContentView(R.layout.activity_main);



            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            setContentView(R.layout.activity_main);
            //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            //setSupportActionBar(toolbar);

            ecoBottomText = findViewById(R.id.bottom_sheet_top_eco);
            dateBottomText = findViewById(R.id.bottom_sheet_top_date);
            longestStreakEcoText = findViewById(R.id.longestStreakEcoText);
            longestStreakDateText = findViewById(R.id.longestStreakDateText);
            daysTillFinishedDateText = findViewById(R.id.daysTillFinishedDateText);
            daysTillFinishedEcoText = findViewById(R.id.daysTillFinishedEcoText);
            longestDateHabitText = findViewById(R.id.longestDateHabitText);
            failedTotalText = findViewById(R.id.failedTotalText);
            bottomSheetPieEco = findViewById(R.id.chart_bottomSheetPieEco);
            bottomSheetPieDate = findViewById(R.id.chart_bottomSheetPieDate);

            bottomSheet();

            swipeContainer = (SwipeRefreshLayout) findViewById(R.id.recyclerSwipeContainer);
            // Setup refresh listener which triggers new data loading
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Your code to refresh the list here.
                    // Make sure you call swipeContainer.setRefreshing(false)
                    // once the network request has completed successfully.
                    saveData.readFromFile();
                    //saveData.readFromFile();
                }
            });
            // Configure the refreshing colors
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);


            /*code to ask user for permission to store data.
            int REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CODE);
            //*/

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_addHabit);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseContext(), HabitActivity.class);
                    intent.putExtra("TITLE", "Add new habit");
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivityForResult(intent, 500);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });

            //DONE: Implement this into habits model?
            Collections.sort(habits, Habit.HabitComparator);

            initRecyclerView();
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                SaveData saveData = new SaveData();
                saveData.readFromFile();
            }
            updateBottomSheet();


            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

            mainLayout = findViewById(R.id.main_parent_layout);
        }
    }


    public static void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationChannel == null) {

            notificationChannel = new NotificationChannel(GlobalConstants.CHANNEL_ID,
                    GlobalConstants.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(notificationChannel);
        }
        mAlarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        TimeZone tz = TimeZone.getTimeZone("CET");

        if (Habit.habits != null && preferences.getBoolean(GlobalConstants.KEY_PREF_NOT_ON, false)) {

            long timeLeft = 0;
            boolean first = true;
            DateHabit closeHabit = null;

            Calendar rightNow =  Calendar.getInstance();
            long offsetFromUtc = tz.getOffset(rightNow.getTimeInMillis());

            int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY); // return the hour in 24 hrs format (ranging from 0-23)
            int currentMin = rightNow.get(Calendar.MINUTE);

            long desiredTime = (long)((preferences.getFloat(GlobalConstants.KEY_PREF_NOT_TIME, 0))*60f*60f*1000f);
            //TODO: Find out why some need - 1 hour and others don't
            long timeToNote = (desiredTime) - (TimeUnit.HOURS.toMillis(currentHourIn24Format)) - TimeUnit.MINUTES.toMillis(currentMin);
            long fullDaysInMillis = 0;

            //DONE: THE RIGHT calculations
            for (int i = 0; i < Habit.habits.size() - 1; i++) {
                if (Habit.habits.get(i) instanceof DateHabit) {
                    //TODO: Find out why I need plus 1 day and others don't
                    long tempDaysInMillis = ((DateHabit) Habit.habits.get(i)).getDateGoalMillis() + TimeUnit.DAYS.toMillis(1);
                    long tempFullDaysInMillis = TimeUnit.DAYS.toMillis(TimeUnit.MILLISECONDS.toDays(tempDaysInMillis));

                    if (tempFullDaysInMillis + timeToNote > 0)  {

                        if (first) {
                            timeLeft = (tempFullDaysInMillis + timeToNote);
                            closeHabit = (DateHabit) Habit.habits.get(i);
                            first = false;

                        } else if (timeLeft > (tempFullDaysInMillis + timeToNote)) {
                            fullDaysInMillis = tempFullDaysInMillis;
                            closeHabit = (DateHabit) Habit.habits.get(i);
                            timeLeft = (tempFullDaysInMillis + timeToNote);
                        }
                    }
                }
            }
            if(closeHabit != null) {

                Intent intent = new Intent(context, NotificationUpdate.class);
                intent.putExtra("habitName", closeHabit.getTitle());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1234,
                        intent, 0);

                //long accurateTime = System.currentTimeMillis() + timeLeft + offsetFromUtc;
                ZonedDateTime zdt = LocalDateTime.now().atZone(ZoneId.systemDefault());
                long accurateTime = zdt.toInstant().toEpochMilli() + timeLeft;

                //Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime().toInstant() + timeLeft;

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, new Date(accurateTime).getTime(), pendingIntent);
                }else
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, new Date(accurateTime).getTime(), pendingIntent);

                Log.d("Notification", "Notification for: \""+ closeHabit.getTitle() + "\" created and will occur in: " +  (TimeUnit.MILLISECONDS.toMinutes(timeLeft)) + " min");
                Log.d("Notification", "Notification for: \""+ closeHabit.getTitle() + "\" created and will occur at: " +  new Date(accurateTime).toString());
            }
        }
    }


    public void bottomSheet() {
        // get the bottom sheet view
        LinearLayout llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        final ImageButton btnBottomSheet = (ImageButton) findViewById(R.id.btn_bottomSheetToggle);

        // init the bottom sheet behavior
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        btnBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    btnBottomSheet.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more));
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    btnBottomSheet.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less));
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

        });

        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetPieEco.setVisibility(View.INVISIBLE);
        bottomSheetPieDate.setVisibility(View.INVISIBLE);

        // set hideable or not
        bottomSheetBehavior.setHideable(false);

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;  
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        //DO stuff when expanded
                        btnBottomSheet.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more));
                        bottomSheetPieEco.setVisibility(View.VISIBLE);
                        bottomSheetPieDate.setVisibility(View.VISIBLE);
                        bottomSheetPieEco.animateY(500, Easing.EaseInOutQuad);

                        //bottomSheetPieEco.animateXY(500, 500);
                        bottomSheetPieDate.animateY(500, Easing.EaseInOutQuad);
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        //DO stuff when collapsed
                        btnBottomSheet.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less));
                        bottomSheetPieEco.setVisibility(View.INVISIBLE);
                        bottomSheetPieDate.setVisibility(View.INVISIBLE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING: {

                    }

                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MAIN", "Paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() != null ) {
            updateRecyclerView(false, true, true);
            refreshUi();
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && resultCode == RESULT_OK) {
            adapter.notifyItemInserted(habits.size());
            favAdapter.notifyItemInserted(habits.size());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.t
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
            startActivity(intent);

        }
        if (id == R.id.action_populate) {
            populateData(true);
            return true;
        }
        if (id == R.id.action_remove) {
            removeData();
            return true;
        }
        if (id == R.id.action_refresh) {
            updateRecyclerView();
            refreshUi();
        }
        if (id == R.id.action_SortMenuAlph) {
            Collections.sort(Habit.habits, Habit.HabitComparator);
            favAdapter.notifyDataSetChanged();
            adapter.notifyItemRangeChanged(0, Habit.habits.size());
        }
        if (id == R.id.action_SortMenuRemain) {
            Collections.sort(Habit.habits, Habit.HabitComparatorGoal);
            favAdapter.notifyDataSetChanged();
            adapter.notifyItemRangeChanged(0, Habit.habits.size());
        }
        if (id == R.id.action_SortMenuType) {
            Collections.sort(Habit.habits, Habit.HabitComparatorType);
            favAdapter.notifyDataSetChanged();
            adapter.notifyItemRangeChanged(0, Habit.habits.size());
        }

        return super.onOptionsItemSelected(item);
    }

    private void  initRecyclerView(){

        favoriteRecyclerView = findViewById(R.id.favorite_recycler_view);

        favAdapter = new MyFavoriteAdapter(this);
        favoriteRecyclerView.setAdapter(favAdapter);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        favoriteRecyclerView.setLayoutParams(new ConstraintLayout.LayoutParams(0,0));

        recyclerView = findViewById(R.id.recycler_view);
        adapter = new MyAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new rec_SwipeDelete(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean onboarding = sharedPref.getBoolean(GlobalConstants.KEY_PREF_ONBOARD, false);

        Log.d("Sharedpref", Boolean.toString(onboarding));

        if (!onboarding) {
            ViewGroup.LayoutParams params = favoriteRecyclerView.getLayoutParams();
            params.height = 350;
            favoriteRecyclerView.setLayoutParams(params);
            onBoard(findViewById(R.id.fab_addHabit));
        }
    }


    private void populateData(boolean save) {
        ArrayList<Habit> testHabits = new ArrayList<Habit>();
        Date date = new Date();
        long dayms = 86400000;
        Habit gumHabit = new EconomicHabit(getString(R.string.test_habit_1), getString(R.string.test_habit_1_description), (date.getTime() - (dayms*8)), 0, 800, 10, false);
        Habit sodaHabit = new EconomicHabit(getString(R.string.test_habit_2), getString(R.string.test_habit_2_description), (date.getTime() - (dayms*15)), 10, 500, 20, true);
        Habit poop = new EconomicHabit(getString(R.string.test_habit_3), getString(R.string.test_habit_3_description), (date.getTime() - (dayms*4)), 10, 100, 100, false);
        Habit scoop = new DateHabit(getString(R.string.test_habit_4), getString(R.string.test_habit_4_description), (date.getTime() - (dayms*15)), 60, false);
        Habit date2 = new DateHabit(getString(R.string.test_habit_5), getString(R.string.test_habit_5_description), (date.getTime() - (dayms*12)), 20, false);
        date2.setFailureTimes(3);
        date2.setFailDate(date.getTime()-(dayms*7));
        scoop.setFailureTimes(6);
        scoop.setFailDate(date.getTime()-(dayms*4));
        sodaHabit.setFailDate(date.getTime() - (dayms*3));
        ((EconomicHabit) sodaHabit).setFailedTotal(100);
        sodaHabit.setFailureTimes(1);


        testHabits.add((EconomicHabit) gumHabit);
        testHabits.add((EconomicHabit) sodaHabit);
        testHabits.add((EconomicHabit) poop);
        testHabits.add((DateHabit) scoop);
        testHabits.add((DateHabit) date2);
        SaveData saveData = new SaveData();

        for (Habit habit : testHabits) {
            if (habit instanceof DateHabit) {
                habits.add((DateHabit) habit);
                adapter.notifyItemInserted(habits.size());
                favAdapter.notifyItemInserted(habits.size());
                favAdapter.notifyDataSetChanged();
                if(save) {
                    saveData.saveData(habit, GlobalConstants.DATE_HABIT);
                }
            } else if (habit instanceof EconomicHabit) {
                habits.add((EconomicHabit) habit);
                adapter.notifyItemInserted(habits.size());
                favAdapter.notifyItemInserted(habits.size());
                favAdapter.notifyDataSetChanged();
                if(save) {
                    saveData.saveData(habit, GlobalConstants.ECO_HABIT);
                }
            }
        }
        testHabits.clear();
        Collections.sort(Habit.habits, Habit.HabitComparator);
        favAdapter.notifyDataSetChanged();
        refreshUi();
        }

        private void removeData() {
            SaveData saveData = new SaveData();
            for (Habit habit: habits) {
                if(habit instanceof EconomicHabit) {
                    saveData.removeData(habit, GlobalConstants.ECO_HABIT);
                } else if(habit instanceof DateHabit) {
                    saveData.removeData(habit, GlobalConstants.DATE_HABIT);
                }
            }
            adapter.notifyItemRangeRemoved(0, habits.size());
            favAdapter.notifyItemRangeRemoved(0, habits.size());
            habits.clear();
            updateBottomSheet();
            refreshUi();
        }

        public static void updateRecyclerView(){
            adapter.notifyDataSetChanged();
            favAdapter.notifyDataSetChanged();
            updateBottomSheet();
            setRefreshing();
            //runLayoutAnimation(true);
        }
        public static void updateRecyclerView(boolean animation, boolean bottomsheet, boolean animatefav){
        adapter.notifyDataSetChanged();
        favAdapter.notifyDataSetChanged();
        if(bottomsheet) {
            updateBottomSheet();
        }
        if (animation)
        {
            //runLayoutAnimation(animatefav);
        }
        setRefreshing();
        }


    public static void setRefreshing() {
        if(swipeContainer.isRefreshing()) {
            swipeContainer.setRefreshing(false);
        }
    }


    public static void updateBottomSheet() {
        totalSaved = 0;
        totalDays = 0;
        failedTotal = 0;
        longestDateHabit = 0;
        daysTillFinishedDate = -1 ;
        daysTillFinishedEco = -1 ;
        longestStreakEco = -1;
        longestStreakDate = -1;
        ArrayList<PieEntry> entriesEco = new ArrayList<>();
        ArrayList<PieEntry> entriesDate = new ArrayList<>();

        //Currency from sharedPrefs
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);

        String currency = sharedPref.getString
                (GlobalConstants.KEY_PREF_CURRENCY, "");


        for (Habit habit: habits) {
            if (habit instanceof EconomicHabit) {
                if (((EconomicHabit) habit).getProgress() < 0) {
                    totalSaved += ((EconomicHabit) habit).getProgress();
                } else {
                    totalSaved += 0;
                }
                //totalSaved += ((EconomicHabit) habit).getProgress();
                failedTotal += (((EconomicHabit) habit).getFailedTotal());
                ecoBottomText.setText(context.getString(R.string.left_for_goals) + Math.abs(totalSaved) + " " + currency) ;
                if(failedTotal > 0){
                    failedTotalText.setText(context.getString(R.string.total_spent) + failedTotal);
                } else {
                    failedTotalText.setText(context.getString(R.string.no_fails));
                }
                entriesEco.add(new PieEntry(abs(((EconomicHabit) habit).getProgress()), habit.getTitle()));

                if (Habit.getDateDiff(habit.getFailDate(), new Date().getTime(), ChronoUnit.DAYS) > longestStreakEco && habit.getFailDate() != 0) {
                    longestStreakEco = Habit.getDateDiff(habit.getFailDate(), new Date().getTime(),  ChronoUnit.DAYS);
                    //Log.d("BottomSheet", Long.toString(longestStreakEco));
                    longestStreakName = habit.getTitle();
                    if (longestStreakEco == -1) {
                        longestStreakEcoText.setText(context.getString(R.string.no_fails));
                    }else {
                        longestStreakEcoText.setText(context.getString(R.string.days_since_last_fail) + longestStreakEco + " (" + longestStreakName + ")");
                    }
                }
                //TODO: double check that the math here is correct.
                float dateGoalL = Habit.getDateDiff(habit.getStartDate(), new Date().getTime(), ChronoUnit.DAYS);
                float saved = (( ((EconomicHabit) habit).getGoalValue() + (dateGoalL*((EconomicHabit) habit).getAlternativePrice()) ) - (dateGoalL*((EconomicHabit) habit).getPrice()) + ((EconomicHabit) habit).getFailedTotal());
                float pricePerDay = ( ((EconomicHabit) habit).getPrice() - ((EconomicHabit) habit).getAlternativePrice());
                float daysRemaining = (saved/pricePerDay);
                if (daysRemaining > daysTillFinishedEco && ((EconomicHabit) habit).getGoalValue() != 0) {
                    daysTillFinishedEco = daysRemaining;
                    daysTillFinishedEcoText.setText(context.getString(R.string.days_till_finished) + Math.round(daysRemaining));
                }



            } if (habit instanceof DateHabit) {
                totalDays += habit.getDaysFromStart();
                dateBottomText.setText(context.getString(R.string.days_without) + totalDays + context.getString(R.string.days));
                entriesDate.add(new PieEntry(Habit.getDateDiff(habit.getStartDate(), new Date().getTime(), ChronoUnit.DAYS), habit.getTitle()));
                if (Habit.getDateDiff(habit.getFailDate(), new Date().getTime(), ChronoUnit.DAYS) > longestStreakDate && habit.getFailDate() != 0) {
                    longestStreakDate = Habit.getDateDiff(habit.getFailDate(), new Date().getTime(),  ChronoUnit.DAYS);
                    Log.d("BottomSheet", Long.toString(longestStreakDate));
                    longestStreakName = habit.getTitle();
                    if (longestStreakDate == -1) {
                        longestStreakDateText.setText(context.getString(R.string.no_fails));
                    }else {
                        longestStreakDateText.setText(context.getString(R.string.days_since_last_fail) + longestStreakDate + " (" + longestStreakName + ")");
                    }
                }
                if (Habit.getDateDiff(habit.getStartDate(), new Date().getTime(), ChronoUnit.DAYS) > longestDateHabit && habit.getStartDate() != 0) {
                    longestDateHabit = Habit.getDateDiff(habit.getStartDate(), new Date().getTime(),  ChronoUnit.DAYS);
                    longestDateName = habit.getTitle();
                    if (longestDateHabit == -1) {
                        longestDateHabitText.setText(context.getString(R.string.no_date_habits));
                    }else {
                        longestDateHabitText.setText(context.getString(R.string.longest_habit) + longestDateHabit + " (" + longestDateName + ")");
                    }
                }
                Calendar c = Calendar.getInstance();
                c.setTime(new Date(habit.getStartDate()));
                c.add(Calendar.DATE,((DateHabit)habit).getDateGoalValue());
                if (Habit.getDateDiff(new Date().getTime(),c.getTimeInMillis() , ChronoUnit.DAYS) > daysTillFinishedDate && ((DateHabit) habit).getDateGoalValue() != 0) {
                    daysTillFinishedDate = Habit.getDateDiff(new Date().getTime(),c.getTimeInMillis(),  ChronoUnit.DAYS);
                    daysTillFinishedDateText.setText(context.getString(R.string.days_till_finished) + daysTillFinishedDate);
                }
            }




        }
        PieDataSet dataSetEco = new PieDataSet(entriesEco, "");
        PieData dataEco = new PieData(dataSetEco);
        PieDataSet dataSetDate = new PieDataSet(entriesDate, "");
        PieData dataDate = new PieData(dataSetDate);
        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        dataSetEco.setColors(colors);
        dataSetEco.setDrawIcons(false);

        dataSetDate.setColors(colors);
        dataSetDate.setDrawIcons(false);

        bottomSheetPieEco.getDescription().setEnabled(false);
//            bottomSheetPieEco.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        bottomSheetPieEco.getLegend().setWordWrapEnabled(true);
        bottomSheetPieEco.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        bottomSheetPieEco.setDrawEntryLabels(false);
        bottomSheetPieEco.setDragDecelerationFrictionCoef(2f);
        bottomSheetPieEco.setDrawHoleEnabled(false);
        bottomSheetPieEco.setData(dataEco);
        bottomSheetPieEco.highlightValue(null);
        bottomSheetPieEco.invalidate();

        bottomSheetPieDate.getDescription().setEnabled(false);
        bottomSheetPieDate.getLegend().setWordWrapEnabled(true);
        bottomSheetPieDate.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        bottomSheetPieDate.setDrawEntryLabels(false);
        bottomSheetPieDate.setDrawHoleEnabled(false);
        bottomSheetPieDate.setDragDecelerationFrictionCoef(2f);
        bottomSheetPieDate.setData(dataDate);
        bottomSheetPieDate.highlightValue(null);
        bottomSheetPieDate.invalidate();

    }

    public void onBoard(final View view) {


        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // make an
                SimpleTarget firstTarget = new SimpleTarget.Builder(MainActivity.this)
                        .setPoint(findViewById(R.id.fab_addHabit))
                        .setShape(new Circle(200f))
                        .setTitle(getString(R.string.tutorial_main_title_welcome))
                        .setDescription(getString(R.string.tutorial_main_desc_newhabit))
                        .build();

                //View two = findViewById(R.id.favorite_recycler_view);
                View two = findViewById(R.id.favorite_recycler_view);

                int[] twoLocation = new int[2];
                two.getLocationInWindow(twoLocation);
                float twoX = twoLocation[0] + 150;
                float twoY = twoLocation[1] + 150;

                SimpleTarget secondTarget = new SimpleTarget.Builder(MainActivity.this)
                        .setPoint(twoX, twoY)
                        .setShape(new Circle(250f))
                        .setTitle(getString(R.string.tutorial_main_title_favourite))
                        .setDescription(getString(R.string.tutorial_main_desc_favdisplay))
                        .build();



                //View three = recyclerView.getLayoutManager().findViewByPosition(1).findViewById(R.id.favoriteBtn);
                View three = findViewById(R.id.recycler_view);
                int[] threeLocation = new int[2];
                three.getLocationInWindow(threeLocation);
                float threeX = threeLocation[0] + 1015;
                float threeY = threeLocation[1] + 55;

                SimpleTarget thirdTarget = new SimpleTarget.Builder(MainActivity.this).setPoint(threeX, threeY)
                        .setShape(new Circle(100f))
                        .setTitle(getString(R.string.tutorial_main_title_favourite))
                        .setDescription(getString(R.string.tutorial_main_desc_favadd))
                        .build();

                float fourX = 1025;
                float fourY = 125;

                SimpleTarget fourthTarget = new SimpleTarget.Builder(MainActivity.this).setPoint(fourX, fourY)
                        .setShape(new Circle(50f))
                        .setTitle(getString(R.string.tutorial_main_title_settings))
                        .setDescription(getString(R.string.tutorial_main_desc_settings))
                        .build();

                Spotlight.with(MainActivity.this)
                        .setOverlayColor(R.color.background)
                        .setDuration(100L)
                        .setAnimation(new DecelerateInterpolator(2f))
                        .setTargets(firstTarget, thirdTarget, secondTarget, fourthTarget)
                        .setClosedOnTouchedOutside(true)
                        /*.setOnSpotlightStateListener(
                                new OnSpotlightStateChangedListener() {
                            @Override
                            public void onStarted() {
                                Toast.makeText(MainActivity.this, "spotlight is started", Toast.LENGTH_SHORT)
                                        .show();
                                populateData(false);
                            }

                            @Override
                            public void onEnded() {
                                Toast.makeText(MainActivity.this, "spotlight is ended", Toast.LENGTH_SHORT).show();
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean(GlobalConstants.KEY_PREF_ONBOARD, true);
                                editor.commit();
                                ViewGroup.LayoutParams params = favoriteRecyclerView.getLayoutParams();
                                params.height = 0;
                                favoriteRecyclerView.setLayoutParams(params);
                                SaveData saveData = new SaveData();
                                saveData.readFromFile();

                            }
                        })
                        */
                        .start();
            }
        });
    }

    private void themefunc() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = preferences.getString("key_theme", "");
        context = getBaseContext();

        if (userTheme.equals("Light")){
            setTheme(R.style.LightTheme);
        }
        else if (userTheme.equals("Dark")){
            setTheme(R.style.DarkTheme);
        }
        else
            setTheme(R.style.AppTheme);

    }

    public static void refreshUi() {

    //TODO: Look into bug if
        updateBottomSheet();
        final int Height = 350;
        if (!Habit.getHaveFavorite() && (favoriteRecyclerView.getLayoutParams().height != 0)) {
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    ViewGroup.LayoutParams params = favoriteRecyclerView.getLayoutParams();
                    params.height = (int) (Height - (Height * interpolatedTime));
                    favoriteRecyclerView.setLayoutParams(params);
                }
            };
            a.setDuration(500);
            favoriteRecyclerView.startAnimation(a);
        } else if (Habit.getHaveFavorite()){
            if (favoriteRecyclerView.getLayoutParams().height == 0) {
                Animation a = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        ViewGroup.LayoutParams params = favoriteRecyclerView.getLayoutParams();
                        params.height = (int) ((Height * interpolatedTime));
                        favoriteRecyclerView.setAlpha(1 * interpolatedTime);
                        favoriteRecyclerView.setLayoutParams(params);
                    }
                };
                a.setDuration(500);
                favoriteRecyclerView.startAnimation(a);
            }
        }
    };

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof MyAdapter.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = Habit.habits.get(viewHolder.getAdapterPosition()).getTitle();
            int habitType = 0;

            // backup of removed item for undo purpose
            final Habit deletedHabit = Habit.habits.get(viewHolder.getAdapterPosition());
            if (deletedHabit instanceof DateHabit) {
                habitType = GlobalConstants.DATE_HABIT;
            } else if (deletedHabit instanceof EconomicHabit) {
                habitType = GlobalConstants.ECO_HABIT;
            }
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view, and Habit arraylist.
            Habit.habits.remove(viewHolder.getAdapterPosition());
            final SaveData saveData = new SaveData();
            saveData.removeData(deletedHabit, habitType);
            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(recyclerView, name + " " + getString(R.string.removed), Snackbar.LENGTH_LONG);
            snackbar.setAction(getString(R.string.undo), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(deletedHabit instanceof DateHabit) {
                        Habit.habits.add((DateHabit) deletedHabit);
                        adapter.notifyItemInserted(Habit.habits.size());
                        saveData.saveData(deletedHabit, GlobalConstants.DATE_HABIT);
                    } else if (deletedHabit instanceof EconomicHabit) {
                        Habit.habits.add((EconomicHabit) deletedHabit);
                        adapter.notifyItemInserted(Habit.habits.size());
                        saveData.saveData(deletedHabit, GlobalConstants.ECO_HABIT);
                    }
                    // undo is selected, restore the deleted item

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}