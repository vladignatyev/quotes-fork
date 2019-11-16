package com.quote.mosaic.game.expandable;

import androidx.recyclerview.widget.RecyclerView;

import com.quote.mosaic.game.swipeable.action.SwipeResultAction;

class ExpandableSwipeableItemInternalUtils {
    private ExpandableSwipeableItemInternalUtils() {
    }

    @SuppressWarnings("unchecked")
    public static SwipeResultAction invokeOnSwipeItem(
            BaseExpandableSwipeableItemAdapter<?, ?> adapter, RecyclerView.ViewHolder holder,
            int groupPosition, int childPosition, int result) {

        if (childPosition == RecyclerView.NO_POSITION) {
            return ((ExpandableSwipeableItemAdapter) adapter).onSwipeGroupItem(holder, groupPosition, result);
        } else {
            return ((ExpandableSwipeableItemAdapter) adapter).onSwipeChildItem(holder, groupPosition, childPosition, result);
        }
    }
}
