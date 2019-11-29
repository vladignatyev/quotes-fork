package com.quote.mosaic.ui.main.play.game.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.quote.mosaic.R
import com.quote.mosaic.core.common.utils.FireLike
import com.quote.mosaic.core.common.utils.Vibrator
import com.quote.mosaic.databinding.GameSuccessDialogBinding
import com.quote.mosaic.databinding.GameSuccessDoubleUpBinding
import com.quote.mosaic.databinding.OnboardingGameFinishDialogBinding
import com.quote.mosaic.databinding.OnboardingGameSuccessDialogBinding
import com.quote.mosaic.ui.main.play.game.GameViewModel
import lv.chi.firelike.IconEmitterManager

object GameDialogBuilder {

    //=========== GAME SUCCESS =============//
    fun showGameSuccessDialog(
        context: Context, sharedViewModel: GameViewModel, onCompleted: (alert: AlertDialog) -> Unit
    ) {
        val binding: GameSuccessDialogBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.game_success_dialog, null, true
            )

        val iconEmitter = IconEmitterManager(
            binding.container,
            FireLike.flame(R.drawable.ic_coins)
        )

        val alert = AlertDialog
            .Builder(context, R.style.DialogStyle)
            .setView(binding.root)
            .show()

        var counter = 0

        binding.run {
            viewModel = sharedViewModel

            collectCoins.setOnClickListener {
                if (counter < 3) {
                    binding.image.playAnimation()
                    iconEmitter.emitIconFromView(binding.centerPoint)
                    Vibrator.vibrate(context)
                    counter++
                } else {
                    onCompleted(alert)
                }

                val titleResId = when (counter) {
                    0 -> R.string.game_button_success_collect_coins
                    1 -> R.string.game_button_success_collect_coins_2
                    2 -> R.string.game_button_success_collect_coins_3
                    3 -> R.string.game_button_success_collect_coins_4
                    else -> R.string.game_button_success_collect_coins
                }
                collectCoins.setText(titleResId)
            }

            close.setOnClickListener { alert.dismiss() }
        }
    }

    fun showDoubleUpDialog(
        context: Context,
        sharedViewModel: GameViewModel,
        onCompleted: (alert: AlertDialog) -> Unit
    ) {
        val binding: GameSuccessDoubleUpBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.game_success_double_up, null, true
            )

        val alert = AlertDialog
            .Builder(context, R.style.DoubleUpDialog)
            .setCancelable(false)
            .setView(binding.root)
            .show()

        binding.run {
            viewModel = sharedViewModel
            close.setOnClickListener { alert.dismiss() }
            doubleUp.setOnClickListener { onCompleted(alert) }
        }
    }

    //========== ONBOARDING GAME ============//
    fun showOnboardingGameSuccessDialog(
        context: Context, initialBalance: String, onCompleted: (alert: AlertDialog) -> Unit
    ) {
        val binding: OnboardingGameSuccessDialogBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.onboarding_game_success_dialog, null, true
            )

        binding.coinReward.text =
            context.getString(R.string.onboarding_label_reward, initialBalance)

        val iconEmitter = IconEmitterManager(
            binding.container,
            FireLike.flame(R.drawable.ic_coins)
        )

        val alert = AlertDialog
            .Builder(context, R.style.DialogStyle)
            .setCancelable(false)
            .setView(binding.root)
            .show()

        var counter = 0

        binding.run {
            collectCoins.setOnClickListener {
                if (counter < 3) {
                    binding.image.playAnimation()
                    iconEmitter.emitIconFromView(binding.centerPoint)
                    Vibrator.vibrate(context)
                    counter++
                } else {
                    onCompleted(alert)
                }

                val titleResId = when (counter) {
                    0 -> R.string.game_button_success_collect_coins
                    1 -> R.string.game_button_success_collect_coins_2
                    2 -> R.string.game_button_success_collect_coins_3
                    3 -> R.string.game_button_success_collect_coins_4
                    else -> R.string.game_button_success_collect_coins
                }
                collectCoins.setText(titleResId)
            }
        }
    }

    fun showOnboardingFinishDialog(
        context: Context,
        onCompleted: (alert: AlertDialog) -> Unit
    ) {
        val binding: OnboardingGameFinishDialogBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.onboarding_game_finish_dialog, null, true
            )

        val alert = AlertDialog
            .Builder(context, R.style.DoubleUpDialog)
            .setCancelable(false)
            .setView(binding.root)
            .show()

        binding.startPlay.setOnClickListener { onCompleted(alert) }

    }
}