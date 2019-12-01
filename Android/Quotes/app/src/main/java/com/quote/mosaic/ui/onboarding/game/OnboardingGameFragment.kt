package com.quote.mosaic.ui.onboarding.game

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.ui.main.play.game.utils.GameDialogBuilder
import com.quote.mosaic.core.common.utils.manageViewGroupTapable
import com.quote.mosaic.core.manager.AnalyticsManager
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.databinding.OnboardingGameFragmentBinding
import com.quote.mosaic.game.GameListener
import com.quote.mosaic.ui.main.MainActivity
import com.quote.mosaic.ui.onboarding.OnboardingViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OnboardingGameFragment : AppFragment(), GameListener {

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    private val vm: OnboardingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsManager.logOnboardingGameStarted()
        vm.loadUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<OnboardingGameFragmentBinding>(
        inflater, R.layout.onboarding_game_fragment, container, false
    ).apply {
        viewModel = vm
        gameView.setData(getString(R.string.onboarding_label_default_quote_mixed).split(" "))
        gameView.setListener(this@OnboardingGameFragment)
        gameView.setTextColor(R.color.game_background_blue)
    }.root

    override fun onResume() {
        super.onResume()
        analyticsManager.logCurrentScreen(requireActivity(), "Onboarding Game Screen")
        binding().welcomeMsg.text =
            getString(R.string.onboarding_label_welcome_aboard, userManager.getUserName())
    }

    private var completed = false

    override fun onQuoteOrderChanged(userVariant: ArrayList<String>) {
        val badChar = "\u200E"
        val correctQuote = getString(R.string.onboarding_label_default_quote)
        if (!completed && userVariant.joinToString(" ").replace(badChar, "") == correctQuote) {
            completed = true
            levelCompleted()
        }
    }

    private fun levelCompleted() {
        analyticsManager.logOnboardingGameFinished()
        binding().container.manageViewGroupTapable(binding().container, false)
        showKonfetti()
        Completable
            .timer(1200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                binding().gameView.removeItems()
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

    private fun showSuccessDialog() {
        GameDialogBuilder.showOnboardingGameSuccessDialog(
            requireContext(),
            vm.state.initialBalance.get()
        ) { parentDialog ->
            parentDialog.dismiss()

            Completable.timer(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    GameDialogBuilder.showOnboardingFinishDialog(requireContext()) {
                        analyticsManager.logOnboardingFinished()
                        startActivity(MainActivity.newIntent(requireContext()))
                        it.dismiss()
                    }
                }
        }
    }

    private fun binding() = viewBinding<OnboardingGameFragmentBinding>()
}