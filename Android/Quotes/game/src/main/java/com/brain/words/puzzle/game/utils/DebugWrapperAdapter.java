package com.brain.words.puzzle.game.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brain.words.puzzle.game.adapter.AdapterPathSegment;
import com.brain.words.puzzle.game.adapter.SimpleWrapperAdapter;
import com.brain.words.puzzle.game.adapter.UnwrapPositionResult;
import com.brain.words.puzzle.game.adapter.WrapperAdapter;
import com.brain.words.puzzle.game.utils.annotation.DebugWrapperAdapterSettingFlags;

/**
 * A wrapper adapter for debugging purpose.
 */
public class DebugWrapperAdapter extends SimpleWrapperAdapter<RecyclerView.ViewHolder> {
    public static final int FLAG_VERIFY_WRAP_POSITION = 1;
    public static final int FLAG_VERIFY_UNWRAP_POSITION = 1 << 1;

    public static final int FLAGS_ALL_DEBUG_FEATURES = FLAG_VERIFY_WRAP_POSITION | FLAG_VERIFY_UNWRAP_POSITION;

    private int mFlags = FLAGS_ALL_DEBUG_FEATURES;

    /**
     * Constructor
     *
     * @param adapter The debug target adapter
     */
    @SuppressWarnings("unchecked")
    public DebugWrapperAdapter(@NonNull RecyclerView.Adapter adapter) {
        super(adapter);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This class also invokes {@link WrapperAdapter#unwrapPosition(UnwrapPositionResult, int)} of the child adapter when
     * verify option is enabled.
     * If inconsistency has been detected, <code>IllegalStateException</code> will be thrown.
     */
    @Override
    public int wrapPosition(@NonNull AdapterPathSegment pathSegment, int position) {
        if (((mFlags & FLAG_VERIFY_WRAP_POSITION) != 0) && (getWrappedAdapter() instanceof WrapperAdapter)) {
            final WrapperAdapter wrapperAdapter = (WrapperAdapter) getWrappedAdapter();

            final int wrappedPosition = wrapperAdapter.wrapPosition(pathSegment, position);

            if (wrappedPosition != RecyclerView.NO_POSITION) {
                final UnwrapPositionResult tmpResult = new UnwrapPositionResult();
                wrapperAdapter.unwrapPosition(tmpResult, wrappedPosition);

                if (tmpResult.position != position) {
                    final String wrappedClassName = getWrappedAdapter().getClass().getSimpleName();
                    throw new IllegalStateException(
                            "Found a WrapperAdapter implementation issue while executing wrapPosition(): " + wrappedClassName + "\n" +
                                    "wrapPosition(" + position + ") returns " + wrappedPosition + ", but " +
                                    "unwrapPosition(" + wrappedPosition + ") returns " + tmpResult.position);
                }
            }
        }

        return super.wrapPosition(pathSegment, position);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This class also invokes {@link WrapperAdapter#wrapPosition(AdapterPathSegment, int)} of the child adapter when
     * verify option is enabled.
     * If inconsistency has been detected, <code>IllegalStateException</code> will be thrown.
     */
    @Override
    public void unwrapPosition(@NonNull UnwrapPositionResult dest, int position) {
        if (((mFlags & FLAG_VERIFY_UNWRAP_POSITION) != 0) && (getWrappedAdapter() instanceof WrapperAdapter)) {
            final WrapperAdapter wrapperAdapter = (WrapperAdapter) getWrappedAdapter();

            final UnwrapPositionResult tmpResult = new UnwrapPositionResult();
            wrapperAdapter.unwrapPosition(tmpResult, position);

            if (tmpResult.isValid()) {
                final AdapterPathSegment segment = new AdapterPathSegment(tmpResult.adapter, tmpResult.tag);
                final int reWrappedPosition = wrapperAdapter.wrapPosition(segment, tmpResult.position);

                if (position != reWrappedPosition) {
                    final String wrappedClassName = getWrappedAdapter().getClass().getSimpleName();
                    throw new IllegalStateException(
                            "Found a WrapperAdapter implementation issue while executing unwrapPosition(): " + wrappedClassName + "\n" +
                                    "unwrapPosition(" + position + ") returns " + tmpResult.position + ", but " +
                                    "wrapPosition(" + tmpResult.position + ") returns " + reWrappedPosition);
                }
            }
        }

        super.unwrapPosition(dest, position);
    }

    /**
     * Sets setting flags.
     *
     * @param flags Bit-ORof debug feature flags.
     */
    public void setSettingFlags(@DebugWrapperAdapterSettingFlags int flags) {
        mFlags = flags;
    }

    /**
     * Returns current setting flags.
     *
     * @return Bit-OR of debug feature flags.
     */
    @DebugWrapperAdapterSettingFlags
    public int getSettingFlags() {
        return mFlags;
    }
}
