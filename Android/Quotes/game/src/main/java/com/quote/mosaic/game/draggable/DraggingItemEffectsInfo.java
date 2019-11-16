package com.quote.mosaic.game.draggable;

import android.view.animation.Interpolator;

class DraggingItemEffectsInfo {
    int durationMillis;
    float scale = 1.0f;
    float rotation = 0.0f;
    float alpha = 1.0f;
    Interpolator scaleInterpolator = null;
    Interpolator rotationInterpolator = null;
    Interpolator alphaInterpolator = null;
}
