package com.tomandfelix.stapp2.graphtools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Pair;

import com.androidplot.exception.PlotRenderException;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.RectRegion;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYRegionFormatter;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 3/03/2015.
 */
public class CustomXYRenderer extends XYSeriesRenderer<CustomXYFormatter> {

    public CustomXYRenderer(XYPlot plot) {
        super(plot);
    }

    @Override
    public void onRender(Canvas canvas, RectF plotArea) throws PlotRenderException {
        List<XYSeries> seriesList = getPlot().getSeriesListForRenderer(this.getClass());
        if (seriesList != null) {
            for (XYSeries series : seriesList) {
                //synchronized(series) {
                drawSeries(canvas, plotArea, series, getFormatter(series));
                //}
            }
        }
    }

    @Override
    public void doDrawLegendIcon(Canvas canvas, RectF rect, CustomXYFormatter formatter) {
        if(formatter.getIncreasePaint() != null) {
            canvas.drawLine(rect.left, rect.bottom, rect.right, rect.top, formatter.getIncreasePaint());
        }
    }

    protected void appendToPath(Path path, PointF thisPoint, PointF lastPoint) {
        path.moveTo(lastPoint.x, lastPoint.y);
        path.lineTo(thisPoint.x, thisPoint.y);
    }

    protected void drawSeries(Canvas canvas, RectF plotArea, XYSeries series, CustomXYFormatter formatter) {
        PointF thisPoint;
        PointF lastPoint = null;
        PointF firstPoint = null;
        Paint increasePaint = formatter.getIncreasePaint();
        Paint levelPaint = formatter.getLevelPaint();
        Paint decreasePaint = formatter.getDecreasePaint();

        Path increasePath = null;
        Path levelPath = null;
        Path decreasePath = null;
        ArrayList<Pair<PointF, Integer>> points = new ArrayList<>(series.size());
        for (int i = 0; i < series.size(); i++) {
            Number y = series.getY(i);
            Number x = series.getX(i);

            if (y != null && x != null) {
                thisPoint = ValPixConverter.valToPix(
                        x,
                        y,
                        plotArea,
                        getPlot().getCalculatedMinX(),
                        getPlot().getCalculatedMaxX(),
                        getPlot().getCalculatedMinY(),
                        getPlot().getCalculatedMaxY());
                points.add(new Pair<>(thisPoint, i));
            } else {
                thisPoint = null;
            }

            if(increasePaint != null && levelPaint != null && decreasePaint != null && thisPoint != null) {
                if (firstPoint == null) {
                    increasePath = new Path();
                    levelPath = new Path();
                    decreasePath = new Path();
                    firstPoint = thisPoint;
                }

                if (lastPoint != null) {
                    if(thisPoint.y == lastPoint.y) {
                        appendToPath(levelPath, thisPoint, lastPoint);
                    } else if(thisPoint.y < lastPoint.y) {
                        appendToPath(increasePath, thisPoint, lastPoint);
                    } else {
                        appendToPath(decreasePath, thisPoint, lastPoint);
                    }
                }

                lastPoint = thisPoint;
            } else {
                if(lastPoint != null) {
                    renderPath(canvas, plotArea, increasePath, levelPath, decreasePath, firstPoint, lastPoint, formatter);
                }
                firstPoint = null;
                lastPoint = null;
            }
        }
        if(increasePaint != null && decreasePaint != null && firstPoint != null) {
            renderPath(canvas, plotArea, increasePath, levelPath, decreasePath, firstPoint, lastPoint, formatter);
        }
    }

    protected void renderPath(Canvas canvas, RectF plotArea, Path increasePath, Path levelPath, Path decreasePath, PointF firstPoint, PointF lastPoint, CustomXYFormatter formatter) {
        Path outlinePath1 = new Path(increasePath);
        Path outlinePath2 = new Path(levelPath);
        Path outlinePath3 = new Path(decreasePath);

        // determine how to close the path for filling purposes:
        // We always need to calculate this path because it is also used for
        // masking off for region highlighting.
        switch (formatter.getFillDirection()) {
            case BOTTOM:
                increasePath.lineTo(lastPoint.x, plotArea.bottom);
                increasePath.lineTo(firstPoint.x, plotArea.bottom);
                increasePath.close();
                break;
            case TOP:
                increasePath.lineTo(lastPoint.x, plotArea.top);
                increasePath.lineTo(firstPoint.x, plotArea.top);
                increasePath.close();
                break;
            case RANGE_ORIGIN:
                float originPix = ValPixConverter.valToPix(
                        getPlot().getRangeOrigin().doubleValue(),
                        getPlot().getCalculatedMinY().doubleValue(),
                        getPlot().getCalculatedMaxY().doubleValue(),
                        plotArea.height(),
                        true);
                originPix += plotArea.top;

                increasePath.lineTo(lastPoint.x, originPix);
                increasePath.lineTo(firstPoint.x, originPix);
                increasePath.close();
                break;
            default:
                throw new UnsupportedOperationException("Fill direction not yet implemented: " + formatter.getFillDirection());
        }


        //}

        // draw any visible regions on top of the base region:
        double minX = getPlot().getCalculatedMinX().doubleValue();
        double maxX = getPlot().getCalculatedMaxX().doubleValue();
        double minY = getPlot().getCalculatedMinY().doubleValue();
        double maxY = getPlot().getCalculatedMaxY().doubleValue();

        // draw each region:
        for (RectRegion r : RectRegion.regionsWithin(formatter.getRegions().elements(), minX, maxX, minY, maxY)) {
            XYRegionFormatter f = formatter.getRegionFormatter(r);
            RectF regionRect = r.getRectF(plotArea, minX, maxX, minY, maxY);
            if (regionRect != null) {
                try {
                    canvas.save(Canvas.ALL_SAVE_FLAG);
                    canvas.clipPath(increasePath);
                    canvas.drawRect(regionRect, f.getPaint());
                } finally {
                    canvas.restore();
                }
            }
        }

        // finally we draw the outline path on top of everything else:
        if(formatter.getIncreasePaint() != null && formatter.getLevelPaint() != null && formatter.getDecreasePaint() != null) {
            canvas.drawPath(outlinePath1, formatter.getIncreasePaint());
            canvas.drawPath(outlinePath2, formatter.getLevelPaint());
            canvas.drawPath(outlinePath3, formatter.getDecreasePaint());
        }

        increasePath.rewind();
    }
}
