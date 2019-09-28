package com.brain.words.puzzle.game.utils.annotation;

import androidx.annotation.IntDef;

import com.brain.words.puzzle.game.utils.DebugWrapperAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(flag = true, value = {
        DebugWrapperAdapter.FLAG_VERIFY_WRAP_POSITION,
        DebugWrapperAdapter.FLAG_VERIFY_UNWRAP_POSITION,
})
@Retention(RetentionPolicy.SOURCE)
public @interface DebugWrapperAdapterSettingFlags {
}
