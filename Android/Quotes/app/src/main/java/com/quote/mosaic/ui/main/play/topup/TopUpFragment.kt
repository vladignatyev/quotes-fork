package com.quote.mosaic.ui.main.play.topup

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionInflater
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.ext.supportEmailIntent
import com.quote.mosaic.core.manager.AnalyticsManager
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.databinding.TopupCompletePopupBinding
import com.quote.mosaic.databinding.TopupFailedPopupBinding
import com.quote.mosaic.databinding.TopupFragmentBinding
import javax.inject.Inject

class TopUpFragment : AppFragment() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var vmFactory: TopUpViewModel.Factory

    private lateinit var vm: TopUpViewModel

    private lateinit var adapter: TopUpProductsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(TopUpViewModel::class.java)
        vm.setUp(userManager.getUserName(), userManager.getUserBalance().toString())
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<TopupFragmentBinding>(
        inflater, R.layout.topup_fragment, container, false
    ).apply {
        updateBackgroundColor(this.container)
        fragment = this@TopUpFragment
        viewModel = vm

        adapter = TopUpProductsAdapter { vm.buyProduct(requireActivity(), it) }
        items.adapter = adapter
        items.addItemDecoration(TopUpProductsAdapter.decoration(10))
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.products.subscribe {
            adapter.submitList(it)
            binding().items.scheduleLayoutAnimation()
        }.untilStopped()

        vm.state.failureTrigger.subscribe {
            vm.reset()
            showError()
        }.untilStopped()

        vm.state.successTrigger.subscribe {
            vm.reset()
            showSuccess()
        }.untilStopped()
    }

    override fun onResume() {
        super.onResume()
        analyticsManager.logCurrentScreen(requireActivity(), "Topup Screen")
    }

    private fun showError() {
        val binding: TopupFailedPopupBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()), R.layout.topup_failed_popup, null, true
            )

        val alert = AlertDialog.Builder(requireContext(), R.style.DialogStyle)
            .setCancelable(false)
            .setView(binding.root)
            .show()

        binding.tryAgain.setOnClickListener {
            alert.dismiss()
        }

        binding.askSupport.setOnClickListener {
            if (isAdded) {
                alert.dismiss()
                val intent = Intent.createChooser(
                    Intent().supportEmailIntent(requireContext(), userManager),
                    requireContext().getString(R.string.shared_label_send)
                )
                startActivity(intent)
            }
        }

    }

    private fun showSuccess() {
        val binding: TopupCompletePopupBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()), R.layout.topup_complete_popup, null, true
            )

        val alert = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setView(binding.root)
            .show()

        binding.grab.setOnClickListener {
            alert.dismiss()
            vm.loadProducts()
        }
    }

    private fun binding() = viewBinding<TopupFragmentBinding>()
}