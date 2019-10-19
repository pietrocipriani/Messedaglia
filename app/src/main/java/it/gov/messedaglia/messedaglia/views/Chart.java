package it.gov.messedaglia.messedaglia.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import it.gov.messedaglia.messedaglia.Utils;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi.MarksData.Subject;

public class Chart extends View {

    private final static int POINT_RADIUS = Utils.dpToPx(5);

    private final Paint paint;

    private Subject sbj;

    private float animation = 0;


    public Chart(Context context) {
        this(context, null, 0);
    }

    public Chart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Chart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.paint = new Paint();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animation = 0;
        animator.setDuration(2000);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener((anim) -> {
            animation = anim.getAnimatedFraction();
            invalidate();
        });
        animator.start();

    }

    public void setSubject (Subject sbj) {
        this.sbj = sbj;
        invalidate();
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

        drawPath(canvas);
    }

    private void drawPath (Canvas canvas) {
        if (sbj == null || sbj.marks.isEmpty()) return;
        paint.setColor(0xFF000000);

        float xTranslation = (float) getWidth()/(sbj.marks.size()+1);

        canvas.translate(xTranslation, getHeight());

        for (RegisterApi.MarksData.Mark mark : sbj.marks) {
            canvas.drawCircle(0, - (float)mark.decimalValue/10*getHeight() * animation, POINT_RADIUS, paint);
            canvas.translate(xTranslation, 0);
        }
    }

}
