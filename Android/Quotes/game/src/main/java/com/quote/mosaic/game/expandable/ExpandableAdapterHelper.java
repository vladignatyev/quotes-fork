package com.quote.mosaic.game.expandable;

import androidx.recyclerview.widget.RecyclerView;

class ExpandableAdapterHelper {
    public static final long NO_EXPANDABLE_POSITION = 0xffffffffffffffffL;

    private static final long LOWER_32BIT_MASK = 0x00000000ffffffffL;
    private static final long LOWER_31BIT_MASK = 0x000000007fffffffL;

    public static long getPackedPositionForChild(int groupPosition, int childPosition) {
        return ((long) childPosition << 32) | (groupPosition & LOWER_32BIT_MASK);
    }

    public static long getPackedPositionForGroup(int groupPosition) {
        return ((long) RecyclerView.NO_POSITION << 32) | (groupPosition & LOWER_32BIT_MASK);
    }

    public static int getPackedPositionChild(long packedPosition) {
        return (int) (packedPosition >>> 32);
    }

    public static int getPackedPositionGroup(long packedPosition) {
        return (int) (packedPosition & LOWER_32BIT_MASK);
    }

    private ExpandableAdapterHelper() {
    }
}
