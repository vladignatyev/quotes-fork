package com.quote.mosaic.ui.onboarding.category

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.quote.mosaic.R
import com.quote.mosaic.core.AppFragment
import com.quote.mosaic.core.manager.AnalyticsManager
import com.quote.mosaic.databinding.OnboardingTopicFragmentBinding
import com.quote.mosaic.ui.main.play.CategoryClickListener
import com.quote.mosaic.ui.main.play.topic.TopicAdapter
import com.quote.mosaic.ui.main.play.topic.category.CategoryModel
import com.quote.mosaic.ui.main.play.topic.section.SectionModel
import com.quote.mosaic.ui.onboarding.OnboardingViewModel
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import javax.inject.Inject

class OnboardingTopicFragment : AppFragment(), CategoryClickListener {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    private val vm: OnboardingViewModel by activityViewModels()

    private lateinit var adapter: TopicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsManager.logOnboardingOpenCategoryStarted()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<OnboardingTopicFragmentBinding>(
        inflater, R.layout.onboarding_topic_fragment, container, false
    ).apply {
        fragment = this@OnboardingTopicFragment
        adapter = TopicAdapter(this@OnboardingTopicFragment)
        items.adapter = adapter
        adapter.submitList(defaultList())
    }.root

    override fun onResume() {
        super.onResume()
        analyticsManager.logCurrentScreen(requireActivity(), "Onboarding Topic Screen")
    }

    override fun onCompletedClicked(id: Int) {}

    override fun onOpenedClicked(id: Int, name: String) {
        analyticsManager.logOnboardingOpenedTapped()
        analyticsManager.logOnboardingOpenCategoryFinished()
        findNavController().navigate(R.id.action_onboardingCategoryFragment_to_onboardingGameFragment)
    }

    override fun onRefreshClicked() {}

    override fun onClosedClicked(id: Int, name: String) {
        analyticsManager.logOnboardingClosedTapped()
        vm.state.balance.set("10")
        adapter.submitList(openCategoryList())
        binding().viewKonfetti.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED, Color.BLUE, Color.CYAN)
            .setDirection(0.0, 359.0)
            .setSpeed(4f, 7f)
            .setFadeOutEnabled(true)
            .setTimeToLive(10L)
            .addShapes(Shape.RECT, Shape.CIRCLE)
            .addSizes(Size(12))
            .setPosition(-50f, binding().viewKonfetti.width + 500f, -500f, -50f)
            .streamFor(3000, 300L)
    }


    private fun defaultList() = listOf(
        SectionModel.Item(
            0, getString(R.string.onboarding_label_open_category), listOf(
                CategoryModel.Closed(
                    0,
                    getString(R.string.onboarding_label_warmup),
                    "40",
                    "https://i.imgur.com/vUCoJQf.png",
                    false
                )
            )
        )
    )

    private fun openCategoryList() = listOf(
        SectionModel.Item(
            0, getString(R.string.onboarding_label_go_inside), listOf(
                CategoryModel.Open(
                    0,
                    getString(R.string.onboarding_label_warmup),
                    0,
                    1,
                    0,
                    "https://i.imgur.com/vUCoJQf.png",
                    R.drawable.background_category_open_overlay_blue
                )
            )
        )
    )

    private fun binding() = viewBinding<OnboardingTopicFragmentBinding>()

    companion object {

        fun newInstance() = OnboardingTopicFragment()
    }
}