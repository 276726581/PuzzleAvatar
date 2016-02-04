package example.com.puzzleavatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by TimoRD on 2016/2/2.
 */
public class DrawView extends View {

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap pic = BitmapFactory.decodeResource(getResources(), R.drawable.pic);
        draw(canvas, pic, 0, 200, 5);
        draw(canvas, pic, 200, 150, 3);
        draw(canvas, pic, 350, 100, 2);
        if (!pic.isRecycled()) {
            pic.recycle();
        }
    }

    private void draw(Canvas canvas, Bitmap pic, int left, int size, int border) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (int i = 1; i <= 5; i++) {
            Bitmap[] array = new Bitmap[i];
            for (int j = 0; j < array.length; j++) {
                array[j] = pic;
            }
            Bitmap bitmap = PuzzleAvatarUtil.puzzle(array, size, border);
            canvas.drawBitmap(bitmap, left, (i - 1) * size, paint);
        }
    }
}
