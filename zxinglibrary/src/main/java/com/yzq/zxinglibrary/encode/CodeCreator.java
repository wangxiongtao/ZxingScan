package com.yzq.zxinglibrary.encode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class CodeCreator {
    public static final Map<EncodeHintType, Object> HINTS = new EnumMap<>(EncodeHintType.class);

    static {
        HINTS.put(EncodeHintType.CHARACTER_SET, "utf-8");
        HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        HINTS.put(EncodeHintType.MARGIN, 0);
    }


    /*生成二维码*/
    public static Bitmap createQRCode(String content, int w, int h, Bitmap logo) {


        if (TextUtils.isEmpty(content)) {
            return null;
        }
        /*偏移量*/
        int offsetX = w / 2;
        int offsetY = h / 2;

        /*生成logo*/
        Bitmap logoBitmap = null;

        if (logo != null) {
            Matrix matrix = new Matrix();
//            float scaleFactor = Math.min(w * 1.0f / 5 / logo.getWidth(), h * 1.0f / 5 / logo.getHeight());
//            matrix.postScale(scaleFactor, scaleFactor);
            logoBitmap = Bitmap.createBitmap(logo, 0, 0, logo.getWidth(), logo.getHeight(), matrix, true);
        }


        /*如果log不为null,重新计算偏移量*/
        int logoW = 0;
        int logoH = 0;
        if (logoBitmap != null) {
            logoW = logoBitmap.getWidth();
            logoH = logoBitmap.getHeight();
            offsetX = (w - logoW) / 2;
            offsetY = (h - logoH) / 2;
        }

        /*指定为UTF-8*/
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //容错级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        //设置空白边距的宽度
        hints.put(EncodeHintType.MARGIN, 0);
        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = null;
        try {
            matrix = new MultiFormatWriter().encode(content,
                    BarcodeFormat.QR_CODE, w, h, hints);

            // 二维矩阵转为一维像素数组,也就是一直横着排了
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (x >= offsetX && x < offsetX + logoW && y >= offsetY && y < offsetY + logoH) {
                        int pixel = logoBitmap.getPixel(x - offsetX, y - offsetY);
                        if (pixel == 0) {
                            if (matrix.get(x, y)) {
                                pixel = 0xff000000;
                            } else {
                                pixel = 0xffffffff;
                            }
                        }
                        pixels[y * w + x] = pixel;
                    } else {
                        if (matrix.get(x, y)) {
                            pixels[y * w + x] = 0xff000000;
                        } else {
                            pixels[y * w + x] = 0xffffffff;
                        }
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(w, h,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;


        } catch (WriterException e) {

            System.out.print(e);
            return null;
        }


    }




































    /**
     * 同步创建黑色前景色、白色背景色的二维码图片。该方法是耗时操作，请在子线程中调用。
     *
     * @param content 要生成的二维码图片内容
     * @param size    图片宽高，单位为px
     */
    public static Bitmap syncEncodeQRCode(String content, int size) {
        return syncEncodeQRCode(content, size, Color.BLACK, Color.WHITE, null);
    }
    /**
     * 同步创建黑色前景色、白色背景色的二维码图片。该方法是耗时操作，请在子线程中调用。
     *
     * @param content 要生成的二维码图片内容
     * @param size    图片宽高，单位为px
     */
    public static Bitmap syncEncodeQRCode(String content, Bitmap bitmap,int size) {
        return syncEncodeQRCode(content, size, Color.BLACK, Color.WHITE, bitmap);
    }


     /* 同步创建指定前景色、指定背景色、带logo的二维码图片。该方法是耗时操作，请在子线程中调用。
            *
            * @param content         要生成的二维码图片内容
     * @param size            图片宽高，单位为px
     * @param foregroundColor 二维码图片的前景色
     * @param backgroundColor 二维码图片的背景色
     * @param logo            二维码图片的logo
     */
    public static Bitmap syncEncodeQRCode(String content, int size, int foregroundColor, int backgroundColor, Bitmap logo) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, HINTS);
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * size + x] = foregroundColor;
                    } else {
                        pixels[y * size + x] = backgroundColor;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return addLogoToQRCode(bitmap, logo);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }











    /**
     * 添加logo到二维码图片上
     */
    private static Bitmap addLogoToQRCode(Bitmap src, Bitmap logo) {
        if (src == null || logo == null) {
            return src;
        }

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            e.printStackTrace();
            bitmap = null;
        }
        return bitmap;
    }

    /**
     * 同步创建条形码图片
     *
     * @param content  要生成条形码包含的内容
     * @param width    条形码的宽度，单位px
     * @param height   条形码的高度，单位px
     * @param textSize 字体大小，单位px，如果等于0则不在底部绘制文字
     * @return 返回生成条形的位图
     */
    public static Bitmap syncEncodeBarcode(String content, int width, int height, int textSize) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 0);

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            if (textSize > 0) {
                bitmap = showContent(bitmap, content, textSize);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 显示条形的内容
     *
     * @param barcodeBitmap 已生成的条形码的位图
     * @param content       条形码包含的内容
     * @param textSize      字体大小，单位px
     * @return 返回生成的新条形码位图
     */
    private static Bitmap showContent(Bitmap barcodeBitmap, String content, int textSize) {
        if (TextUtils.isEmpty(content) || null == barcodeBitmap) {
            return null;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        int textWidth = (int) paint.measureText(content);
        Paint.FontMetrics fm = paint.getFontMetrics();
        int textHeight = (int) (fm.bottom - fm.top);
        float scaleRateX = barcodeBitmap.getWidth() * 1.0f / textWidth;
        if (scaleRateX < 1) {
            paint.setTextScaleX(scaleRateX);
        }
        int baseLine = barcodeBitmap.getHeight() + textHeight;
        Bitmap bitmap = Bitmap.createBitmap(barcodeBitmap.getWidth(), barcodeBitmap.getHeight() + 2 * textHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas();
        canvas.drawColor(Color.WHITE);
        canvas.setBitmap(bitmap);
        canvas.drawBitmap(barcodeBitmap, 0, 0, null);
        canvas.drawText(content, barcodeBitmap.getWidth() / 2, baseLine, paint);
        canvas.save();
        canvas.restore();
        return bitmap;
    }

}
