package com.brain.words.puzzle.quotes.ui.main.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.brain.words.puzzle.data.UserManager
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppFragment
import com.brain.words.puzzle.quotes.databinding.MainGameFragmentBinding
import javax.inject.Inject

class GameFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: GameViewModel.Factory

    private lateinit var vm: GameViewModel

    private lateinit var adapter: GameTopicPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders
            .of(this, vmFactory)
            .get(GameViewModel::class.java)
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<MainGameFragmentBinding>(
        inflater, R.layout.main_game_fragment, container, false
    ).apply {
        fragment = this@GameFragment
        viewModel = vm
        adapter = GameTopicPagerAdapter(childFragmentManager)
        topics.adapter = adapter
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.categories.subscribe {
            adapter.submitList(it)
        }.untilStopped()
    }

    fun topupClicked() {
        findNavController().navigate(R.id.action_gameFragment_to_topupFragment)
    }

    private fun binding() = viewBinding<MainGameFragmentBinding>()
}