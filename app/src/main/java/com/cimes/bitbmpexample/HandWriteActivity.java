package com.cimes.bitbmpexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
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
//    private ts_func Ts_func=new ts_func(this);
//    private FuckingService FS=new FuckingService(this);
    private File sdCard = Environment.getExternalStorageDirectory();
//    private sqliteDB DH;
    private SQLiteDatabase mDB = null;
    private LinePathView paintView;
    private LinearLayout ll;
    private Button btn_save,btn_clear;
    public  boolean debug= false;  // 偵錯用
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_handwrite);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_handwrite);
        //ButterKnife.bind(this);
        setResult(50);

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

                        //linepathview.saveBmp("/sdcard/Download/sign.bmp", true, 10);
                        // BMP 轉成 PNG
                        bmpToPNG();

                        // 取得SharedPreference設定("Preference"為設定檔的名稱)
                        //SharedPreferences settings = getSharedPreferences("pf_Select_Value", 0);
                        // 置入name屬性的字串
                        //settings.edit().putString("mode", "showSign").commit();

                        setResult(100);
                        //Toast.makeText(HandWriteActivity.this, "已儲存", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(HandWriteActivity.this, "已儲存\r\n"+savePath, Toast.LENGTH_SHORT).show();
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
        //String strPath = Environment.getExternalStorageDirectory()+ File.separator + "ids_temp";

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
