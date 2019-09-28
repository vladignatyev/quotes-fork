package com.brain.words.puzzle.game.expandable;


import com.brain.words.puzzle.game.draggable.ItemDraggableRange;

public class ChildPositionItemDraggableRange extends ItemDraggableRange {
    public ChildPositionItemDraggableRange(int start, int end) {
        super(start, end);
    }

    protected String getClassName() {
        return "ChildPositionItemDraggableRange";
    }
}
