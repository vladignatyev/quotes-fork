package com.quote.mosaic.game.utils;

import androidx.recyclerview.widget.RecyclerView;

import com.quote.mosaic.game.adapter.SimpleWrapperAdapter;

/**
 * This class exists just for compatibility purpose and it will be deprecated soon. Use {@link SimpleWrapperAdapter} directly.
 * @param <VH> ViewHolder type
 */
// @Deprecated
public class BaseWrapperAdapter<VH extends RecyclerView.ViewHolder> extends SimpleWrapperAdapter<VH> {
    public BaseWrapperAdapter(RecyclerView.Adapter<VH> adapter) {
        super(adapter);
    }
}
