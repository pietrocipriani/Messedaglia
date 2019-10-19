package it.gov.messedaglia.messedaglia.fragments.register;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.view.animation.AccelerateInterpolator;

import it.gov.messedaglia.messedaglia.Utils;

public class TitleMarkView extends AppCompatTextView {
    private float progress = 0;
    private final float mark;
    private final Paint paint = new Paint();

    public TitleMarkView(Context context, float mark, String text) {
        super(context);

        setText(text);
        setPadding(Utils.dpToPx(10), Utils.dpToPx(5), Utils.dpToPx(10), Utils.dpToPx(5));
        setTextSize(Utils.dpToPx(12));
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setTypeface(Typeface.DEFAULT_BOLD);
        setTextColor(0xFF000000);

        this.mark = mark;

        if (mark < 0.6) paint.setColor(0xFFFF9999);
        else paint.setColor(0xFF99FF99);

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(2000);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener((anim) -> {
            progress = anim.getAnimatedFraction();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(paint.getColor() | 0xFF000000);
        canvas.drawRect(0, 0, getWidth()*progress*mark, getHeight(), paint);
        paint.setColor(paint.getColor() & 0x7FFFFFFF);
        canvas.drawRect(getWidth()*progress*mark, 0, getWidth(), getHeight(), paint);
        super.onDraw(canvas);
    }
}
