package com.tomandfelix.stapp2.graphtools;

import android.graphics.Color;
import android.util.Log;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesFormatter;
import com.androidplot.xy.XYStepMode;
import com.tomandfelix.stapp2.persistency.DBLog;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.tools.Logging;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Tom on 14/03/2015.
 */
public abstract class GraphParser {
    private static Number getGraphTime(Date date) {
        return date.getTime() / 1000;
    }

    private static void setGraphParameters(XYPlot plot) {
        // Removes the unnecessary elements from the graph & it's layout manager
        plot.getLayoutManager().remove(plot.getTitleWidget());
        plot.getLayoutManager().remove(plot.getLegendWidget());
        plot.getLayoutManager().remove(plot.getDomainLabelWidget());
        plot.getLayoutManager().remove(plot.getRangeLabelWidget());

        plot.setTitleWidget(null);
        plot.setLegendWidget(null);
        plot.setDomainLabelWidget(null);
        plot.setRangeLabelWidget(null);

        // removes background colors from the graph, from outermost to inner most layer
        plot.setBackgroundPaint(null);
        plot.getGraphWidget().setBackgroundPaint(null);
        plot.getGraphWidget().setGridBackgroundPaint(null);
        plot.setBorderPaint(null);

        // removes horizontal and vertical grid lines from the graph
        plot.getGraphWidget().setRangeGridLinePaint(null);
        plot.getGraphWidget().setDomainGridLinePaint(null);

        // Sets the color for the axes and their labels
        plot.getGraphWidget().getRangeLabelPaint().setColor(Color.GRAY);
        plot.getGraphWidget().getDomainLabelPaint().setColor(Color.GRAY);
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.GRAY);
        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.GRAY);

        // Scalable constants for the axes and label size
        final float labelSize = PixelUtils.spToPix(12);
        final float axesWidth = PixelUtils.dpToPix(2.5f);

        // Apply the above settings
        plot.getGraphWidget().getRangeLabelPaint().setTextSize(labelSize);
        plot.getGraphWidget().getDomainLabelPaint().setTextSize(labelSize);
        plot.getGraphWidget().getDomainOriginLinePaint().setStrokeWidth(axesWidth);
        plot.getGraphWidget().getRangeOriginLinePaint().setStrokeWidth(axesWidth);
    }

    //------------------------------------------------------- DAILY GRAPH ----------------------------------------------------------------------

    private static class SeriesData {
        private Number[] scores;
        private Number[] timeValues;

        public SeriesData(int size) {
            scores = new Number[size];
            timeValues = new Number[size];
        }
    }

    public static class DailyGraphData {
        private XYSeries dataSeries = null;
        private XYSeries maxSeries = null;
        private Format graphScoreFormat;
        private Format graphTimeFormat;
        private static final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        private long totalGood;
        private long totalBad;
        private double totalScore;

        public DailyGraphData() {
            totalGood = 0;
            totalBad = 0;
            totalScore = 0;
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public String getTotalGood() {
            return df.format(new Date(totalGood * 1000));
        }

        public String getTotalBad() {
            return df.format(new Date(totalBad * 1000));
        }

        public double getTotalScore() {
            return totalScore;
        }
    }

    private static boolean checkLogs(ArrayList<DBLog> logs) {
        for(DBLog log : logs) {
            switch(log.getAction()) {
                case DatabaseHelper.LOG_STAND:
                case DatabaseHelper.LOG_SIT:
                    return true;
            }
        }
        return false;
    }

    public static DailyGraphData formatDailyData(ArrayList<DBLog> dayLogs, ArrayList<DBLog> connectionLogs) {
        DailyGraphData result = null;
        if(dayLogs != null && dayLogs.size() > 0 && checkLogs(dayLogs) && connectionLogs != null && connectionLogs.size() > 0) {
            result = new DailyGraphData();
            if(!dayLogs.get(dayLogs.size() - 1).getAction().equals(DatabaseHelper.LOG_DISCONNECT)) {
                DBLog now = new DBLog(DatabaseHelper.LOG_DISCONNECT, new Date(), 0);
                dayLogs.add(now);
                connectionLogs.add(now);
            }
            connectionLogs.add(1, dayLogs.get(1));

            SeriesData scoresData = getSeries(dayLogs);
            SeriesData maxData = getSeries(connectionLogs);

            final double maxScore = maxData.scores[maxData.scores.length - 1].doubleValue();

            for(int i = 1; i < scoresData.scores.length; i++) {
                if(scoresData.scores[i].doubleValue() > scoresData.scores[i - 1].doubleValue()) {
                    result.totalGood += scoresData.timeValues[i].doubleValue() - scoresData.timeValues[i - 1].doubleValue();
                } else if(scoresData.scores[i].doubleValue() < scoresData.scores[i - 1].doubleValue()) {
                    result.totalBad += scoresData.timeValues[i].doubleValue() - scoresData.timeValues[i - 1].doubleValue();
                }
            }
            result.totalScore = scoresData.scores[scoresData.scores.length - 1].doubleValue() / maxScore * 100;

            result.dataSeries = new SimpleXYSeries(Arrays.asList(scoresData.timeValues), Arrays.asList(scoresData.scores), "data");
            result.maxSeries = new SimpleXYSeries(Arrays.asList(maxData.timeValues), Arrays.asList(maxData.scores), "max");

            result.graphTimeFormat = new Format() {
                private SimpleDateFormat df = new SimpleDateFormat("HH:mm");

                @Override
                public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                    Date date = new Date(((Number) object).longValue() * 1000);
                    return df.format(date, buffer, field);
                }

                @Override
                public Object parseObject(String string, ParsePosition position) {
                    return null;
                }
            };

            result.graphScoreFormat = new Format() {
                @Override
                public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                    double percentage = ((Number) object).doubleValue() / maxScore * 100;
                    return buffer.append(Long.toString(Math.round(percentage))).append(" %");
                }

                @Override
                public Object parseObject(String string, ParsePosition position) {
                    return null;
                }
            };
        }
        return result;
    }

    private static SeriesData getSeries(ArrayList<DBLog> logs) {
        SeriesData s = new SeriesData(logs.size());
        for(int i = 0; i < logs.size(); i++) {
            DBLog l = logs.get(i);
            s.timeValues[i] = getGraphTime(l.getDatetime());
            if(l.getAction().equals(DatabaseHelper.LOG_CONNECT)) {
                if(i == 0) {
                    s.scores[i] = 0;
                } else {
                    s.scores[i] = s.scores[i - 1];
                }
            } else if(l.getAction().equals(DatabaseHelper.LOG_DISCONNECT)) {
                DBLog status = logs.get(getStatusIndex(logs, i));
                if(status.getAction().equals(DatabaseHelper.LOG_SIT) || status.getAction().equals(DatabaseHelper.LOG_STAND)) {
                    s.scores[i] = Logging.getIncreasingScore(l.getDatetime().getTime() - logs.get(i - 1).getDatetime().getTime(), s.scores[i - 1].doubleValue());
                } else {
                    s.scores[i] = Logging.getDecreasingScore(getSittingTimeBefore(logs, i), logs.get(getLastSitIndexBefore(logs, i)).getData());
                }
            } else { // LOG_SIT, LOG_STAND, LOG_OVERTIME or LOG_ACHIEVED_SCORE
                s.scores[i] = l.getData();
            }
        }
        return s;
    }

    private static int getStatusIndex(ArrayList<DBLog> logs, int index) {
        for(int i = index; i >= 0; i--) {
            if(logs.get(i).getAction().equals(DatabaseHelper.LOG_SIT) || logs.get(i).getAction().equals(DatabaseHelper.LOG_STAND) || logs.get(i).getAction().equals(DatabaseHelper.LOG_OVERTIME)) {
                return i;
            }
        }
        return -1;
    }

    private static long getSittingTimeBefore(ArrayList<DBLog> logs, int index) {
        long result = logs.get(index).getDatetime().getTime();
        index--;
        while(!logs.get(index).getAction().equals(DatabaseHelper.LOG_SIT)) {
            if(logs.get(index).getAction().equals(DatabaseHelper.LOG_CONNECT)) {
                result -= logs.get(index).getDatetime().getTime();
            } else if(logs.get(index).getAction().equals(DatabaseHelper.LOG_DISCONNECT)) {
                result += logs.get(index).getDatetime().getTime();
            }
            index--;
        }
        result -= logs.get(index).getDatetime().getTime();
        return result;
    }

    private static int getLastSitIndexBefore(ArrayList<DBLog> logs, int index) {
        for(int i = index; i >= 0; i--) {
            if(logs.get(i).getAction().equals(DatabaseHelper.LOG_SIT)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean formatDailyGraph(XYPlot plot, DailyGraphData data, int inCreaseColor, int levelColor, int decreaseColor) {
        if(data != null && data.dataSeries != null && data.maxSeries != null) {
            plot.setRangeStep(XYStepMode.SUBDIVIDE, 5);
            plot.setRangeValueFormat(data.graphScoreFormat);

            plot.setDomainStep(XYStepMode.SUBDIVIDE, 6);
            plot.setDomainValueFormat(data.graphTimeFormat);

            XYSeriesFormatter dataFormatter = new CustomXYFormatter(inCreaseColor, levelColor, decreaseColor);
            XYSeriesFormatter maxFormatter = new LineAndPointFormatter(Color.GRAY, null, null, null);

            plot.addSeries(data.maxSeries, maxFormatter);
            plot.addSeries(data.dataSeries, dataFormatter);
            setGraphParameters(plot);

            //Sets the right margins for the graph
            plot.getGraphWidget().setMargins(PixelUtils.dpToPix(20), PixelUtils.dpToPix(20), PixelUtils.dpToPix(20), PixelUtils.dpToPix(20));
            return true;
        } else {
            return false;
        }
    }

    //------------------------------------------------------- 2 WEEK GRAPH ---------------------------------------------------------------------

    public static class LongTermGraphData {
        private XYSeries data = null;
        private Format graphScoreFormat;
        private Format graphTimeFormat;
        private static final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        private double averageScore;
        private long averageConnectionTime;

        public LongTermGraphData() {
            averageScore = 0;
            averageConnectionTime = 0;
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public double getAverageScore() {
            return averageScore;
        }

        public String getAverageConnectionTime() {
            return df.format(new Date(averageConnectionTime));
        }
    }

    private static int getTimeIndex(DBLog log, Calendar ref) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(log.getDatetime());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        int result =  Math.round((cal.getTimeInMillis() - ref.getTimeInMillis()) / (24 * 60 * 60 * 1000));
        return result;
    }

    public static LongTermGraphData formatLongTermData(ArrayList<DBLog> logs, Date now) {
        LongTermGraphData result = null;
        if(logs != null && logs.size() > 0) {
            result = new LongTermGraphData();
            Number[] data = new Number[14];

            Calendar ref  = Calendar.getInstance();
            ref.setTime(now);
            ref.set(Calendar.HOUR_OF_DAY, 12);
            ref.set(Calendar.MINUTE, 0);
            ref.set(Calendar.SECOND, 0);
            ref.set(Calendar.MILLISECOND, 0);
            ref.add(Calendar.DATE, -14);
            final Date refDate = ref.getTime();

            for(int i = 0; i < logs.size(); i++) {
                if(logs.get(i).getAction().equals(DatabaseHelper.LOG_ACH_SCORE_PERC)) {
                    result.averageScore += logs.get(i).getData();
                    data[getTimeIndex(logs.get(i), ref)] = logs.get(i).getData();
                } else if(logs.get(i).getAction().equals(DatabaseHelper.LOG_STOP_DAY)) {
                    result.averageConnectionTime += logs.get(i).getData();
                }
            }
            result.averageScore = result.averageScore / 14;
            result.averageConnectionTime = result.averageConnectionTime / 14;

            result.data = new SimpleXYSeries(Arrays.asList(data), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "data");

            result.graphTimeFormat = new Format() {
                private SimpleDateFormat df = new SimpleDateFormat("EEE");

                @Override
                public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                    int pos = ((Number) object).intValue();
                    Date date = new Date(refDate.getTime() + (pos - 1) * 24 * 60 * 60 * 1000);
                    return df.format(date, buffer, field);
                }

                @Override
                public Object parseObject(String string, ParsePosition position) {
                    return null;
                }
            };

            result.graphScoreFormat = new Format() {
                @Override
                public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                    return buffer.append(Long.toString(Math.round(((Number) object).doubleValue()))).append(" %");
                }

                @Override
                public Object parseObject(String string, ParsePosition position) {
                    return null;
                }
            };
        }
        return result;
    }

    public static boolean formatLongTermGraph(XYPlot plot, LongTermGraphData data, int highColor, int midColor, int lowColor) {
        if(data != null && data.data != null) {
            plot.setRangeStep(XYStepMode.SUBDIVIDE, 5);
            plot.setRangeValueFormat(data.graphScoreFormat);
            plot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);

//            plot.setDomainStep(XYStepMode.SUBDIVIDE, 14);
//            plot.setDomainValueFormat(data.graphTimeFormat);
//            plot.setDomainBoundaries(data.lowerBoundary, data.higherBoundary, BoundaryMode.FIXED);
            plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
            plot.setDomainValueFormat(data.graphTimeFormat);
            plot.setDomainBoundaries(0, 14, BoundaryMode.FIXED);
            plot.getGraphWidget().setDomainLabelOrientation(-90);

            CustomBarFormatter dataFormatter = new CustomBarFormatter(highColor, midColor, lowColor);
//            BarFormatter dataFormatter = new BarFormatter(Color.YELLOW, Color.TRANSPARENT);

            plot.addSeries(data.data, dataFormatter);
            setGraphParameters(plot);

            //Sets the right margins for the graph
            plot.getGraphWidget().setMargins(PixelUtils.dpToPix(20), PixelUtils.dpToPix(20), PixelUtils.dpToPix(20), PixelUtils.dpToPix(20));
            return true;
        } else {
            return false;
        }
    }
}