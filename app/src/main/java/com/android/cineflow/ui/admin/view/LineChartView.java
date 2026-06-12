package com.android.cineflow.ui.admin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Smoothed line chart with axis labels, gridlines, and gradient fill. */
public class LineChartView extends View {

    public static class Point {
        public final String xLabel;
        public final float value;
        public Point(String xLabel, float value) {
            this.xLabel = xLabel;
            this.value = value;
        }
    }

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Point> points = new ArrayList<>();
    private int lineColor = Color.parseColor("#E50914");

    public LineChartView(Context c) { super(c); init(); }
    public LineChartView(Context c, @Nullable AttributeSet a) { super(c, a); init(); }
    public LineChartView(Context c, @Nullable AttributeSet a, int d) { super(c, a, d); init(); }

    private void init() {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        fillPaint.setStyle(Paint.Style.FILL);
        gridPaint.setColor(Color.parseColor("#E9ECEF"));
        gridPaint.setStrokeWidth(1f);
        axisPaint.setColor(Color.parseColor("#868E96"));
        axisPaint.setTextAlign(Paint.Align.CENTER);
        dotPaint.setStyle(Paint.Style.FILL);
        dotInnerPaint.setColor(Color.WHITE);
        dotInnerPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(List<Point> points, int lineColor) {
        this.points = points != null ? points : new ArrayList<>();
        this.lineColor = lineColor;
        linePaint.setColor(lineColor);
        dotPaint.setColor(lineColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.size() < 2) return;
        float density = getResources().getDisplayMetrics().density;
        linePaint.setStrokeWidth(2.5f * density);
        axisPaint.setTextSize(10f * density);

        float padL = 36 * density;
        float padR = 12 * density;
        float padT = 12 * density;
        float padB = 22 * density;

        float w = getWidth();
        float h = getHeight();
        float chartW = w - padL - padR;
        float chartH = h - padT - padB;

        float max = 0f, min = Float.MAX_VALUE;
        for (Point p : points) {
            if (p.value > max) max = p.value;
            if (p.value < min) min = p.value;
        }
        if (max == min) max = min + 1;
        float range = max - min;

        // 4 horizontal gridlines + y labels
        for (int i = 0; i <= 4; i++) {
            float y = padT + chartH * (i / 4f);
            canvas.drawLine(padL, y, w - padR, y, gridPaint);
            float val = max - range * (i / 4f);
            String s = formatTick(val);
            axisPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(s, padL - 4 * density,
                    y + axisPaint.getTextSize() / 3f, axisPaint);
        }

        int n = points.size();
        float stepX = chartW / (n - 1);
        float[] xs = new float[n];
        float[] ys = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = padL + stepX * i;
            ys[i] = padT + chartH * (1f - (points.get(i).value - min) / range);
        }

        // Gradient fill under the curve
        Path fillPath = buildSmoothPath(xs, ys);
        fillPath.lineTo(xs[n - 1], padT + chartH);
        fillPath.lineTo(xs[0], padT + chartH);
        fillPath.close();
        int fillTop = (lineColor & 0x00FFFFFF) | 0x55000000;
        int fillBot = (lineColor & 0x00FFFFFF) | 0x00000000;
        fillPaint.setShader(new LinearGradient(0, padT, 0, padT + chartH,
                fillTop, fillBot, Shader.TileMode.CLAMP));
        canvas.drawPath(fillPath, fillPaint);
        fillPaint.setShader(null);

        // Line
        Path linePath = buildSmoothPath(xs, ys);
        canvas.drawPath(linePath, linePaint);

        // Dots
        float dotR = 4 * density;
        float innerR = 2 * density;
        for (int i = 0; i < n; i++) {
            canvas.drawCircle(xs[i], ys[i], dotR, dotPaint);
            canvas.drawCircle(xs[i], ys[i], innerR, dotInnerPaint);
        }

        // X labels — show ~6
        axisPaint.setTextAlign(Paint.Align.CENTER);
        int labelStep = Math.max(1, n / 6);
        for (int i = 0; i < n; i += labelStep) {
            canvas.drawText(points.get(i).xLabel, xs[i],
                    h - 6 * density, axisPaint);
        }
    }

    private Path buildSmoothPath(float[] xs, float[] ys) {
        Path path = new Path();
        path.moveTo(xs[0], ys[0]);
        for (int i = 1; i < xs.length; i++) {
            float cx = (xs[i - 1] + xs[i]) / 2f;
            path.cubicTo(cx, ys[i - 1], cx, ys[i], xs[i], ys[i]);
        }
        return path;
    }

    private String formatTick(float v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000f);
        if (v >= 1_000) return String.format("%.1fk", v / 1_000f);
        return String.valueOf(Math.round(v));
    }
}
