package com.android.cineflow.ui.admin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
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
    private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Bar> bars = new ArrayList<>();

    public BarChartView(Context c) { super(c); init(); }
    public BarChartView(Context c, @Nullable AttributeSet a) { super(c, a); init(); }
    public BarChartView(Context c, @Nullable AttributeSet a, int d) { super(c, a, d); init(); }

    private void init() {
        trackPaint.setColor(Color.parseColor("#2A2A2A"));
        labelPaint.setColor(Color.parseColor("#FFFFFF"));
        valuePaint.setColor(Color.parseColor("#FF6600"));
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
        int rowHeight = (int) (44 * density);
        int desired = Math.max(bars.size(), 1) * rowHeight + (int) (8 * density);
        int w = MeasureSpec.getSize(wSpec);
        setMeasuredDimension(w, desired);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        if (bars.isEmpty()) {
            Paint empty = new Paint(Paint.ANTI_ALIAS_FLAG);
            empty.setColor(Color.parseColor("#666666"));
            empty.setTextSize(13 * density);
            empty.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(getContext().getString(com.android.cineflow.R.string.admin_chart_empty),
                    getWidth() / 2f, getHeight() / 2f + empty.getTextSize() / 3f, empty);
            return;
        }

        labelPaint.setTextSize(12 * density);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        valuePaint.setTextSize(12 * density);
        valuePaint.setFakeBoldText(true);

        float max = 0f;
        for (Bar b : bars) if (b.value > max) max = b.value;
        if (max <= 0) max = 1;

        int w = getWidth();
        float rowH = 44 * density;
        float barH = 8 * density;
        float radius = barH / 2f;
        float gapLabelBar = 6 * density;
        float valueGap = 8 * density;

        // Reserve right column for value text — sized by widest value string.
        float valueColW = 0f;
        for (Bar b : bars) {
            float vw = valuePaint.measureText(b.valueLabel);
            if (vw > valueColW) valueColW = vw;
        }
        float barLeft = 0f;
        float barRight = w - valueColW - valueGap;
        if (barRight - barLeft < 40 * density) barRight = barLeft + 40 * density;

        for (int i = 0; i < bars.size(); i++) {
            Bar b = bars.get(i);
            float rowTop = i * rowH;

            // Row 1: ellipsized label across full width.
            float labelMax = w;
            CharSequence shown = TextUtils.ellipsize(b.label, labelPaint, labelMax, TextUtils.TruncateAt.END);
            float labelBaseline = rowTop + labelPaint.getTextSize() + 2 * density;
            canvas.drawText(shown, 0, shown.length(), barLeft, labelBaseline, labelPaint);

            // Row 2: bar + value.
            float trackTop = labelBaseline + gapLabelBar;
            RectF track = new RectF(barLeft, trackTop, barRight, trackTop + barH);
            canvas.drawRoundRect(track, radius, radius, trackPaint);

            float ratio = b.value / max;
            float fillRight = barLeft + (barRight - barLeft) * ratio;
            barPaint.setColor(b.color);
            RectF fill = new RectF(barLeft, trackTop, fillRight, trackTop + barH);
            canvas.drawRoundRect(fill, radius, radius, barPaint);

            valuePaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(b.valueLabel, w,
                    trackTop + barH / 2f + valuePaint.getTextSize() / 3f, valuePaint);
        }
    }
}
