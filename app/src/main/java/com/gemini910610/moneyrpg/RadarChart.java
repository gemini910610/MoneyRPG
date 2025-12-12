package com.gemini910610.moneyrpg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RadarChart extends View
{
    Paint radar_paint, radar_border_paint, value_paint, value_border_paint, text_paint;
    private final float[] angle_90, angle_150, angle_210, angle_270, angle_330, angle_30;
    private Map<String, Integer> max_value = new Hashtable<>();
    private final Map<String, Integer> data = new Hashtable<>();

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
        return new float[]{(float) Math.cos(radian), -(float) Math.sin(radian)};
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

        List<Pair<String, float[]>> ability_angles = List.of(
                new Pair<>("STR", angle_90),
                new Pair<>("DEX", angle_30),
                new Pair<>("AGI", angle_330),
                new Pair<>("VIT", angle_270),
                new Pair<>("WIS", angle_210),
                new Pair<>("LUC", angle_150)
        );

        Path path = new Path();
        for (Pair<String, float[]> pair: ability_angles)
        {
            String ability = pair.first;
            float[] angle = pair.second;

            int value = Objects.requireNonNull(data.getOrDefault(ability, 0));
            int max_value = Objects.requireNonNull(this.max_value.getOrDefault(ability, 100));
            if (ability.equals("STR"))
            {
                moveToPolar(path, radius * value / max_value, angle);
            }
            else
            {
                lineToPolar(path, radius * value / max_value, angle);
            }
        }
        path.close();

        canvas.drawPath(path, value_paint);
        canvas.drawPath(path, value_border_paint);
    }

    private void drawText(Canvas canvas)
    {
        int height = getHeight();
        float radius = getWidth() * 0.5f;

        Map<String, Pair<float[], Float>> ability_text_info = Map.of(
                "STR", new Pair<>(angle_90, -0.02f),
                "DEX", new Pair<>(angle_30, -0.03f),
                "AGI", new Pair<>(angle_330, 0.03f),
                "VIT", new Pair<>(angle_270, 0.02f),
                "WIS", new Pair<>(angle_210, 0.03f),
                "LUC", new Pair<>(angle_150, -0.03f)
        );

        for (Map.Entry<String, Pair<float[], Float>> entry: ability_text_info.entrySet())
        {
            String ability = entry.getKey();
            Pair<float[], Float> pair = entry.getValue();
            float[] angle = pair.first;
            float rate = pair.second;

            textOnPolar(canvas, ability, radius, angle, height * rate);
        }
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
        for (String ability: max_value.keySet())
        {
            int value = Player.getAbility(ability);
            data.put(ability, value);
        }
        invalidate();
    }

    public void setMaxValue(Map<String, Integer> max_value) { this.max_value = max_value; }
}
