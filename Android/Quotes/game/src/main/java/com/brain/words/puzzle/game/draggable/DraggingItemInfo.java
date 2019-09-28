package com.brain.words.puzzle.game.draggable;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;

import com.brain.words.puzzle.game.utils.CustomRecyclerViewUtils;

public class DraggingItemInfo {
    public final int width;
    public final int height;
    public final long id;
    public final int initialItemLeft;
    public final int initialItemTop;
    public final int grabbedPositionX;
    public final int grabbedPositionY;
    public final Rect margins;
    public final int spanSize;

    public DraggingItemInfo(RecyclerView rv, RecyclerView.ViewHolder vh, int touchX, int touchY) {
        width = vh.itemView.getWidth();
        height = vh.itemView.getHeight();
        id = vh.getItemId();
        initialItemLeft = vh.itemView.getLeft();
        initialItemTop = vh.itemView.getTop();
        grabbedPositionX = touchX - initialItemLeft;
        grabbedPositionY = touchY - initialItemTop;
        margins = new Rect();
        CustomRecyclerViewUtils.getLayoutMargins(vh.itemView, margins);
        spanSize = CustomRecyclerViewUtils.getSpanSize(vh);
    }

    private DraggingItemInfo(DraggingItemInfo info, RecyclerView.ViewHolder vh) {
        id = info.id;
        width = vh.itemView.getWidth();
        height = vh.itemView.getHeight();
        margins = new Rect(info.margins);
        spanSize = CustomRecyclerViewUtils.getSpanSize(vh);
        initialItemLeft = info.initialItemLeft;
        initialItemTop = info.initialItemTop;

        final float pcx = info.width * 0.5f;
        final float pcy = info.height * 0.5f;
        final float cx = width * 0.5f;
        final float cy = height * 0.5f;

        final float centerOffsetX = info.grabbedPositionX - pcx;
        final float centerOffsetY = info.grabbedPositionY - pcy;

        final float gpx = cx + centerOffsetX;
        final float gpy = cy + centerOffsetY;

        grabbedPositionX = (int)((gpx >= 0 && gpx < width) ? gpx : cx);
        grabbedPositionY = (int)((gpy >= 0 && gpy < height) ? gpy : cy);
    }

    public static DraggingItemInfo createWithNewView(DraggingItemInfo info, RecyclerView.ViewHolder vh) {
        return new DraggingItemInfo(info, vh);
    }
}
