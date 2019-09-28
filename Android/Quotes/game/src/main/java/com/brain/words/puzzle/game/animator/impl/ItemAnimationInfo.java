package com.brain.words.puzzle.game.animator.impl;

import androidx.recyclerview.widget.RecyclerView;

public abstract class ItemAnimationInfo {
    public abstract RecyclerView.ViewHolder getAvailableViewHolder();

    public abstract void clear(RecyclerView.ViewHolder holder);
}

