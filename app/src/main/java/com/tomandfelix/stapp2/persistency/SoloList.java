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
                    StApp.makeToast("Quest complete, you have lost!");
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

        Solo.Processor mathematicalGenius = new Solo.Processor(){
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
        };

        String standToWinTitle = "Stand to win";
        String randomStandUpTitle = "Random stand up";
        String randomSwitchTitle = "Random switch";
        String enduranceTitle = "Endurance";
        String mathematicalGeniusTitle = "Quiz";
        String standToWinDescription = "Stand for more than <required> minutes within <duration> minutes";
        String randomStandUpDescription = "Within a period of <duration> minutes you will get the task to stand for 1 minute after every random period of time. Start standing when you feel your phone vibrating!";
        String randomSwitchDescription = "Within a period of <duration> minutes, you will get the task to change position after every random period of time. Switch position when you feel your phone vibrating!";
        String enduranceDescription = "Within a period of <duration> minutes, you can't stay in the same position for more than <max>";
        String mathematicalGeniusDescriptionTenQuestions = "The more questions you get right, the more xp you will get for the next <duration> minutes. First try to answer the 10 questions in 60 seconds and earn more xp when answered all right.";
        String mathematicalGeniusDescriptionFifteenQuestions = "The more questions you get right, the more xp you will get for the next <duration> minutes. First try to answer the 15 questions in 60 seconds and earn more the xp when answered all right.";
        String mathematicalGeniusDescriptionTwentyQuestions = "The more questions you get right, the more xp you will get for the next <duration> minutes. First try to answer the 20 questions in 60 seconds and earn more the xp when answered all right.";
        list.add(0, new Solo(0, standToWinTitle, standToWinDescription.replace("<required>", Integer.toString(10)), 150, 0, 30, Solo.Difficulty.EASY, standToWinProcessor));
        list.add(1, new Solo(3, randomStandUpTitle, randomStandUpDescription, 250, 1500, 30, Solo.Difficulty.EASY, randomStandUpProcessor));
        list.add(2, new Solo(6, randomSwitchTitle, randomSwitchDescription, 250, 5000, 10, Solo.Difficulty.EASY, randomSwitchProcessor));
        list.add(3, new Solo(9, enduranceTitle, enduranceDescription.replace("<max>", "1 minute"), 250, 3000, 10, Solo.Difficulty.EASY, enduranceProcessor));
        list.add(4,new Solo(12,mathematicalGeniusTitle,mathematicalGeniusDescriptionTenQuestions.replace("<duration>", "10 minutes"), 250, 1500, 10, Solo.Difficulty.EASY, mathematicalGenius));
        list.add(5, new Solo(1, standToWinTitle, standToWinDescription.replace("<required>", Integer.toString(15)), 300, 2000, 30, Solo.Difficulty.MEDIUM, standToWinProcessor));
        list.add(6, new Solo(4, randomStandUpTitle, randomStandUpDescription, 500, 4000, 60, Solo.Difficulty.MEDIUM, randomStandUpProcessor));
        list.add(7, new Solo(7, randomSwitchTitle, randomSwitchDescription, 500, 10000, 15, Solo.Difficulty.MEDIUM, randomSwitchProcessor));
        list.add(8, new Solo(10, enduranceTitle, enduranceDescription.replace("<max>", "30 seconds"), 600, 5000, 10, Solo.Difficulty.MEDIUM, enduranceProcessor));
        list.add(9, new Solo(2, standToWinTitle, standToWinDescription.replace("<required>", Integer.toString(20)), 600, 4500, 30, Solo.Difficulty.HARD, standToWinProcessor));
        list.add(10,new Solo(13,mathematicalGeniusTitle,mathematicalGeniusDescriptionFifteenQuestions.replace("<duration>", "15 minutes"), 1000, 5000, 15, Solo.Difficulty.MEDIUM, mathematicalGenius));
        list.add(11, new Solo(5, randomStandUpTitle, randomStandUpDescription, 750, 10000, 90, Solo.Difficulty.HARD, randomStandUpProcessor));
        list.add(12, new Solo(8, randomSwitchTitle, randomSwitchDescription, 750, 15000, 20, Solo.Difficulty.HARD, randomSwitchProcessor));
        list.add(13, new Solo(11, enduranceTitle, enduranceDescription.replace("<max>", "30 seconds"), 1000, 10000, 15, Solo.Difficulty.HARD, enduranceProcessor));
        list.add(14,new Solo(14,mathematicalGeniusTitle,mathematicalGeniusDescriptionTwentyQuestions.replace("<duration>", "20 minutes"), 1700, 10000, 20, Solo.Difficulty.HARD, mathematicalGenius));
        return list;
    }


}
