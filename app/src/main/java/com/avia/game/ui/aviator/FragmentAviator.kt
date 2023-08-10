package com.avia.game.ui.aviator

import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.avia.game.R
import com.avia.game.core.library.shortToast
import com.avia.game.databinding.FragmentAviatorBinding
import com.avia.game.domain.other.StarSP
import com.avia.game.ui.other.ViewBindingFragment
import io.github.hyuwah.draggableviewlib.DraggableListener
import io.github.hyuwah.draggableviewlib.DraggableView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FragmentAviator :
    ViewBindingFragment<FragmentAviatorBinding>(FragmentAviatorBinding::inflate) {
    private val sp by lazy {
        StarSP(requireContext())
    }
    private val viewModel: AviatorViewModel by viewModels()
    private lateinit var playerPlaneView: DraggableView<ImageView>
    private val xy by lazy {
        val display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        Pair(size.x, size.y)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPlayerView()
        setBalance()

        viewModel.starsCallback = {
            sp.setBalance(1)
            setBalance()
        }

        binding.menu.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.booster.setOnClickListener {
            if (!viewModel.isBoost) {
                if (sp.getBalance() >= 15) {
                    viewModel.boost()
                    sp.setBalance(-15)
                    setBalance()
                    shortToast(requireContext(), "Boost was activated for 15 seconds, scores X2")
                } else {
                    shortToast(requireContext(), "Not enough stars")
                }
            }
        }

        binding.magnet.setOnClickListener {
            if (!viewModel.isMagnet) {
                if (sp.getBalance() >= 15) {
                    viewModel.magnet()
                    sp.setBalance(-15)
                    setBalance()
                    shortToast(requireContext(), "Magnet was activated for 15 seconds")
                } else {
                    shortToast(requireContext(), "Not enough stars")
                }
            }
        }

        binding.timer.setOnClickListener {
            if (!viewModel.isTimer) {
                if (sp.getBalance() >= 15) {
                    viewModel.timer()
                    sp.setBalance(-15)
                    setBalance()
                    shortToast(requireContext(), "Timer was activated for 30 seconds, sky is clear")
                } else {
                    shortToast(requireContext(), "Not enough stars")
                }
            }
        }

        viewModel.enemies.observe(viewLifecycleOwner) {
            binding.enemiesLayout.removeAllViews()
            it.forEach { enemy ->
                val enemyView = ImageView(requireContext())
                enemyView.layoutParams = ViewGroup.LayoutParams(dpToPx(120), dpToPx(90))
                enemyView.setImageResource(R.drawable.enemy)
                enemyView.x = enemy.first
                enemyView.y = enemy.second
                binding.enemiesLayout.addView(enemyView)
            }
        }

        viewModel.stars.observe(viewLifecycleOwner) {
            binding.starsLayout.removeAllViews()
            it.forEach { star ->
                val starView = ImageView(requireContext())
                starView.layoutParams = ViewGroup.LayoutParams(dpToPx(30), dpToPx(30))
                starView.setImageResource(R.drawable.star)
                starView.x = star.first
                starView.y = star.second
                binding.starsLayout.addView(starView)
            }
        }

        viewModel.lives.observe(viewLifecycleOwner) {
            binding.livesLayout.removeAllViews()
            repeat(it) {
                val liveView = ImageView(requireContext())
                liveView.layoutParams = LinearLayout.LayoutParams(dpToPx(20), dpToPx(20)).apply {
                    marginStart = dpToPx(2)
                    marginEnd = dpToPx(2)
                }
                liveView.setImageResource(R.drawable.life)
                binding.livesLayout.addView(liveView)
            }

            if (it == 0 && viewModel.gameState) {
                viewModel.gameState = false
                viewModel.stop()
                findNavController().navigate(FragmentAviatorDirections.actionFragmentAviatorToDialogOver(viewModel.scores.value!!, viewModel.starsAmount))
            }
        }

        viewModel.scores.observe(viewLifecycleOwner) {
            binding.scores.text = it.toString()
        }

        lifecycleScope.launch {
            delay(30)
            if (viewModel.playerXY == 0f to 0f) {
                viewModel.setPlayerXY(
                    ((xy.first / 2) - (binding.player.width / 2)).toFloat(),
                    xy.second - dpToPx(180).toFloat()
                )
                playerPlaneView.getView().x = viewModel.playerXY.first
                playerPlaneView.getView().y = viewModel.playerXY.second
            }
            if (viewModel.gameState) {
                viewModel.start(
                    0,
                    xy.first,
                    dpToPx(120),
                    xy.second,
                    dpToPx(90),
                    binding.player.width,
                    binding.player.height,
                    dpToPx(30)
                )
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    private fun setBalance() {
        binding.balance.text = sp.getBalance().toString()
    }

    private fun setupPlayerView() {
        playerPlaneView = DraggableView.Builder(binding.player)
            .setListener(object : DraggableListener {
                override fun onPositionChanged(view: View) {
                    viewModel.setPlayerXY(x = view.x, y = view.y)
                }
            })
            .build()
        playerPlaneView.getView().x = viewModel.playerXY.first
        playerPlaneView.getView().y = viewModel.playerXY.second
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}