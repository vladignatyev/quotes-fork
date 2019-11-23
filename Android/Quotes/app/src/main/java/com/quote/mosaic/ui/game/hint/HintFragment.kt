package com.quote.mosaic.ui.game.hint

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.quote.mosaic.R
import com.quote.mosaic.core.AppDialogFragment
import com.quote.mosaic.core.common.args
import com.quote.mosaic.core.common.parentAs
import com.quote.mosaic.data.model.overview.QuoteDO
import com.quote.mosaic.databinding.HintFragmentBinding
import javax.inject.Inject

class HintFragment : AppDialogFragment() {

    @Inject
    lateinit var vmFactory: HintViewModel.Factory

    private lateinit var vm: HintViewModel

    private lateinit var listener: Listener

    private lateinit var adapter: HintAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentAs()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(HintViewModel::class.java)
        vm.setUp(args().getParcelable(KEY_HINT_QUOTE)!!, args().getStringArrayList(KEY_HINT_USER_VARIANT)!!)
        vm.init()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<HintFragmentBinding>(
        inflater, R.layout.hint_fragment, container, false
    ).apply {
        fragment = this@HintFragment
        viewModel = vm
        adapter = HintAdapter({
            this@HintFragment.dismiss()
        }, {
            listener.onShowBalanceClicked()
        }, {
            onHintClicked(it)
        })
        items.adapter = adapter
    }.root

    private fun onHintClicked(hint: HintModel) {
        if (hint is HintModel.CoinHint) {
            when (hint.type) {
                HintType.NEXT_WORD -> vm.findNextWord()
                HintType.AUTHOR -> vm.findAuthor()
            }
        } else if (hint is HintModel.SkipHint) {
            vm.skipLevel()
        }
    }

    override fun onStart() {
        super.onStart()

        vm.state.items.subscribe {
            adapter.submitList(it)
        }.untilStopped()

        vm.state.hintTrigger.subscribe {
            listener.onHintReceived(it)
            dismiss()
        }.untilStopped()

        vm.state.skipTrigger.subscribe {
            listener.onSkipLevelClicked()
            dismiss()
        }.untilStopped()
    }

    interface Listener {
        fun onShowBalanceClicked()
        fun onSkipLevelClicked()
        fun onHintReceived(hint: String)
    }

    companion object {

        private const val KEY_HINT_QUOTE = "KEY_HINT_QUOTE"
        private const val KEY_HINT_USER_VARIANT = "KEY_HINT_USER_VARIANT"

        fun newInstance(quoteDO: QuoteDO, userVariant: List<String>) = HintFragment().apply {
            arguments = Bundle().apply {
                putParcelable(KEY_HINT_QUOTE, quoteDO)
                putStringArrayList(KEY_HINT_USER_VARIANT, ArrayList(userVariant))
            }
        }
    }
}