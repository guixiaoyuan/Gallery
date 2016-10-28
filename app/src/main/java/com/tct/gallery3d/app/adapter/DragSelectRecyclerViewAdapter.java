package com.tct.gallery3d.app.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

public abstract class DragSelectRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends CursorRecyclerAdapter<RecyclerView.ViewHolder> {

    public DragSelectRecyclerViewAdapter(Cursor cursor) {
        super(cursor);
    }

    public DragSelectRecyclerViewAdapter() {
        super(null);
    }

    public boolean isIndexSelectable(int index) {
        return true;
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        holder.itemView.setTag(holder);
    }

    public abstract void selectRange(int from, int to, int min, int max);
}