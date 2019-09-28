package com.brain.words.puzzle.game.animator;

import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemAnimator for Draggable item. This animator is required to work animations properly on drop an item.
 */
public class DraggableItemAnimator extends RefactoredDefaultItemAnimator {

    @Override
    protected void onSetup() {
        super.onSetup();
        super.setSupportsChangeAnimations(false);
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        if (oldHolder == newHolder && fromX == toX && fromY == toY) {
            // WORKAROUND: Skip animateChange() for the dropped item. Should be implemented better approach.
            dispatchChangeFinished(oldHolder, true);
            return false;
        }

        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
    }
}
