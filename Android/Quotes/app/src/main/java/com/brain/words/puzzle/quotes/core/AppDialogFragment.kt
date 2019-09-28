package com.brain.words.puzzle.quotes.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

abstract class AppDialogFragment : AppCompatDialogFragment() {

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
    override fun onResume() {
        super.onResume()
        setFullScreen()
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

    private fun setFullScreen() {
        dialog?.window?.attributes = dialog?.window?.attributes
            ?.apply {
                width = LinearLayout.LayoutParams.MATCH_PARENT
                height = LinearLayout.LayoutParams.WRAP_CONTENT
            } as android.view.WindowManager.LayoutParams
    }
}