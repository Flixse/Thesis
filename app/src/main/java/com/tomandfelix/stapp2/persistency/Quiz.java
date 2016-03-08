package com.tomandfelix.stapp2.persistency;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Flixse on 25/02/2016.
 */
public class Quiz {
    static public final int ENGLISH = 1;
    static public final int DUTCH = 2;
    static public final int FRENCH = 3;
    private int quizId;
    private int languageId;
    private String quizQuestion;
    private String correctAnswer;
    private String[] wrongAnswers = new String[3];
    boolean answerCorrect = false;

    public Quiz(int quizId,int languageId, String quizQuestion, String correctAnswer, String[] wrongAnswers){
        this.quizId = quizId;
        this.languageId = languageId;
        this.quizQuestion = quizQuestion;
        this.correctAnswer = correctAnswer;
        this.wrongAnswers = wrongAnswers;
    }

    public int getLanguageId() {
        return languageId;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public String getQuizQuestion() {
        return quizQuestion;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String[] getWrongAnswers() {
        return wrongAnswers;
    }

    public ArrayList<String> getRandomizedPossibleAnswers(){
        Random rnd = new Random();
        String[] possibleAnswers = new String[4];
        possibleAnswers[0] = correctAnswer;
        possibleAnswers[1] = wrongAnswers[0];
        possibleAnswers[2] = wrongAnswers[1];
        possibleAnswers[3] = wrongAnswers[2];
        for(int i = possibleAnswers.length - 1; i > 0; i --){
            int index = rnd.nextInt(i + 1);
            String a = possibleAnswers[index];
            possibleAnswers[i] = a;
        }
        ArrayList<String> randomAnswers = new ArrayList<>();
        for(int j = 0; j < possibleAnswers.length ; j ++ ){
            randomAnswers.add(possibleAnswers[j]);
        }
        return randomAnswers;
    }

    public void setAnswerCorrect(){
        answerCorrect = true;
    }

    public boolean getAnswerCorrect(){
        return answerCorrect;
    }


}
