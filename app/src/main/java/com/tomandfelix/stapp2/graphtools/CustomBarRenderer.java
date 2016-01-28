package com.tomandfelix.stapp2.graphtools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.androidplot.exception.PlotRenderException;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Tom on 14/03/2015.
 */
public class CustomBarRenderer extends XYSeriesRenderer<CustomBarFormatter> {
    private float barWidth = 10;
    private float barGap = 1;
    private Comparator<Bar> barComparator = new BarComparator();

    public CustomBarRenderer(XYPlot plot) {
        super(plot);
    }

    /**
     * Sets the width of the bars when using the FIXED_WIDTH render style
     * @param barWidth
     */
    public void setBarWidth(float barWidth) {
        this.barWidth = barWidth;
    }

    /**
     * Sets the size of the gap between the bar (or bar groups) when using the VARIABLE_WIDTH render style
     * @param barGap
     */
    public void setBarGap(float barGap) {
        this.barGap = barGap;
    }

    /**
     * Sets a {@link Comparator} used for sorting bars.
     */
    public void setBarComparator(Comparator<Bar> barComparator) {
        this.barComparator = barComparator;
    }

    @Override
    public void doDrawLegendIcon(Canvas canvas, RectF rect, CustomBarFormatter formatter) {
        canvas.drawRect(rect, formatter.getMidPaint());
    }

    /**
     * Retrieves the BarFormatter instance that corresponds with the series passed in.
     * Can be overridden to return other BarFormatters as a result of touch events etc.
     * @param index index of the point being rendered.
     * @param series XYSeries to which the point being rendered belongs.
     * @return
     */
    @SuppressWarnings("UnusedParameters")
    public CustomBarFormatter getFormatter(int index, XYSeries series) {
        return getFormatter(series);
    }

    public void onRender(Canvas canvas, RectF plotArea) throws PlotRenderException {

        List<XYSeries> sl = getPlot().getSeriesListForRenderer(this.getClass());

        TreeMap<Number, BarGroup> axisMap = new TreeMap<>();

        // dont try to render anything if there's nothing to render.
        if(sl == null) return;

        /*
         * Build the axisMap (yVal,BarGroup)... a TreeMap of BarGroups
         * BarGroups represent a point on the X axis where a single or group of bars need to be drawn.
         */

        // For each Series
        for(XYSeries series : sl) {
            BarGroup barGroup;

            // For each value in the series
            for(int i = 0; i < series.size(); i++) {

                if (series.getX(i) != null) {

                    // get a new bar object
                    Bar b = new Bar(series,i,plotArea);

                    // Find or create the barGroup
                    if (axisMap.containsKey(b.intX)) {
                        barGroup = axisMap.get(b.intX);
                    } else {
                        barGroup = new BarGroup(b.intX,plotArea);
                        axisMap.put(b.intX, barGroup);
                    }
                    barGroup.addBar(b);
                }

            }
        }

        // Loop through the axisMap linking up prev pointers
        BarGroup prev, current;
        prev = null;
        for(Map.Entry<Number, BarGroup> mapEntry : axisMap.entrySet()) {
            current = mapEntry.getValue();
            current.prev = prev;
            prev = current;
        }


        // The default gap between each bar section
        int gap  = (int) barGap;

        // Determine roughly how wide (rough_width) this bar should be. This is then used as a default width
        // when there are gaps in the data or for the first/last bars.
        float f_rough_width = ((plotArea.width() - ((axisMap.size() - 1) * gap)) / (axisMap.size() - 1));
        int rough_width = (int) f_rough_width;
        if (rough_width < 0) rough_width = 0;
        if (gap > rough_width) {
            gap = rough_width / 2;
        }

        //Log.d("PARAMTER","PLOT_WIDTH=" + plotArea.width());
        //Log.d("PARAMTER","BAR_GROUPS=" + axisMap.size());
        //Log.d("PARAMTER","ROUGH_WIDTH=" + rough_width);
        //Log.d("PARAMTER","GAP=" + gap);

		/*
		 * Calculate the dimensions of each barGroup and then draw each bar within it according to
		 * the Render Style and Width Style.
		 */

        for(Number key : axisMap.keySet()) {

            BarGroup barGroup = axisMap.get(key);

            // Determine the exact left and right X for the Bar Group
            // use intX and go halfwidth either side.
            barGroup.leftX = barGroup.intX - (int) (barWidth / 2);
            barGroup.width = (int) barWidth;
            barGroup.rightX = barGroup.leftX + barGroup.width;

            //Log.d("BAR_GROUP", "rough_width=" + rough_width + " width=" + barGroup.width + " <" + barGroup.leftX + "|" + barGroup.intX + "|" + barGroup.rightX + ">");

    		/*
    		 * Draw the bars within the barGroup area.
    		 */
            int width = barGroup.width / barGroup.bars.size();
            int leftX = barGroup.leftX;
            Collections.sort(barGroup.bars, barComparator);
            for (Bar b : barGroup.bars) {
                CustomBarFormatter formatter = b.formatter();
                //Log.d("BAR", "width=" + width + " <" + leftX + "|" + b.intX + "|" + (leftX + width) + "> " + b.intY);
                if (b.barGroup.width >= 2) {
                    Paint paint;
                    switch (b.level) {
                        case "High":
                            paint = formatter.getHighPaint();
                            break;
                        case "Mid":
                            paint = formatter.getMidPaint();
                            break;
                        default:
                            paint = formatter.getLowPaint();
                            break;
                    }
                    canvas.drawRect(leftX, b.intY, leftX + width, b.barGroup.plotArea.bottom, paint);
                }
                leftX = leftX + width;
            }

        }

    }

