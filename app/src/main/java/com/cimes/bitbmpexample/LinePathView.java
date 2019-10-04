package com.cimes.bitbmpexample;

/**
 * Created by Thomas on 2017/08/21.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
//import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * 繪製Path的View 用於簽名
 *
 * @author wastrel
 */
@SuppressLint("ClickableViewAccessibility")
public class LinePathView extends View {

    private  static final String TAG=LinePathView.class.getSimpleName();
    private Context mContext;

    /**
     * 筆劃X座標起點
     */
    private float mX;
    /**
     * 筆劃Y座標起點
     */
    private float mY;
    /**
     * 手寫畫筆
     */
    private final Paint mGesturePaint = new Paint();
    /**
     * 路徑
     */
    private final Path mPath = new Path();
    /**
     * 背景畫布
     */
    private Canvas cacheCanvas;
    /**
     * 背景Bitmap緩存
     */
    private Bitmap cacheBitmap;
    /**
     * 是否已經簽名
     */
    private boolean isTouched = false;

    /**
     * 圓圈大小
     */
    private int radius = 40;

    /**
     * 畫筆寬度 px；
     */
    private int mPaintWidth = 6;

    /**
     * 前景色
     */
    private int mPenColor = Color.RED;

    //private int mBackColor=Color.TRANSPARENT;
    private int mBackColor= Color.WHITE;

    public LinePathView(Context context) {
        super(context);
        init(context);
    }

    public LinePathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LinePathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        this.mContext = context;
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setStyle(Style.STROKE);
        mGesturePaint.setStrokeWidth(mPaintWidth);
        mGesturePaint.setColor(mPenColor);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Resources res = getContext().getResources();
        Bitmap blankImage = BitmapFactory.decodeResource(res, R.drawable.car_1);
        cacheBitmap = Bitmap.createScaledBitmap(blankImage, getWidth(), getHeight(), false);

