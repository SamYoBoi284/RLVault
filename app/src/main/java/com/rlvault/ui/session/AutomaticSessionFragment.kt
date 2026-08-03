package com.rlvault.ui.session

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rlvault.R
import com.rlvault.databinding.FragmentAutomaticSessionBinding
import com.rlvault.di.ServiceLocator


class AutomaticSessionFragment :
    Fragment(R.layout.fragment_automatic_session) {


    private var _binding:
            FragmentAutomaticSessionBinding? = null

    private val binding
        get() = _binding!!


    private val viewModel:
            AutomaticSessionViewModel by viewModels {

        AutomaticSessionViewModel.Factory(
            ServiceLocator.sessionRepository
        )
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        _binding =
            FragmentAutomaticSessionBinding.bind(view)


        binding.startButton.setOnClickListener {

            viewModel.startSession()

        }


        binding.endButton.setOnClickListener {

    showEndSessionDialog()

}


        viewModel.activeSession.observe(
            viewLifecycleOwner
        ) { session ->


            if (session == null) {

                binding.statusText.text =
                    "No Active Session"

                binding.startButton.visibility =
                    View.VISIBLE

                binding.endButton.visibility =
                    View.GONE


            } else {


                binding.statusText.text =
                    "Active Session\nStarted: ${session.startTime}"


                binding.startButton.visibility =
                    View.GONE

                binding.endButton.visibility =
                    View.VISIBLE
            }
        }
    }


private fun showEndSessionDialog() {

    val dialogView = layoutInflater.inflate(
        R.layout.dialog_end_session,
        null
    )


    val winsInput =
        dialogView.findViewById<android.widget.EditText>(
            R.id.winsInput
        )

    val lossesInput =
        dialogView.findViewById<android.widget.EditText>(
            R.id.lossesInput
        )

    val rankInput =
        dialogView.findViewById<android.widget.EditText>(
            R.id.rankInput
        )

    val notesInput =
        dialogView.findViewById<android.widget.EditText>(
            R.id.notesInput
        )


    androidx.appcompat.app.AlertDialog.Builder(requireContext())
        .setTitle("End Session")
        .setView(dialogView)

        .setPositiveButton("Save") { _, _ ->


            viewModel.endSession(

                wins =
                    winsInput.text.toString()
                        .toIntOrNull() ?: 0,


                losses =
                    lossesInput.text.toString()
                        .toIntOrNull() ?: 0,


                rank =
                    rankInput.text.toString()
                        .ifBlank { null },


                notes =
                    notesInput.text.toString()
                        .ifBlank { null }
            )

        }

        .setNegativeButton(
            "Cancel",
            null
        )

        .show()
}

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}