package com.quote.mosaic.ui.game.hint

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.quote.mosaic.R
import com.quote.mosaic.core.AppDialogFragment
import com.quote.mosaic.databinding.HintFragmentBinding
import com.quote.mosaic.ui.game.GameViewModel

class HintFragment : AppDialogFragment() {

    private val vm: GameViewModel by activityViewModels()

    private lateinit var adapter: HintAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.loadHints()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<HintFragmentBinding>(
        inflater, R.layout.hint_fragment, container, false
    ).apply {
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        fragment = this@HintFragment

        adapter = HintAdapter({
            this@HintFragment.dismiss()
        }, {
            vm.showBalance()
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

        vm.state.onHintsReceived.subscribe {
            adapter.submitList(it)
        }.untilStopped()

        vm.state.showHintTriggered.subscribe {
            dismiss()
        }.untilStopped()

        vm.state.skipLevelTriggered.subscribe {
            dismiss()
        }.untilStopped()

        vm.state.showBalanceTrigger.subscribe {
            findNavController().navigate(R.id.action_hintFragment_to_topUpFragment)
        }.untilStopped()
    }
}