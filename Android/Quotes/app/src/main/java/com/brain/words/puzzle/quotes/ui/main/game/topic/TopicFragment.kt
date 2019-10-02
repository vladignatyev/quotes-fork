package com.brain.words.puzzle.quotes.ui.main.game.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppFragment
import com.brain.words.puzzle.quotes.databinding.MainGameTopicFragmentBinding
import javax.inject.Inject

class TopicFragment : AppFragment() {

    @Inject
    lateinit var vmFactory: TopicViewModel.Factory

    private lateinit var vm: TopicViewModel

    private lateinit var adapter: TopicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders.of(this, vmFactory)
            .get(TopicViewModel::class.java)
        vm.init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<MainGameTopicFragmentBinding>(
        inflater, R.layout.main_game_topic_fragment, container, false
    ).apply {
        fragment = this@TopicFragment
        viewModel = vm
        adapter = TopicAdapter()
        items.adapter = adapter
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.sections.subscribe {
            adapter.submitList(it)
        }.untilStopped()
    }

    companion object {

        private const val MODEL_ID = "MODEL_ID"

        fun newInstance(model: TopicModel) = TopicFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MODEL_ID, model)
            }
        }
    }
}