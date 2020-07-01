package cti.com.androidmonitor.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import cti.com.androidmonitor.R;

public class CustomProgressBar extends View {

    private int color, progress_color, textColor;

    private float progress = 0;

    private String text1 = "Space usage", text2 = null;

    private float center, halfSide;

    public CustomProgressBar(Context context, AttributeSet attr) {
        super(context, attr);
        //init();
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attr,
                R.styleable.CustomProgressBar,
                0, 0);
        try {
            color = a.getColor(R.styleable.CustomProgressBar_colorBar, Color.parseColor("#787878"));
            progress_color = a.getColor(R.styleable.CustomProgressBar_colorBar_progress, Color.parseColor("#007DD6"));
            textColor = a.getColor(R.styleable.CustomProgressBar_colorBar_text, Color.BLUE);
            text2 = a.getString(R.styleable.CustomProgressBar_smallText);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        float w = (float) getWidth();
        float h = (float) getHeight();
        float minSide = Math.min(w, h);
        halfSide = minSide/2.5F;
        center = minSide/2.F;
    }

    float getTextSizeByX(Paint paint, String str) {
        int text1Length = str.length();
        float[] widths = new float[text1Length];
        paint.getTextWidths(str, 0, text1Length, widths);
        float sizeTextX = 0;
        for (int i = 0; i < text1Length; i++ ) {
            sizeTextX+=widths[i];
        }
        return sizeTextX;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        init();
        float textSize = center-halfSide;
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(30.F);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(new RectF(center-halfSide, center-halfSide, center+halfSide, center+halfSide), 0, 360, false, paint);
        Paint pText = new Paint(TextPaint.ANTI_ALIAS_FLAG);
        pText.setTextSize(textSize);
        pText.setColor(textColor);
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),"fonts/Roboto-LightItalic.ttf");
        pText.setTypeface(tf);
        float xMinuxForText = getTextSizeByX(pText, text1)/2.F;
        canvas.drawText(text1, center-xMinuxForText, center-xMinuxForText/4.F, pText);
        pText.setTextSize(textSize/1.5F);
        if (text2 != null) {
            xMinuxForText = getTextSizeByX(pText, text2) / 2.F;
            canvas.drawText(text2, center - xMinuxForText, center + xMinuxForText / 4.F, pText);
        }
        Paint pColor = new Paint();
        pColor.setColor(progress_color);
        pColor.setStrokeWidth(30.F);
        pColor.setStyle(Paint.Style.STROKE);
        canvas.drawArc(new RectF(center-halfSide, center-halfSide, center+halfSide, center+halfSide), 180, progress, false, pColor);
    }

    public void setText(String text) {
        text2 = text;
    }

    public void setBigText(String text) {
        text1 = text;
    }

    public void setProgress(int progressPer) {
        progress = (float) progressPer*3.6F;
    }
}
