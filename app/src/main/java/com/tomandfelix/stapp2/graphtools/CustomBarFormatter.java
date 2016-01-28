package com.tomandfelix.stapp2.graphtools;

import android.graphics.Color;
import android.graphics.Paint;

import com.androidplot.ui.SeriesRenderer;
import com.androidplot.xy.FillDirection;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYRegionFormatter;
import com.androidplot.xy.XYSeriesFormatter;

/**
 * Created by Tom on 14/03/2015.
 */
public class CustomBarFormatter extends XYSeriesFormatter<XYRegionFormatter> {

    public FillDirection getFillDirection() {
        return fillDirection;
    }

    /**
     * Sets which edge to use to close the line's path for filling purposes.
     * See {@link FillDirection}.
     * @param fillDirection The filldirection
     */
    public void setFillDirection(FillDirection fillDirection) {
        this.fillDirection = fillDirection;
    }

    protected FillDirection fillDirection = FillDirection.BOTTOM;
    protected Paint highPaint;
    protected Paint midPaint;
    protected Paint lowPaint;

    /**
     * Should only be used in conjunction with calls to configure()...
     */
    public CustomBarFormatter() {
        this(Color.GREEN, Color.YELLOW, Color.RED);
    }

    public CustomBarFormatter(Integer highColor, Integer midColor, Integer lowColor) {
        this(highColor, midColor, lowColor, FillDirection.BOTTOM);
    }

    public CustomBarFormatter(Integer highColor, Integer midColor, Integer lowColor, FillDirection fillDir) {
        initHighPaint(highColor);
        initMidPaint(midColor);
        initLowPaint(lowColor);
        setFillDirection(fillDir);
    }

    @Override
    public Class<? extends SeriesRenderer> getRendererClass() {
        return CustomBarRenderer.class;
    }

    @Override
    public SeriesRenderer getRendererInstance(XYPlot plot) {
        return new CustomBarRenderer(plot);
    }

    protected void initHighPaint(Integer highColor) {
        if (highColor == null) {
            highPaint = null;
        } else {
            highPaint = new Paint();
            highPaint.setColor(highColor);
            highPaint.setAntiAlias(true);
            highPaint.setStyle(Paint.Style.FILL);
        }
    }

    protected void initMidPaint(Integer midColor) {
        if (midColor == null) {
            midPaint = null;
        } else {
            midPaint = new Paint();
            midPaint.setColor(midColor);
            midPaint.setAntiAlias(true);
            midPaint.setStyle(Paint.Style.FILL);
        }
    }

    protected void initLowPaint(Integer lowColor) {
        if (lowColor == null) {
            lowPaint = null;
        } else {
            lowPaint = new Paint();
            lowPaint.setColor(lowColor);
            lowPaint.setAntiAlias(true);
            lowPaint.setStyle(Paint.Style.FILL);
        }
    }

    public Paint getHighPaint() {
        return highPaint;
    }

    public void setHighPaint(Paint HighPaint) {
        this.highPaint = highPaint;
    }

    public Paint getMidPaint() {
        return midPaint;
    }

    public void setMidPaint(Paint MidPaint) {
        this.midPaint = midPaint;
    }

    public Paint getLowPaint() {
        return lowPaint;
    }

    public void setLowPaint(Paint LowPaint) {
        this.lowPaint = lowPaint;
    }
}
