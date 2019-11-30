package com.quote.mosaic.ui.main.play.topic

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.common.args
import com.quote.mosaic.databinding.OverviewTopicFragmentBinding
import com.quote.mosaic.ui.main.play.game.GameFragment
import com.quote.mosaic.ui.main.play.CategoryClickListener
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TopicFragment : AppFragment(), CategoryClickListener {

    @Inject
    lateinit var vmFactory: TopicViewModel.Factory

    private lateinit var vm: TopicViewModel

    private lateinit var adapter: TopicAdapter

    private var initialLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders.of(this, vmFactory)
            .get(TopicViewModel::class.java)
        vm.setUp(args().getParcelable(MODEL_ID)!!)
        vm.init()

        adapter = TopicAdapter(this@TopicFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<OverviewTopicFragmentBinding>(
        inflater, R.layout.overview_topic_fragment, container, false
    ).apply {
        fragment = this@TopicFragment
        viewModel = vm
        items.run {
            adapter = this@TopicFragment.adapter
            isNestedScrollingEnabled = true
            setHasFixedSize(true)
        }
    }.root

    override fun onStart() {
        super.onStart()
        vm.state.sections.subscribe {
            adapter.submitList(it)
            checkIfNeededToScrollUp()
        }.untilStopped()
    }

    //Work around. Recyclerview scrolled down for some reasons on first loading.
    private fun checkIfNeededToScrollUp() {
        if (initialLoading) {
            Completable
                .timer(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    binding().items.scrollToPosition(0)
                    initialLoading = false
                }.untilStopped()
        }
    }

    override fun onClosedClicked(id: Int) {
        vm.openCategory(id)
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
        findNavController().navigate(
            R.id.action_overviewFragment_to_gameFragment, Bundle().apply {
                putInt(GameFragment.SELECTED_CATEGORY_ID, id)
            }
        )
    }

    override fun onRefreshClicked() {
        vm.refresh()
    }

    private fun binding() = viewBinding<OverviewTopicFragmentBinding>()

    companion object {

        private const val MODEL_ID = "MODEL_ID"

        fun newInstance(model: TopicModel) = TopicFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MODEL_ID, model)
            }
        }
    }
}