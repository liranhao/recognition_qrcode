package com.recognition.qrcode.recognition_qrcode;

import androidx.annotation.NonNull;
import android.app.Activity;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
/**
 * RecognitionQrcodePlugin
 */
public class RecognitionQrcodePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private static final int TIME_OUT = 30000;
    private Activity currentActivity;
    private QrCodeActivityResultListener listener;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "recognition_qrcode");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.currentActivity = binding.getActivity();
        listener = new QrCodeActivityResultListener();
        binding.addActivityResultListener(listener);

    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        this.currentActivity = binding.getActivity();

    }

    @Override
    public void onDetachedFromActivity() {

    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("setConfig")) {
            RecognitionConfig config = RecognitionConfig.getInstance();
            config.setConfig((Map) call.arguments);
        } else if (call.method.equals("recognitionQrcode")) {
            final String arguments = call.arguments.toString();
            Bitmap bitmap = null;
            if (arguments.contains("http://") || arguments.contains("https://")) {
                new Thread(new Runnable() {
                    public void run() {
                        Bitmap bitmap = getBitmap(arguments);
                        handleResults(bitmap, result);
                    }
                }).start();
                return;
            } else {
                bitmap = BitmapFactory.decodeFile(arguments);
                if (bitmap == null) {
                    try {
                        byte[] bitmapArray;
                        bitmapArray = Base64.decode(arguments, Base64.DEFAULT);
                        bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.error("-2", "Image not found", null);
                    }
                }
            }
            if (bitmap != null) {
                handleResults(bitmap, result);
                return;
            }
            result.error("-2", "Image not found", null);
        } else {
            result.notImplemented();
        }
    }
    public void handleResults(final Bitmap bitmap, @NonNull final Result result){
        listener.currentResult = result;
        final BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_ALL_FORMATS)
                        .build();
        BarcodeScanner scanner = BarcodeScanning.getClient();
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        // Task completed successfully
                        // ...
                        if (barcodes.size() == 1) {
                            final Barcode result1 = barcodes.get(0);
                            Map hashMap = new HashMap();
                            hashMap.put("value", result1.getRawValue());
                            hashMap.put("code", "0");
                            result.success(hashMap);
                        } else if (barcodes.size() > 1) {
                            QRImageActivity.image = bitmap;
                            QRImageActivity.results = barcodes;
                            Intent intent = new Intent(currentActivity, QRImageActivity.class);
                            currentActivity.startActivityForResult(intent, 0);
                        } else {
                            result.error("-1", "Image parsing failed", null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...

                    }
                });
    }




    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    public Bitmap getBitmap(String urlStr) {
        // From Network
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                return bitmap;
            }
            return null;
        } catch (Throwable ex) {
            return null;
        }
    }
}

class  QrCodeActivityResultListener implements PluginRegistry.ActivityResultListener {
    public Result currentResult;
    QrCodeActivityResultListener( ){
    }
    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode,resultCode, data);
        if(currentResult != null && data != null && resultCode == 1){
            Map hashMap = new HashMap();
            hashMap.put("value", data.getStringExtra("value"));
            hashMap.put("code", "0");
            currentResult.success(hashMap);
        }
        return false;
    }
}