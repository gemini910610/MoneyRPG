package com.gemini910610.moneyrpg;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.function.Consumer;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder>
{
    private final ArrayList<RecordData> datum;
    private final Consumer<RecordData> onLongClicked;

    public RecordAdapter(ArrayList<RecordData> datum, Consumer<RecordData> onLongClicked)
    {
        this.datum = datum;
        this.onLongClicked = onLongClicked;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view, onLongClicked);
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

    public void setItems(ArrayList<RecordData> datum)
    {
        this.datum.clear();
        this.datum.addAll(datum);
        notifyDataSetChanged();
    }

    public void updateItem(RecordData old_data, RecordData new_data)
    {
        int old_position = indexOf(old_data);
        datum.remove(old_position);
        notifyItemRemoved(old_position);

        addItem(new_data);
    }

    public void removeItem(RecordData data)
    {
        int position = indexOf(data);
        datum.remove(data);
        notifyItemRemoved(position);
    }

    private int findInsertPosition(RecordData new_data)
    {
        for (int i = 0; i < datum.size(); i++)
        {
            String new_date = new_data.date;
            RecordData data = datum.get(i);
            String old_date = data.date;
            if (new_date.compareTo(old_date) > 0)
            {
                return i;
            }
        }
        return datum.size();
    }

    private int indexOf(RecordData data)
    {
        long id = data.id;
        for (int i = 0; i < datum.size(); i++)
        {
            if (datum.get(i).id == id)
            {
                return i;
            }
        }
        return -1;
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder
    {
        private final View view;
        private final TextView title_text, date_text, money_text;

        private final Consumer<RecordData> onLongClick;

        public RecordViewHolder(View view, Consumer<RecordData> onLongClicked)
        {
            super(view);

            this.view = view;
            title_text = view.findViewById(R.id.record_title);
            date_text = view.findViewById(R.id.date_text);
            money_text = view.findViewById(R.id.money_text);

            this.onLongClick = onLongClicked;
        }

        public void setData(RecordData data)
        {
            title_text.setText(data.title);
            date_text.setText(data.date);
            money_text.setText(String.valueOf(data.money));

            int color = data.is_earn ? Color.GREEN : Color.YELLOW;
            ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(color));

            view.setOnLongClickListener(view -> {
                onLongClick.accept(data);
                return true;
            });
        }
    }

    public static class RecordData
    {
        private long id;
        public String title;
        public String date;
        public int money;
        public boolean is_earn;

        public RecordData(String title, String date, int money, boolean is_earn)
        {
            this.title = title;
            this.date = date;
            this.money = money;
            this.is_earn = is_earn;
        }

        public long getID() { return id; }

        public void setID(long id) { this.id = id; }
    }
}
