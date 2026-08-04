package com.rlvault.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rlvault.R

/**
 * Progress destination — placeholder only. Will eventually hold Statistics, Goals, and
 * Progress; none of that is implemented yet.
 */
class ProgressFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    companion object {
        const val TAG = "ProgressFragment"
    }
}
