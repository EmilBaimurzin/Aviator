package com.avia.game.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.avia.game.R
import com.avia.game.core.library.ViewBindingDialog
import com.avia.game.databinding.DialogOverBinding

class DialogOver: ViewBindingDialog<DialogOverBinding>(DialogOverBinding::inflate) {
    private val args: DialogOverArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Dialog_No_Border)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setCancelable(false)
        dialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                findNavController().popBackStack(R.id.fragmentFirst, false, false)
                true
            } else {
                false
            }
        }

        binding.stars.text = args.stars.toString()
        binding.scores.text = args.scores.toString()

        binding.menuButton.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentFirst, false, false)
        }

        binding.restart.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentFirst, false, false)
            findNavController().navigate(R.id.action_fragmentMain_to_fragmentAviator)
        }

    }
}