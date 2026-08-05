package com.homeops.gymtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int NEARBY_PERMISSION_REQUEST = 2001;
    private static final String SERVICE_ID = "com.homeops.gymtracker.NEARBY_WORKBOOK";
    private static final Strategy NEARBY_STRATEGY = Strategy.P2P_POINT_TO_POINT;
    private static final String UPDATE_MANIFEST_URL = "https://raw.githubusercontent.com/kth1022/gym-tracker-android/main/latest.json";

    private WebView webView;
    private SharedPreferences appStorage;
    private ValueCallback<Uri[]> filePathCallback;
    private ConnectionsClient connectionsClient;
    private Runnable pendingNearbyAction;
    private String pendingNearbyJson;
    private String connectedEndpointId;
    private boolean pageReady;
    private String pendingImportBase64;
    private String pendingImportFilename;
    private File pendingUpdateApk;

    @Override
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectionsClient = Nearby.getConnectionsClient(this);
        appStorage = getSharedPreferences("gym_tracker_storage", MODE_PRIVATE);

        webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageReady = true;
                flushPendingImport();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = params.createIntent();
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this, "No file picker available", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.addJavascriptInterface(new StorageBridge(), "AndroidStorage");
        webView.loadUrl("file:///android_asset/gym_tracker_app.html");
        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST || filePathCallback == null) return;
        Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
        filePathCallback.onReceiveValue(result);
        filePathCallback = null;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        stopNearby();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingUpdateApk != null && pendingUpdateApk.exists() && canInstallPackages()) {
            File apk = pendingUpdateApk;
            pendingUpdateApk = null;
            installDownloadedApk(apk);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != NEARBY_PERMISSION_REQUEST) return;
        if (allNearbyPermissionsGranted() && pendingNearbyAction != null) {
            Runnable action = pendingNearbyAction;
            pendingNearbyAction = null;
            action.run();
        } else {
            pendingNearbyAction = null;
            notifyStatus("Nearby permission denied.");
        }
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;
        Uri uri = null;
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        } else if (Intent.ACTION_VIEW.equals(action)) {
            uri = intent.getData();
        }
        if (uri == null) return;
        try {
            byte[] bytes = readAllBytes(uri);
            pendingImportBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            pendingImportFilename = filenameFromUri(uri);
            flushPendingImport();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open shared workbook: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private byte[] readAllBytes(Uri uri) throws Exception {
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) throw new IllegalStateException("No input stream");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            return out.toByteArray();
        }
    }

    private String filenameFromUri(Uri uri) {
        String last = uri.getLastPathSegment();
        if (last == null || last.trim().isEmpty()) return "Shared_Workbook.xlsx";
        int slash = Math.max(last.lastIndexOf('/'), last.lastIndexOf('\\'));
        return safeFilename(slash >= 0 ? last.substring(slash + 1) : last);
    }

    private void flushPendingImport() {
        if (!pageReady || pendingImportBase64 == null) return;
        importWorkbookBase64(pendingImportBase64, pendingImportFilename, "auto");
        pendingImportBase64 = null;
        pendingImportFilename = null;
    }

    private void importWorkbookBase64(String base64, String filename, String mode) {
        String script = "window.importWorkbookBase64 && window.importWorkbookBase64("
                + JSONObject.quote(base64) + ","
                + JSONObject.quote(filename == null ? "Shared_Workbook.xlsx" : filename) + ","
                + JSONObject.quote(mode == null ? "auto" : mode) + ");";
        runOnUiThread(() -> webView.evaluateJavascript(script, null));
    }

    private void importFriendPayload(String jsonPayload) {
        String script = "window.importFriendPayload && window.importFriendPayload("
                + JSONObject.quote(jsonPayload == null ? "{}" : jsonPayload) + ");";
        runOnUiThread(() -> webView.evaluateJavascript(script, null));
    }

    private void startFriendCodeScan() {
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build();
        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options);
        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String rawValue = barcode.getRawValue();
                    if (rawValue == null || rawValue.trim().isEmpty()) {
                        notifyStatus("QR code was empty.");
                        return;
                    }
                    importFriendPayload(rawValue);
                })
                .addOnCanceledListener(() -> notifyStatus("QR scan canceled."))
                .addOnFailureListener(e -> notifyStatus("QR scan failed: " + e.getMessage()));
    }

    private void notifyStatus(String message) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            if (webView != null) {
                webView.evaluateJavascript("window.onNearbyStatus && window.onNearbyStatus(" + JSONObject.quote(message) + ");", null);
            }
        });
    }

    private void notifyUpdateStatus(String status, String message) {
        notifyUpdateStatus(status, message, null, null, 0, true);
    }

    private void notifyUpdateStatus(String status, String message, String latestVersionName, String releaseNotes, int latestVersionCode) {
        notifyUpdateStatus(status, message, latestVersionName, releaseNotes, latestVersionCode, true);
    }

    private void notifyUpdateStatus(String status, String message, String latestVersionName, String releaseNotes, int latestVersionCode, boolean showToast) {
        try {
            JSONObject json = new JSONObject();
            json.put("status", status);
            json.put("message", message);
            json.put("currentVersionCode", BuildConfig.VERSION_CODE);
            json.put("currentVersionName", BuildConfig.VERSION_NAME);
            if (latestVersionCode > 0) json.put("latestVersionCode", latestVersionCode);
            if (latestVersionName != null) json.put("latestVersionName", latestVersionName);
            if (releaseNotes != null) json.put("releaseNotes", releaseNotes);
            String script = "window.onUpdateStatus && window.onUpdateStatus(" + json + ");";
            runOnUiThread(() -> {
                if (webView != null) webView.evaluateJavascript(script, null);
                if (showToast) Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            });
        } catch (Exception ignored) {
        }
    }

    private void notifyBridgePostResult(String requestId, boolean ok, int statusCode, String body, String error) {
        try {
            JSONObject json = new JSONObject();
            json.put("requestId", requestId == null ? "" : requestId);
            json.put("ok", ok);
            json.put("statusCode", statusCode);
            json.put("body", body == null ? "" : body);
            json.put("error", error == null ? "" : error);
            String script = "window.onBridgePostResult && window.onBridgePostResult(" + json + ");";
            runOnUiThread(() -> {
                if (webView != null) webView.evaluateJavascript(script, null);
            });
        } catch (Exception ignored) {
        }
    }

    private boolean canInstallPackages() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || getPackageManager().canRequestPackageInstalls();
    }

    private void requestInstallPermission(File apk) {
        pendingUpdateApk = apk;
        notifyUpdateStatus("permission", "Allow Gym Tracker to install app updates, then return here.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void checkForUpdateFromManifest() {
        checkForUpdateFromManifest(false, false);
    }

    private void checkForUpdateFromManifest(boolean downloadWhenAvailable, boolean quietCurrent) {
        new Thread(() -> {
            try {
                if (!quietCurrent) notifyUpdateStatus("checking", "Checking for updates...");
                JSONObject manifest = readJsonUrl(UPDATE_MANIFEST_URL);
                int latestVersionCode = manifest.optInt("versionCode", 0);
                String latestVersionName = manifest.optString("versionName", "");
                String apkUrl = manifest.optString("apkUrl", "");
                String expectedSha256 = manifest.optString("sha256", "");
                String releaseNotes = manifest.optString("releaseNotes", "");
                long expectedSize = manifest.optLong("size", 0);

                if (latestVersionCode <= BuildConfig.VERSION_CODE) {
                    if (!quietCurrent) {
                        notifyUpdateStatus("current", "Gym Tracker is up to date.", latestVersionName, releaseNotes, latestVersionCode);
                    }
                    return;
                }
                if (apkUrl.trim().isEmpty() || expectedSha256.trim().isEmpty()) {
                    if (!quietCurrent) {
                        notifyUpdateStatus("error", "Update manifest is missing APK details.", latestVersionName, releaseNotes, latestVersionCode);
                    }
                    return;
                }

                if (!downloadWhenAvailable) {
                    notifyUpdateStatus(
                            "available",
                            "Update " + latestVersionName + " is available.",
                            latestVersionName,
                            releaseNotes,
                            latestVersionCode,
                            !quietCurrent
                    );
                    return;
                }

                notifyUpdateStatus(
                        "downloading",
                        "Update " + latestVersionName + " found. Downloading...",
                        latestVersionName,
                        releaseNotes,
                        latestVersionCode
                );
                File apk = downloadUpdateApk(apkUrl, expectedSha256, expectedSize);
                notifyUpdateStatus("ready", "Update verified. Opening Android installer.", latestVersionName, releaseNotes, latestVersionCode);
                runOnUiThread(() -> installDownloadedApk(apk));
            } catch (Exception e) {
                if (!quietCurrent) notifyUpdateStatus("error", "Update failed: " + e.getMessage());
            }
        }, "GymTrackerUpdate").start();
    }

    private JSONObject readJsonUrl(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(20000);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "GymTracker/" + BuildConfig.VERSION_NAME);
        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            return new JSONObject(out.toString(StandardCharsets.UTF_8.name()));
        } finally {
            conn.disconnect();
        }
    }

    private File downloadUpdateApk(String apkUrl, String expectedSha256, long expectedSize) throws Exception {
        File dir = new File(getCacheDir(), "updates");
        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Could not create update folder");
        File apk = new File(dir, "gym_tracker_update.apk");

        HttpURLConnection conn = (HttpURLConnection) new URL(apkUrl).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-Agent", "GymTracker/" + BuildConfig.VERSION_NAME);
        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(apk)) {
            byte[] buffer = new byte[16384];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        } finally {
            conn.disconnect();
        }

        if (expectedSize > 0 && apk.length() != expectedSize) {
            throw new IllegalStateException("Downloaded APK size did not match manifest");
        }
        String actualSha256 = sha256(apk);
        if (!actualSha256.equalsIgnoreCase(expectedSha256)) {
            if (!apk.delete()) apk.deleteOnExit();
            throw new IllegalStateException("Downloaded APK hash did not match manifest");
        }
        return apk;
    }

    private String sha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new BufferedInputStream(new java.io.FileInputStream(file))) {
            byte[] buffer = new byte[16384];
            int read;
            while ((read = in.read(buffer)) != -1) digest.update(buffer, 0, read);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : digest.digest()) sb.append(String.format(Locale.US, "%02X", b));
        return sb.toString();
    }

    private void installDownloadedApk(File apk) {
        if (!apk.exists()) {
            notifyUpdateStatus("error", "Downloaded update file was not found.");
            return;
        }
        if (!canInstallPackages()) {
            requestInstallPermission(apk);
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", apk);
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(uri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(install);
    }

    private void ensureNearbyPermissions(Runnable action) {
        List<String> missing = missingNearbyPermissions();
        if (missing.isEmpty()) {
            action.run();
            return;
        }
        pendingNearbyAction = action;
        ActivityCompat.requestPermissions(this, missing.toArray(new String[0]), NEARBY_PERMISSION_REQUEST);
    }

    private boolean allNearbyPermissionsGranted() {
        return missingNearbyPermissions().isEmpty();
    }

    private List<String> missingNearbyPermissions() {
        List<String> missing = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 31) {
            addMissing(missing, Manifest.permission.BLUETOOTH_ADVERTISE);
            addMissing(missing, Manifest.permission.BLUETOOTH_CONNECT);
            addMissing(missing, Manifest.permission.BLUETOOTH_SCAN);
        }
        if (Build.VERSION.SDK_INT >= 33) {
            addMissing(missing, Manifest.permission.NEARBY_WIFI_DEVICES);
        }
        if (Build.VERSION.SDK_INT >= 29 && Build.VERSION.SDK_INT <= 31) {
            addMissing(missing, Manifest.permission.ACCESS_FINE_LOCATION);
        } else if (Build.VERSION.SDK_INT <= 28) {
            addMissing(missing, Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        return missing;
    }

    private void addMissing(List<String> missing, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            missing.add(permission);
        }
    }

    private void startNearbyReceive() {
        stopNearby();
        connectionsClient.startAdvertising(
                "Gym Tracker",
                SERVICE_ID,
                connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(NEARBY_STRATEGY).build()
        ).addOnSuccessListener(unused -> notifyStatus("Nearby receive is on. Ask the other user to send."))
         .addOnFailureListener(e -> notifyStatus("Nearby receive failed: " + e.getMessage()));
    }

    private void startNearbySend(String filename, String base64Workbook) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "GymTrackerWorkbookV1");
            json.put("filename", safeFilename(filename));
            json.put("base64", base64Workbook);
            pendingNearbyJson = json.toString();
            if (pendingNearbyJson.getBytes(StandardCharsets.UTF_8).length > ConnectionsClient.MAX_BYTES_DATA_SIZE) {
                pendingNearbyJson = null;
                notifyStatus("Nearby file is too large. Use regular share for this workbook.");
                return;
            }
        } catch (Exception e) {
            notifyStatus("Nearby send failed: " + e.getMessage());
            return;
        }
        stopNearby();
        connectionsClient.startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(NEARBY_STRATEGY).build()
        ).addOnSuccessListener(unused -> notifyStatus("Looking for a nearby Gym Tracker receiver."))
         .addOnFailureListener(e -> notifyStatus("Nearby discovery failed: " + e.getMessage()));
    }

    private void startNearbySendText(String jsonPayload) {
        try {
            JSONObject json = new JSONObject(jsonPayload);
            String type = json.optString("type");
            if (!"GymTrackerFriendV1".equals(type)) {
                notifyStatus("Nearby payload type is not supported.");
                return;
            }
            pendingNearbyJson = json.toString();
            if (pendingNearbyJson.getBytes(StandardCharsets.UTF_8).length > ConnectionsClient.MAX_BYTES_DATA_SIZE) {
                pendingNearbyJson = null;
                notifyStatus("Nearby payload is too large.");
                return;
            }
        } catch (Exception e) {
            notifyStatus("Nearby send failed: " + e.getMessage());
            return;
        }
        stopNearby();
        connectionsClient.startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(NEARBY_STRATEGY).build()
        ).addOnSuccessListener(unused -> notifyStatus("Looking for a nearby Gym Tracker receiver."))
         .addOnFailureListener(e -> notifyStatus("Nearby discovery failed: " + e.getMessage()));
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
            connectionsClient.requestConnection("Gym Tracker", endpointId, connectionLifecycleCallback)
                    .addOnFailureListener(e -> notifyStatus("Nearby connection failed: " + e.getMessage()));
        }

        @Override
        public void onEndpointLost(String endpointId) {
        }
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo info) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                    .addOnFailureListener(e -> notifyStatus("Nearby accept failed: " + e.getMessage()));
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            Status status = result.getStatus();
            if (!status.isSuccess()) {
                notifyStatus("Nearby connection rejected.");
                return;
            }
            connectedEndpointId = endpointId;
            connectionsClient.stopAdvertising();
            connectionsClient.stopDiscovery();
            notifyStatus("Nearby connected.");
            if (pendingNearbyJson != null) {
                Payload payload = Payload.fromBytes(pendingNearbyJson.getBytes(StandardCharsets.UTF_8));
                connectionsClient.sendPayload(endpointId, payload)
                        .addOnSuccessListener(unused -> notifyStatus("Nearby data sent."))
                        .addOnFailureListener(e -> notifyStatus("Nearby send failed: " + e.getMessage()));
                pendingNearbyJson = null;
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            if (endpointId.equals(connectedEndpointId)) connectedEndpointId = null;
            notifyStatus("Nearby disconnected.");
        }
    };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            if (payload.getType() != Payload.Type.BYTES || payload.asBytes() == null) return;
            try {
                String raw = new String(payload.asBytes(), StandardCharsets.UTF_8);
                JSONObject json = new JSONObject(raw);
                String type = json.optString("type");
                if ("GymTrackerWorkbookV1".equals(type)) {
                    importWorkbookBase64(json.optString("base64"), json.optString("filename", "Nearby_Workbook.xlsx"), "auto");
                    notifyStatus("Nearby workbook received.");
                } else if ("GymTrackerFriendV1".equals(type)) {
                    importFriendPayload(json.toString());
                    notifyStatus("Nearby friend code received.");
                }
            } catch (Exception e) {
                notifyStatus("Nearby import failed: " + e.getMessage());
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                connectionsClient.disconnectFromEndpoint(endpointId);
            }
        }
    };

    private void stopNearby() {
        if (connectionsClient != null) connectionsClient.stopAllEndpoints();
        connectedEndpointId = null;
    }

    private String safeFilename(String filename) {
        String safe = filename == null ? "gym_tracker.xlsx" : filename.replaceAll("[^A-Za-z0-9._-]+", "_");
        if (!safe.toLowerCase().endsWith(".xlsx")) safe += ".xlsx";
        return safe;
    }

    private String safeTextFilename(String filename) {
        String safe = filename == null ? "gym_tracker_recovery.json" : filename.replaceAll("[^A-Za-z0-9._-]+", "_");
        if (!safe.toLowerCase().endsWith(".json")) safe += ".json";
        return safe;
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void saveWorkbook(String filename, String base64Workbook) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = Base64.decode(base64Workbook, Base64.DEFAULT);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, safeFilename(filename));
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Gym Tracker");
                    ContentResolver resolver = getContentResolver();
                    Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri == null) throw new IllegalStateException("Could not create download");
                    try (OutputStream out = resolver.openOutputStream(uri)) {
                        if (out == null) throw new IllegalStateException("Could not open download");
                        out.write(bytes);
                    }
                    Toast.makeText(MainActivity.this, "Saved to Downloads/Gym Tracker", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface
        public void shareWorkbook(String filename, String base64Workbook) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = Base64.decode(base64Workbook, Base64.DEFAULT);
                    File dir = new File(getCacheDir(), "shared");
                    if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Could not create share folder");
                    File file = new File(dir, safeFilename(filename));
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        out.write(bytes);
                    }
                    Uri uri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", file);
                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    send.putExtra(Intent.EXTRA_STREAM, uri);
                    send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(send, "Share workout file"));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Share failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface
        public void shareText(String title, String text) {
            runOnUiThread(() -> {
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_SUBJECT, title == null ? "Gym Tracker" : title);
                send.putExtra(Intent.EXTRA_TEXT, text == null ? "" : text);
                startActivity(Intent.createChooser(send, title == null ? "Share" : title));
            });
        }

        @JavascriptInterface
        public void saveTextFile(String filename, String text) {
            runOnUiThread(() -> {
                try {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, safeTextFilename(filename));
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Gym Tracker/Recovery");
                    ContentResolver resolver = getContentResolver();
                    Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri == null) throw new IllegalStateException("Could not create download");
                    try (OutputStream out = resolver.openOutputStream(uri)) {
                        if (out == null) throw new IllegalStateException("Could not open download");
                        out.write((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
                    }
                    Toast.makeText(MainActivity.this, "Saved to Downloads/Gym Tracker/Recovery", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface
        public void shareTextFile(String filename, String text) {
            runOnUiThread(() -> {
                try {
                    File dir = new File(getCacheDir(), "shared");
                    if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Could not create share folder");
                    File file = new File(dir, safeTextFilename(filename));
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        out.write((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
                    }
                    Uri uri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", file);
                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType("application/json");
                    send.putExtra(Intent.EXTRA_STREAM, uri);
                    send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(send, "Share recovery file"));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Share failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface
        public void nearbyReceive() {
            runOnUiThread(() -> ensureNearbyPermissions(MainActivity.this::startNearbyReceive));
        }

        @JavascriptInterface
        public void nearbySendWorkbook(String filename, String base64Workbook) {
            runOnUiThread(() -> ensureNearbyPermissions(() -> startNearbySend(filename, base64Workbook)));
        }

        @JavascriptInterface
        public void nearbySendText(String jsonPayload) {
            runOnUiThread(() -> ensureNearbyPermissions(() -> startNearbySendText(jsonPayload)));
        }

        @JavascriptInterface
        public void scanFriendCode() {
            runOnUiThread(MainActivity.this::startFriendCodeScan);
        }

        @JavascriptInterface
        public String currentVersionInfo() {
            try {
                JSONObject json = new JSONObject();
                json.put("versionCode", BuildConfig.VERSION_CODE);
                json.put("versionName", BuildConfig.VERSION_NAME);
                json.put("androidSdk", Build.VERSION.SDK_INT);
                json.put("manufacturer", Build.MANUFACTURER);
                json.put("model", Build.MODEL);
                return json.toString();
            } catch (Exception e) {
                return "{}";
            }
        }

        @JavascriptInterface
        public void checkForUpdate() {
            checkForUpdateFromManifest();
        }

        @JavascriptInterface
        public void checkForUpdateQuiet() {
            checkForUpdateFromManifest(false, true);
        }

        @JavascriptInterface
        public void downloadUpdate() {
            checkForUpdateFromManifest(true, false);
        }

        @JavascriptInterface
        public void postJson(String requestId, String endpointUrl, String jsonPayload) {
            new Thread(() -> {
                HttpURLConnection conn = null;
                try {
                    if (endpointUrl == null || !endpointUrl.toLowerCase(Locale.US).startsWith("https://")) {
                        throw new IllegalArgumentException("Endpoint must use HTTPS.");
                    }
                    byte[] bytes = (jsonPayload == null ? "{}" : jsonPayload).getBytes(StandardCharsets.UTF_8);
                    conn = (HttpURLConnection) new URL(endpointUrl).openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    conn.setRequestProperty("Accept", "application/json, text/plain, */*");
                    conn.setRequestProperty("User-Agent", "GymTracker/" + BuildConfig.VERSION_NAME);
                    conn.setFixedLengthStreamingMode(bytes.length);
                    try (OutputStream out = conn.getOutputStream()) {
                        out.write(bytes);
                    }
                    int status = conn.getResponseCode();
                    InputStream in = status >= 200 && status < 400 ? conn.getInputStream() : conn.getErrorStream();
                    String body = "";
                    if (in != null) {
                        try (InputStream input = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = input.read(buffer)) != -1) out.write(buffer, 0, read);
                            body = out.toString(StandardCharsets.UTF_8.name());
                        }
                    }
                    notifyBridgePostResult(requestId, status >= 200 && status < 300, status, body, null);
                } catch (Exception e) {
                    notifyBridgePostResult(requestId, false, 0, "", e.getMessage());
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }, "GymTrackerPostJson").start();
        }

        @JavascriptInterface
        public String makeQrDataUrl(String text) {
            try {
                int size = 640;
                BitMatrix matrix = new MultiFormatWriter().encode(text == null ? "" : text, BarcodeFormat.QR_CODE, size, size);
                Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                return "data:image/png;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
            } catch (Exception e) {
                return "";
            }
        }

        @JavascriptInterface
        public void nearbyStop() {
            runOnUiThread(() -> {
                stopNearby();
                notifyStatus("Nearby stopped.");
            });
        }

        @JavascriptInterface
        public void openUrl(String url) {
            runOnUiThread(() -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Could not open demo link", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public class StorageBridge {
        @JavascriptInterface
        public String get(String key) {
            if (key == null || !appStorage.contains(key)) return null;
            return appStorage.getString(key, null);
        }

        @JavascriptInterface
        public boolean set(String key, String value) {
            if (key == null) return false;
            appStorage.edit().putString(key, value == null ? "" : value).apply();
            return true;
        }

        @JavascriptInterface
        public String dumpAll() {
            JSONObject json = new JSONObject();
            try {
                for (String key : appStorage.getAll().keySet()) {
                    Object value = appStorage.getAll().get(key);
                    json.put(key, value == null ? JSONObject.NULL : String.valueOf(value));
                }
            } catch (Exception e) {
                return "{}";
            }
            return json.toString();
        }
    }
}
