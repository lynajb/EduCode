package com.example.educode.ui.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class ResultView extends View {

    private final Paint boxPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint textBackgroundPaint = new Paint();
    private List<DetectionResult> results = new ArrayList<>();

    private int imageWidth = 1;
    private int imageHeight = 1;

    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Box Style
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(10f); // Slightly thinner for elegance
        boxPaint.setColor(Color.parseColor("#00E676"));
        boxPaint.setStrokeCap(Paint.Cap.ROUND);
        boxPaint.setStrokeJoin(Paint.Join.ROUND);
        boxPaint.setAntiAlias(true);

        // Text Style - REDUCED SIZE
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f); // Reduced from 55f to 40f
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setAntiAlias(true);

        // Text Background
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setColor(Color.parseColor("#99000000"));
        textBackgroundPaint.setAntiAlias(true);
    }

    public void setResults(List<DetectionResult> results, int imgWidth, int imgHeight) {
        this.results = results;
        this.imageWidth = imgWidth;
        this.imageHeight = imgHeight;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (results == null || results.isEmpty()) return;

        float scaleX = (float) getWidth() / imageWidth;
        float scaleY = (float) getHeight() / imageHeight;
        float scale = Math.min(scaleX, scaleY);

        float dx = (getWidth() - (imageWidth * scale)) / 2f;
        float dy = (getHeight() - (imageHeight * scale)) / 2f;

        for (DetectionResult result : results) {
            float left = result.x * scale + dx;
            float top = result.y * scale + dy;
            float right = (result.x + result.width) * scale + dx;
            float bottom = (result.y + result.height) * scale + dy;

            // Draw Box
            RectF boxRect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(boxRect, 15f, 15f, boxPaint);

            // Draw Label
            String labelText = result.label.toUpperCase(); // Removed confidence to save space

            Rect textBounds = new Rect();
            textPaint.getTextBounds(labelText, 0, labelText.length(), textBounds);

            float padding = 12f;
            float bgLeft = left;
            float bgTop = top - textBounds.height() - (padding * 2);
            float bgRight = left + textBounds.width() + (padding * 2);
            float bgBottom = top;

            if (bgTop < 0) {
                float height = bgBottom - bgTop;
                bgTop = top;
                bgBottom = top + height;
            }

            RectF bgRect = new RectF(bgLeft, bgTop, bgRight, bgBottom);
            canvas.drawRoundRect(bgRect, 8f, 8f, textBackgroundPaint);

            canvas.drawText(labelText, bgLeft + padding, bgBottom - padding, textPaint);
        }
    }

    public static class DetectionResult {
        public float x, y, width, height;
        public String label;
        public String description;
        public double confidence;

        public DetectionResult(float x, float y, float width, float height, String label, String description, double confidence) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
            this.description = description;
            this.confidence = confidence;
        }
    }
}