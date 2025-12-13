package com.gemini910610.moneyrpg;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder>
{
    private final ArrayList<RecordData> datum;

    public RecordAdapter(ArrayList<RecordData> datum)
    {
        this.datum = datum;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position)
    {
        holder.setData(datum.get(position));
    }

    @Override
    public int getItemCount()
    {
        return datum.size();
    }

    public void addItem(RecordData data)
    {
        int position = findInsertPosition(data);
        datum.add(position, data);
        notifyItemInserted(position);
    }

    private int findInsertPosition(RecordData new_data)
    {
        LocalDate new_date = LocalDate.parse(new_data.date);

        for (int i = 0; i < datum.size(); i++)
        {
            LocalDate date = LocalDate.parse(datum.get(i).date);
            if (new_date.isAfter(date))
            {
                return i;
            }
        }
        return datum.size();
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder
    {
        private final View view;
        private final TextView title_text, date_text, money_text;

        public RecordViewHolder(View view)
        {
            super(view);

            this.view = view;
            title_text = view.findViewById(R.id.record_title);
            date_text = view.findViewById(R.id.date_text);
            money_text = view.findViewById(R.id.money_text);
        }

        public void setData(RecordData data)
        {
            title_text.setText(data.title);
            date_text.setText(data.date);
            money_text.setText(String.valueOf(data.money));

            Drawable drawable = view.getBackground();
            drawable.setColorFilter(data.is_earn ? Color.GREEN : Color.YELLOW, PorterDuff.Mode.SCREEN);
            view.setBackground(drawable);

            view.setOnLongClickListener(view -> {
                // reset data
                return true;
            });
        }
    }

    public static class RecordData
    {
        private int id;
        public String title;
        public String date;
        public int money;
        public boolean is_earn;

        public RecordData(int id, String title, String date, int money, boolean is_earn)
        {
            this.id = id;
            this.title = title;
            this.date = date;
            this.money = money;
            this.is_earn = is_earn;
        }
    }
}
