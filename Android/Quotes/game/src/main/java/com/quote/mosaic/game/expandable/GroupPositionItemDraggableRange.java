package com.quote.mosaic.game.expandable;

import com.quote.mosaic.game.draggable.ItemDraggableRange;

public class GroupPositionItemDraggableRange extends ItemDraggableRange {
    public GroupPositionItemDraggableRange(int start, int end) {
        super(start, end);
    }

    protected String getClassName() {
        return "GroupPositionItemDraggableRange";
    }
}
