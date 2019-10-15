package it.gov.messedaglia.messedaglia.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Chart extends View {

    private final Paint paint;


    public Chart(Context context) {
        this(context, null, 0);
    }

    public Chart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Chart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.paint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize(500, widthMeasureSpec);
        int height = resolveSize(width*9/16, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(0xFF99FF99);
        canvas.drawRect(0, 0, getWidth(), getHeight()*4/10f, paint);
        paint.setColor(0xFFFF9999);
        canvas.drawRect(0, getHeight()*4/10f, getWidth(), getHeight(), paint);
    }
}
