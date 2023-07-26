package com.recognition.qrcode.recognition_qrcode;

import androidx.annotation.NonNull;
import android.app.Activity;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
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
    private ActivityPluginBinding binding;
    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);
    static {
        List<BarcodeFormat> allFormats = new ArrayList<>();
        allFormats.add(BarcodeFormat.AZTEC);
        allFormats.add(BarcodeFormat.CODABAR);
        allFormats.add(BarcodeFormat.CODE_39);
        allFormats.add(BarcodeFormat.CODE_93);
        allFormats.add(BarcodeFormat.CODE_128);
        allFormats.add(BarcodeFormat.DATA_MATRIX);
        allFormats.add(BarcodeFormat.EAN_8);
        allFormats.add(BarcodeFormat.EAN_13);
        allFormats.add(BarcodeFormat.ITF);
        allFormats.add(BarcodeFormat.MAXICODE);
        allFormats.add(BarcodeFormat.PDF_417);
        allFormats.add(BarcodeFormat.QR_CODE);
        allFormats.add(BarcodeFormat.RSS_14);
        allFormats.add(BarcodeFormat.RSS_EXPANDED);
        allFormats.add(BarcodeFormat.UPC_A);
        allFormats.add(BarcodeFormat.UPC_E);
        allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION);
        HINTS.put(DecodeHintType.TRY_HARDER, BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, allFormats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
//        HINTS.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//        //复杂模式
//        HINTS.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "recognition_qrcode");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.currentActivity = binding.getActivity();
        this.binding = binding;

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

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        if(registrar.activity() == null){
            return;
        }
        RecognitionQrcodePlugin plugin = new RecognitionQrcodePlugin();
        plugin.currentActivity = registrar.activity();

        final MethodChannel channel = new MethodChannel(registrar.messenger(), "recognition_qrcode");
        channel.setMethodCallHandler(plugin);
//        registrar.addActivityResultListener(new QrCodeActivityResultListener(currentResult));
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
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
        binding.addActivityResultListener(new QrCodeActivityResultListener(result));
        new Thread(new Runnable() {
            public void run() {
                final com.google.zxing.Result[] res = decodeImage(bitmap);
                if(res == null){
                    result.error("-1", "Image parsing failed", null);
                }
                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        if (res.length == 1) {
                            final com.google.zxing.Result result1 = res[0];
                            Map hashMap = new HashMap();
                            hashMap.put("value", result1.getText());
                            hashMap.put("code", "0");
                            result.success(hashMap);
                        } else if (res.length > 1) {
                            QRImageActivity.image = bitmap;
                            QRImageActivity.results = res;
                            Intent intent = new Intent(currentActivity, QRImageActivity.class);
                            currentActivity.startActivityForResult(intent, 0);
                        } else {
                            result.error("-1", "Image parsing failed", null);
                        }
                    }
                });
            }
        }).start();
    }


    public com.google.zxing.Result[] decodeImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        try {
            com.google.zxing.Result[] res = new QRCodeMultiReader().decodeMultiple(new BinaryBitmap(new GlobalHistogramBinarizer(source)), HINTS);
            return res;
        } catch (NotFoundException e) {
            try {
                Binarizer binarizer = new HybridBinarizer(source);
                BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
                com.google.zxing.Result res = new MultiFormatReader().decode(binaryBitmap, HINTS);
                List<com.google.zxing.Result> results = new ArrayList<>();
                results.add(res);
                return results.toArray(new com.google.zxing.Result[results.size()]);
            } catch (NotFoundException e1) {
                e1.printStackTrace();
            }
            return new com.google.zxing.Result[0];
        }
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
    private Result currentResult;
    QrCodeActivityResultListener(final Result result ){
        this.currentResult = result;
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