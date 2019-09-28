package com.brain.words.puzzle.game.expandable;

import androidx.recyclerview.widget.RecyclerView;

import com.brain.words.puzzle.game.expandable.annotation.ExpandableItemStateFlags;

/**
 * <p>Interface which provides required information for expanding item.</p>
 * <p>Implement this interface on your sub-class of the {@link RecyclerView.ViewHolder}.</p>
 */
public interface ExpandableItemViewHolder {
    /**
     * Sets the state flags value for expanding item
     *
     * @param flags Bitwise OR of these flags;
     *              - {@link ExpandableItemConstants#STATE_FLAG_IS_GROUP}
     *              - {@link ExpandableItemConstants#STATE_FLAG_IS_CHILD}
     *              - {@link ExpandableItemConstants#STATE_FLAG_IS_EXPANDED}
     *              - {@link ExpandableItemConstants#STATE_FLAG_IS_UPDATED}
     */
    void setExpandStateFlags(@ExpandableItemStateFlags int flags);

    /**
     * Gets the state flags value for expanding item
     *
     * @return Bitwise OR of these flags;
     * - {@link ExpandableItemConstants#STATE_FLAG_IS_GROUP}
     * - {@link ExpandableItemConstants#STATE_FLAG_IS_CHILD}
     * - {@link ExpandableItemConstants#STATE_FLAG_IS_EXPANDED}
     * - {@link ExpandableItemConstants#STATE_FLAG_IS_UPDATED}
     */
    @ExpandableItemStateFlags
    int getExpandStateFlags();
}
