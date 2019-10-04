package com.cimes.bitbmpexample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HandWriteActivity extends AppCompatActivity {
    private Context context;
    private final String TAG = "HandWriteActivity";
    //取得震動服務
    private Vibrator myVibrator;
    private File sdCard = Environment.getExternalStorageDirectory();

    private SQLiteDatabase mDB = null;
    private LinePathView paintView;
    private LinearLayout ll;
    private Button btn_save,btn_clear;
    public  boolean debug= false;  // 偵錯用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_handwrite);
        //ButterKnife.bind(this);
        setResult(50);


        //震動服務
        myVibrator = (Vibrator) this.context.getSystemService(Service.VIBRATOR_SERVICE);

        paintView= (LinePathView)findViewById(R.id.paintView);
        ll= (LinearLayout)findViewById(R.id.ll);
        btn_save= (Button)findViewById(R.id.btn_save);
        btn_clear= (Button)findViewById(R.id.btn_clear);

        Drawable drawabled  =  this.getResources().getDrawable(R.drawable.car_1);
        paintView.setBackground(drawabled);
        //設置保存監聽
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paintView.getTouched()) {
                    try {
                        String savePath=sdCard.getAbsolutePath() + File.separator+"Download/sign.bmp";
                        paintView.save(savePath, false, 10);

                        // BMP 轉成 PNG
                        bmpToPNG();

                        myVibrator.vibrate(new long[]{10, 100, 30, 100, 10}, -1);
                        setResult(100);

                        finish();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {

                    Toast.makeText(HandWriteActivity.this, "您沒有簽名~", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.clear();
            }
        });

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
    public void RefreshEXStorage(String file){

        MediaScannerConnection.scanFile(context, new String[] {file}, null,null);

    }
    public void bmpToPNG() {
        try {
            //File sdCard = Environment.getExternalStorageDirectory();
            String bmpfile=sdCard.getAbsolutePath() + File.separator+"Download/sign.bmp";
            String pngfile=sdCard.getAbsolutePath() + File.separator+"Download/sign.png";
            Bitmap bmp = BitmapFactory.decodeFile(bmpfile);
            File file = new File(pngfile);
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream out = new FileOutputStream(pngfile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            RefreshEXStorage(pngfile);
            RefreshEXStorage(bmpfile);
            Toast.makeText(HandWriteActivity.this, "已儲存\r\n"+pngfile, Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
