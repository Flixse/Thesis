package com.tomandfelix.stapp2.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarDeterminate;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.Solo;
import com.tomandfelix.stapp2.persistency.SoloList;
import com.tomandfelix.stapp2.service.ShimmerService;
import com.tomandfelix.stapp2.tools.Logging;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenSoloQuest extends ServiceActivity {
    private static OpenSoloHandler handler = new OpenSoloHandler();
    private Solo solo;
    public static final int MSG_REFRESH = 1;
    private ButtonRectangle button;
    private ProgressBarDeterminate progress;
    private TextView result;
    private TextView questionTitle;
    private TextView question;
    private TextView countDown;
    private EditText answer;
    private int state;
    private List<String> questions;
    private JexlEngine jexl;
    private boolean viewPicker = false;
    private Runnable questionsRunnable;
    private Runnable timeToDoQuestionsRunnable;
    private Handler questionsHandler;
    private Handler timeDoToQuestionsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int id = getIntent().getIntExtra("position", -1);
        if(id == -1)
            finish();
        solo = SoloList.getSolo(id);
        if(solo.getName().equals("Quiz")) {
            viewPicker = true;
            setContentView(R.layout.activity_open_solo_quest_quiz);
            solo.setMultiplier(0.8);
        }else{
            viewPicker = false;
            setContentView(R.layout.activity_open_solo_quest);
        }
        super.onCreate(savedInstanceState);
        questions = new ArrayList<>();
        questions = solo.getQuestions();
        for(int i = 0 ; i < questions.size(); i++){
            Log.d("question" + i, questions.get(i));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler.setInstance(this);
        TextView name = (TextView) findViewById(R.id.solo_quest_name);
        final TextView description = (TextView) findViewById(R.id.solo_quest_description);
        name.setText(solo.getName());
        description.setText(solo.getDescription());
        button = (ButtonRectangle) findViewById(R.id.solo_quest_button);
        progress = (ProgressBarDeterminate) findViewById(R.id.open_solo_quest_progress);
        result = (TextView) findViewById(R.id.solo_quest_result);

        questionTitle = (TextView) findViewById(R.id.question_title);
        question = (TextView) findViewById(R.id.solo_quest_quiz_question);
        countDown = (TextView) findViewById(R.id.open_solo_quest_timer);
        answer = (EditText) findViewById(R.id.open_solo_quest_quiz_answer);
        updateViews();
        if(viewPicker) {
            questionsHandler = new Handler();
            questionsRunnable = new Runnable() {
                @Override
                public void run() {
                    if (solo.getQuestions().isEmpty()){
                        questionsHandler.postDelayed(this, 100);
                    }else{
                        question.setVisibility(View.VISIBLE);
                        answer.setVisibility(View.VISIBLE);
                        questionTitle.setVisibility(View.VISIBLE);
                        countDown.setVisibility(View.VISIBLE);
                        questions = solo.getQuestions();
                        jexl = new JexlEngine();
                        question.setText(questions.get(0));
                        answer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                                if (i == EditorInfo.IME_ACTION_DONE) {
                                    if(!answer.getText().toString().equals("")) {
                                        Expression e = jexl.createExpression(questions.remove(0));
                                        JexlContext context = new MapContext();
                                        int result = (Integer) e.evaluate(context);
                                        if (result == Integer.parseInt(answer.getText().toString())) {
                                            Log.d("answer correct", "you answered " + answer.getText().toString());
                                            solo.setAnswersCorrect(solo.getAnswersCorrect() + 1);
                                        } else {
                                            Log.d("answer wrong", "you answered " + answer.getText().toString() + ", the correct answer was " + result);
                                        }
                                        answer.getText().clear();
                                        if (!questions.isEmpty()) {
                                            question.setText(questions.get(0));
                                        }else{
                                            question.setText("no more questions");
                                        }
                                        answer.getText().clear();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Please fill in your answer", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                return false;
                            }
                        });
                    }
                }
            };
        }
        timeDoToQuestionsHandler = new Handler();
        timeToDoQuestionsRunnable = new Runnable(){
            @Override
            public void run() {
                questionTitle.setText("Answer time is over!");
                question.setText("You have answered " + solo.getAnswersCorrect() + " question(s) correctly");
                question.setTextSize(20);
                answer.setVisibility(View.GONE);
            }
        };

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

    public void onButton(View v) {
        if(solo.getData() == null) {
            solo.start();
            questionsRunnable.run();
            if(viewPicker){
                new CountDownTimer(60000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        countDown.setText("seconds remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        countDown.setVisibility(View.GONE);
                    }
                }.start();
            }
            timeDoToQuestionsHandler.postDelayed(timeToDoQuestionsRunnable, 60000);
        } else {
            solo.clear();
            updateViews();
        }
    }

    public static Handler getHandler() {
        return handler;
    }

    public void updateViews() {
        if(solo.getHandler() == null) {
            if(solo.getData() == null ) {
                if(Arrays.asList(Logging.STATE_CONNECTED, Logging.STATE_SIT, Logging.STATE_STAND, Logging.STATE_OVERTIME).contains(state)) {
                    button.setText("Start");
                    button.setEnabled(true);
                } else {
                    button.setText("Please connect a sensor first");
                    button.setEnabled(false);
                }
                button.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                result.setVisibility(View.INVISIBLE);
            } else {
                button.setText("Dismiss");
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
}
