package com.quote.mosaic.ui.game

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.Explode
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.core.common.args
import com.quote.mosaic.core.ui.data.ExampleDataProvider
import com.quote.mosaic.databinding.GameFragmentBinding
import com.quote.mosaic.game.animator.DraggableItemAnimator
import com.quote.mosaic.game.draggable.RecyclerViewDragDropManager
import com.quote.mosaic.game.utils.WrapperAdapterUtils
import com.quote.mosaic.ui.common.dialog.DialogBuilder
import com.quote.mosaic.ui.onboarding.game.OnboardingGameAdapter
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GameFragment : AppFragment() {

    @Inject
    lateinit var billingManager: BillingManager

    private val vm: GameViewModel by activityViewModels()

    private lateinit var dataProvider: ExampleDataProvider
    private lateinit var gameAdapter: OnboardingGameAdapter

    private var gameWrapperAdapter: RecyclerView.Adapter<*>? = null
    private var gameManager: RecyclerViewDragDropManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.setUp(args().getInt(GameActivity.SELECTED_CATEGORY_ID))
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<GameFragmentBinding>(
        inflater, R.layout.game_fragment, container, false
    ).apply {
        fragment = this@GameFragment
        viewModel = vm
        this.container.setBackgroundColor(
            ContextCompat.getColor(requireContext(), userPreferences.getBackgroundColor())
        )
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.onNextLevelReceived.subscribe { mixedQuote ->
            val correctQuote = vm.state.currentQuote.get()?.splitted!!
            gameWrapperAdapter = null
            manageViewGroupTapable(binding().container, true)
            startNewLevel(correctQuote, mixedQuote)
        }.untilStopped()

        vm.state.levelCompletedTrigger.subscribe {
            showSuccessDialog()
        }.untilStopped()

        vm.state.showHintTriggered.subscribe {
            onHintReceived(it)
        }.untilStopped()

        vm.state.skipLevelTriggered.subscribe {
            onLevelCompleted()
        }.untilStopped()
    }

    private fun showSuccessDialog() {
        DialogBuilder.showGameSuccessDialog(requireContext(), vm) { successDialog ->
            successDialog.dismiss()
            vm.reset()

            if (vm.state.isLastQuote.get()) {
                requireActivity().onBackPressed()
            } else {
                vm.loadLevel()

                if (vm.doubleUpPossible()) {
                    Completable.timer(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .subscribe {
                            DialogBuilder.showDoubleUpDialog(requireContext(), vm) {
                                vm.showDoubleUpVideo(requireActivity())
                                it.dismiss()
                            }
                        }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        billingManager.warmUp()
    }

    override fun onPause() {
        vm.setCurrentVariant(dataProvider.getCurrentQuote())
        gameManager?.cancelDrag()
        super.onPause()
    }

    override fun onDestroy() {
        gameManager?.release()
        WrapperAdapterUtils.releaseAll(gameWrapperAdapter)
        super.onDestroy()
    }

    fun onBackPressed() {
        requireActivity().onBackPressed()
    }

    fun showHints() {
        vm.setCurrentVariant(dataProvider.getCurrentQuote())
        findNavController().navigate(R.id.action_gameFragment_to_hintFragment)
    }

    private fun onHintReceived(hint: String) {
        val snackbar = Snackbar.make(binding().root, hint, Snackbar.LENGTH_INDEFINITE)
        snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            .maxLines = 5
        snackbar
            .setAction(R.string.shared_label_remember) { snackbar.dismiss() }
            .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .show()
    }

    private fun binding() = viewBinding<GameFragmentBinding>()

    /**
     *
     * Logic connected with overview drag & drop mechanics
     *
     * */
    private fun startNewLevel(originalQuote: List<String>, mixedQuote: List<String>) {
        if (gameWrapperAdapter != null) return

        dataProvider = ExampleDataProvider()
        dataProvider.addQuote(originalQuote, mixedQuote)
        gameAdapter =
            OnboardingGameAdapter(userPreferences.getBackgroundColor()) { onLevelCompleted() }
        gameAdapter.setDataProvider(dataProvider)

        gameManager = RecyclerViewDragDropManager().apply {
            setInitiateOnLongPress(true)
            setInitiateOnMove(true)
            setLongPressTimeout(100)
            draggingItemAlpha = 0.8f
            draggingItemScale = 1.3f
            draggingItemRotation = 15.0f
            attachRecyclerView(binding().gameRv)
        }

        binding().gameRv.run {
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            gameWrapperAdapter = gameManager?.createWrappedAdapter(gameAdapter)
            adapter = gameWrapperAdapter
            itemAnimator = DraggableItemAnimator()
            setHasFixedSize(false)
        }
    }

    private fun onLevelCompleted() {
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

        manageViewGroupTapable(binding().container, false)
        gameManager?.release()

        Completable
            .timer(1700, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                removeCardsWithAnimation()
                WrapperAdapterUtils.releaseAll(gameWrapperAdapter)
                vm.markLevelAsCompleted()
            }
            .untilStopped()
    }

    private fun removeCardsWithAnimation() {
        val viewRect = Rect()
        binding().gameRv.getGlobalVisibleRect(viewRect)

        val explode: Transition = Explode()
        explode.epicenterCallback = object : Transition.EpicenterCallback() {
            override fun onGetEpicenter(transition: Transition): Rect {
                return viewRect
            }
        }
        explode.duration = 1000
        TransitionManager.beginDelayedTransition(binding().gameRv, explode)
        binding().gameRv.adapter = null
    }

    private fun manageViewGroupTapable(viewGroup: ViewGroup, enabled: Boolean) {
        val childCount = viewGroup.childCount
        for (i in 0 until childCount) {
            val view = viewGroup.getChildAt(i)
            view.isEnabled = enabled
            if (view is ViewGroup) {
                manageViewGroupTapable(view, enabled)
            }
        }
    }

}