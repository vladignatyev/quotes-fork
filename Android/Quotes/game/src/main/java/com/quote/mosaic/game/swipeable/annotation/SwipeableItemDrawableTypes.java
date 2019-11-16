package com.quote.mosaic.game.swipeable.annotation;

import androidx.annotation.IntDef;

import com.quote.mosaic.game.swipeable.SwipeableItemConstants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(flag = false, value = {
        SwipeableItemConstants.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND,
        SwipeableItemConstants.DRAWABLE_SWIPE_LEFT_BACKGROUND,
        SwipeableItemConstants.DRAWABLE_SWIPE_UP_BACKGROUND,
        SwipeableItemConstants.DRAWABLE_SWIPE_RIGHT_BACKGROUND,
        SwipeableItemConstants.DRAWABLE_SWIPE_DOWN_BACKGROUND,
})
@Retention(RetentionPolicy.SOURCE)
public @interface SwipeableItemDrawableTypes {
}
