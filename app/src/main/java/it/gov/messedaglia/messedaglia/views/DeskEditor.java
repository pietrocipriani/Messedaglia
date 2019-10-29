package it.gov.messedaglia.messedaglia.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import it.gov.messedaglia.messedaglia.Utils;

public class DeskEditor extends View {

    private final DrawableClass classPlant;


    public DeskEditor(Context context) {
        this (context, null, 0);
    }
    public DeskEditor(Context context, @Nullable AttributeSet attrs) {
        this (context, attrs, 0);
    }
    public DeskEditor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super (context, attrs, defStyleAttr);

        classPlant = new DrawableClass(
                new DrawableColumn(
                        new DrawableRow(2),
                        new DrawableRow(2),
                        new DrawableRow(2),
                        new DrawableRow(2)
                ),
                new DrawableColumn(
                        new DrawableRow(3),
                        new DrawableRow(2),
                        new DrawableRow(2),
                        new DrawableRow(2)
                ),
                new DrawableColumn(
                        new DrawableRow(2),
                        new DrawableRow(2),
                        new DrawableRow(2),
                        new DrawableRow(2)
                )
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        classPlant.draw(canvas, getWidth(), getHeight());
    }
}

abstract class Drawable {
    final Drawable[] children;

    Drawable (Drawable... children) {
        this.children = children;
    }

    abstract void draw (Canvas cv, float width, float height);
    abstract int widthWeight ();
    abstract int heightWeight ();
}
class DrawableClass extends Drawable {

    DrawableClass (Drawable... children) {
        super(children);
    }

    @Override
    void draw(Canvas cv, float width, float height) {
        float unit = Math.min(width/widthWeight(), height/heightWeight());
        cv.save();
        cv.translate(unit, height-(2+DrawableDesk.DESK_WEIGHT)*unit);
        for (Drawable d : children) {
            cv.translate(0, -unit * d.heightWeight());
            d.draw(cv, d.widthWeight()*unit, d.heightWeight()*unit);
            cv.translate((d.widthWeight()+1)*unit, unit * d.heightWeight());
        }
        cv.restore();
        cv.save();
        cv.translate(width/2-DrawableDesk.DESK_WEIGHT*unit, height-(1+DrawableDesk.DESK_WEIGHT)*unit);
        DrawableDesk.paint.setStyle(Paint.Style.STROKE);
        cv.drawRect(0, 0, DrawableDesk.DESK_WEIGHT*2*unit, DrawableDesk.DESK_WEIGHT*unit, DrawableDesk.paint);
        cv.restore();
    }

    @Override
    int widthWeight() {
        int c = children.length+1;
        for (Drawable d : children) c += d.widthWeight();
        return Math.max(c, DrawableDesk.DESK_WEIGHT*2+2);
    }

    @Override
    int heightWeight() {
        int c = 3+DrawableDesk.DESK_WEIGHT;
        int max = 0;
        for (Drawable d : children) max = Math.max(max, d.heightWeight());
        return c+max;
    }
}
class DrawableColumn extends Drawable {

    DrawableColumn (Drawable... children) {
        super(children);
    }

    @Override
    void draw(Canvas cv, float width, float height) {
        float unit = Math.min(width/widthWeight(), height/heightWeight());
        cv.save();
        cv.translate(width/2, 0);
        for (Drawable d : children) {
            cv.translate(-unit*d.widthWeight()/2, 0);
            d.draw(cv, unit*d.widthWeight(), unit*d.heightWeight());
            cv.translate(unit*d.widthWeight()/2, unit*(d.heightWeight()+1));
        }
        cv.restore();
    }

    @Override
    int widthWeight() {
        int c = 0;
        for (Drawable d : children) c = Math.max(c, d.widthWeight());
        return c;
    }

    @Override
    int heightWeight() {
        int c = children.length-1;
        for (Drawable d : children) c += d.heightWeight();
        return c;
    }
}
class DrawableRow extends Drawable {

    DrawableRow (int desksCount) {
        super(createArray(desksCount));
    }

    private static DrawableDesk[] createArray (int deskCount) {
        DrawableDesk[] array = new DrawableDesk[deskCount];
        Arrays.fill(array, new DrawableDesk());
        return array;
    }

    @Override
    void draw(Canvas cv, float width, float height) {
        float unit = Math.min(width/widthWeight(), height/heightWeight());
        cv.save();
        cv.translate(0, height/2);
        for (Drawable d : children) {
            cv.translate(0, -unit*d.heightWeight()/2);
            d.draw(cv, unit*d.widthWeight(), unit*d.heightWeight());
            cv.translate(unit*d.widthWeight(), unit*d.heightWeight()/2);
        }
        cv.restore();
    }

    @Override
    int widthWeight() {
        int c = 0;
        for (Drawable d : children) c += d.widthWeight();
        return c;
    }

    @Override
    int heightWeight() {
        int c = 0;
        for (Drawable d : children) c = Math.max(c, d.heightWeight());
        return c;
    }
}
class DrawableDesk extends Drawable {
    final static int DESK_WEIGHT = 2;
    final static Paint paint = new Paint();

    final static ArrayList<Integer> NUMBERS = new ArrayList<>(25);
    static int index = 0;
    static int textHeight;

    static {
        paint.setColor(0xFF000000);
        paint.setStrokeWidth(Utils.dpToPx(3));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Utils.dpToPx(20));
        Rect bounds = new Rect();
        paint.getTextBounds("0", 0, 1, bounds);
        textHeight = bounds.height();
        for (int i=1; i<=25; i++) NUMBERS.add(i);
        Collections.shuffle(NUMBERS);
    }

    DrawableDesk (Drawable... children) {
        super(children);
    }

    @Override
    void draw(Canvas cv, float width, float height) {
        float unit = Math.min(width/widthWeight(), height/heightWeight());
        paint.setStyle(Paint.Style.STROKE);
        cv.drawRect((width-DESK_WEIGHT*unit)/2, (height-DESK_WEIGHT*unit)/2, (width+DESK_WEIGHT*unit)/2, (height+DESK_WEIGHT*unit)/2, paint);
        paint.setStyle(Paint.Style.FILL);
        cv.drawText(String.valueOf(NUMBERS.get(index++ % NUMBERS.size())), width/2, (height+textHeight)/2, paint);
    }

    @Override
    int widthWeight() {
        return DESK_WEIGHT;
    }

    @Override
    int heightWeight() {
        return DESK_WEIGHT;
    }
}

