package com.tomandfelix.stapp2.graphtools;

import android.graphics.Color;
import android.graphics.Paint;

import com.androidplot.ui.SeriesRenderer;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.FillDirection;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYRegionFormatter;
import com.androidplot.xy.XYSeriesFormatter;

/**
 * Created by Tom on 3/03/2015.
 */
public class CustomXYFormatter extends XYSeriesFormatter<XYRegionFormatter> {

    private static final float DEFAULT_LINE_STROKE_WIDTH_DP   = 2;

    public FillDirection getFillDirection() {
        return fillDirection;
    }

    /**
     * Sets which edge to use to close the line's path for filling purposes.
     * See {@link FillDirection}.
     * @param fillDirection
     */
    public void setFillDirection(FillDirection fillDirection) {
        this.fillDirection = fillDirection;
    }

    protected FillDirection fillDirection = FillDirection.BOTTOM;
    protected Paint increasePaint;
    protected Paint levelPaint;
    protected Paint decreasePaint;

    /**
     * Should only be used in conjunction with calls to configure()...
     */
    public CustomXYFormatter() {
        this(Color.GREEN, Color.YELLOW, Color.RED);
    }

    public CustomXYFormatter(Integer increaseColor, Integer levelColor, Integer decreaseColor) {
        this(increaseColor, levelColor, decreaseColor, FillDirection.BOTTOM);
    }

    public CustomXYFormatter(Integer increaseColor, Integer levelColor, Integer decreaseColor, FillDirection fillDir) {
        initIncreasePaint(increaseColor);
        initLevelPaint(levelColor);
        initDecreasePaint(decreaseColor);
        setFillDirection(fillDir);
    }

    @Override
    public Class<? extends SeriesRenderer> getRendererClass() {
        return CustomXYRenderer.class;
    }

    @Override
    public SeriesRenderer getRendererInstance(XYPlot plot) {
        return new CustomXYRenderer(plot);
    }

    protected void initIncreasePaint(Integer increaseColor) {
        if (increaseColor == null) {
            increasePaint = null;
        } else {
            increasePaint = new Paint();
            increasePaint.setAntiAlias(true);
            increasePaint.setStrokeWidth(PixelUtils.dpToPix(DEFAULT_LINE_STROKE_WIDTH_DP));
            increasePaint.setColor(increaseColor);
            increasePaint.setStyle(Paint.Style.STROKE);
        }
    }

    protected void initLevelPaint(Integer levelColor) {
        if (levelColor == null) {
            levelPaint = null;
        } else {
            levelPaint = new Paint();
            levelPaint.setAntiAlias(true);
            levelPaint.setStrokeWidth(PixelUtils.dpToPix(DEFAULT_LINE_STROKE_WIDTH_DP));
            levelPaint.setColor(levelColor);
            levelPaint.setStyle(Paint.Style.STROKE);
        }
    }

    protected void initDecreasePaint(Integer decreaseColor) {
        if (decreaseColor == null) {
            decreasePaint = null;
        } else {
            decreasePaint = new Paint();
            decreasePaint.setAntiAlias(true);
            decreasePaint.setStrokeWidth(PixelUtils.dpToPix(DEFAULT_LINE_STROKE_WIDTH_DP));
            decreasePaint.setColor(decreaseColor);
            decreasePaint.setStyle(Paint.Style.STROKE);
        }
    }

    public Paint getIncreasePaint() {
        return increasePaint;
    }

    public void setIncreasePaint(Paint increasePaint) {
        this.increasePaint = increasePaint;
    }

    public Paint getLevelPaint() {
        return levelPaint;
    }

    public void setLevelPaint(Paint levelPaint) {
        this.levelPaint = levelPaint;
    }

    public Paint getDecreasePaint() {
        return decreasePaint;
    }

    public void setDecreasePaint(Paint decreasePaint) {
        this.decreasePaint = decreasePaint;
    }
}
