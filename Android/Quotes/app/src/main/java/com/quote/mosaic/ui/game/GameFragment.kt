package com.quote.mosaic.ui.game

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.billing.BillingManager
import com.quote.mosaic.core.common.args
import com.quote.mosaic.core.common.utils.DialogBuilder
import com.quote.mosaic.core.common.utils.findColor
import com.quote.mosaic.core.common.utils.manageViewGroupTapable
import com.quote.mosaic.databinding.GameFragmentBinding
import com.quote.mosaic.game.GameListener
import com.quote.mosaic.ui.main.MainActivity
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GameFragment : AppFragment(), GameListener {

    @Inject
    lateinit var billingManager: BillingManager

    private val vm: GameViewModel by activityViewModels()

    private var levelCompleted = false

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
        gameView.setTextColor(userPreferences.getBackgroundColor())
        gameView.setListener(this@GameFragment)
        root.setBackgroundColor(requireContext().findColor(userPreferences.getBackgroundColor()))
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.onNextLevelReceived.subscribe { mixedQuote ->
            levelCompleted = false
            binding().root.manageViewGroupTapable(binding().root, true)
            binding().gameView.setData(mixedQuote)
        }.untilStopped()

        vm.state.levelCompletedTrigger.subscribe {
            showSuccessDialog()
        }.untilStopped()

        vm.state.hintReceivedTrigger.subscribe {
            onHintReceived(it)
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

    fun showHints() {
        findNavController().navigate(R.id.action_gameFragment_to_hintFragment)
    }

    private fun onHintReceived(hint: String) {
        val snackbar = Snackbar.make(binding().root, hint, Snackbar.LENGTH_INDEFINITE)
        snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            .maxLines = 5
        snackbar
            .setAction(R.string.shared_label_remember) { snackbar.dismiss() }
            .setActionTextColor(requireContext().findColor(R.color.white))
            .show()
    }

    private fun showSuccessDialog() {
        DialogBuilder.showGameSuccessDialog(requireContext(), vm) { successDialog ->
            successDialog.dismiss()
            vm.reset()

            if (vm.state.isLastQuote.get()) {
                startActivity(MainActivity.newIntent(requireContext()))
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

}