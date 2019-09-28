package com.brain.words.puzzle.game.composedadapter;

import android.util.SparseIntArray;

import com.brain.words.puzzle.game.adapter.ItemViewTypeComposer;

class SegmentedViewTypeTranslator {
    private SparseIntArray mWrapSegmentMap = new SparseIntArray();
    private SparseIntArray mUnwrapSegmentMap = new SparseIntArray();

    public SegmentedViewTypeTranslator() {
    }

    public int wrapItemViewType(int segment, int viewType) {
        final int packedSegments = (segment << 16) | ItemViewTypeComposer.extractSegmentPart(viewType);
        final int flattenSegments;

        final int index = mWrapSegmentMap.indexOfKey(packedSegments);

        if (index >= 0) {
            flattenSegments = mWrapSegmentMap.valueAt(index);
        } else {
            flattenSegments = mWrapSegmentMap.size() + 1;

            if (flattenSegments > ItemViewTypeComposer.MAX_SEGMENT) {
                throw new IllegalStateException("Failed to allocate a new wrapped view type.");
            }

            mWrapSegmentMap.put(packedSegments, flattenSegments);
            mUnwrapSegmentMap.put(flattenSegments, packedSegments);
        }

        return ItemViewTypeComposer.composeSegment(flattenSegments, viewType);
    }

    public long unwrapViewType(int viewType) {
        final int flattenSegment = ItemViewTypeComposer.extractSegmentPart(viewType);
        final int index = mUnwrapSegmentMap.indexOfKey(flattenSegment);

        if (index < 0) {
            throw new IllegalStateException("Corresponding wrapped view type is not found!");
        }

        final int packedSegments = mUnwrapSegmentMap.valueAt(index);
        final long packedViewType = (((long) packedSegments) << 32) | (((long) viewType) & 0xFFFFFFFFL);

        return packedViewType;
    }

    public static int extractWrappedViewType(long packedViewType) {
        final int segment = (int)(packedViewType >>> 32) & 0xFFFF;
        final int viewType = (int) (packedViewType & 0xFFFFFFFFL);
        return ItemViewTypeComposer.composeSegment(segment, viewType);
    }

    public static int extractWrapperSegment(long packedViewType) {
        return (int) ((packedViewType >>> 48) & 0xFFFF);
    }
}
