package com.gemini910610.moneyrpg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class RadarChart extends View
{
    Paint radar_paint, radar_border_paint, value_paint, value_border_paint, text_paint;
    private final float[] angle_90, angle_150, angle_210, angle_270, angle_330, angle_30;
    private final int[] max_data = {100, 50, 50, 100, 20, 20};
    private final int[] data = {0, 0, 0, 0, 0, 0};

    public RadarChart(Context context, AttributeSet attributes)
    {
        super(context, attributes);

        radar_paint = new Paint();
        radar_paint.setStrokeWidth(2.5f);
        radar_paint.setStyle(Paint.Style.STROKE);
        radar_paint.setStrokeCap(Paint.Cap.ROUND);
        radar_paint.setStrokeJoin(Paint.Join.ROUND);

        radar_border_paint = new Paint();
        radar_border_paint.setStrokeWidth(10);
        radar_border_paint.setStyle(Paint.Style.STROKE);
        radar_border_paint.setStrokeCap(Paint.Cap.ROUND);
        radar_border_paint.setStrokeJoin(Paint.Join.ROUND);

        value_paint = new Paint();
        value_paint.setColor(Color.GREEN);
        value_paint.setAlpha(100);
        value_paint.setStrokeCap(Paint.Cap.ROUND);
        value_paint.setStrokeJoin(Paint.Join.ROUND);

        value_border_paint = new Paint();
        value_border_paint.setColor(Color.GREEN);
        value_border_paint.setStrokeWidth(5);
        value_border_paint.setStyle(Paint.Style.STROKE);
        value_border_paint.setStrokeCap(Paint.Cap.ROUND);
        value_border_paint.setStrokeJoin(Paint.Join.ROUND);

        text_paint = new Paint();
        text_paint.setTextSize(50);
        text_paint.setTextAlign(Paint.Align.CENTER);
        text_paint.setTypeface(getResources().getFont(R.font.pixel));
        text_paint.setLetterSpacing(0.1f);

        angle_90 = getUnitVector(90);
        angle_150 = getUnitVector(150);
        angle_210 = getUnitVector(210);
        angle_270 = getUnitVector(270);
        angle_330 = getUnitVector(330);
        angle_30 = getUnitVector(30);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas)
    {
        super.onDraw(canvas);

        drawRadar(canvas);
        drawRadarBorder(canvas);
        drawValues(canvas);
        drawText(canvas);
    }

    private float[] getUnitVector(int angle)
    {
        double radian = angle / 180.0 * Math.PI;
        return new float[]{(float)Math.cos(radian), -(float)Math.sin(radian)};
    }

    private void drawRadar(Canvas canvas)
    {
        int width = getWidth();

        Path path = new Path();
        for (float radius = width * 0.4f; radius > 0; radius -= width * 0.1f)
        {
            moveToPolar(path, radius, angle_90);
            lineToPolar(path, radius, angle_150);
            lineToPolar(path, radius, angle_210);
            lineToPolar(path, radius, angle_270);
            lineToPolar(path, radius, angle_330);
            lineToPolar(path, radius, angle_30);
            path.close();
        }

        canvas.drawPath(path, radar_paint);
    }

    private void drawRadarBorder(Canvas canvas)
    {
        float radius = getWidth() * 0.5f;

        Path path = new Path();
        moveToPolar(path, radius, angle_90);
        lineToPolar(path, radius, angle_150);
        lineToPolar(path, radius, angle_210);
        lineToPolar(path, radius, angle_270);
        lineToPolar(path, radius, angle_330);
        lineToPolar(path, radius, angle_30);
        lineToPolar(path, radius, angle_90);
        lineToPolar(path, radius, angle_270);
        moveToPolar(path, radius, angle_150);
        lineToPolar(path, radius, angle_330);
        moveToPolar(path, radius, angle_210);
        lineToPolar(path, radius, angle_30);

        canvas.drawPath(path, radar_border_paint);
    }

    private void drawValues(Canvas canvas)
    {
        float radius = getWidth() * 0.5f;

        Path path = new Path();
        moveToPolar(path, radius * data[0] / max_data[0], angle_90);
        lineToPolar(path, radius * data[1] / max_data[1], angle_30);
        lineToPolar(path, radius * data[2] / max_data[2], angle_330);
        lineToPolar(path, radius * data[3] / max_data[3], angle_270);
        lineToPolar(path, radius * data[4] / max_data[4], angle_210);
        lineToPolar(path, radius * data[5] / max_data[5], angle_150);
        path.close();

        canvas.drawPath(path, value_paint);
        canvas.drawPath(path, value_border_paint);
    }

    private void drawText(Canvas canvas)
    {
        int height = getHeight();
        float radius = getWidth() * 0.5f;

        textOnPolar(canvas, "STR", radius, angle_90, -height * 0.02f);
        textOnPolar(canvas, "DEX", radius, angle_30, -height * 0.03f);
        textOnPolar(canvas, "AGI", radius, angle_330, height * 0.03f);
        textOnPolar(canvas, "VIT", radius, angle_270, height * 0.02f);
        textOnPolar(canvas, "WIS", radius, angle_210, height * 0.03f);
        textOnPolar(canvas, "LUC", radius, angle_150, -height * 0.03f);
    }

    private void moveToPolar(Path path, float radius, float[] angle)
    {
        float center_x = getWidth() * 0.5f;
        float center_y = getHeight() * 0.5f;

        path.moveTo(center_x + angle[0] * radius, center_y + angle[1] * radius);
    }

    private void lineToPolar(Path path, float radius, float[] angle)
    {
        float center_x = getWidth() * 0.5f;
        float center_y = getHeight() * 0.5f;

        path.lineTo(center_x + angle[0] * radius, center_y + angle[1] * radius);
    }

    private void textOnPolar(Canvas canvas, String text, float radius, float[] angle, float delta_y)
    {
        float center_x = getWidth() * 0.5f;
        float center_y = getHeight() * 0.5f;
        Paint.FontMetrics metrics = text_paint.getFontMetrics();
        float text_distance = (metrics.bottom - metrics.top) / 2 - metrics.bottom;

        canvas.drawText(text, center_x + angle[0] * radius, center_y + angle[1] * radius + delta_y + text_distance, text_paint);
    }

    public void update()
    {
        data[0] = Player.Instance.getSTR();
        data[1] = Player.Instance.getDEX();
        data[2] = Player.Instance.getAGI();
        data[3] = Player.Instance.getVIT();
        data[4] = Player.Instance.getWIS();
        data[5] = Player.Instance.getLUC();
    }
}
