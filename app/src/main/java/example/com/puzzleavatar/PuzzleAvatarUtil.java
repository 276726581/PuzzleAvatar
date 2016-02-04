package example.com.puzzleavatar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.Arrays;

/**
 * Created by TimoRD on 2016/1/26.
 */
public final class PuzzleAvatarUtil {

    private PuzzleAvatarUtil() {

    }

    /**
     * 拼图
     *
     * @param bitmaps
     * @param size
     * @param border
     * @return
     */
    public static Bitmap puzzle(Bitmap[] bitmaps, float size, float border) {
        Bitmap[] bitmapArray;
        if (bitmaps.length > 5) {
            bitmapArray = Arrays.copyOfRange(bitmaps, 0, 5);
        } else {
            bitmapArray = bitmaps;
        }

        Bitmap localBitmap = Bitmap.createBitmap((int) size, (int) size, Bitmap.Config.ARGB_8888);
        Canvas localCanvas = new Canvas(localBitmap);
        localCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Point[] pointArray = calcPoint(size, calcCenterDistance(size, bitmapArray.length), bitmapArray.length);
        Bitmap bitmap = drawPuzzle(bitmapArray, pointArray, size, calcCircleSize(size, bitmapArray.length), border);
        localCanvas.drawBitmap(bitmap, 0, 0, paint);

        return localBitmap;
    }

    /**
     * 调整中心距
     *
     * @param size
     * @param num
     * @return
     */
    private static float calcCenterDistance(float size, int num) {
        switch (num) {
            case 1:
                return size;
            case 2:
                return size / 4.5f;
            case 3:
                return size / 4.5f;
            case 4:
                return size / 3.5f;
            case 5:
            default:
                return size / 4;
        }
    }

    /**
     * 调整圆的直径
     *
     * @param size
     * @param num
     * @return
     */
    private static float calcCircleSize(float size, int num) {
        switch (num) {
            case 1:
                return size / 1.15f;
            case 2:
                return size / 1.8f;
            case 3:
                return size / 2f;
            case 4:
                return size / 2.2f;
            case 5:
            default:
                return size / 2.5f;
        }
    }

