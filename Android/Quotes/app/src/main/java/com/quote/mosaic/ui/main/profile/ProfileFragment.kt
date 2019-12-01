package com.quote.mosaic.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.common.utils.Ime
import com.quote.mosaic.core.ext.shareAppIntent
import com.quote.mosaic.core.ext.supportEmailIntent
import com.quote.mosaic.core.manager.AnalyticsManager
import com.quote.mosaic.data.manager.UserManager
import com.quote.mosaic.databinding.ProfileFragmentBinding
import com.quote.mosaic.ui.onboarding.OnboardingActivity
import eltos.simpledialogfragment.SimpleDialog
import eltos.simpledialogfragment.color.SimpleColorDialog
import javax.inject.Inject

class ProfileFragment : AppFragment(), SimpleDialog.OnDialogResultListener {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var vmFactory: ProfileViewModel.Factory

    private lateinit var vm: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(ProfileViewModel::class.java)
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<ProfileFragmentBinding>(
        inflater, R.layout.profile_fragment, container, false
    ).apply {
        fragment = this@ProfileFragment
        viewModel = vm
        shareButton.startShimmerAnimation()
        updateBackgroundColor(this.container)
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.logoutTrigger.subscribe {
            startActivity(OnboardingActivity.newIntent(requireContext()))
            requireActivity().finishAffinity()
        }.untilStopped()

        vm.state.successTrigger.subscribe {
            Ime.hide(binding().nameEditText)
            Snackbar.make(
                binding().root, R.string.shared_label_saved, Snackbar.LENGTH_SHORT
            ).show()
            vm.reset()
        }.untilStopped()

    }

    override fun onResume() {
        super.onResume()
        analyticsManager.logCurrentScreen(requireActivity(), "Profile Screen")
    }

    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        if (which == SimpleDialog.OnDialogResultListener.BUTTON_POSITIVE) {
            when (dialogTag) {
                COLOR_DIALOG -> changeBackgroundColor(extras.getInt(SimpleColorDialog.SELECTED_SINGLE_POSITION))
            }
        }
        return true
    }

    private fun changeBackgroundColor(position: Int) {
        val colors = listOf(
            R.drawable.ic_circle_shape_blue,
            R.drawable.ic_circle_shape_purple,
            R.drawable.ic_circle_shape_gray,
            R.drawable.ic_circle_shape_green,
            R.drawable.ic_circle_shape_red,
            R.drawable.ic_circle_shape_black
        )

        analyticsManager.logBackgroundColorChanged(
            when (colors[position]) {
                R.drawable.ic_circle_shape_blue -> "Blue"
                R.drawable.ic_circle_shape_purple -> "Purple"
                R.drawable.ic_circle_shape_gray -> "Gray"
                R.drawable.ic_circle_shape_green -> "Green"
                R.drawable.ic_circle_shape_red -> "Red"
                R.drawable.ic_circle_shape_black -> "Black"
                else -> "Unknown"
            }
        )

        vm.changeColor(colors[position])
        updateBackgroundColor(binding().container)
    }

    fun onColorPickerClicked() {
        SimpleColorDialog.build()
            .title(R.string.profile_label_choose_color)
            .colors(requireContext().resources.getIntArray(R.array.picker_colors))
            .show(this, COLOR_DIALOG)
    }

    fun shareClicked() {
        analyticsManager.logShareClicked()
        val intent = Intent().shareAppIntent(requireContext())
        startActivity(intent)
    }

    fun feedbackClicked() {
        analyticsManager.logProblemFeedbackClicked()
        val intent = Intent.createChooser(
            Intent().supportEmailIntent(requireContext(), userManager),
            requireContext().getString(R.string.shared_label_send)
        )
        startActivity(intent)
    }

    private fun binding() = viewBinding<ProfileFragmentBinding>()

    companion object {
        private const val COLOR_DIALOG = "COLOR_DIALOG"
    }
}