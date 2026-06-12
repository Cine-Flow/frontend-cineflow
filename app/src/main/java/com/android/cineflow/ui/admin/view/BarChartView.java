package com.android.cineflow.ui.admin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Horizontal bar chart with label on the left, value on the right. */
public class BarChartView extends View {

    public static class Bar {
        public final String label;
        public final float value;
        public final String valueLabel;
        public final int color;
        public Bar(String label, float value, String valueLabel, int color) {
            this.label = label;
            this.value = value;
            this.valueLabel = valueLabel;
            this.color = color;
        }
    }

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Bar> bars = new ArrayList<>();

    public BarChartView(Context c) { super(c); init(); }
    public BarChartView(Context c, @Nullable AttributeSet a) { super(c, a); init(); }
    public BarChartView(Context c, @Nullable AttributeSet a, int d) { super(c, a, d); init(); }

    private void init() {
        trackPaint.setColor(Color.parseColor("#E9ECEF"));
        labelPaint.setColor(Color.parseColor("#212529"));
        valuePaint.setColor(Color.parseColor("#495057"));
        valuePaint.setTextAlign(Paint.Align.RIGHT);
    }

    public void setBars(List<Bar> bars) {
        this.bars = bars != null ? bars : new ArrayList<>();
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        super.onMeasure(wSpec, hSpec);
        float density = getResources().getDisplayMetrics().density;
        int rowHeight = (int) (38 * density);
        int desired = Math.max(bars.size(), 1) * rowHeight + (int) (8 * density);
        int w = MeasureSpec.getSize(wSpec);
        setMeasuredDimension(w, desired);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bars.isEmpty()) return;
        float density = getResources().getDisplayMetrics().density;

        labelPaint.setTextSize(13 * density);
        valuePaint.setTextSize(12 * density);
        valuePaint.setFakeBoldText(true);

        float max = 0f;
        for (Bar b : bars) if (b.value > max) max = b.value;
        if (max <= 0) max = 1;

        int w = getWidth();
        float labelCol = w * 0.35f;
        float valueCol = w * 0.16f;
        float barLeft = labelCol + 8 * density;
        float barRight = w - valueCol - 8 * density;
        float rowH = 38 * density;
        float barH = 10 * density;
        float radius = barH / 2f;

        for (int i = 0; i < bars.size(); i++) {
            Bar b = bars.get(i);
            float cy = i * rowH + rowH / 2f;
            canvas.drawText(b.label, 0, cy + labelPaint.getTextSize() / 3f, labelPaint);

            float trackTop = cy - barH / 2f;
            RectF track = new RectF(barLeft, trackTop, barRight, trackTop + barH);
            canvas.drawRoundRect(track, radius, radius, trackPaint);

            float ratio = b.value / max;
            float fillRight = barLeft + (barRight - barLeft) * ratio;
            barPaint.setColor(b.color);
            RectF fill = new RectF(barLeft, trackTop, fillRight, trackTop + barH);
            canvas.drawRoundRect(fill, radius, radius, barPaint);

            canvas.drawText(b.valueLabel, w, cy + valuePaint.getTextSize() / 3f, valuePaint);
        }
    }
}