    /**
     * 画拼图
     *
     * @param bitmapArray
     * @param pointArray
     * @param canvasSize
     * @param circleSize
     * @param border
     * @return
     */
    private static Bitmap drawPuzzle(Bitmap[] bitmapArray, Point[] pointArray, float canvasSize, float circleSize, float border) {
        Bitmap localBitmap = Bitmap.createBitmap((int) canvasSize, (int) canvasSize, Bitmap.Config.ARGB_8888);
        Canvas localCanvas = new Canvas(localBitmap);
        localCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap[] circleBitmapArray = new Bitmap[pointArray.length];
        for (int i = 0; i < bitmapArray.length; i++) {
            circleBitmapArray[i] = drawCircle(bitmapArray[i]);
        }

        RectF[] rectFArray = new RectF[pointArray.length];
        for (int i = 0; i < bitmapArray.length; i++) {
            RectF localRectF = new RectF();
            Location location = calcLocation(pointArray[i], circleSize);
            localRectF.set(location.left, location.top, location.width, location.height);
            rectFArray[i] = localRectF;
        }

        switch (circleBitmapArray.length) {
            case 1: {
                Bitmap bitmap = circleBitmapArray[0];
                Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                RectF rectF = rectFArray[0];
                localCanvas.drawBitmap(bitmap, rect, rectF, paint);
            }
            break;
            case 2: {
                Bitmap srcBitmap1 = circleBitmapArray[0];
                RectF srcRectF1 = rectFArray[0];
                Bitmap dstBitmap1 = circleBitmapArray[1];
                RectF dstRectF1 = rectFArray[1];
                Bitmap bitmap1 = clipBitmap(srcBitmap1, srcRectF1, dstBitmap1, dstRectF1, canvasSize, border);
                localCanvas.drawBitmap(bitmap1, 0, 0, paint);
                if (!bitmap1.isRecycled()) {
                    bitmap1.recycle();
                }

                Bitmap srcBitmap2 = circleBitmapArray[1];
                Rect srcRect2 = new Rect(0, 0, srcBitmap2.getWidth(), srcBitmap2.getHeight());
                RectF srcRectF2 = rectFArray[1];
                localCanvas.drawBitmap(srcBitmap2, srcRect2, srcRectF2, paint);
            }
            break;
            case 3:
            case 4:
            case 5:
            default: {
                for (int i = 0; i < circleBitmapArray.length; i++) {
                    Bitmap srcBitmap;
                    RectF srcRectF;
                    Bitmap dstBitmap;
                    RectF dstRectF;
                    if (0 == i) {
                        srcBitmap = circleBitmapArray[i];
                        srcRectF = rectFArray[i];
                        dstBitmap = circleBitmapArray[circleBitmapArray.length - 1];
                        dstRectF = rectFArray[circleBitmapArray.length - 1];
                    } else {
                        srcBitmap = circleBitmapArray[i];
                        srcRectF = rectFArray[i];
                        dstBitmap = circleBitmapArray[i - 1];
                        dstRectF = rectFArray[i - 1];
                    }

                    Bitmap bitmap = clipBitmap(srcBitmap, srcRectF, dstBitmap, dstRectF, canvasSize, border);
                    localCanvas.drawBitmap(bitmap, 0, 0, paint);
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }
            break;
        }

        for (Bitmap bitmap : circleBitmapArray) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        return localBitmap;
    }

    /**
     * 裁剪图片
     *
     * @param srcBitmap
     * @param srcRectF
     * @param dstBitmap
     * @param dstRectF
     * @param size
     * @param border
     * @return
     */
    private static Bitmap clipBitmap(Bitmap srcBitmap, RectF srcRectF, Bitmap dstBitmap, RectF dstRectF, float size, float border) {
        Bitmap localBitmap = Bitmap.createBitmap((int) size, (int) size, Bitmap.Config.ARGB_8888);
        Canvas localCanvas = new Canvas(localBitmap);
        localCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect srcRect = new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
        localCanvas.drawBitmap(srcBitmap, srcRect, srcRectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        RectF borderRectF = new RectF(dstRectF.left - border, dstRectF.top - border, dstRectF.right + border, dstRectF.bottom + border);
        Rect dstRect = new Rect(0, 0, dstBitmap.getWidth(), dstBitmap.getHeight());
        localCanvas.drawBitmap(dstBitmap, dstRect, borderRectF, paint);
        paint.setXfermode(null);

        return localBitmap;
    }

    /**
     * 裁剪圆形图片
     *
     * @param bitmap
     * @return
     */
    private static Bitmap drawCircle(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());

        Bitmap localBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas localCanvas = new Canvas(localBitmap);
        localCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int left = bitmap.getWidth() / 2 - size / 2;
        int top = bitmap.getHeight() / 2 - size / 2;

        RectF rectF = new RectF(0, 0, size, size);
        localCanvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        localCanvas.drawBitmap(bitmap, -left, -top, paint);
        paint.setXfermode(null);

        return localBitmap;
    }

    /**
     * 计算中心点
     *
     * @param size
     * @param centerDistance
     * @param num
     * @return
     */
    private static Point[] calcPoint(float size, float centerDistance, int num) {
        Point[] pointArray;

        float centerX = size / 2;
        float centerY = size / 2;

        switch (num) {
            case 1: {
                pointArray = new Point[1];
                for (int i = 0; i < pointArray.length; i++) {
                    pointArray[i] = new Point();
                }
                pointArray[0].x = centerX;
                pointArray[0].y = centerY;
            }
            break;
            case 2: {
                pointArray = new Point[2];
                for (int i = 0; i < pointArray.length; i++) {
                    pointArray[i] = new Point();
                }
                float x;
                float y;

                x = (float) (centerDistance * Math.sin(Math.PI / 4));
                y = (float) (centerDistance * Math.cos(Math.PI / 4));
                pointArray[0].x = centerX - x;
                pointArray[0].y = centerX - y;

                x = (float) (centerDistance * Math.sin(Math.PI / 4));
                y = (float) (centerDistance * Math.cos(Math.PI / 4));
                pointArray[1].x = centerX + x;
                pointArray[1].y = centerX + y;
            }
            break;
            case 3: {
                pointArray = new Point[3];
                for (int i = 0; i < pointArray.length; i++) {
                    pointArray[i] = new Point();
                }
                float x;
                float y;

                x = 0;
                y = (float) (centerDistance * Math.sin(Math.PI / 2));
                pointArray[0].x = centerX + x;
                pointArray[0].y = centerY - y;

                x = (float) (centerDistance * Math.sin(Math.PI / 3));
                y = (float) (centerDistance * Math.cos(Math.PI / 6));
                pointArray[1].x = centerX + x;
                pointArray[1].y = centerY + y;

                x = (float) (centerDistance * Math.sin(Math.PI / 3));
                y = (float) (centerDistance * Math.cos(Math.PI / 6));
                pointArray[2].x = centerX - x;
                pointArray[2].y = centerY + y;
            }
            break;
            case 4: {
                pointArray = new Point[4];
                for (int i = 0; i < pointArray.length; i++) {
                    pointArray[i] = new Point();
                }
                float x;
                float y;

                x = (float) (centerDistance * Math.sin(Math.PI / 4));
                y = (float) (centerDistance * Math.cos(Math.PI / 4));
                pointArray[0].x = centerX + x;
                pointArray[0].y = centerY - y;

                x = (float) (centerDistance * Math.sin(Math.PI / 4));
                y = (float) (centerDistance * Math.cos(Math.PI / 4));
                pointArray[1].x = centerX + x;
                pointArray[1].y = centerY + y;

                x = (float) (centerDistance * Math.sin(Math.PI / 4));
                y = (float) (centerDistance * Math.cos(Math.PI / 4));
                pointArray[2].x = centerX - x;
                pointArray[2].y = centerY + y;

                x = (float) (centerDistance * Math.sin(Math.PI / 4));
                y = (float) (centerDistance * Math.cos(Math.PI / 4));
                pointArray[3].x = centerX - x;
                pointArray[3].y = centerY - y;
            }
            break;
            case 5:
            default: {
                pointArray = new Point[5];
                for (int i = 0; i < pointArray.length; i++) {
                    pointArray[i] = new Point();
                }
                float x;
                float y;

                x = 0;
                y = (float) (centerDistance * Math.sin(Math.PI / 2));
                pointArray[0].x = centerX + x;
                pointArray[0].y = centerY - y;

                x = (float) (centerDistance * Math.cos(Math.PI / 10));
                y = (float) (centerDistance * Math.sin(Math.PI / 10));
                pointArray[1].x = centerX + x;
                pointArray[1].y = centerY - y;

                x = (float) (centerDistance * Math.sin(Math.PI * 36 / 180));
                y = (float) (centerDistance * Math.cos(Math.PI * 36 / 180));
                pointArray[2].x = centerX + x;
                pointArray[2].y = centerY + y;

                x = (float) (centerDistance * Math.sin(Math.PI * 36 / 180));
                y = (float) (centerDistance * Math.cos(Math.PI * 36 / 180));
                pointArray[3].x = centerX - x;
                pointArray[3].y = centerY + y;

                x = (float) (centerDistance * Math.cos(Math.PI / 10));
                y = (float) (centerDistance * Math.sin(Math.PI / 10));
                pointArray[4].x = centerX - x;
                pointArray[4].y = centerY - y;
            }
            break;
        }

        return pointArray;
    }

    /**
     * 计算位置
     *
     * @param point
     * @param size
     * @return
     */
    private static Location calcLocation(Point point, float size) {
        Location location = new Location();
        location.left = point.x - size / 2;
        location.top = point.y - size / 2;
        location.width = location.left + size;
        location.height = location.top + size;

        return location;
    }

    private static class Point {
        public float x;
        public float y;
    }

    private static class Location {
        public float top;
        public float left;
        public float width;
        public float height;
    }
}
