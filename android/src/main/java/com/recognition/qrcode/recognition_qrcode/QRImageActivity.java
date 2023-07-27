package com.recognition.qrcode.recognition_qrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.Shape;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.fragment.app.FragmentActivity;

import com.google.mlkit.vision.barcode.Barcode;
import java.util.List;

public class QRImageActivity extends Activity {
    public static Bitmap image;
    public static List<Barcode> results;
    private ImageView imageView;
    private RelativeLayout rootView;
    private RecognitionConfig config;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_image);
        rootView = findViewById(R.id.root_view);
        imageView = findViewById(R.id.img_view);
        imageView.setImageBitmap(QRImageActivity.image);
        Button btn = findViewById(R.id.btn);
        config = RecognitionConfig.getInstance();
        if(config.cancelTitle != null){
            btn.setText(config.cancelTitle);
        }
        if(config.cancelTitleFontSize > 0){
            btn.setTextSize((float) config.cancelTitleFontSize);
        }
        btn.setOnClickListener(new CancelClickListener(this));
        rootView.post(new Runnable() {
            @Override
            public void run() {
                initView();
            }
        });

    }
    private void initView(){

        List<Barcode> results = QRImageActivity.results;
        QrRect imgRect = calculateClientRectOfImageInUIImageView();
        Bitmap iconBitMap = null;
        try {
            iconBitMap = BitmapFactory.decodeByteArray(config.icon,0 ,config.icon.length);
        } catch (Throwable ex){

        }
        for (int i = 0; i < results.size(); i++) {
            Barcode result = results.get(i);
            Rect rect = result.getBoundingBox();
            QrRect qrRect = new QrRect(rect.left, rect.top,rect.width(), rect.height()) ;
            QrRect currentQrRect = calculateBarcodeRect(imgRect, qrRect);
            ImageView icon = new ImageView(this);
            icon.setOnClickListener(new BarcodeClickListener(this, result.getRawValue()));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)Math.round(config.iconWidth * 2), (int)Math.round(config.iconHeight * 2));
            params.leftMargin = (int) Math.round(currentQrRect.x + currentQrRect.width / 2 - config.iconWidth);
            params.topMargin = (int) Math.round( currentQrRect.y + currentQrRect.height / 2 - config.iconHeight);
            if(iconBitMap != null){
                icon.setImageBitmap(iconBitMap);
            } else {
                icon.setBackgroundResource(R.drawable.img_view_normal);
                icon.setImageResource(R.drawable.bx_right_arrow);
            }

            icon.setLayoutParams(params);
            rootView.addView(icon);
        }
//        rootView.addView();
//        ImageView imageView1 = new ImageView();
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams();

    }

    private QrRect calculateClientRectOfImageInUIImageView(){
        float scale = getScale();
        float imgW = (float)QRImageActivity.image.getWidth() * scale;
        float imgH = (float)QRImageActivity.image.getHeight() * scale;

        float x = (float)(imageView.getWidth() - imgW) / 2;
        float y = (float)(imageView.getHeight() - imgH) / 2;
        QrRect rect = new QrRect( x, y, imgW, imgH);
        return rect;
    }
    private float getScale(){
        float scaleW  = (float)imageView.getWidth() / (float)QRImageActivity.image.getWidth();
        float scaleH = (float)imageView.getHeight() / (float)QRImageActivity.image.getHeight();
        Log.i("image.width", String.valueOf(QRImageActivity.image.getWidth()));
        Log.i("imageView.width", String.valueOf(imageView.getWidth()));
        float scale = Math.min(scaleH, scaleW);
        return scale;
    }

    private QrRect calculateBarcodeRect(QrRect imgRect, QrRect barRect){
        float aspect = getScale();
        float x=  barRect.x * aspect + imgRect.x;
        float y= barRect.y * aspect + imgRect.y;

        float width = barRect.width * aspect;
        float height = barRect.height * aspect;

        return  new QrRect(x,y,width,height);
    }
}
class CancelClickListener implements View.OnClickListener{
    Activity activity;
    CancelClickListener(final Activity activity){
        this.activity = activity;
    }
    @Override
    public void onClick(View view) {
        activity.finish();
    }
}
class BarcodeClickListener implements View.OnClickListener{
    Activity activity;
    String value;
    BarcodeClickListener(final Activity activity,
    final String value){
        this.activity = activity;
        this.value = value;
    }
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(value);
        intent.putExtra("value", value);
        activity.setResult(1, intent);
        activity.finish();
    }
}
class QrRect{
    float x;
    float y;
     float width;
     float height;
    QrRect(float x,
        float y,
        float width,
        float height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}