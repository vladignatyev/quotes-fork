package com.brain.words.puzzle.quotes.ui.main.game.topic

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.brain.words.puzzle.quotes.R
import com.brain.words.puzzle.quotes.core.AppFragment
import com.brain.words.puzzle.quotes.core.common.args
import com.brain.words.puzzle.quotes.databinding.MainGameTopicFragmentBinding
import com.brain.words.puzzle.quotes.ui.main.game.GameListener
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import javax.inject.Inject

class TopicFragment : AppFragment(), GameListener {

    @Inject
    lateinit var vmFactory: TopicViewModel.Factory

    private lateinit var vm: TopicViewModel

    private lateinit var adapter: TopicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders.of(this, vmFactory)
            .get(TopicViewModel::class.java)
        vm.setUp(args().getParcelable(MODEL_ID)!!)
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
        adapter = TopicAdapter(this@TopicFragment)
        items.adapter = adapter
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.sections.subscribe {
            adapter.submitList(it)
            binding().items.setHasFixedSize(true)
            binding().items.isNestedScrollingEnabled = true
        }.untilStopped()
    }

    override fun onClosedClicked(id: Int) {
    }

    override fun onCompletedClicked(id: Int) {
        binding().viewKonfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED, Color.BLUE, Color.CYAN)
            .setDirection(0.0, 359.0)
            .setSpeed(4f, 7f)
            .setFadeOutEnabled(true)
            .setTimeToLive(10L)
            .addShapes(Shape.RECT, Shape.CIRCLE)
            .addSizes(Size(12))
            .setPosition(-50f, binding().viewKonfetti.width + 500f, -50f, -50f)
            .streamFor(300, 30L)
    }

    override fun onOpenedClicked(id: Int) {
        println("----- onOpenedClicked")
    }

    private fun binding() = viewBinding<MainGameTopicFragmentBinding>()

    companion object {

        private const val MODEL_ID = "MODEL_ID"

        fun newInstance(model: TopicModel) = TopicFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MODEL_ID, model)
            }
        }
    }
}