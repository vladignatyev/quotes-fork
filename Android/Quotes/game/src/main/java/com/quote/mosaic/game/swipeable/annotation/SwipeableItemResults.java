package com.quote.mosaic.game.swipeable.annotation;


import androidx.annotation.IntDef;

import com.quote.mosaic.game.swipeable.SwipeableItemConstants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(flag = false, value = {
        SwipeableItemConstants.RESULT_NONE,
        SwipeableItemConstants.RESULT_CANCELED,
        SwipeableItemConstants.RESULT_SWIPED_LEFT,
        SwipeableItemConstants.RESULT_SWIPED_UP,
        SwipeableItemConstants.RESULT_SWIPED_RIGHT,
        SwipeableItemConstants.RESULT_SWIPED_DOWN,
})
@Retention(RetentionPolicy.SOURCE)
public @interface SwipeableItemResults {
}
