package com.quote.mosaic.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.quote.mosaic.core.manager.UserPreferences
import com.quote.mosaic.core.common.utils.findColor
import com.quote.mosaic.ui.main.MainActivity
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import javax.inject.Inject

/**
 * Base class for application's fragments.
 */
abstract class AppFragment : Fragment() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private var viewBinding: ViewDataBinding? = null

    private val disposeOnStop = CompositeDisposable()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
        Timber.w("Calling startActivity() from within a fragment")
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = DataBindingUtil.getBinding(view)
    }

    @CallSuper
    override fun onStop() {
        disposeOnStop.clear()
        super.onStop()
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding?.unbind()
        viewBinding = null
    }

    protected fun <B : ViewDataBinding> viewBinding(): B {
        val binding = viewBinding

        return if (binding != null) {
            @Suppress("UNCHECKED_CAST")
            binding as B
        } else {
            throw IllegalStateException("View is not a data binding layout")
        }
    }

    protected fun Disposable.untilStopped(): Disposable {
        disposeOnStop += this
        return this
    }

    fun updateBackgroundColor(
        container: ViewGroup,
        views: List<View> = emptyList()
    ) {
        (requireActivity() as? MainActivity)?.let {
            it.binding.bottomBar.setBackgroundResource(userPreferences.getBackgroundBarColor())
            it.window.statusBarColor =
                requireContext().findColor(userPreferences.getBackgroundBarColor())
            it.binding.adView.setBackgroundResource(userPreferences.getBackgroundBarColor())
        }
        container.setBackgroundResource(userPreferences.getBackgroundColor())

        views.forEach {
            it.setBackgroundResource(userPreferences.getBackgroundColor())
        }
    }
}