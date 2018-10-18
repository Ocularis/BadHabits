package model;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//DONE: Implement maths in class? IE: getters for progress?

public class DateHabit extends Habit {
    private Integer dateGoalValue;

    public static ArrayList<DateHabit> dateHabits = new ArrayList<DateHabit>();

    public DateHabit(String title, String description, Date startDate, Integer dateGoalValue) {
        super(title, description, startDate);
        this.dateGoalValue = dateGoalValue;
    }

    public Integer getDateGoalValue() {
        return dateGoalValue;
    }

    public void setDateGoalValue(Integer dateGoalValue) {
        this.dateGoalValue = dateGoalValue;
    }

    public String getDateGoal(){
        String dateGoal;
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyy");
        Calendar c = Calendar.getInstance();
        c.setTime(this.getStartDate());
        c.add(Calendar.DATE,this.getDateGoalValue());
        Date endDate = new Date(c.getTimeInMillis());
        //dateGoal = simpleDateFormat.format(endDate);
        long dateGoalL = ChronoUnit.DAYS.between(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate(), endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate());
        dateGoal = Long.toString(dateGoalL);
        if (Long.valueOf(dateGoalL) > 0) {
            return dateGoal + " Remaining";
        }
        else return "Mål nådd";
    }

}
