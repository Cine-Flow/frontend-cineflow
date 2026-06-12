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

public class DonutChartView extends View {

    public static class Slice {
        public final String label;
        public final float value;
        public final int color;
        public Slice(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private List<Slice> slices = new ArrayList<>();
    private String centerLabel = "";
    private String centerSubLabel = "";
    private int trackColor = Color.parseColor("#E9ECEF");

    public DonutChartView(Context c) { super(c); init(); }
    public DonutChartView(Context c, @Nullable AttributeSet a) { super(c, a); init(); }
    public DonutChartView(Context c, @Nullable AttributeSet a, int d) { super(c, a, d); init(); }

    private void init() {
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);
        textPaint.setColor(Color.parseColor("#212529"));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        subTextPaint.setColor(Color.parseColor("#868E96"));
        subTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setSlices(List<Slice> slices) {
        this.slices = slices != null ? slices : new ArrayList<>();
        invalidate();
    }

    public void setCenterText(String label, String subLabel) {
        this.centerLabel = label != null ? label : "";
        this.centerSubLabel = subLabel != null ? subLabel : "";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight();
        int size = Math.min(w, h);
        float density = getResources().getDisplayMetrics().density;
        float stroke = 18f * density;
        arcPaint.setStrokeWidth(stroke);

        float pad = stroke / 2f + 2f * density;
        rect.set(pad, pad, size - pad, size - pad);
        rect.offset((w - size) / 2f, (h - size) / 2f);

        float total = 0;
        for (Slice s : slices) total += s.value;

        arcPaint.setColor(trackColor);
        canvas.drawArc(rect, 0, 360, false, arcPaint);

        if (total > 0) {
            float start = -90f;
            float gap = 2f;
            for (Slice s : slices) {
                if (s.value <= 0) continue;
                float sweep = (s.value / total) * 360f - gap;
                if (sweep < 0) sweep = 0;
                arcPaint.setColor(s.color);
                canvas.drawArc(rect, start, sweep, false, arcPaint);
                start += sweep + gap;
            }
        }

        textPaint.setTextSize(22f * density);
        subTextPaint.setTextSize(11f * density);
        float cx = rect.centerX();
        float cy = rect.centerY();
        canvas.drawText(centerLabel, cx, cy + 6f * density, textPaint);
        canvas.drawText(centerSubLabel, cx, cy + 22f * density, subTextPaint);
    }
}
