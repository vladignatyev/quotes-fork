package com.quote.mosaic.game.adapter;

import androidx.recyclerview.widget.RecyclerView;

/**
 * The result object of {@link WrapperAdapter#unwrapPosition(UnwrapPositionResult, int)}.
 * This class is mutable that is why it is intended to reuse the same instance multiple times to avoid object creations.
 */
public class UnwrapPositionResult {
    /**
     * Adapter
     */
    public RecyclerView.Adapter adapter;

    /**
     * Tag object
     *
     * <p>The tag object can be used to identify the path.
     * (e.g.: wrapper adapter can use a same child adapter multiple times)</p>
     */
    public Object tag;

    /**
     * Unwrapped position
     */
    public int position = RecyclerView.NO_POSITION;

    /**
     * Clear fields
     */
    public void clear() {
        adapter = null;
        tag = null;
        position = RecyclerView.NO_POSITION;
    }

    /**
     * Returns the result is valid.
     * @return True if the result object indicates valid position. Otherwise, false.
     */
    public boolean isValid() {
        return (adapter != null) && (position != RecyclerView.NO_POSITION);
    }
}
