package com.quote.mosaic.game.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * An RecyclerView adapter which wraps another adapter(s).
 */
public interface WrapperAdapter<VH extends RecyclerView.ViewHolder> extends WrappedAdapter<VH> {

    /**
     * Unwraps position. This method converts the passed wrapped position to child adapter's position.
     *
     * @param dest     The destination
     * @param position The wrapped position to be unwrapped
     */
    void unwrapPosition(@NonNull UnwrapPositionResult dest, int position);

    /**
     * Wraps position. This method converts the passed child adapter's position to wrapped position.
     *
     * @param pathSegment The path segment of the child adapter
     * @param position    The child adapter's position to be wrapped
     * @return Wrapped position
     */
    int wrapPosition(@NonNull AdapterPathSegment pathSegment, int position);

    /**
     * Gets wrapped children adapters.
     *
     * @param adapters The destination
     */
    void getWrappedAdapters(@NonNull List<RecyclerView.Adapter> adapters);

    /**
     * Releases bounded resources.
     */
    void release();
}
