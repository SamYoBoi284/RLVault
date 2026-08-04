package com.rlvault.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rlvault.R

/**
 * Settings destination — placeholder only. Holds About / Version for now; Developer Options
 * will live here later, behind the classic "tap the version number seven times" unlock.
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.versionValue).setOnClickListener {
            // TODO: Tapping this 7 times in a row should unlock Developer Options (which will
            // then appear here in Settings). Not implemented yet — see DeveloperModeActivity
            // for the existing dev-mode screen this will eventually link to.
        }
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}
