package com.brain.words.puzzle.game.draggable;

import com.brain.words.puzzle.game.draggable.annotation.DraggableItemStateFlags;

/**
 * Interface which provides required information for dragging item.
 *
 * Implement this interface on your sub-class of the {@link androidx.recyclerview.widget.RecyclerView.ViewHolder}.
 */
public interface DraggableItemViewHolder {
    /**
     * Sets the state flags value for dragging item
     *
     * @param flags Bitwise OR of these flags;
     *              - {@link DraggableItemConstants#STATE_FLAG_DRAGGING}
     *              - {@link DraggableItemConstants#STATE_FLAG_IS_ACTIVE}
     *              - {@link DraggableItemConstants#STATE_FLAG_IS_IN_RANGE}
     *              - {@link DraggableItemConstants#STATE_FLAG_IS_UPDATED}
     */
    void setDragStateFlags(@DraggableItemStateFlags int flags);

    /**
     * Gets the state flags value for dragging item
     *
     * @return  Bitwise OR of these flags;
     *              - {@link DraggableItemConstants#STATE_FLAG_DRAGGING}
     *              - {@link DraggableItemConstants#STATE_FLAG_IS_ACTIVE}
     *              - {@link DraggableItemConstants#STATE_FLAG_IS_IN_RANGE}
     *              - {@link DraggableItemConstants#STATE_FLAG_IS_UPDATED}
     */
    @DraggableItemStateFlags
    int getDragStateFlags();
}
