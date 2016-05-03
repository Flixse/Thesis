package com.tomandfelix.stapp2.activity;

/**
 * Created by Tom on 18/12/2014.
 * Every activity after the login screen will extend this activity, it takes care of the connection with the service
 */

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.Challenge;
import com.tomandfelix.stapp2.persistency.Solo;

import java.util.Locale;

public abstract class ServiceActivity extends ActionBarActivity {

    protected Toolbar toolbar;
    protected StApp app;
    static public final String PROFILE_VIEW_TUTORIAL = "profile_view_tutorial";
    static public final String LEADERBOARD_VIEW_TUTORIAL = "leaderboard_view_tutorial";
    static public final String GRAPH_VIEW_TUTORIAL = "graph_view_tutorial";
    static public final String SOLO_QUEST_VIEW_TUTORIAL = "solo_quest_view_tutorial";
    static public final String OPEN_SOLO_QUEST_VIEW_TUTORIAL = "open_solo_quest_view_tutorial";
    static public final String CHALLENGE_VIEW_TUTORIAL = "challenge_view_tutorial";
    static public final String CHALLENGE_LEADERBOARD_VIEW_TUTORIAL = "challenge_leaderboard_view_tutorial";
    static public final String SETTINGS_VIEW_TUTORIAL = "settings_view_tutorial";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.app = (StApp) getApplication();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    public String getNameOfQuest(int kind){
        String nameOfQuest;
        switch (kind) {
            case Solo.STAND_TO_WIN:
                nameOfQuest = getString(R.string.quest_stand_to_win_title);
                break;
            case Solo.RANDOM_STAND_UP:
                nameOfQuest = getString(R.string.quest_random_stand_up_title);
                break;
            case Solo.RANDOM_SWITCH:
                nameOfQuest = getString(R.string.quest_random_switch_title);
                break;
            case Solo.ENDURANCE:
                nameOfQuest = getString(R.string.quest_endurance_title);
                break;
            case Solo.EARN_YOUR_SITTING_TIME:
                nameOfQuest = getString(R.string.quest_earn_your_sitting_time_title);
                break;
            case Solo.EARN_DURATION_TIME:
                nameOfQuest = getString(R.string.quest_earn_duration_time);
                break;
            case Solo.SOLVE_QUESTION_FOR_MORE_XP:
                nameOfQuest = getString(R.string.quest_solve_questions_for_more_xp_title);
                break;
            default:
                nameOfQuest = "";
                break;
        }
        return nameOfQuest;
    }
    public String getDescriptionOfQuest(Solo solo){
        String descriptionOfQuest;

        switch(solo.getKind()){
            case Solo.STAND_TO_WIN:
                switch(solo.getDifficulty()){
                    case EASY:
                        descriptionOfQuest = getString(R.string.quest_stand_to_win_description, 10, solo.getDuration());
                        break;
                    case MEDIUM:
                        descriptionOfQuest = getString(R.string.quest_stand_to_win_description, 15, solo.getDuration());
                        break;
                    case HARD:
                        descriptionOfQuest = getString(R.string.quest_stand_to_win_description, 20, solo.getDuration());
                        break;
                    default:
                        descriptionOfQuest = "";
                        break;
                }
                break;
            case Solo.RANDOM_STAND_UP:
                descriptionOfQuest = getString(R.string.quest_random_stand_up_description, solo.getDuration());
                break;
            case Solo.RANDOM_SWITCH:
                descriptionOfQuest = getString(R.string.quest_random_switch_description, solo.getDuration());
                break;
            case Solo.ENDURANCE:
                switch(solo.getDifficulty()){
                    case EASY:
                        switch(Locale.getDefault().getLanguage()){
                            case "nl":
                                descriptionOfQuest = getString(R.string.quest_endurance_description, solo.getDuration(), "1 minuut");
                                break;
                            case "fr":
                                descriptionOfQuest = getString(R.string.quest_endurance_description, solo.getDuration(), "1 minute");
                                break;
                            case "en":
                                descriptionOfQuest = getString(R.string.quest_endurance_description, solo.getDuration(), "1 minute");
                                break;
                            default:
                                descriptionOfQuest = "";
                                break;
                        }

                        break;
                    case MEDIUM:
                    case HARD:
                        switch(Locale.getDefault().getLanguage()){
                            case "nl":
                                descriptionOfQuest = getString(R.string.quest_endurance_description, solo.getDuration(), "30 seconden");
                                break;
                            case "fr":
                                descriptionOfQuest = getString(R.string.quest_endurance_description, solo.getDuration(), "30 secondes");
                                break;
                            case "en":
                                descriptionOfQuest = getString(R.string.quest_endurance_description, solo.getDuration(), "30 secondes");
                                break;
                            default:
                                descriptionOfQuest = "";
                                break;
                        }
                        break;
                    default:
                        descriptionOfQuest = "";
                        break;
                }
                break;
            case Solo.EARN_YOUR_SITTING_TIME:
                switch(solo.getDifficulty()){
                    case EASY:
                        descriptionOfQuest = getString(R.string.quest_earn_your_sitting_time_description, 60);
                        break;
                    case MEDIUM:
                        descriptionOfQuest = getString(R.string.quest_earn_your_sitting_time_description, 50);
                        break;
                    case HARD:
                        descriptionOfQuest = getString(R.string.quest_earn_your_sitting_time_description, 40);
                        break;
                    default:
                        descriptionOfQuest = "";
                        break;
                }
                break;
            case Solo.EARN_DURATION_TIME:
                switch(solo.getDifficulty()){
                    case EASY:
                        descriptionOfQuest = getString(R.string.quest_earn_your_sitting_time_description, 60);
                        break;
                    case MEDIUM:
                        descriptionOfQuest = getString(R.string.quest_earn_your_sitting_time_description, 50);
                        break;
                    case HARD:
                        descriptionOfQuest = getString(R.string.quest_earn_your_sitting_time_description, 40);
                        break;
                    default:
                        descriptionOfQuest = "";
                        break;
                }
                break;
            case Solo.SOLVE_QUESTION_FOR_MORE_XP:
                switch(solo.getDifficulty()){
                    case EASY:
                        descriptionOfQuest = getString(R.string.quest_solve_questions_for_more_xp_description, 10, solo.getDuration());
                        break;
                    case MEDIUM:
                        descriptionOfQuest = getString(R.string.quest_solve_questions_for_more_xp_description, 15, solo.getDuration());
                        break;
                    case HARD:
                        descriptionOfQuest = getString(R.string.quest_solve_questions_for_more_xp_description, 20, solo.getDuration());
                        break;
                    default:
                        descriptionOfQuest = "";
                        break;
                }
                break;
            default:
                descriptionOfQuest = "";
                break;
        }
        return descriptionOfQuest;
    }

