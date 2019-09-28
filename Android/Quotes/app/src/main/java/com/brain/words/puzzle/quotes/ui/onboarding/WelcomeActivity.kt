package com.brain.words.puzzle.quotes.ui.onboarding


import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppActivity
import com.brain.words.puzzle.quotes.databinding.WelcomeActivityBinding
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class WelcomeActivity : AppActivity(), HasAndroidInjector {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var vmFactory: WelcomeViewModel.Factory

    private lateinit var vm: WelcomeViewModel

    private lateinit var binding: WelcomeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(WelcomeViewModel::class.java)
        vm.init()

        binding = DataBindingUtil.setContentView<WelcomeActivityBinding>(this, R.layout.welcome_activity)
            .apply {
                activity = this@WelcomeActivity
                viewModel = vm
            }
    }

    override fun androidInjector(): AndroidInjector<Any> = fragmentDispatchingAndroidInjector

    companion object {

        fun newIntent(context: Context) = Intent(context, WelcomeActivity::class.java)

    }
}