package com.quote.mosaic.game.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adapter path segment
 */
public class AdapterPathSegment {
    /**
     * Adapter
     */
    public final RecyclerView.Adapter adapter;

    /**
     * Tag object
     */
    public final Object tag;

    /**
     * Constructor.
     *
     * @param adapter The adapter
     * @param tag The tag object
     */
    public AdapterPathSegment(@NonNull RecyclerView.Adapter adapter, @Nullable Object tag) {
        this.adapter = adapter;
        this.tag = tag;
    }
}