    public class Bar {
        public final XYSeries series;
        public final int seriesIndex;
        public final double yVal, xVal;
        public final int intX, intY;
        public final float pixX, pixY;
        protected BarGroup barGroup;
        public String level;

        public Bar(XYSeries series, int seriesIndex, RectF plotArea) {
            this.series = series;
            this.seriesIndex = seriesIndex;

            this.xVal = series.getX(seriesIndex).doubleValue();
            this.pixX = ValPixConverter.valToPix(xVal, getPlot().getCalculatedMinX().doubleValue(), getPlot().getCalculatedMaxX().doubleValue(), plotArea.width(), false) + (plotArea.left);
            this.intX = (int) pixX;

            if (series.getY(seriesIndex) != null) {
                this.yVal = series.getY(seriesIndex).doubleValue();
                level = yVal > 90 ? "High" : (yVal > 75 ? "Mid" : "Low");
                this.pixY = ValPixConverter.valToPix(yVal, getPlot().getCalculatedMinY().doubleValue(), getPlot().getCalculatedMaxY().doubleValue(), plotArea.height(), true) + plotArea.top;
                this.intY = (int) pixY;
            } else {
                this.yVal = 0;
                level = "Low";
                this.pixY = plotArea.bottom;
                this.intY = (int) pixY;
            }
        }
        public CustomBarFormatter formatter() {
            return getFormatter(seriesIndex, series);
        }
    }

    private class BarGroup {
        public ArrayList<Bar> bars;
        public int intX;
        public int width, leftX, rightX;
        public RectF plotArea;
        public BarGroup prev;

        public BarGroup(int intX, RectF plotArea) {
            // Setup the TreeMap with the required comparator
            this.bars = new ArrayList<>(); // create a comparator that compares series title given the index.
            this.intX = intX;
            this.plotArea = plotArea;
        }

        public void addBar(Bar bar) {
            bar.barGroup = this;
            this.bars.add(bar);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public class BarComparator implements Comparator<Bar>{
        @Override
        public int compare(Bar bar1, Bar bar2) {
            return bar1.series.getTitle().compareToIgnoreCase(bar2.series.getTitle());
        }
    }

}