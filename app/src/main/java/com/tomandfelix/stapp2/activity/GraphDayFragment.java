package com.tomandfelix.stapp2.activity;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.graphtools.GraphParser;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;

/**
 * Created by Tom on 18/03/2015.
 */
public class GraphDayFragment extends Fragment {
    private GraphParser.DailyGraphData dailydata;

    public GraphDayFragment() {
        super();
        dailydata = GraphParser.formatDailyData(DatabaseHelper.getInstance().getTodaysLogs(), DatabaseHelper.getInstance().getTodaysConnectionLogs());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return drawDailyPlot(inflater, container);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewGroup container = (ViewGroup) getView();
        if(container != null) {
            container.removeAllViewsInLayout();
            container.addView(drawDailyPlot(LayoutInflater.from(getActivity()), container));
        }
    }

    private View drawDailyPlot(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        XYPlot plot = (XYPlot) rootView.findViewById(R.id.chart);
        boolean success = GraphParser.formatDailyGraph(plot, dailydata,
                getResources().getColor(R.color.green),
                getResources().getColor(R.color.orange),
                getResources().getColor(R.color.red));
        if(!success) {
            rootView.findViewById(R.id.chart).setVisibility(View.GONE);
            rootView.findViewById(R.id.graph_error).setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.graph_error)).setText(getResources().getString(R.string.graph_daily_error));
        }
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ((TextView) rootView.findViewById(R.id.graph_label_1)).setText(getString(R.string.graph_good_time));
            ((TextView) rootView.findViewById(R.id.graph_label_2)).setText(getString(R.string.graph_bad_time));
            ((TextView) rootView.findViewById(R.id.graph_label_3)).setText(getString(R.string.graph_score));
            if(success) {
                ((TextView) rootView.findViewById(R.id.graph_value_1)).setText(dailydata.getTotalGood());
                ((TextView) rootView.findViewById(R.id.graph_value_2)).setText(dailydata.getTotalBad());
                TextView score = (TextView) rootView.findViewById(R.id.graph_value_3);
                score.setText(String.format("%.1f", dailydata.getTotalScore()) + " %");
                if(dailydata.getTotalScore() > 90) {score.setTextColor(getResources().getColor(R.color.green));}
                else if (dailydata.getTotalScore() < 75) {score.setTextColor(getResources().getColor(R.color.red));}
                else {score.setTextColor(getResources().getColor(R.color.orange));}
            } else {
                ((TextView) rootView.findViewById(R.id.graph_value_1)).setText("00:00:00");
                ((TextView) rootView.findViewById(R.id.graph_value_2)).setText("00:00:00");
                ((TextView) rootView.findViewById(R.id.graph_value_3)).setText("N/A");
            }
        }
        return rootView;
    }
}
