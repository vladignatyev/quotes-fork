package com.quote.mosaic.ui.main.play.game.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.quote.mosaic.R
import com.quote.mosaic.databinding.GameHintAuthorDialogBinding
import com.quote.mosaic.databinding.GameHintNextWordDialogBinding
import com.quote.mosaic.databinding.GameHintSkipDialogBinding
import com.quote.mosaic.ui.main.play.game.GameViewModel

object HintDialogBuilder {

    //=========== Author Hint =============//
    fun showAuthorHintDialog(
        context: Context, sharedViewModel: GameViewModel
    ): AlertDialog {
        val binding: GameHintAuthorDialogBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.game_hint_author_dialog, null, true
            )

        val alert = AlertDialog
            .Builder(context, R.style.DialogStyle)
            .setCancelable(false)
            .setView(binding.root)
            .show()

        binding.viewModel = sharedViewModel
        binding.close.setOnClickListener { alert.dismiss() }

        alert.show()

        return alert
    }

    //=========== Skip Level Hint =============//
    fun showSkipLevelDialog(
        context: Context, sharedViewModel: GameViewModel
    ): AlertDialog {
        val binding: GameHintSkipDialogBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.game_hint_skip_dialog, null, true
            )

        val alert = AlertDialog
            .Builder(context, R.style.DialogStyle)
            .setCancelable(false)
            .setView(binding.root)
            .show()

        binding.viewModel = sharedViewModel
        binding.close.setOnClickListener { alert.dismiss() }

        alert.show()

        return alert
    }

    //=========== Skip Level Hint =============//
    fun showNextWordDialog(
        context: Context, sharedViewModel: GameViewModel, onSkipVideoClicked: () -> Unit
    ): AlertDialog {
        val binding: GameHintNextWordDialogBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.game_hint_next_word_dialog, null, true
            )

        val alert = AlertDialog
            .Builder(context, R.style.DialogStyle)
            .setCancelable(false)
            .setView(binding.root)
            .show()

        binding.viewModel = sharedViewModel
        binding.close.setOnClickListener { alert.dismiss() }
        binding.videoSkip.setOnClickListener { onSkipVideoClicked() }

        alert.show()

        return alert
    }
}