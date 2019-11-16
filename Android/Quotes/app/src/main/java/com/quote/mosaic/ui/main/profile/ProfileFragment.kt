package com.quote.mosaic.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.databinding.ProfileFragmentBinding
import com.quote.mosaic.ui.onboarding.OnboardingActivity
import eltos.simpledialogfragment.SimpleDialog
import eltos.simpledialogfragment.color.SimpleColorDialog
import javax.inject.Inject

class ProfileFragment : AppFragment(), SimpleDialog.OnDialogResultListener {

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
        updateBackgroundColor(this.container)
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.logoutTrigger.subscribe {
            startActivity(OnboardingActivity.newIntent(requireContext()))
            requireActivity().finishAffinity()
        }.untilStopped()
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

        vm.changeColor(colors[position])
        updateBackgroundColor(binding().container)
    }

    fun onColorPickerClicked() {
        SimpleColorDialog.build()
            .title(R.string.settings_label_choose_color)
            .colors(requireContext().resources.getIntArray(R.array.picker_colors))
            .show(this, COLOR_DIALOG)
    }

    private fun binding() = viewBinding<ProfileFragmentBinding>()

    companion object {
        private const val COLOR_DIALOG = "COLOR_DIALOG"
    }
}