package com.tomandfelix.stapp2.persistency;

import com.tomandfelix.stapp2.activity.OpenSoloQuest;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.tools.Algorithms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Flixse on 14/07/2015.
 */
public class SoloList {
    public static final List<Solo> solos = createSoloList();

    public static Solo getSolo(int id) {
        return solos.get(id);
    }

    public static List<Solo> getList() {
        return solos;
    }

    private static List<Solo> createSoloList() {
        ArrayList<Solo> list = new ArrayList<>();

        Solo.Processor standToWinProcessor = new Solo.Processor() {
            @Override
            public void start(Solo solo) {
                solo.setData(new Date());
                schedule(solo);
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            public void checkProgress(Solo solo) {
                Date start = null;
                if(solo.getData() instanceof Date) {
                    start = (Date) solo.getData();
                }
                if(start != null && new Date().getTime() - start.getTime() >= 1800000) {
                    solo.lost();
                } else {
                    double result = (double) Algorithms.millisecondsStood(start, new Date());
                    solo.setProgress(Math.min((result * 100d / ((double) getNeeded(solo))), 100d));
                    if (solo.getProgress() == 100) {
                        solo.won();
                    } else {
                        schedule(solo);
                    }
                }
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private long getNeeded(Solo solo) {
                switch(solo.getDifficulty()) {
                    case EASY:
                        return 600000;
                    case MEDIUM:
                        return 900000;
                    case HARD:
                        return 1200000;
                    default :
                        return 0;
                }
            }

            private void schedule(final Solo solo) {
                solo.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkProgress(solo);
                    }
                }, getNeeded(solo) / 100);
            }
        };
        Solo.Processor randomStandUpProcessor = new Solo.Processor() {
            @Override
            public void start(final Solo solo) {
                Random random = new Random();
                for(int i = 0; i < solo.getDuration() / 10; i++) {
                    int randomInt = random.nextInt(510) * 1000;
                    solo.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alert(solo);
                        }
                    }, i * 600000 + randomInt);
                    solo.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkProgress(solo);
                        }
                    }, i * 600000 + 90000 + randomInt);
                }
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private void alert(Solo solo) {
                solo.setData("Stand up in the next 30 seconds, for at least a minute");
                StApp.vibrate(2000);
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private void checkProgress(Solo solo) {
                Date end = new Date();
                Date start = new Date(end.getTime() - 90000);
                long result = Algorithms.millisecondsStood(start, end);
                if(result < 60000) {
                    StApp.makeToast("QUEST_LOST");
                    solo.setData("Quest complete, you have lost!");
                    solo.lost();
                } else {
                    solo.setData(null);
                    solo.setProgress(Math.min(solo.getProgress() + 1000d / ((double) solo.getDuration()), 100d));
                    if(solo.getProgress() == 100) {
                        solo.won();
                    }
                }
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }
        };
        Solo.Processor randomSwitchProcessor = new Solo.Processor(){
            @Override
            public void start(final Solo solo) {
                Random random = new Random();
                for(int i = 0; i < solo.getDuration() / 2; i++){
                    int randomInt = random.nextInt(90) * 1000;
                    solo.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alert(solo);
                        }
                    }, i * 120000 + randomInt);
                    solo.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkProgress(solo);
                        }
                    }, i * 120000 + 30000 + randomInt);
                }
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private void alert(Solo solo) {
                DBLog lastSitStand = DatabaseHelper.getInstance().getLastSitStand();
                if(lastSitStand != null) {
                    if(lastSitStand.getAction().equals(DatabaseHelper.LOG_SIT)) {
                        solo.setData("Start standing within the next 30 seconds");
                    } else {
                        solo.setData("Start sitting within the next 30 seconds");
                    }
                }
                StApp.vibrate(2000);
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private void checkProgress(Solo solo){
                Date end = new Date();
                Date start = new Date(end.getTime() - 30000);
                ArrayList<DBLog> logs = DatabaseHelper.getInstance().getSitStandBetween(start, end);
                if(logs != null && logs.size() == 1) {
                    solo.setProgress(Math.min(solo.getProgress() + 200d / ((double) solo.getDuration()), 100d));
                    if(solo.getProgress() == 100) {
                        solo.won();
                    }
                } else {
                    solo.lost();
                }
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }
        };
        Solo.Processor enduranceProcessor = new Solo.Processor() {
            @Override
            public void start(final Solo solo) {
                solo.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkProgress(solo);
                    }
                }, getMax(solo));
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private void checkProgress(final Solo solo) {
                Date now = new Date();
                Date start = new Date(now.getTime() - getMax(solo));
                ArrayList<DBLog> logs = DatabaseHelper.getInstance().getSitStandBetween(start, now);
                if(logs != null && logs.size() >= 1) {
                    DBLog last = logs.get(logs.size() - 1);
                    solo.setProgress(Math.min(solo.getProgress() + (last.getDatetime().getTime() - start.getTime()) / (600d * solo.getDuration()), 100d));
                    if(solo.getProgress() == 100) {
                        solo.won();
                    } else {
                        solo.getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkProgress(solo);
                            }
                        }, now.getTime() - last.getDatetime().getTime() + getMax(solo));
                    }
                } else {
                    solo.lost();
                }
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private long getMax(Solo solo) {
                switch(solo.getDifficulty()) {
                    case EASY:
                        return 60000;
                    case MEDIUM:
                    case HARD:
                        return 30000;
                    default :
                        return 0;
                }
            }
        };

        /*Solo.Processor mathematicalGenius = new Solo.Processor(){
            @Override
            public void start(final Solo solo) {
                solo.setQuestions(getListOfQuestions(solo));
                solo.setData(new Date());
                schedule(solo);
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private void checkProgress(Solo solo){
                Date start = null;
                if(solo.getData() instanceof Date) {
                    start = (Date) solo.getData();
                }
                if(start != null && new Date().getTime() - start.getTime() >= 1800000) {
                    solo.lost();
                } else {
                    double result = (double) Algorithms.millisecondsStood(start, new Date());
                    solo.setProgress(Math.min((result * 100d / ((double) getNeeded(solo))), 100d));
                    if (solo.getProgress() == 100) {
                        solo.won();
                    } else {
                        schedule(solo);
                    }
                }
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private int getNeeded(Solo solo){
                switch(solo.getDifficulty()) {
                    case EASY:
                        return 600000;
                    case MEDIUM:
                        return 900000;
                    case HARD:
                        return 1200000;
                    default :
                        return 0;
                }
            }

            private void schedule(final Solo solo) {
                solo.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkProgress(solo);
                    }
                }, getNeeded(solo) / 100);
            }

            public List<String> getListOfQuestions(Solo solo){
                switch(solo.getDifficulty()) {
                    case EASY:
                        return DatabaseHelper.getInstance().getQuestionsWithDifficulty(0);
                    case MEDIUM:
                        return DatabaseHelper.getInstance().getQuestionsWithDifficulty(1);
                    case HARD:
                        return DatabaseHelper.getInstance().getQuestionsWithDifficulty(2);
                    default:
                        return null;

                }
            }
        };*/

        Solo.Processor EarnSittingTime = new Solo.Processor() {
            @Override
            public void start(Solo solo) {
                solo.setData(new Date());
                schedule(solo);
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            public void checkProgress(Solo solo) {
                Date start = null;
                if(solo.getData() instanceof Date) {
                    start = (Date) solo.getData();
                }
                if(start != null && new Date().getTime() - start.getTime() >= 1800000) {
                    solo.lost();
                } else {
                    double result = (double) Algorithms.millisecondsStood(start, new Date());
                    solo.setProgress(Math.min((result * 100d / ((double) 600000 + (1200000 - getNeeded(solo) * solo.getAnswersCorrect()))), 100d));
                    if (solo.getProgress() == 100) {
                        solo.won();
                    } else {
                        schedule(solo);
                    }
                }
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private void schedule(final Solo solo){
                solo.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkProgress(solo);
                    }
                }, 600000/100);
            }

            private long getNeeded(Solo solo) {
                switch(solo.getDifficulty()) {
                    case EASY:
                        return 60000;
                    case MEDIUM:
                        return 50000;
                    case HARD:
                        return 40000;
                    default :
                        return 0;
                }
            }

        };

        Solo.Processor EarningDuration = new Solo.Processor() {
            @Override
            public void start(Solo solo) {
                solo.setData(new Date());
                schedule(solo);
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            public void checkProgress(Solo solo) {
                Date start = null;
                if(solo.getData() instanceof Date) {
                    start = (Date) solo.getData();
                }
                if(start != null && new Date().getTime() - start.getTime() >= solo.getDuration() + 30000 * solo.getAnswersCorrect() ) {
                    solo.lost();
                } else {
                    double result = (double) Algorithms.millisecondsStood(start, new Date());
                    solo.setProgress(Math.min((result * 100d / ((double) getNeeded(solo))), 100d));
                    if (solo.getProgress() == 100) {
                        solo.won();
                    } else {
                        schedule(solo);
                    }
                }
                OpenSoloQuest.getHandler().obtainMessage(OpenSoloQuest.MSG_REFRESH).sendToTarget();
            }

            private void schedule(final Solo solo){
                solo.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkProgress(solo);
                    }
                }, 600000/100);
            }

            private long getNeeded(Solo solo) {
                switch(solo.getDifficulty()) {
                    case EASY:
                        return 600000;
                    case MEDIUM:
                        return 720000;
                    case HARD:
                        return 840000;
                    default :
                        return 0;
                }
            }

        };

        list.add(0, new Solo(0, Solo.STAND_TO_WIN, 150, 0, 30, Solo.Difficulty.EASY, standToWinProcessor));
        list.add(1, new Solo(1, Solo.STAND_TO_WIN, 300, 2000, 30, Solo.Difficulty.MEDIUM, standToWinProcessor));
        list.add(2, new Solo(2, Solo.STAND_TO_WIN, 600, 4500, 30, Solo.Difficulty.HARD, standToWinProcessor));
        list.add(3, new Solo(3, Solo.RANDOM_STAND_UP, 250, 1500, 30, Solo.Difficulty.EASY, randomStandUpProcessor));
        list.add(4, new Solo(4, Solo.RANDOM_STAND_UP, 500, 4000, 60, Solo.Difficulty.MEDIUM, randomStandUpProcessor));
        list.add(5, new Solo(5, Solo.RANDOM_STAND_UP , 750, 10000, 90, Solo.Difficulty.HARD, randomStandUpProcessor));
        list.add(6, new Solo(6, Solo.RANDOM_SWITCH, 250, 5000, 10, Solo.Difficulty.EASY, randomSwitchProcessor));
        list.add(7, new Solo(7, Solo.RANDOM_SWITCH, 500, 10000, 15, Solo.Difficulty.MEDIUM, randomSwitchProcessor));
        list.add(8, new Solo(8, Solo.RANDOM_SWITCH, 750, 15000, 20, Solo.Difficulty.HARD, randomSwitchProcessor));
        list.add(9, new Solo(9, Solo.ENDURANCE, 250, 3000, 10, Solo.Difficulty.EASY, enduranceProcessor));
        list.add(10, new Solo(10, Solo.ENDURANCE, 600, 5000, 10, Solo.Difficulty.MEDIUM, enduranceProcessor));
        list.add(11, new Solo(11, Solo.ENDURANCE, 1000, 10000, 15, Solo.Difficulty.HARD, enduranceProcessor));
        list.add(12,new Solo(12, Solo.EARN_YOUR_SITTING_TIME,  250, 1500, 10, Solo.Difficulty.EASY, EarnSittingTime));
        list.add(13,new Solo(13, Solo.EARN_YOUR_SITTING_TIME , 1000, 5000, 15, Solo.Difficulty.MEDIUM, EarnSittingTime));
        list.add(14,new Solo(14, Solo.EARN_YOUR_SITTING_TIME, 1700, 10000, 20, Solo.Difficulty.HARD, EarnSittingTime));
        list.add(15, new Solo(15, Solo.EARN_DURATION_TIME, 300, 1750, 450000, Solo.Difficulty.EASY, EarningDuration));
        list.add(16, new Solo(15, Solo.EARN_DURATION_TIME, 1200, 6000, 450000, Solo.Difficulty.MEDIUM, EarningDuration));
        list.add(17, new Solo(15, Solo.EARN_DURATION_TIME, 1800, 12000, 450000, Solo.Difficulty.HARD, EarningDuration));
        list.add(18, new Solo(0, Solo.SOLVE_QUESTION_FOR_MORE_XP, 150, 0, 30, Solo.Difficulty.EASY, standToWinProcessor));
        list.add(19, new Solo(1, Solo.SOLVE_QUESTION_FOR_MORE_XP, 300, 2000, 30, Solo.Difficulty.MEDIUM, standToWinProcessor));
        list.add(20, new Solo(2, Solo.SOLVE_QUESTION_FOR_MORE_XP, 600, 4500, 30, Solo.Difficulty.HARD, standToWinProcessor));


        return list;
    }


}
