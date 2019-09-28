package com.brain.words.puzzle.game.expandable;

import androidx.recyclerview.widget.RecyclerView;

import com.brain.words.puzzle.game.swipeable.RecyclerViewSwipeManager;
import com.brain.words.puzzle.game.swipeable.action.SwipeResultAction;

public interface ExpandableSwipeableItemAdapter<GVH extends RecyclerView.ViewHolder, CVH extends RecyclerView.ViewHolder>
    extends BaseExpandableSwipeableItemAdapter<GVH, CVH> {

    /**
     * Called when group item is swiped.
     *
     * *Note that do not change data set and do not call notifyDataXXX() methods inside of this method.*
     *
     * @param holder The ViewHolder which is associated to the swiped item.
     * @param groupPosition Group position.
     * @param result The result code of user's swipe operation.
     *              {@link RecyclerViewSwipeManager#RESULT_CANCELED},
     *              {@link RecyclerViewSwipeManager#RESULT_SWIPED_LEFT},
     *              {@link RecyclerViewSwipeManager#RESULT_SWIPED_UP},
     *              {@link RecyclerViewSwipeManager#RESULT_SWIPED_RIGHT} or
     *              {@link RecyclerViewSwipeManager#RESULT_SWIPED_DOWN}
     *
     * @return {@link SwipeResultAction} object.
     */
    SwipeResultAction onSwipeGroupItem(GVH holder, int groupPosition, int result);

    /**
     * Called when child item is swiped.
     *
     * *Note that do not change data set and do not call notifyDataXXX() methods inside of this method.*
     *
     * @param holder The ViewHolder which is associated to the swiped item.
     * @param groupPosition Group position.
     * @param childPosition Child position.
     * @param result The result code of user's swipe operation.
     *              {@link RecyclerViewSwipeManager#RESULT_CANCELED},
     *              {@link RecyclerViewSwipeManager#RESULT_SWIPED_LEFT},
     *              {@link RecyclerViewSwipeManager#RESULT_SWIPED_UP},
     *              {@link RecyclerViewSwipeManager#RESULT_SWIPED_RIGHT} or
     *              {@link RecyclerViewSwipeManager#RESULT_SWIPED_DOWN}
     *
     * @return {@link SwipeResultAction} object.
     */
    SwipeResultAction onSwipeChildItem(CVH holder, int groupPosition, int childPosition, int result);
}
