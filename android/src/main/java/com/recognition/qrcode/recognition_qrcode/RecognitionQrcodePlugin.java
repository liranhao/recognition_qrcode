package com.recognition.qrcode.recognition_qrcode;

import androidx.annotation.NonNull;

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
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

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

/**
 * RecognitionQrcodePlugin
 */
public class RecognitionQrcodePlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private static final int TIME_OUT = 30000;
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
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "recognition_qrcode");
        channel.setMethodCallHandler(new RecognitionQrcodePlugin());
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
                        Bitmap map = getBitmap(arguments);
                        final String res = decodeImage(map);
                        Handler mainThread = new Handler(Looper.getMainLooper());
                        mainThread.post(new Runnable() {
                            @Override
                            public void run() {
                                if (res != null) {
                                    Map hashMap = new HashMap();
                                    hashMap.put("value", res);
                                    hashMap.put("code", "0");
                                    result.success(hashMap);
                                } else {
                                    result.error("-1", "Image parsing failed", null);
                                }
                            }
                        });
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
                final Bitmap currentBitMap = bitmap;
                new Thread(new Runnable() {
                    Handler mainThread = new Handler(Looper.getMainLooper());

                    public void run() {
                        final String res = decodeImage(currentBitMap);
                        mainThread.post(new Runnable() {
                            @Override
                            public void run() {
                                if (res != null) {
                                    Map hashMap = new HashMap();
                                    hashMap.put("value", res);
                                    hashMap.put("code", "0");
                                    result.success(hashMap);
                                } else {
                                    result.error("-1", "Image parsing failed", null);
                                }
                                return;
                            }
                        });
                    }
                });
            }
            result.error("-2", "Image not found", null);
        } else {
            result.notImplemented();
        }
    }

    public String decodeImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
        try {
            com.google.zxing.Result res = new MultiFormatReader().decode(binaryBitmap, HINTS);
            Log.d("ac", res.getText());
            return res.getText();
        } catch (NotFoundException e) {
            try {
                com.google.zxing.Result res = new MultiFormatReader().decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)), HINTS);
                return res.getText();
            } catch (NotFoundException e1) {
                e1.printStackTrace();
            }
            return null;
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
