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

import java.util.Date;

/**
 * Created by Tom on 18/03/2015.
 */
public class GraphWeekFragment extends Fragment {
    private GraphParser.LongTermGraphData longTermData;

    public GraphWeekFragment() {
        super();
        longTermData = GraphParser.formatLongTermData(DatabaseHelper.getInstance().get2WeekEndLogs(), new Date());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return drawWeekPlot(inflater, container);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewGroup container = (ViewGroup) getView();
        if(container != null) {
            container.removeAllViewsInLayout();
            container.addView(drawWeekPlot(LayoutInflater.from(getActivity()), container));
        }
    }

    public View drawWeekPlot(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        XYPlot plot = (XYPlot) rootView.findViewById(R.id.chart);
        boolean success = GraphParser.formatLongTermGraph(plot, longTermData,
                getResources().getColor(R.color.green),
                getResources().getColor(R.color.orange),
                getResources().getColor(R.color.red));
        if(!success) {
            rootView.findViewById(R.id.chart).setVisibility(View.GONE);
            rootView.findViewById(R.id.graph_error).setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.graph_error)).setText(getResources().getString(R.string.graph_2week_error));
        }
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ((TextView) rootView.findViewById(R.id.graph_label_1)).setText(getString(R.string.graph_average_score));
            ((TextView) rootView.findViewById(R.id.graph_label_2)).setText(getString(R.string.graph_average_connection_time));
            ((TextView) rootView.findViewById(R.id.graph_label_3)).setText("");
            if(success) {
                TextView averageScore = (TextView) rootView.findViewById(R.id.graph_value_1);
                averageScore.setText(String.format("%.1f", longTermData.getAverageScore()) + " %");
                if(longTermData.getAverageScore() > 90) {averageScore.setTextColor(getResources().getColor(R.color.green));}
                else if (longTermData.getAverageScore() < 75) {averageScore.setTextColor(getResources().getColor(R.color.red));}
                else {averageScore.setTextColor(getResources().getColor(R.color.orange));}
                ((TextView) rootView.findViewById(R.id.graph_value_2)).setText(longTermData.getAverageConnectionTime());
                ((TextView) rootView.findViewById(R.id.graph_value_3)).setText("");
            } else {
                ((TextView) rootView.findViewById(R.id.graph_value_1)).setText("N/A");
                ((TextView) rootView.findViewById(R.id.graph_value_2)).setText("00:00:00");
                ((TextView) rootView.findViewById(R.id.graph_value_3)).setText("");
            }
        }
        return rootView;
    }
}