        cacheCanvas = new Canvas();
        cacheCanvas.drawColor(mBackColor);
        cacheCanvas.setBitmap(cacheBitmap);
        isTouched=false;
//        cachebBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
//        cacheCanvas = new Canvas(cachebBitmap);
//        cacheCanvas.drawColor(mBackColor);
//        isTouched=false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                isTouched = true;
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                // 通過畫布繪製多點形成的圖形
                //cacheCanvas.drawPath(mPath, mGesturePaint);
                // 畫圓
                cacheCanvas.drawCircle(mX, mY, radius, mGesturePaint);
                mPath.reset();
                break;
        }
        // 更新繪製
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            canvas.drawBitmap(cacheBitmap, 0, 0, mGesturePaint);
            // 通過畫布繪製多點形成的圖形
            //canvas.drawPath(mPath, mGesturePaint);

            canvas.drawCircle(mX, mY, radius, mGesturePaint);
        } catch (Exception ex) {

            Log.w(TAG, "Exception:" + ex.toString());
            ex.printStackTrace();
            return ;
        }

    }

    // 手指點下螢幕時調用
    private void touchDown(MotionEvent event) {

        // mPath.rewind();
        // 重置繪製路線，即隱藏之前繪製的軌跡
        mPath.reset();
        float x = event.getX();
        float y = event.getY();

        mX = x;
        mY = y;
        // mPath繪製的繪製起點
        mPath.moveTo(x, y);
    }

    // 手指在螢幕上滑動時調用
    private void touchMove(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);

        // 兩點之間的距離大於等於3時，生成貝塞爾繪製曲線
        if (dx >= 3 || dy >= 3) {
            // 設置貝茲曲線的操作點為起點和終點的一半
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;

            // 二次貝塞爾，實現平滑曲線；previousX, previousY為操作點，cX, cY為終點
            mPath.quadTo(previousX, previousY, cX, cY);

            // 第二次執行時，第一次結束調用的座標值將作為第二次調用的初始座標值
            mX = x;
            mY = y;
        }
    }

    /**
     * 清除畫板
     */
    public void clear() {
        if (cacheCanvas != null) {
            Resources res = getContext().getResources();
            Bitmap blankImage = BitmapFactory.decodeResource(res, R.drawable.car_1);
            cacheBitmap = Bitmap.createScaledBitmap(blankImage, getWidth(), getHeight(), false);
            mPath.reset();

            isTouched = false;
            mGesturePaint.setColor(mPenColor);
            cacheCanvas.drawColor(mBackColor, PorterDuff.Mode.CLEAR);
            //cacheCanvas.drawColor(mBackColor);
            cacheCanvas.setBitmap(cacheBitmap);
            mGesturePaint.setColor(mPenColor);
            invalidate();
        }
    }

    /**
     * 保存畫板
     *
     * @param path 保存到路徑
     */

    public void save(String path) throws IOException {
        save(path, false, 0);
    }

    /**
     * 保存畫板
     *
     * @param path       保存到路
     * @param clearBlank 是否清楚空白區域
     * @param blank  邊緣空白區域
     */
    public void save(String path, boolean clearBlank, int blank) throws IOException {

        Bitmap bitmap=cacheBitmap;
        //BitmapUtil.createScaledBitmapByHeight(srcBitmap, 300);//  壓縮圖片
        if (clearBlank) {
            bitmap = clearBlank(bitmap, blank);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

        byte[] buffer = bos.toByteArray();
        if (buffer != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(buffer);
            outputStream.close();
        }
    }

    /**
     * 獲取畫板的bitmap
     * @return
     */
    public Bitmap getBitMap()
    {
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        Bitmap bitmap=getDrawingCache();
        setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 逐行掃描 清楚邊界空白。
     *
     * @param bp
     * @param blank 邊距留多少個圖元
     * @return
     */
    private Bitmap clearBlank(Bitmap bp, int blank) {
        int HEIGHT = bp.getHeight();
        int WIDTH = bp.getWidth();
        int top = 0, left = 0, right = 0, bottom = 0;
        int[] pixs = new int[WIDTH];
        boolean isStop;
        for (int y = 0; y < HEIGHT; y++) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    top = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        for (int y = HEIGHT - 1; y >= 0; y--) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    bottom = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        pixs = new int[HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    left = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        for (int x = WIDTH - 1; x > 0; x--) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    right = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        if (blank < 0) {
            blank = 0;
        }
        left = left - blank > 0 ? left - blank : 0;
        top = top - blank > 0 ? top - blank : 0;
        right = right + blank > WIDTH - 1 ? WIDTH - 1 : right + blank;
        bottom = bottom + blank > HEIGHT - 1 ? HEIGHT - 1 : bottom + blank;
        return Bitmap.createBitmap(bp, left, top, right - left, bottom - top);
    }

    /**
     * 設置畫筆寬度 默認寬度為10px
     *
     * @param mPaintWidth
     */
    public void setPaintWidth(int mPaintWidth) {
        mPaintWidth = mPaintWidth > 0 ? mPaintWidth : 6;
        this.mPaintWidth = mPaintWidth;
        mGesturePaint.setStrokeWidth(mPaintWidth);

    }

    // 簽名區域的背景色
    public void setBackColor(@ColorInt int backColor)
    {
        mBackColor=backColor;
        //mBackColor=Color.RED;
    }


    /**
     * 設置畫筆顏色
     *
     * @param mPenColor
     */
    public void setPenColor(int mPenColor) {
        this.mPenColor = mPenColor;
        mGesturePaint.setColor(mPenColor);
    }

    /**
     * 是否有簽名
     *
     * @return
     */
    public boolean getTouched() {
        return isTouched;
    }

    /**
     * 把一個View的物件轉換成bitmap
     */
    static Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        //能畫緩存就返回false
        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e("BtPrinter", "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);
        return bitmap;
    }


    /**
     * 將Bitmap存為 .bmp格式圖片
     * param bitmap
     */
    public void saveBmp(String path, boolean clearBlank, int blank) throws IOException {

        try {
            Bitmap bitmap = cacheBitmap;
            //BitmapUtil.createScaledBitmapByHeight(srcBitmap, 300);//  壓縮圖片
            // 裁剪空白
            if (clearBlank) {
                bitmap = clearBlank(bitmap, blank);
            }


            // 調整圖片大小
            bitmap=resizeBmp(bitmap);
            // 轉成單色
            //bitmap=convertToBlackWhite(bitmap);
            bitmap=gray2Binary(bitmap);


            //ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

            //byte[] buffer = bos.toByteArray();
            //if (buffer != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
//            OutputStream outputStream = new FileOutputStream(file);
//            outputStream.write(buffer);
//            outputStream.close();
            // 點陣圖大小
            int nBmpWidth = bitmap.getWidth();
            int nBmpHeight = bitmap.getHeight();
            // 圖像資料大小
            int bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4);
            FileOutputStream fileos = new FileOutputStream(path);
            // bmp文件頭
            int bfType = 0x4d42;
            long bfSize = 14 + 40 + bufferSize;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            long bfOffBits = 14 + 40;
            // 保存bmp檔頭
            writeWord(fileos, bfType);
            writeDword(fileos, bfSize);
            writeWord(fileos, bfReserved1);
            writeWord(fileos, bfReserved2);
            writeDword(fileos, bfOffBits);
            // bmp信息頭
            long biSize = 40L;
            long biWidth = nBmpWidth;
            long biHeight = nBmpHeight;
            int biPlanes = 1;
            int biBitCount = 24;
            long biCompression = 0L;
            long biSizeImage = 0L;
            long biXpelsPerMeter = 0L;
            long biYPelsPerMeter = 0L;
            long biClrUsed = 0L;
            long biClrImportant = 0L;
            // 保存bmp資訊頭
            writeDword(fileos, biSize);
            writeLong(fileos, biWidth);
            writeLong(fileos, biHeight);
            writeWord(fileos, biPlanes);
            writeWord(fileos, biBitCount);
            writeDword(fileos, biCompression);
            writeDword(fileos, biSizeImage);
            writeLong(fileos, biXpelsPerMeter);
            writeLong(fileos, biYPelsPerMeter);
            writeDword(fileos, biClrUsed);
            writeDword(fileos, biClrImportant);
            // 圖元掃描
            byte bmpData[] = new byte[bufferSize];
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += 3) {
                    int clr = bitmap.getPixel(wRow, nCol);
                    bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
                }

            fileos.write(bmpData);
            fileos.flush();
            fileos.close();


        }catch(Exception e){

            Log.d(TAG,e.toString());
            e.printStackTrace();
        }
    }

    // TODO 沒用到就刪除

    public void saveBmp(Bitmap bitmap) {
        if (bitmap == null)
            return;
        // 點陣圖大小
        int nBmpWidth = bitmap.getWidth();
        int nBmpHeight = bitmap.getHeight();
        // 圖像資料大小
        int bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4);
        try {
            // 存儲檔案名
            String filename = "/sdcard/Download/test.bmp";
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileos = new FileOutputStream(filename);
            // bmp文件頭
            int bfType = 0x4d42;
            long bfSize = 14 + 40 + bufferSize;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            long bfOffBits = 14 + 40;
            // 保存bmp檔頭
            writeWord(fileos, bfType);
            writeDword(fileos, bfSize);
            writeWord(fileos, bfReserved1);
            writeWord(fileos, bfReserved2);
            writeDword(fileos, bfOffBits);
            // bmp信息頭
            long biSize = 40L;
            long biWidth = nBmpWidth;
            long biHeight = nBmpHeight;
            int biPlanes = 1;
            int biBitCount = 24;
            long biCompression = 0L;
            long biSizeImage = 0L;
            long biXpelsPerMeter = 0L;
            long biYPelsPerMeter = 0L;
            long biClrUsed = 0L;
            long biClrImportant = 0L;
            // 保存bmp資訊頭
            writeDword(fileos, biSize);
            writeLong(fileos, biWidth);
            writeLong(fileos, biHeight);
            writeWord(fileos, biPlanes);
            writeWord(fileos, biBitCount);
            writeDword(fileos, biCompression);
            writeDword(fileos, biSizeImage);
            writeLong(fileos, biXpelsPerMeter);
            writeLong(fileos, biYPelsPerMeter);
            writeDword(fileos, biClrUsed);
            writeDword(fileos, biClrImportant);
            // 圖元掃描
            byte bmpData[] = new byte[bufferSize];
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += 3) {
                    int clr = bitmap.getPixel(wRow, nCol);
                    bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
                }

            fileos.write(bmpData);
            fileos.flush();
            fileos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void writeWord(FileOutputStream stream, int value) throws IOException {
        byte[] b = new byte[2];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        stream.write(b);
    }

    public void writeDword(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    public void writeLong(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    // TODO 沒用到就刪除
    //對圖像進行二值化處理
    public Bitmap gray2Binary(Bitmap graymap) {
        //得到圖形的寬度和長度
        int width = graymap.getWidth();
        int height = graymap.getHeight();
        //創建二值化圖像
        Bitmap binarymap = null;
        binarymap = graymap.copy(Config.ARGB_8888, true);
        //依次迴圈，對圖像的圖元進行處理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到當前圖元的值
                int col = binarymap.getPixel(i, j);
                //得到alpha通道的值
                int alpha = col & 0xFF000000;
                //得到圖像的圖元RGB的值
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B計算出X代替原來的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                //對圖像進行二值化處理
                if (gray <= 95) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                // 新的ARGB
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                //設置新圖像的當前圖元值
                binarymap.setPixel(i, j, newColor);
            }
        }
        return binarymap;
    }


// TODO 沒用到就刪除
    /**
     * 將彩色圖轉換為黑白圖
     *
     * @ param 點陣圖
     * @return 返回轉換好的點陣圖
     */
    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth(); // 獲取點陣圖的寬
        int height = bmp.getHeight(); // 獲取點陣圖的高
        int[] pixels = new int[width * height]; // 通過點陣圖的大小創建圖元點陣列

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
//        Bitmap newBmp = Bitmap.createBitmap(width, height, Config.RGB_565);
//        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
//        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height);
//        return resizeBmp;

        Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
        //result.setPixels(pixels, 0, width, 0, 0, width, height);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    public static Bitmap resizeBmp(Bitmap bmp) {
        Bitmap resizedBitmap=null;
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            float scaleWidth = ((float) 200) / width;
            float scaleHeight = ((float) 100) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);
        try {
            // "RECREATE" THE NEW BITMAP
            resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, false);
            bmp.recycle();
        }catch (Exception e){

        }
        return resizedBitmap;

    }
}