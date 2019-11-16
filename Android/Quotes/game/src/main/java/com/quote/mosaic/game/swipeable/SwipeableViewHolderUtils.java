package com.quote.mosaic.game.swipeable;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

class SwipeableViewHolderUtils {
    public static View getSwipeableContainerView(RecyclerView.ViewHolder vh) {
        if (vh instanceof SwipeableItemViewHolder) {
            return getSwipeableContainerView((SwipeableItemViewHolder) vh);
        } else {
            return null;
        }
    }

    public static View getSwipeableContainerView(SwipeableItemViewHolder vh) {
        if (vh instanceof RecyclerView.ViewHolder) {
            View containerView = vh.getSwipeableContainerView();
            View itemView = ((RecyclerView.ViewHolder) vh).itemView;

            if (containerView == itemView) {
                throw new IllegalStateException("Inconsistency detected! getSwipeableContainerView() returns itemView");
            }

            return containerView;
        } else {
            return null;
        }
    }
}
