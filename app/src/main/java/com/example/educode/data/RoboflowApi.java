package com.example.educode.data;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.example.educode.ui.camera.ResultView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RoboflowApi {

    // TODO: Replace with your own Roboflow API key
    // Store it in local.properties and read via BuildConfig — never hardcode keys
    private static final String API_KEY = BuildConfig.ROBOFLOW_API_KEY;

    private static final String[] MODEL_IDS = {
            "road-signs-ohan1/1",
            "road-signs-6ih4y/1",
            "traffic-road-signs/2",
            "traffic-signs-and-traffic-lights/6"
    };

    private final OkHttpClient client;

    public RoboflowApi() {
        client = new OkHttpClient.Builder()
                .connectTimeout(45, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .build();
    }

    public interface ApiCallback {
        void onSuccess(List<ResultView.DetectionResult> results);
        void onError(String error);
    }

    public void detect(Bitmap bitmap, ApiCallback callback) {
        String base64Image = bitmapToBase64(bitmap);
        RequestBody body = RequestBody.create(base64Image, MediaType.parse("application/x-www-form-urlencoded"));

        List<ResultView.DetectionResult> allRawResults = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(MODEL_IDS.length);
        final boolean[] hasNetworkError = {false};
        final String[] errorMessage = {""};

        for (String modelId : MODEL_IDS) {
            String url = "https://detect.roboflow.com/" + modelId + "?api_key=" + API_KEY;
            Request request = new Request.Builder().url(url).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("Roboflow", "Failed: " + modelId, e);
                    hasNetworkError[0] = true;
                    errorMessage[0] = e.getMessage();
                    latch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        List<ResultView.DetectionResult> modelResults = parseRoboflowResponse(responseData);
                        allRawResults.addAll(modelResults);
                    } else {
                        Log.e("Roboflow", "Error from " + modelId + ": " + response.code());
                    }
                    latch.countDown();
                }
            });
        }

        new Thread(() -> {
            try {
                latch.await(45, TimeUnit.SECONDS);
                if (allRawResults.isEmpty() && hasNetworkError[0]) {
                    callback.onError("Erreur réseau: " + errorMessage[0]);
                } else {
                    List<ResultView.DetectionResult> finalResults = filterOverlappingBoxes(new ArrayList<>(allRawResults));
                    callback.onSuccess(finalResults);
                }
            } catch (InterruptedException e) {
                callback.onError("Interrompu");
            }
        }).start();
    }

    private List<ResultView.DetectionResult> filterOverlappingBoxes(List<ResultView.DetectionResult> boxes) {
        if (boxes.isEmpty()) return boxes;
        Collections.sort(boxes, (a, b) -> Double.compare(b.confidence, a.confidence));

        List<ResultView.DetectionResult> selected = new ArrayList<>();
        boolean[] active = new boolean[boxes.size()];
        for (int i = 0; i < boxes.size(); i++) active[i] = true;

        for (int i = 0; i < boxes.size(); i++) {
            if (!active[i]) continue;
            ResultView.DetectionResult boxA = boxes.get(i);
            selected.add(boxA);

            for (int j = i + 1; j < boxes.size(); j++) {
                if (!active[j]) continue;
                ResultView.DetectionResult boxB = boxes.get(j);
                if (calculateIoU(boxA, boxB) > 0.45) {
                    active[j] = false;
                }
            }
        }
        return selected;
    }

    private float calculateIoU(ResultView.DetectionResult a, ResultView.DetectionResult b) {
        float xA = Math.max(a.x, b.x);
        float yA = Math.max(a.y, b.y);
        float xB = Math.min(a.x + a.width, b.x + b.width);
        float yB = Math.min(a.y + a.height, b.y + b.height);
        float interArea = Math.max(0, xB - xA) * Math.max(0, yB - yA);
        float boxAArea = a.width * a.height;
        float boxBArea = b.width * b.height;
        return interArea / (boxAArea + boxBArea - interArea);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private List<ResultView.DetectionResult> parseRoboflowResponse(String jsonResponse) {
        List<ResultView.DetectionResult> results = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray predictions = json.getJSONArray("predictions");

            for (int i = 0; i < predictions.length(); i++) {
                JSONObject obj = predictions.getJSONObject(i);
                double confidence = obj.getDouble("confidence");
                String rawClassName = obj.getString("class");

                String shortLabel = getShortFrenchLabel(rawClassName);
                String longDescription = getLongFrenchDescription(rawClassName);

                float x = (float) obj.getDouble("x");
                float y = (float) obj.getDouble("y");
                float width = (float) obj.getDouble("width");
                float height = (float) obj.getDouble("height");
                float x1 = x - (width / 2);
                float y1 = y - (height / 2);

                results.add(new ResultView.DetectionResult(x1, y1, width, height, shortLabel, longDescription, confidence));
            }
        } catch (Exception e) {
            Log.e("Roboflow", "Parse error", e);
        }
        return results;
    }
    // --- SHORT LABEL (For Bounding Box) ---
    private String getShortFrenchLabel(String rawName) {
        if (rawName == null) return "Panneau";
        String name = rawName.trim();
        String lower = name.toLowerCase(); // Make it case-insensitive

        // --- 1. SPECIFIC SIGNS (Check these FIRST) ---

        // SPEED LIMITS
        if (lower.contains("speed") || lower.contains("vitesse") || lower.contains("limit") || lower.matches(".*\\d+.*km.*")) {
            String number = name.replaceAll("[^0-9]", "");
            if (!number.isEmpty()) return "Vitesse " + number;
        }

        // STOP & YIELD
        if (lower.contains("stop")) return "STOP";
        if (lower.contains("give way") || lower.contains("cédez")) return "Cédez le passage";

        // PEDESTRIANS / SCHOOL / ANIMALS
        if (lower.contains("children") || lower.contains("enfants") || lower.contains("école") || lower.contains("ecole")) return "École";
        if (lower.contains("pedestrian") || lower.contains("piétons") || lower.contains("pietons") || lower.contains("zebra")) return "Passage Piétons";
        if (lower.contains("animal")) return "Animaux";

        // DIRECTION / OBLIGATION
        // FIX: Explicitly check for "enter_left_lane" vs "enter_right_lane"
        if (lower.contains("enter_left_lane")) return "Voie de Gauche";
        if (lower.contains("enter_right_lane")) return "Voie de Droite"; // If this class exists

        if (lower.contains("keep-right") || (lower.contains("droite") && lower.contains("oblig"))) return "Obligation Droite";
        if (lower.contains("keep-left") || (lower.contains("gauche") && lower.contains("oblig"))) return "Obligation Gauche";
        if (lower.contains("ahead only") || lower.contains("tout droit")) return "Tout Droit";
        if (lower.contains("round") || lower.contains("rond")) return "Rond-point";
        if (lower.contains("one way") || lower.contains("sens unique")) return "Sens Unique";
        if (lower.contains("dead end") || lower.contains("impasse")) return "Impasse";

        // TURNING
        if (lower.contains("no_u_turn") || lower.contains("demi-tour")) return "Demi-tour Interdit";

        if (lower.contains("turn") && lower.contains("left") && (lower.contains("no") || lower.contains("inter"))) return "Interdit Tourner G.";
        if (lower.contains("turn") && lower.contains("left")) return "Tourner à Gauche";

        if (lower.contains("turn") && lower.contains("right") && (lower.contains("no") || lower.contains("inter"))) return "Interdit Tourner D.";
        if (lower.contains("turn") && lower.contains("right")) return "Tourner à Droite";

        // CURVES
        if (lower.contains("curve") || lower.contains("virage")) {
            if (lower.contains("left") || lower.contains("gauche")) return "Virage Gauche";
            if (lower.contains("right") || lower.contains("droite")) return "Virage Droite";
            return "Virage Dangereux";
        }

        // ROAD CONDITIONS
        if (lower.contains("work") || lower.contains("travaux")) return "Travaux";
        if (lower.contains("slippery") || lower.contains("glissante")) return "Route Glissante";
        if (lower.contains("bump") || lower.contains("uneven") || lower.contains("ralentisseur")) return "Ralentisseur";

        // PROHIBITIONS
        if (lower.contains("no entry") || lower.contains("sens interdit") || lower.contains("do_not_enter")) return "Sens Interdit";
        if (lower.contains("no parking") || lower.contains("stationnement") && (lower.contains("inter") || lower.contains("no"))) return "Stationnement Interdit";
        if (lower.contains("no_over_taking") || lower.contains("dépasser")) return "Dépassement Interdit";
        if (lower.contains("horn")) return "Klaxon Interdit";
        if (lower.contains("truck")) return "Interdit Camions";

        // INDICATIONS
        if (lower.contains("parking") && !lower.contains("no")) return "Parking";
        if (lower.contains("bus")) return "Arrêt de Bus";
        if (lower.contains("highway") || lower.contains("autoroute")) return "Autoroute";

        // LIGHTS
        if (lower.contains("red") || lower.contains("rouge")) return "Feu Rouge";
        if (lower.contains("green") || lower.contains("vert")) return "Feu Vert";
        if (lower.contains("yellow") || lower.contains("orange")) return "Feu Orange";
        if (lower.contains("traffic") && lower.contains("light")) return "Feu Tricolore";

        // --- 2. GENERIC FALLBACKS ---
        if (lower.contains("danger") || lower.contains("warning") || lower.startsWith("att-")) return "Danger";

        if (lower.startsWith("inter-") || lower.contains("interdiction")) {
            String cleanName = name.replace("Inter-", "").replace("Inter", "").replace("_", " ").trim();
            if (cleanName.length() > 0) {
                return "Interdit : " + cleanName.substring(0, 1).toUpperCase() + cleanName.substring(1);
            }
            return "Interdiction";
        }
        if (lower.contains("sens interdit") || lower.contains("no entry") || lower.contains("no_entry") || lower.contains("noentry")) {
            return "Sens Interdit";
        }

        if (lower.contains("highway") || lower.contains("autoroute")) {
            return "Autoroute";
        }
        if (lower.startsWith("indic-")) {
            String cleanName = name.replace("Indic-", "").replace("Indic", "").replace("_", " ").trim();
            if (cleanName.length() > 0) {
                return cleanName.substring(0, 1).toUpperCase() + cleanName.substring(1);
            }
            return "Indication";
        }
        return name.replace("_", " ").trim();
    }

    // --- LONG DESCRIPTION (For Top Text) ---
    private String getLongFrenchDescription(String rawName) {
        String name = rawName.trim();
        String prefix = "Panneau : ";

        if (name.toLowerCase().contains("speed") || name.contains("vitesse") || name.contains("Limit")) {
            String number = name.replaceAll("[^0-9]", "");
            if (!number.isEmpty()) return prefix + "Vitesse limitée à " + number + " km/h";
        }

        switch (name) {
            // STOP
            case "Att-STOP": case "Stop_Sign": case "stop": case "StopSign":
                return prefix + "Arrêt obligatoire (STOP)";

            // CÉDEZ LE PASSAGE
            case "Att-cédez le passage": case "Give Way":
                return prefix + "Cédez le passage";

            // DANGER
            case "Att-danger": case "Attention Please-": case "warning":
                return prefix + "Danger indéterminé";
            case "Slippery Road Ahead": case "Att-route glissante":
                return prefix + "Chaussée glissante";
            case "Att-ralentisseurs": case "SpeedBreaker": case "Uneven Road":
                return prefix + "Ralentisseur ou route déformée";
            case "Att-travaux": case "RoadWork":
                return prefix + "Travaux en cours";
            case "Att-passage animaux sauvages": case "Animal Crossing":
                return prefix + "Passage d'animaux sauvages";
            case "Dangerous Curve":
                return prefix + "Virage dangereux";

            // PIÉTONS / ÉCOLE
            case "Att-passage piétons": case "Indic-passage piétons": case "Pedestrian Crossing": case "ped_crossing":
                return prefix + "Passage pour piétons";
            case "Att-passage enfants": case "Beware of children":
                return prefix + "Passage d'enfants (École)";

            // VIRAGES
            case "Att-virage à droite": case "Dangerous Rright Curve Ahead":
                return prefix + "Virage dangereux à droite";
            case "Att-virage à gauche": case "Dangerous Left Curve Ahead":
                return prefix + "Virage dangereux à gauche";
            case "Att-succession de virages": case "Left Zig Zag Traffic":
                return prefix + "Succession de virages";

            // ROND POINT
            case "Att-rond point": case "Round-About": case "Roundabout":
                return prefix + "Carrefour à sens giratoire";

            // FEUX
            case "Feu rouge": case "red_light": return "Feu rouge : Arrêt absolu";
            case "Feu vert": case "green_light": return "Feu vert : Voie libre";
            case "Traffic_signal": case "traffic_light": return prefix + "Feux tricolores";

            // INTERDICTIONS
            case "Inter-sens": case "No Entry": case "do_not_enter":
                return prefix + "Sens interdit";
            case "Inter-de dépasser": case "No_Over_Taking":
                return prefix + "Interdiction de dépasser";
            case "Inter-arrêt et stationnement": case "do_not_stop":
                return prefix + "Arrêt et stationnement interdits";
            case "Inter-stationnement": case "no_parking":
                return prefix + "Stationnement interdit";
            case "Inter-de faire demi-tour": case "do_not_u_turn":
                return prefix + "Demi-tour interdit";
            case "Inter-de tourner à droite": case "do_not_turn_r":
                return prefix + "Interdiction de tourner à droite";
            case "Inter-de tourner à gauche": case "do_not_turn_l":
                return prefix + "Interdiction de tourner à gauche";

            // OBLIGATIONS / INDICATIONS
            case "Indic-parking": case "parking": case "Parking":
                return prefix + "Parking autorisé";
            case "Oblig-continuez tout droit": case "Straight Ahead Only": case "Ahead Only":
                return prefix + "Obligation d'aller tout droit";
            case "Oblig-continuez à droite": case "Turn right ahead": case "Keep-Right":
                return prefix + "Obligation de serrer à droite";

            // FIX: Explicitly handle enter_left_lane
            case "Oblig-continuez à gauche": case "Turn left ahead": case "Keep-Left": case "enter_left_lane":
                return prefix + "Obligation de serrer à gauche";

            default:
                return prefix + name.replace("_", " ");
        }
    }}
