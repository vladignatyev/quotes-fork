package com.brain.words.puzzle.game.utils;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.brain.words.puzzle.game.draggable.DraggableItemViewHolder;
import com.brain.words.puzzle.game.draggable.annotation.DraggableItemStateFlags;

public abstract class AbstractDraggableItemViewHolder extends RecyclerView.ViewHolder implements DraggableItemViewHolder {
    @DraggableItemStateFlags
    private int mDragStateFlags;

    public AbstractDraggableItemViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setDragStateFlags(@DraggableItemStateFlags int flags) {
        mDragStateFlags = flags;
    }

    @Override
    @DraggableItemStateFlags
    public int getDragStateFlags() {
        return mDragStateFlags;
    }
}
