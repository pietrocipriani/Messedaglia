package it.gov.messedaglia.messedaglia.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.Sampler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import it.gov.messedaglia.messedaglia.Utils;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi;

public class MarkView extends View {
    private final static int SUFFICIENT_COLORS[] = new int[]{0x00009900, 0xFF009900};
    private final static int INSUFFICIENT_COLORS[] = new int[]{0x00990000, 0xFF990000};
    private final static int INVALID_COLORS[] = new int[]{0x00000099, 0xFF000099};

    private RegisterApi.MarksData.Mark mark;

    private float progress = 0;

    private final Paint paint = new Paint();
    private SweepGradient gradient = new SweepGradient(0, 0, INVALID_COLORS, new float[]{0, 1});
    private final static Matrix MATRIX = new Matrix();

    private final float textSize;

    static {
        MATRIX.postRotate(-90);
    }

    public MarkView(Context context) {
        this(context, null, 0);
    }

    public MarkView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        gradient.setLocalMatrix(MATRIX);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(2000);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener((anim) -> {
            gradient = new SweepGradient(0, 0, SUFFICIENT_COLORS, new float[]{0, (mark != null ? (float) (360*mark.decimalValue/10) : 360)*anim.getAnimatedFraction()});
            gradient.setLocalMatrix(MATRIX);
            invalidate(anim.getAnimatedFraction());
        });
        animator.start();

        paint.setAntiAlias(true);
        paint.setColor(0xFF000000);
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Utils.dpToPx(20));

        Rect bounds = new Rect();
        paint.getTextBounds("0", 0, 1, bounds);
        textSize = bounds.height();
    }

    public void setMark (RegisterApi.MarksData.Mark mark){
        this.mark = mark;
        gradient = new SweepGradient(0, 0, SUFFICIENT_COLORS, new float[]{0, (float) (mark.decimalValue/10)});
        gradient.setLocalMatrix(MATRIX);

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = resolveSize(Utils.dpToPx(70), heightMeasureSpec);
        int width = resolveSize(height, widthMeasureSpec);
        setMeasuredDimension(width, height);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        int L = Math.min(getWidth()-getPaddingRight()-getPaddingLeft(), getHeight()-getPaddingTop()-getPaddingBottom());

        paint.setShader(gradient);
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(Utils.dpToPx(5));

        /*clip.reset();
        clip.addRect(-L/2f, -L/2f, L/2f, L/2f, Path.Direction.CW);
        clip.addCircle(0, 0, L*5/16f, Path.Direction.CCW);*/

        canvas.translate(getWidth()/2f, getHeight()/2f);
        //canvas.clipPath(clip);

        canvas.drawArc(-L/2f+paint.getStrokeWidth()/2f, -L/2f+paint.getStrokeWidth()/2f, L/2f-paint.getStrokeWidth()/2f, L/2f-paint.getStrokeWidth()/2f, -90, (mark != null ? (float) (360*mark.decimalValue/10) : 360)*progress, false, paint);

        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);

        paint.setStrokeWidth(Utils.dpToPx(1));

        canvas.drawText(mark != null ? mark.displayValue : "?", 0, textSize/2, paint);
        //canvas.drawCircle(0, 0, L/2f, paint);
    }

    public void invalidate (float progress) {
        this.progress = progress;
        invalidate();
    }
}
