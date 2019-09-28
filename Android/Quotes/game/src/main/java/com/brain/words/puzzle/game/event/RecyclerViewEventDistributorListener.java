package com.brain.words.puzzle.game.event;

public interface RecyclerViewEventDistributorListener {
    void onAddedToEventDistributor(BaseRecyclerViewEventDistributor distributor);
    void onRemovedFromEventDistributor(BaseRecyclerViewEventDistributor distributor);
}
