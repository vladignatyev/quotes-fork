package com.quote.mosaic.game.event;

public interface RecyclerViewEventDistributorListener {
    void onAddedToEventDistributor(BaseRecyclerViewEventDistributor distributor);
    void onRemovedFromEventDistributor(BaseRecyclerViewEventDistributor distributor);
}
