package com.quote.mosaic.game.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quote.mosaic.game.adapter.WrappedAdapter;
import com.quote.mosaic.game.adapter.WrapperAdapter;

public class WrappedAdapterUtils {
    private WrappedAdapterUtils() {
    }

    @SuppressWarnings("unchecked")
    public static void invokeOnViewRecycled(@NonNull RecyclerView.Adapter adapter, @NonNull RecyclerView.ViewHolder holder, int viewType) {
        if (adapter instanceof WrapperAdapter) {
            ((WrapperAdapter) adapter).onViewRecycled(holder, viewType);
        } else {
            adapter.onViewRecycled(holder);
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean invokeOnFailedToRecycleView(@NonNull RecyclerView.Adapter adapter, @NonNull RecyclerView.ViewHolder holder, int viewType) {
        if (adapter instanceof WrappedAdapter) {
            return ((WrappedAdapter) adapter).onFailedToRecycleView(holder, viewType);
        } else {
            return adapter.onFailedToRecycleView(holder);
        }
    }

    @SuppressWarnings("unchecked")
    public static void invokeOnViewAttachedToWindow(@NonNull RecyclerView.Adapter adapter, @NonNull RecyclerView.ViewHolder holder, int viewType) {
        if (adapter instanceof WrappedAdapter) {
            ((WrappedAdapter) adapter).onViewAttachedToWindow(holder, viewType);
        } else {
            adapter.onViewAttachedToWindow(holder);
        }
    }

    @SuppressWarnings("unchecked")
    public static void invokeOnViewDetachedFromWindow(RecyclerView.Adapter adapter, RecyclerView.ViewHolder holder, int viewType) {
        if (adapter instanceof WrappedAdapter) {
            ((WrappedAdapter) adapter).onViewDetachedFromWindow(holder, viewType);
        } else {
            adapter.onViewDetachedFromWindow(holder);
        }
    }
}
