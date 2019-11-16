package com.quote.mosaic.game.swipeable;

import androidx.recyclerview.widget.RecyclerView;

import com.quote.mosaic.game.swipeable.action.SwipeResultAction;
import com.quote.mosaic.game.swipeable.annotation.SwipeableItemDrawableTypes;
import com.quote.mosaic.game.swipeable.annotation.SwipeableItemReactions;
import com.quote.mosaic.game.swipeable.annotation.SwipeableItemResults;

public interface SwipeableItemAdapter<T extends RecyclerView.ViewHolder> {

    /**
     * Called when the user is attempt to swipe an item.
     *
     * @param holder The ViewHolder which is associated to item user is attempt to start swiping.
     * @param position The position of the item within the adapter's data set.
     * @param x Touched X position. Relative from the itemView's top-left.
     * @param y Touched Y position. Relative from the itemView's top-left.

     * @return Reaction type. Bitwise OR of these flags;
     *         - {@link SwipeableItemConstants#REACTION_CAN_NOT_SWIPE_LEFT}
     *         - {@link SwipeableItemConstants#REACTION_CAN_NOT_SWIPE_LEFT_WITH_RUBBER_BAND_EFFECT}
     *         - {@link SwipeableItemConstants#REACTION_CAN_SWIPE_LEFT}
     *         - {@link SwipeableItemConstants#REACTION_CAN_NOT_SWIPE_UP}
     *         - {@link SwipeableItemConstants#REACTION_CAN_NOT_SWIPE_UP_WITH_RUBBER_BAND_EFFECT}
     *         - {@link SwipeableItemConstants#REACTION_CAN_SWIPE_UP}
     *         - {@link SwipeableItemConstants#REACTION_CAN_NOT_SWIPE_RIGHT}
     *         - {@link SwipeableItemConstants#REACTION_CAN_NOT_SWIPE_RIGHT_WITH_RUBBER_BAND_EFFECT}
     *         - {@link SwipeableItemConstants#REACTION_CAN_SWIPE_RIGHT}
     *         - {@link SwipeableItemConstants#REACTION_CAN_NOT_SWIPE_DOWN}
     *         - {@link SwipeableItemConstants#REACTION_CAN_NOT_SWIPE_DOWN_WITH_RUBBER_BAND_EFFECT}
     *         - {@link SwipeableItemConstants#REACTION_CAN_SWIPE_DOWN}
     */
    @SwipeableItemReactions
    int onGetSwipeReactionType(T holder, int position, int x, int y);

    /**
     * Called when started swiping an item.
     *
     * Call the {@link RecyclerView.Adapter#notifyDataSetChanged()} method in this callback to get the same behavior with v0.10.x or before.
     *
     * @param holder The ViewHolder that is associated the swiped item.
     * @param position The position of the item within the adapter's data set.
     */
    void onSwipeItemStarted(T holder, int position);

    /**
     * Called when sets background of the swiping item.
     *
     * @param holder The ViewHolder which is associated to the swiping item.
     * @param position The position of the item within the adapter's data set.
     * @param type Background type. One of the
     *          {@link SwipeableItemConstants#DRAWABLE_SWIPE_NEUTRAL_BACKGROUND},
     *          {@link SwipeableItemConstants#DRAWABLE_SWIPE_LEFT_BACKGROUND},
     *          {@link SwipeableItemConstants#DRAWABLE_SWIPE_UP_BACKGROUND},
     *          {@link SwipeableItemConstants#DRAWABLE_SWIPE_RIGHT_BACKGROUND} or
     *          {@link SwipeableItemConstants#DRAWABLE_SWIPE_DOWN_BACKGROUND}.
     */
    void onSetSwipeBackground(T holder, int position, @SwipeableItemDrawableTypes int type);

    /**
     * Called when an item is swiped.
     *
     * *Note that do not change the data set and do not call notifyDataXXX() methods inside of this method.*
     *
     * @param holder The ViewHolder which is associated to the swiped item.
     * @param position The position of the item within the adapter's data set.
     * @param result The result code of user's swipe operation.
     *              {@link SwipeableItemConstants#RESULT_CANCELED},
     *              {@link SwipeableItemConstants#RESULT_SWIPED_LEFT},
     *              {@link SwipeableItemConstants#RESULT_SWIPED_UP},
     *              {@link SwipeableItemConstants#RESULT_SWIPED_RIGHT} or
     *              {@link SwipeableItemConstants#RESULT_SWIPED_DOWN}
     *
     * @return {@link SwipeResultAction} object.
     */
    SwipeResultAction onSwipeItem(T holder, int position, @SwipeableItemResults int result);
}
