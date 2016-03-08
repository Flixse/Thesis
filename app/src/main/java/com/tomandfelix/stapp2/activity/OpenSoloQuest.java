package com.tomandfelix.stapp2.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarDeterminate;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.Quiz;
import com.tomandfelix.stapp2.persistency.ServerHelper;
import com.tomandfelix.stapp2.persistency.Solo;
import com.tomandfelix.stapp2.persistency.SoloList;
import com.tomandfelix.stapp2.service.ShimmerService;
import com.tomandfelix.stapp2.tools.Logging;

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

    private int pos;
    private int quizId;
    private Quiz quiz;
    private ListView answers;
    private AnswersAdapter answerAdapter;
    private ButtonRectangle confirmButton;
    private TextView result;
    private TextView question;
    private LinearLayout quizLayout;
    AdapterView.OnItemClickListener listener;
    private int state;
    private List<Quiz> questions;
    private boolean viewPicker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int id = getIntent().getIntExtra("position", -1);
        if(id == -1)
            finish();
        solo = SoloList.getSolo(id);
        if(solo.getKind() == Solo.EARN_YOUR_SITTING_TIME) {
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler.setInstance(this);
        TextView name = (TextView) findViewById(R.id.solo_quest_name);
        final TextView description = (TextView) findViewById(R.id.solo_quest_description);
        name.setText(getNameOfQuest(solo.getKind()));
        description.setText(getDescriptionOfQuest(solo));
        button = (ButtonRectangle) findViewById(R.id.solo_quest_button);
        progress = (ProgressBarDeterminate) findViewById(R.id.open_solo_quest_progress);
        result = (TextView) findViewById(R.id.solo_quest_result);

        updateViews();
        if(viewPicker){
            ServerHelper.getInstance().getQuizLIst(new ServerHelper.ResponseFunc<List<Quiz>>() {
                @Override
                public void onResponse(List<Quiz> response) {
                    solo.setQuestions(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("error",volleyError.toString());
                }
            });
            quizId = 0;
            answers = (ListView) findViewById(R.id.solo_quest_quiz_answers);
            question = (TextView) findViewById(R.id.solo_quest_quiz_question);
            confirmButton = (ButtonRectangle) findViewById(R.id.solo_quest_confirm_answer_button);
            quizLayout = (LinearLayout) findViewById(R.id.solo_quest_layout);
            questions = solo.getQuestions();
            quiz = questions.get(quizId);
            question.setText(quiz.getQuizQuestion());
            answerAdapter = new AnswersAdapter(this, R.layout.list_item_quiz_answers, quiz.getRandomizedPossibleAnswers());
            listener = new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    pos = i;
                    view.setSelected(true);
                    if(confirmButton.getVisibility()== View.INVISIBLE){
                        confirmButton.setVisibility(View.VISIBLE);
                    }
                }
            };

            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(answerAdapter.getItem(pos).equals(quiz.getCorrectAnswer())){
                        Log.d("answer","correct");
                        solo.setAnswersCorrect(solo.getAnswersCorrect() + 1);
                    }else{
                        Log.d("answer","false");
                    }
                    quizId ++;
                    quiz = questions.get(quizId);
                    question.setText(quiz.getQuizQuestion());
                    answerAdapter.notifyDataSetChanged();
                }
            });



        }


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
                quizLayout.setVisibility(View.GONE);
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

    private class AnswersAdapter extends ArrayAdapter<String>{
        private int itemLayoutId;

        public AnswersAdapter(Context context, int itemLayoutId, List<String> data) {
            super(context, itemLayoutId, data);
            this.itemLayoutId = itemLayoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if( convertView == null){
                convertView = getLayoutInflater().inflate(itemLayoutId, parent);
            }
            RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.quiz_layout);
            TextView answer = (TextView) convertView.findViewById(R.id.quiz_answer);

            if(convertView.isSelected()){
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
