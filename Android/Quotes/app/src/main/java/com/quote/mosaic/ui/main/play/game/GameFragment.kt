package com.quote.mosaic.ui.main.play.game

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.core.common.args
import com.quote.mosaic.core.common.utils.findColor
import com.quote.mosaic.core.common.utils.manageViewGroupTapable
import com.quote.mosaic.databinding.GameFragmentBinding
import com.quote.mosaic.game.GameListener
import com.quote.mosaic.ui.main.MainActivity
import com.quote.mosaic.ui.main.play.game.utils.GameDialogBuilder
import com.quote.mosaic.ui.main.play.game.utils.HintDialogBuilder
import com.quote.mosaic.ui.main.play.game.utils.SnackbarBuilder
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GameFragment : AppFragment(), GameListener {

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var vmFactory: GameViewModel.Factory

    private val vm: GameViewModel by viewModels { vmFactory }

    private var levelCompleted = false
    private var visibleAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.setUp(args().getInt(SELECTED_CATEGORY_ID))
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
        gameView.setTextColor(userPreferences.getBackgroundColor())
        gameView.setListener(this@GameFragment)
        root.setBackgroundColor(requireContext().findColor(userPreferences.getBackgroundColor()))
    }.root

    override fun onStart() {
        super.onStart()

        vm.state.onNextLevelReceived.subscribe { mixedQuote ->
            visibleAlert?.dismiss()
            levelCompleted = false
            binding().root.manageViewGroupTapable(binding().root, true)
            binding().gameView.setData(vm.state.userVariantQuote.get())
        }.untilStopped()

        vm.state.levelCompletedTrigger.subscribe {
            visibleAlert?.dismiss()
            showSuccessDialog()
        }.untilStopped()

        vm.state.hintReceivedTrigger.subscribe {
            binding().menu.close(true)
            visibleAlert?.dismiss()
            SnackbarBuilder.showHintSnackbar(binding().root, userPreferences, it)
        }.untilStopped()

        vm.state.insufficientBalanceTriggered.subscribe {
            binding().menu.close(true)
            visibleAlert?.dismiss()
            topupClicked()
        }.untilStopped()

    }

    override fun onResume() {
        super.onResume()
        billingManager.warmUp()
    }

    override fun onQuoteOrderChanged(userVariant: ArrayList<String>) {
        val correctQuote = vm.state.currentQuote.get()!!.splitted
        if (!levelCompleted && userVariant == correctQuote) {
            levelCompleted = true
            onLevelCompleted()
            vm.markLevelAsCompleted()
        } else {
            vm.setCurrentVariant(userVariant)
        }
    }

    fun topupClicked() {
        val extras = FragmentNavigatorExtras(
            binding().title to "title",
            binding().balance to "balance",
            binding().topup to "topup"
        )

        findNavController().navigate(
            R.id.action_gameFragment_to_topUpFragment, null, null, extras
        )
    }

    fun findNextWordClicked() {
        vm.verifyVideoProducts()
        binding().menu.close(true)
        visibleAlert = HintDialogBuilder.showNextWordDialog(requireContext(), vm) {
            vm.findNextWordVideo(requireActivity())
        }
    }

    fun skipLevelClicked() {
        binding().menu.close(true)
        visibleAlert = HintDialogBuilder.showSkipLevelDialog(requireContext(), vm)
    }

    fun findAuthorClicked() {
        binding().menu.close(true)
        visibleAlert = HintDialogBuilder.showAuthorHintDialog(requireContext(), vm)
    }

    private fun showSuccessDialog() {
        GameDialogBuilder.showGameSuccessDialog(requireContext(), vm) { successDialog ->
            successDialog.dismiss()
            vm.reset()

            if (vm.state.isLastQuote.get()) {
                startActivity(MainActivity.newIntent(requireContext()))
            } else {
                vm.loadLevel()

                if (vm.doubleUpPossible()) {
                    Completable.timer(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .subscribe {
                            GameDialogBuilder.showDoubleUpDialog(requireContext(), vm) {
                                vm.showDoubleUpVideo(requireActivity())
                                it.dismiss()
                            }
                        }
                }
            }
        }
    }

    private fun onLevelCompleted() {
        binding().root.manageViewGroupTapable(binding().root, false)
        showKonfetti()
        Completable
            .timer(1200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { binding().gameView.removeItems() }
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

    private fun binding() = viewBinding<GameFragmentBinding>()

    companion object {
        const val SELECTED_CATEGORY_ID = "SELECTED_CATEGORY_ID"
    }

}