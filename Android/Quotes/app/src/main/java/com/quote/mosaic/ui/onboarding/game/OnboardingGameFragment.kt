package com.quote.mosaic.ui.onboarding.game

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.Explode
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.quote.mosaic.data.UserManager
import com.quote.mosaic.game.animator.DraggableItemAnimator
import com.quote.mosaic.game.draggable.RecyclerViewDragDropManager
import com.quote.mosaic.game.utils.WrapperAdapterUtils
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.common.parentAs
import com.quote.mosaic.core.ui.data.ExampleDataProvider
import com.quote.mosaic.databinding.OnboardingGameFragmentBinding
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OnboardingGameFragment : AppFragment() {

    @Inject lateinit var userManager: UserManager

    private lateinit var listener: Listener

    private var gameWrapperAdapter: RecyclerView.Adapter<*>? = null
    private var gameManager: RecyclerViewDragDropManager? = null

    private lateinit var dataProvider: ExampleDataProvider
    private lateinit var gameAdapter: OnboardingGameAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentAs()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<OnboardingGameFragmentBinding>(
        inflater, R.layout.onboarding_game_fragment, container, false
    ).apply {
        listener = this@OnboardingGameFragment.listener
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initGame()
    }

    override fun onResume() {
        super.onResume()
        binding().welcomeMsg.text = getString(R.string.onboarding_label_welcome_aboard, userManager.getUserName())
    }

    override fun onPause() {
        gameManager?.cancelDrag()
        super.onPause()
    }

    override fun onDestroyView() {
        gameManager?.release()
        WrapperAdapterUtils.releaseAll(gameWrapperAdapter)
        super.onDestroyView()
    }

    private fun initGame() {
        if (gameWrapperAdapter != null) return

        dataProvider = ExampleDataProvider()
        dataProvider.addQuote(listOf("Через", "тернии", "к", "звездам"))
        gameAdapter = OnboardingGameAdapter(R.color.darkPurple) { onSuccess() }
        gameAdapter.setDataProvider(dataProvider)

        initGameManager()

        binding().gameRv.run {
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            gameWrapperAdapter = gameManager?.createWrappedAdapter(gameAdapter)
            adapter = gameWrapperAdapter
            itemAnimator = DraggableItemAnimator()
            setHasFixedSize(false)
        }
    }

    private fun initGameManager() {
        gameManager = RecyclerViewDragDropManager().apply {
            setInitiateOnLongPress(true)
            setInitiateOnMove(true)
            setLongPressTimeout(100)
            draggingItemAlpha = 0.8f
            draggingItemScale = 1.3f
            draggingItemRotation = 15.0f
            attachRecyclerView(binding().gameRv)
        }
    }

    private fun onSuccess() {
        showKonfetti()
        enableDisableViewGroup(binding().container, false)
        gameManager?.release()
        Completable
            .timer(1700, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                removeAll()
                WrapperAdapterUtils.releaseAll(gameWrapperAdapter)
                showSuccessDialog()
            }
            .untilStopped()
    }

    private fun showKonfetti() {
        binding().viewKonfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED, Color.BLUE, Color.CYAN)
            .setDirection(0.0, 359.0)
            .setSpeed(4f, 7f)
            .setFadeOutEnabled(true)
            .setTimeToLive(10000L)
            .addShapes(Shape.RECT, Shape.CIRCLE)
            .addSizes(Size(12))
            .setPosition(-50f, binding().viewKonfetti.width + 500f, -50f, -50f)
            .streamFor(300, 5000L)
    }

    private fun enableDisableViewGroup(viewGroup: ViewGroup, enabled: Boolean) {
        val childCount = viewGroup.childCount
        for (i in 0 until childCount) {
            val view = viewGroup.getChildAt(i)
            view.isEnabled = enabled
            if (view is ViewGroup) {
                enableDisableViewGroup(view, enabled)
            }
        }
    }

    private fun removeAll() {
        // save rect of view in screen coordinates
        val viewRect = Rect()
        binding().gameRv.getGlobalVisibleRect(viewRect)

        // create Explode transition with epicenter
        val explode: Transition = Explode()
        explode.epicenterCallback = object : Transition.EpicenterCallback() {
            override fun onGetEpicenter(transition: Transition): Rect {
                return viewRect
            }
        }
        explode.duration = 1000
        TransitionManager.beginDelayedTransition(binding().gameRv, explode)

        // remove all views from Recycler View
        binding().gameRv.adapter = null
    }

    private fun showSuccessDialog() {
        listener.onGameCompleted()
    }

    interface Listener {

        fun onGameCompleted()
    }

    private fun binding() = viewBinding<OnboardingGameFragmentBinding>()

    companion object {
        fun newInstance() = OnboardingGameFragment()
    }
}