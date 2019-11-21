package com.quote.mosaic.ui.game

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.Explode
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.quote.mosaic.R
import com.quote.mosaic.core.AppActivity
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.core.ui.data.ExampleDataProvider
import com.quote.mosaic.databinding.GameActivityBinding
import com.quote.mosaic.game.animator.DraggableItemAnimator
import com.quote.mosaic.game.draggable.RecyclerViewDragDropManager
import com.quote.mosaic.game.utils.WrapperAdapterUtils
import com.quote.mosaic.ui.game.hint.HintFragment
import com.quote.mosaic.ui.game.success.GameSuccessFragment
import com.quote.mosaic.ui.main.MainActivity
import com.quote.mosaic.ui.onboarding.game.OnboardingGameAdapter
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GameActivity : AppActivity(),
    HasAndroidInjector,
    HintFragment.Listener,
    GameSuccessFragment.Listener {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var vmFactory: GameViewModel.Factory

    private lateinit var vm: GameViewModel

    var gameWrapperAdapter: RecyclerView.Adapter<*>? = null
    var gameManager: RecyclerViewDragDropManager? = null

    lateinit var dataProvider: ExampleDataProvider
    lateinit var gameAdapter: OnboardingGameAdapter
    lateinit var binding: GameActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(GameViewModel::class.java)
        vm.setUp(intent.getIntExtra(SELECTED_CATEGORY_ID, 0))
        vm.init()

        binding = DataBindingUtil.setContentView<GameActivityBinding>(this, R.layout.game_activity)
            .apply {
                activity = this@GameActivity
                viewModel = vm
            }
        setUserSettingsColor()
    }

    override fun onStart() {
        super.onStart()
        vm.state.quoteLoadedTrigger.subscribe {
            showGame(it)
        }.untilStopped()

        vm.state.levelCompletedTrigger.subscribe {
            GameSuccessFragment.newInstance(vm.state.currentQuote.get()!!)
                .show(supportFragmentManager, null)
        }.untilStopped()
    }

    override fun onPause() {
        gameManager?.cancelDrag()
        super.onPause()
    }

    override fun onDestroy() {
        gameManager?.release()
        WrapperAdapterUtils.releaseAll(gameWrapperAdapter)
        super.onDestroy()
    }

    override fun goMainClicked() {
        startActivity(MainActivity.newIntent(this))
    }

    override fun onNextLevelClicked() {
        vm.reset()
        gameWrapperAdapter = null
        supportFragmentManager.popBackStack()
        showGame(vm.state.quoteLoadedTrigger.blockingFirst())
        handleTapEnable(binding.container, true)
    }

    override fun onShowBalanceClicked() {
        showBalance()
    }

    override fun onSkipLevelClicked() {
        onSuccess()
    }

    override fun onHintReceived(hint: String) {
        val snackbar = Snackbar.make(binding.root, hint, Snackbar.LENGTH_INDEFINITE)
        snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 5
        snackbar
            .setAction(R.string.shared_label_remember) { snackbar.dismiss() }
            .setActionTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun showBalance() {

    }

    fun hintClicked() {
        HintFragment.newInstance(vm.state.currentQuote.get()!!, dataProvider.getCurrentQuote())
            .show(supportFragmentManager, null)
    }

    fun goBack() {
        onBackPressed()
    }

    fun removeWordsWithAnimation() {
        Completable
            .timer(1700, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                removeAll()
                WrapperAdapterUtils.releaseAll(gameWrapperAdapter)
                vm.completeLevel()
            }
            .untilStopped()
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    companion object {
        private const val SELECTED_CATEGORY_ID = "SELECTED_CATEGORY_ID"

        fun newIntent(context: Context, id: Int) = Intent(context, GameActivity::class.java).apply {
            putExtra(SELECTED_CATEGORY_ID, id)
        }
    }

}

/**
 *
 * Logic connected with game drag & drop mechanics
 *
 * */
fun GameActivity.showGame(quote: List<String>) {
    if (gameWrapperAdapter != null) return

    dataProvider = ExampleDataProvider()
    dataProvider.addQuote(quote)
    gameAdapter = OnboardingGameAdapter(userPreferences.getBackgroundColor()) { onSuccess() }
    gameAdapter.setDataProvider(dataProvider)

    initGameManager()

    binding.gameRv.run {
        layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        gameWrapperAdapter = gameManager?.createWrappedAdapter(gameAdapter)
        adapter = gameWrapperAdapter
        itemAnimator = DraggableItemAnimator()
        setHasFixedSize(false)
    }
}

fun GameActivity.initGameManager() {
    gameManager = RecyclerViewDragDropManager().apply {
        setInitiateOnLongPress(true)
        setInitiateOnMove(true)
        setLongPressTimeout(100)
        draggingItemAlpha = 0.8f
        draggingItemScale = 1.3f
        draggingItemRotation = 15.0f
        attachRecyclerView(binding.gameRv)
    }
}


fun GameActivity.onSuccess() {
    showKonfetti()
    handleTapEnable(binding.container, false)
    gameManager?.release()
    removeWordsWithAnimation()
}

fun GameActivity.removeAll() {
    // save rect of view in screen coordinates
    val viewRect = Rect()
    binding.gameRv.getGlobalVisibleRect(viewRect)

    // create Explode transition with epicenter
    val explode: Transition = Explode()
    explode.epicenterCallback = object : Transition.EpicenterCallback() {
        override fun onGetEpicenter(transition: Transition): Rect {
            return viewRect
        }
    }
    explode.duration = 1000
    TransitionManager.beginDelayedTransition(binding.gameRv, explode)

    // remove all views from Recycler View
    binding.gameRv.adapter = null
}

/**
 *
 *
 * UI
 *
 * **/
fun GameActivity.showKonfetti() {
    binding.viewKonfetti.build()
        .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED, Color.BLUE, Color.CYAN)
        .setDirection(0.0, 359.0)
        .setSpeed(4f, 7f)
        .setFadeOutEnabled(true)
        .setTimeToLive(10000L)
        .addShapes(Shape.RECT, Shape.CIRCLE)
        .addSizes(Size(12))
        .setPosition(-50f, binding.viewKonfetti.width + 500f, -50f, -50f)
        .streamFor(300, 5000L)
}

fun GameActivity.handleTapEnable(viewGroup: ViewGroup, enabled: Boolean) {
    val childCount = viewGroup.childCount
    for (i in 0 until childCount) {
        val view = viewGroup.getChildAt(i)
        view.isEnabled = enabled
        if (view is ViewGroup) {
            handleTapEnable(view, enabled)
        }
    }
}

fun GameActivity.setUserSettingsColor() {
    window.statusBarColor =
        ContextCompat.getColor(this, userPreferences.getBackgroundBarColor())
    binding.container.setBackgroundColor(
        ContextCompat.getColor(this, userPreferences.getBackgroundColor())
    )
}