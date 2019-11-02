package it.gov.messedaglia.messedaglia.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import it.gov.messedaglia.messedaglia.Utils;
import it.gov.messedaglia.messedaglia.registerapi.RegisterApi.MarksData.Subject;

public class Chart extends View {

    private final static int POINT_RADIUS = Utils.dpToPx(3);

    private final Paint paint;
    private final Path path;

    private Subject sbj;

    private float animation;


    public Chart(Context context) {
        this(context, null, 0);
    }

    public Chart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Chart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.paint = new Paint();
        this.path = new Path();

        paint.setStrokeWidth(Utils.dpToPx(1));

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

    private void drawPath(Canvas canvas) {
        if (sbj == null || sbj.marks.isEmpty()) return;
        paint.setColor(0xFF000000);

        float xTranslation = (float) getWidth()/(sbj.nonBlueMarks.size()+1);

        canvas.translate(xTranslation, getHeight());

        Point[] points = new Point[sbj.nonBlueMarks.size()];

        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(xTranslation*i, - sbj.nonBlueMarks.get(i).decimalValue/10*getHeight() * animation);
            canvas.drawCircle(xTranslation*i, - sbj.nonBlueMarks.get(i).decimalValue/10*getHeight() * animation, POINT_RADIUS, paint);
        }
        if (points.length <= 1) return;
        if (points.length == 2) {
            canvas.drawLine(points[0].x, points[0].y, points[1].x, points[1].y, paint);
            return;
        }

        points = getPathPoints(points);
        path.reset();
        path.moveTo(0, 0);
        int i = 0;
        path.moveTo(points[i].x, points[i].y);
        path.quadTo(points[++i].x, points[i].y, points[++i].x, points[i].y);
        for (i++; i<points.length-2; i++)
            path.cubicTo(points[i].x, points[i].y, points[++i].x, points[i].y, points[++i].x, points[i].y);
        path.quadTo(points[i].x, points[i].y, points[++i].x, points[i].y);

        canvas.drawPath(path, paint);
    }

    private static Point[] getPathPoints (Point[] points) {
        Point[] points2 = new Point[3*points.length-4];
        points2[0] = points[0];
        points2[points2.length-1] = points[points.length-1];
        for (int i1 = 1, i2 = 2; i1 < points.length-1; i1++, i2 += 3){
            float m = points[i1-1].m(points[i1+1]);
            Point point = points[i1];
            points2[i2] = point;
            float deltaX1 = point.deltaX(points[i1-1])/3;
            float deltaX2 = point.deltaX(points[i1+1])/3;
            points2[i2-1] = new Point(point.x+deltaX1, point.y+deltaX1*m);
            points2[i2+1] = new Point(point.x+deltaX2, point.y+deltaX2*m);
        }
        return points2;
    }

    private static class Point {
        float x, y;

        private Point (float x, float y){
            this.x = x;
            this.y = y;
        }

        private float m (Point p2) {
            return deltaY(p2)/deltaX(p2);
        }

        private float deltaX (Point p2) {
            return p2.x-x;
        }
        private float deltaY (Point p2) {
            return p2.y-y;
        }
    }

}
