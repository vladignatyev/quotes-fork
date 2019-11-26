package com.quote.mosaic.ui.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.quote.mosaic.R
import com.quote.mosaic.core.AppActivity
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.core.common.utils.findColor
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.game_activity.*
import javax.inject.Inject

class GameActivity : AppActivity(), HasAndroidInjector {

    @Inject
    lateinit var vmFactory: GameViewModel.Factory

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private lateinit var vm: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(GameViewModel::class.java)

        setContentView(R.layout.game_activity)
        window.statusBarColor =
            findColor(userPreferences.getBackgroundBarColor())

        val hostFragment = gameContainer as NavHostFragment
        hostFragment.navController.setGraph(R.navigation.game, intent.extras)
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    companion object {
        const val SELECTED_CATEGORY_ID = "SELECTED_CATEGORY_ID"

        fun newIntent(context: Context, id: Int) = Intent(context, GameActivity::class.java).apply {
            putExtra(SELECTED_CATEGORY_ID, id)
        }
    }

}