package com.quote.mosaic.game.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter path. This class represents how nested {@link WrapperAdapter}s route items.
 */
public class AdapterPath {
    private List<AdapterPathSegment> mSegments = new ArrayList<>();

    /**
     * Constructor.
     */
    public AdapterPath() {
    }

    /**
     * Appends path segment.
     *
     * @param wrapResult The result object returned by {@link WrapperAdapter#wrapPosition(AdapterPathSegment, int)}.
     * @return {@link AdapterPath} instance itself.
     */
    public AdapterPath append(@NonNull UnwrapPositionResult wrapResult) {
        return append(wrapResult.adapter, wrapResult.tag);
    }

    /**
     * Appends path segment.
     *
     * @param adapter The adapter
     * @param tag The tag object
     * @return {@link AdapterPath} instance itself.
     */
    public AdapterPath append(@NonNull RecyclerView.Adapter adapter, @Nullable Object tag) {
       return append(new AdapterPathSegment(adapter, tag));
    }

    /**
     * Appends path segment.
     *
     * @param segment The path segment
     * @return {@link AdapterPath} instance itself.
     */
    public AdapterPath append(@NonNull AdapterPathSegment segment) {
        mSegments.add(segment);
        return this;
    }

    /**
     * Clears path segments.
     *
     * @return {@link AdapterPath} instance itself.
     */
    public AdapterPath clear() {
        mSegments.clear();
        return this;
    }

    /**
     * Gets whether the path is empty.
     *
     * @return True if the path is empty. Otherwise, false.
     */
    public boolean isEmpty() {
        return mSegments.isEmpty();
    }

    /**
     * Gets path segments.
     *
     * @return The collection of path segments.
     */
    public List<AdapterPathSegment> segments() {
        return mSegments;
    }

    /**
     * Retrieves the first path segment.
     *
     * @return The first path segment.
     */
    public AdapterPathSegment firstSegment() {
        return (!mSegments.isEmpty()) ? (mSegments.get(0)) : null;
    }

    /**
     * Retrieves the last path segment.
     *
     * @return THe last path segment.
     */
    public AdapterPathSegment lastSegment() {
        return (!mSegments.isEmpty()) ? (mSegments.get(mSegments.size() - 1)) : null;
    }
}
