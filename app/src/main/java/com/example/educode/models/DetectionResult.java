package com.example.educode.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetectionResult {
    private int id;
    private String signName;
    private String signImage;
    private float confidence;
    private long timestamp;

    public DetectionResult(int id, String signName, String signImage, float confidence, long timestamp) {
        this.id = id;
        this.signName = signName;
        this.signImage = signImage;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }

    public String getSignName() { return signName; }
    public String getSignImage() { return signImage; }
    public float getConfidence() { return confidence; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedConfidence() {
        return String.format(Locale.getDefault(), "%.0f%%", confidence * 100);
    }
}