    public String getChallengeNameOrDescription(Challenge challenge, boolean nameOrDescription){
        String nameOfChallenge;
        String descriptionOfChallenge;
        switch (challenge.getKind()) {
            case Challenge.ONE_ON_ONE:
                nameOfChallenge = getString(R.string.challenge_one_on_one_title);
                descriptionOfChallenge = getString(R.string.challenge_one_on_one_description, challenge.getDuration());
                break;
            case Challenge.GROUP_COMPETITION:
                nameOfChallenge = getString(R.string.challenge_group_competition_title);
                descriptionOfChallenge = getString(R.string.challenge_grou_competition_description, challenge.getDuration());
                break;
            case Challenge.FOLLOW_THE_TRACK:
                nameOfChallenge = getString(R.string.challenge_follow_the_track_title);
                descriptionOfChallenge =getString(R.string.challenge_follow_the_track_description);
                break;
            case Challenge.ALTERNATELY_STANDING:
                nameOfChallenge = getString(R.string.challenge_alternately_standing_title);
                descriptionOfChallenge = getString(R.string.challenge_alternately_standing_description);
                break;
            default:
                nameOfChallenge = "";
                descriptionOfChallenge = "";
                break;
        }
        return nameOrDescription ? nameOfChallenge : descriptionOfChallenge;
    }




}
