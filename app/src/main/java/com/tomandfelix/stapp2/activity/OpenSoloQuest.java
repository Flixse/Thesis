package com.tomandfelix.stapp2.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarDeterminate;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.Quiz;
import com.tomandfelix.stapp2.persistency.ServerHelper;
import com.tomandfelix.stapp2.persistency.Solo;
import com.tomandfelix.stapp2.persistency.SoloList;
import com.tomandfelix.stapp2.service.ShimmerService;
import com.tomandfelix.stapp2.tools.Logging;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OpenSoloQuest extends ServiceActivity {
    private static OpenSoloHandler handler = new OpenSoloHandler();
    private Solo solo;
    public static final int MSG_REFRESH = 1;
    private ButtonRectangle button;
    private ProgressBarDeterminate progress;
    private Profile mProfile;
    private Quiz quiz;
    private ListView answers;
    private AnswersAdapter answerAdapter;
    private ButtonRectangle confirmButton;
    private TextView result;
    private TextView question;
    private int state;
    private int languageId;
    private int selectedItem;
    private boolean viewPicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int id = getIntent().getIntExtra("position", -1);
        if(id == -1)
            finish();
        solo = SoloList.getSolo(id);
        if(solo.getKind() == Solo.EARN_YOUR_SITTING_TIME || solo.getKind() == Solo.EARN_DURATION_TIME || solo.getKind() == Solo.SOLVE_QUESTION_FOR_MORE_XP) {
            viewPicker = true;
            setContentView(R.layout.activity_open_solo_quest_quiz);
        }else{
            viewPicker = false;
            setContentView(R.layout.activity_open_solo_quest);
        }
        super.onCreate(savedInstanceState);
        mProfile = DatabaseHelper.getInstance().getOwner();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler.setInstance(this);
        TextView name = (TextView) findViewById(R.id.solo_quest_name);
        final TextView description = (TextView) findViewById(R.id.solo_quest_description);
        name.setText(getNameOfQuest(solo.getKind()));
        Log.d("text",getDescriptionOfQuest(solo));
        description.setText(getDescriptionOfQuest(solo));
        button = (ButtonRectangle) findViewById(R.id.solo_quest_button);
        progress = (ProgressBarDeterminate) findViewById(R.id.open_solo_quest_progress);
        result = (TextView) findViewById(R.id.solo_quest_result);
        if(viewPicker){
            answers = (ListView) findViewById(R.id.solo_quest_quiz_answers);
            question = (TextView) findViewById(R.id.solo_quest_quiz_question);
            confirmButton = (ButtonRectangle) findViewById(R.id.solo_quest_confirm_answer_button);
            }
        updateViews();

    }


    @Override
    protected void onResume() {
        super.onResume();

        handler.setInstance(this);
        if(StApp.getHandler() != handler) {
            StApp.setHandler(handler);
        }
        updateViews();
        app.commandService(ShimmerService.REQUEST_STATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(StApp.getHandler() == handler) {
            StApp.setHandler(null);
        }
    }

    public void onStartStopButton(View v) {
        if(solo.getData() == null) {
            solo.start();
            if(viewPicker){
                selectedItem = -1;
                switch (Locale.getDefault().getLanguage()) {
                    case "nl":
                        languageId = 1;
                        break;
                    case "fr":
                        languageId = 2;
                        break;
                    case "en":
                        languageId = 0;
                        break;
                    default :
                        languageId = 0;
                }
                ServerHelper.getInstance().getQuizLIst(mProfile.getId(),languageId,new ServerHelper.ResponseFunc<List<Quiz>>() {
                    @Override
                    public void onResponse(List<Quiz> response) {
                        solo.setQuestions(response);
                        setQuiz();
                        answerAdapter = new AnswersAdapter(OpenSoloQuest.this, R.layout.list_item_quiz_answers, quiz.getRandomizedPossibleAnswers());
                        confirmButton.setVisibility(View.VISIBLE);
                        question.setVisibility(View.VISIBLE);
                        answers.setAdapter(answerAdapter);
                        answers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                view.setSelected(true);
                                confirmButton.setEnabled(true);
                                selectedItem = position;
                                answerAdapter.notifyDataSetChanged();
                            }
                        });
                        confirmButton.setVisibility(View.VISIBLE);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("error",volleyError.toString());
                    }
                });
            }
        } else {
            solo.clear();
            updateViews();
        }
    }

    public void onConfirmButton(View v){
        if(answers.getItemAtPosition(selectedItem).toString().equals(quiz.getCorrectAnswer())){
            solo.incrementAnswersCorrect();
            switch (languageId){
                case 0:
                    if(solo.getAnswersCorrect() == 1){
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_correct_answer, solo.getAnswersCorrect() + " question "), Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_correct_answer, solo.getAnswersCorrect() + " questions "), Toast.LENGTH_LONG).show();
                    }
                    break;
                case 1:
                    if(solo.getAnswersCorrect() == 1){
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_correct_answer, solo.getAnswersCorrect() + " vraag "), Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_correct_answer, solo.getAnswersCorrect() + " vragen "), Toast.LENGTH_LONG).show();
                    }
                    break;
                case 2:
                    if(solo.getAnswersCorrect() == 1){
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_correct_answer, solo.getAnswersCorrect() + " question "), Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_correct_answer, solo.getAnswersCorrect() + " questions "), Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    if(solo.getAnswersCorrect() == 1){
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_correct_answer, solo.getAnswersCorrect() + " question "), Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_correct_answer, solo.getAnswersCorrect() + " questions "), Toast.LENGTH_LONG).show();
                    }
            }

        }else{
            switch (languageId){
                case 0:
                    if(solo.getAnswersCorrect() == 1){
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_wrong_answer, solo.getAnswersCorrect() + " question "), Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_wrong_answer, solo.getAnswersCorrect() + " questions "), Toast.LENGTH_LONG).show();
                    }
                    break;
                case 1:
                    if(solo.getAnswersCorrect() == 1){
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_wrong_answer, solo.getAnswersCorrect() + " vraag "), Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_wrong_answer, solo.getAnswersCorrect() + " vragen "), Toast.LENGTH_LONG).show();
                    }
                    break;
                case 2:
                    if(solo.getAnswersCorrect() == 1){
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_wrong_answer, solo.getAnswersCorrect() + " question "), Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_wrong_answer, solo.getAnswersCorrect() + " questions "), Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    if(solo.getAnswersCorrect() == 1){
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_wrong_answer, solo.getAnswersCorrect() + " question "), Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), getString(R.string.quest_toast_wrong_answer, solo.getAnswersCorrect() + " questions "), Toast.LENGTH_LONG).show();
                    }
            }
        }
        ServerHelper.getInstance().incrementQuizByProfileIdLanguageId(mProfile.getId(), quiz.getQuizId() , new ServerHelper.ResponseFunc<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("response", response.getString("error"));
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        } ,new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if(!ServerHelper.getInstance().checkInternetConnection()) {
                    //askForPassword();
                }
            }
        });
        confirmButton.setEnabled(false);
        selectedItem = -1;
        answers.clearChoices();
        if(!solo.getQuestions().isEmpty()) {
            setQuiz();
            answerAdapter = new AnswersAdapter(OpenSoloQuest.this, R.layout.list_item_quiz_answers, quiz.getRandomizedPossibleAnswers());
            answers.setAdapter(answerAdapter);
        }else{
            answers.setVisibility(View.INVISIBLE);
            question.setText(getString(R.string.quest_no_more_questions));
        }

    }

    public void setQuiz(){
        quiz = null;
        quiz = solo.getQuestions().remove(0);
        question.setText(quiz.getQuizQuestion());
    }

    public static Handler getHandler() {
        return handler;
    }

    public void updateViews() {
        if(solo.getHandler() == null) {
            if(solo.getData() == null ) {
                if(Arrays.asList(Logging.STATE_CONNECTED, Logging.STATE_SIT, Logging.STATE_STAND, Logging.STATE_OVERTIME).contains(state)) {
                    button.setText(getString(R.string.quest_start));
                    button.setEnabled(true);
                } else {
                    button.setText(getString(R.string.quest_connect_sensor));
                    button.setEnabled(false);
                }
                confirmButton.setEnabled(false);
                button.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                result.setVisibility(View.INVISIBLE);
            } else {
                button.setText(getString(R.string.quest_end));
                button.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                if(solo.getData() instanceof String) {
                    result.setText((String) solo.getData());
                    result.setVisibility(View.VISIBLE);
                } else {
                    result.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            button.setVisibility(View.INVISIBLE);
            progress.setProgress((int) solo.getProgress());
            progress.setVisibility(View.VISIBLE);
            if(solo.getData() instanceof String) {
                result.setText((String) solo.getData());
                result.setVisibility(View.VISIBLE);
            } else {
                result.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void updateState(int state) {
        this.state = state;
        updateViews();
    }

    private static class OpenSoloHandler extends Handler {
        private WeakReference<OpenSoloQuest> osq;

        public void setInstance(OpenSoloQuest osq) {
            this.osq = new WeakReference<>(osq);
        }
        @Override
        public void handleMessage(Message msg) {
            if(osq.get() != null) {
                if (msg.what == MSG_REFRESH) {
                    osq.get().updateViews();
                } else {
                    osq.get().updateState(msg.what);
                }
            }
        }
    }

    private class AnswersAdapter extends ArrayAdapter<String>{
        private int itemLayoutId;

        public AnswersAdapter(Context context, int itemLayoutId, List<String> data) {
            super(context, itemLayoutId, data);
            this.itemLayoutId = itemLayoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if( convertView == null){
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutId, parent, false);
            }
            RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.quiz_layout);
            TextView answer = (TextView) convertView.findViewById(R.id.quiz_answer);
            answer.setText(getItem(position));
            if(position == selectedItem){
                layout.setBackgroundColor(getResources().getColor(R.color.accentColor));
                answer.setTextColor(getResources().getColor(R.color.primaryColor));
            }else{
                layout.setBackgroundColor(getResources().getColor(R.color.primaryColor));
                answer.setTextColor(getResources().getColor(R.color.accentColor));
            }
            return convertView;
        }
    }
}
