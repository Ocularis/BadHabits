package no.hiof.andrekar.badhabits;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import model.DateHabit;
import model.EconomicHabit;
import model.Habit;
import model.SaveData;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private Context mContext;

    private SharedPreferences sharedPref;

    View view;

    public MyAdapter(Context context) {
        mContext = context;
        sharedPref =
                PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);

        String userTheme = sharedPref.getString("key_theme","");

        if (userTheme.equals("Light")){

        }
        else if (userTheme.equals("Dark")){
            holder.habitName.setTextColor(mContext.getResources().getColor(GlobalConstants.EDIT_TEXT_COLOR));

            holder.habitGoal.setTextColor(mContext.getResources().getColor(GlobalConstants.EDIT_TEXT_COLOR));

            holder.habitDescription.setTextColor(mContext.getResources().getColor(GlobalConstants.EDIT_TEXT_COLOR));
            //holder.habitGoal.setTextColor(R.attr.colorPrimary);

            //holder.cardView.setBackgroundColor(mContext.getResources().getColor(R.color.primaryColorDark));
            //holder.parentLayout.setBackgroundResource(R.color.colorPrimaryDark);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (Habit.habits.get(position).getIsFavourite()) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RelativeLayout.LayoutParams(0,0));
        }
        else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        }
            /*
            if (position % 2 == 1) {
                holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor("#e8e8e8"));
                //  holder.imageView.setBackgroundColor(Color.parseColor("#FFFAF8FD"));
            }
            */


            //Currency from sharedPrefs
            SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(holder.itemView.getContext());

            String currency = sharedPref.getString
                (GlobalConstants.KEY_PREF_CURRENCY, "");


            holder.habitName.setText(Habit.habits.get(position).getTitle().toString());
            holder.habitDescription.setText(Habit.habits.get(position).getDescription().toString());

            //TODO: reminder
            if (Habit.habits.get(position).getClass() == EconomicHabit.class) {
                holder.habitGoal.setText(Float.toString(((EconomicHabit) Habit.habits.get(position)).getProgress()) +" "+currency);
                holder.habitIcon.setImageResource(R.drawable.ic_coin);
            } else if (Habit.habits.get(position).getClass() == DateHabit.class) {
                holder.habitGoal.setText(((DateHabit) Habit.habits.get(position)).getDateGoal());
                holder.habitIcon.setImageResource(R.drawable.ic_calendar_today);
            }

            if (Habit.habits.get(position).getIsFavourite()) {
                holder.favoriteButton.setImageResource(R.drawable.star_on);
            } else {
                holder.favoriteButton.setImageResource(R.drawable.star_off);
            }

            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View view) {
                                                           Log.d(TAG, "onClick: clicked on: " + Habit.habits.get(position).getTitle());
                                                           ShowHabitActivity.setCurrentNumber(position);
                                                           Intent intent = new Intent(mContext, ShowHabitActivity.class);
                                                           mContext.startActivity(intent);
                                                       }
                                                   });


            holder.favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImageButton imgB = view.findViewById(R.id.favoriteBtn);


                    if (Habit.habits.get(position).getIsFavourite()) {
                        Habit.habits.get(position).setFavourite(false);
                        holder.favoriteButton.setImageResource(R.drawable.star_off);
                    } else {
                        Habit.habits.get(position).setFavourite(true);
                        holder.favoriteButton.setImageResource(R.drawable.star_on);
                    }
                    SaveData saveData = new SaveData();
                    if (Habit.habits.get(position).getClass() == DateHabit.class) {
                        saveData.saveData(Habit.habits.get(position), 2);
                    } else if (Habit.habits.get(position).getClass() == EconomicHabit.class) {
                        saveData.saveData(Habit.habits.get(position), 1);
                    }

                    //Collections.sort(Habit.habits, Habit.HabitComparator);
                    notifyItemChanged(position);
                    //Collections.sort(Habit.habits, Habit.FavComparator_Help);
                    MainActivity.refreshUi();
                    MainActivity.favAdapter.updateFavs();
                    MainActivity.favAdapter.notifyDataSetChanged();
                }
            });
        }


    @Override
    public int getItemCount() {
        return Habit.habits.size();
    }


    public void addP(int pos) {
        notifyItemInserted(pos);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView habitName;
        TextView habitGoal;
        TextView habitDescription;
        ImageButton favoriteButton;
        RelativeLayout parentLayout;
        ImageView habitIcon;



        public ViewHolder(View itemView) {
            super(itemView);
            habitName = itemView.findViewById(R.id.habit_name);
            habitGoal = itemView.findViewById(R.id.habit_goal);
            habitDescription = itemView.findViewById(R.id.habit_description);
            favoriteButton = itemView.findViewById(R.id.favoriteBtn);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            habitIcon = itemView.findViewById(R.id.img_habitIcon);

        }
    }
}
