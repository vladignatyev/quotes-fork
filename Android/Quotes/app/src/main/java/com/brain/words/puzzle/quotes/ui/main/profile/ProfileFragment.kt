package com.brain.words.puzzle.quotes.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppFragment
import com.brain.words.puzzle.quotes.databinding.ProfileFragmentBinding
import javax.inject.Inject

class ProfileFragment : AppFragment() {

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
    }.root
}