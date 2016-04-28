package com.tomandfelix.stapp2.persistency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Flixse on 25/02/2016.
 */
public class Quiz {
    private int quizId;
    private int languageId;
    private String quizQuestion;
    private String correctAnswer;
    private ArrayList<String> wrongAnswers;
    boolean answerCorrect = false;

    public Quiz(int quizId,int languageId, String quizQuestion, String correctAnswer, ArrayList<String> wrongAnswers){
        this.quizId = quizId;
        this.languageId = languageId;
        this.quizQuestion = quizQuestion;
        this.correctAnswer = correctAnswer;
        this.wrongAnswers = wrongAnswers;
    }

    public String getQuizQuestion() {
        return quizQuestion;
    }

    public int getQuizId(){
        return this.quizId;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public ArrayList<String> getWrongAnswers() {
        return wrongAnswers;
    }

    public ArrayList<String> getRandomizedPossibleAnswers(){
        Random rnd = new Random();
        String[] possibleAnswers = new String[4];
        possibleAnswers[0] = correctAnswer;
        possibleAnswers[1] = wrongAnswers.get(0);
        possibleAnswers[2] = wrongAnswers.get(1);
        possibleAnswers[3] = wrongAnswers.get(2);
        ArrayList<String> randomAnswers = new ArrayList<>();
        for(int j = 0; j < possibleAnswers.length ; j ++ ){
            randomAnswers.add(possibleAnswers[j]);
        }
        Collections.shuffle(randomAnswers);
        return randomAnswers;
    }

    public void setAnswerCorrect(){
        answerCorrect = true;
    }

    public boolean getAnswerCorrect(){
        return answerCorrect;
    }


}
