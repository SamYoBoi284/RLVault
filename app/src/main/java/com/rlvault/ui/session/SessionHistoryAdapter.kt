package com.rlvault.ui.session


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rlvault.data.model.Session
import com.rlvault.databinding.ItemSessionHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class SessionHistoryAdapter :
    RecyclerView.Adapter<SessionHistoryAdapter.ViewHolder>() {


    private val sessions =
        mutableListOf<Session>()



    fun submitList(
        list: List<Session>
    ) {

        sessions.clear()

        sessions.addAll(list)

        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {


        val binding =
            ItemSessionHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )


        return ViewHolder(binding)
    }



    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        holder.bind(
            sessions[position]
        )
    }



    override fun getItemCount() =
        sessions.size



    class ViewHolder(
        private val binding:
        ItemSessionHistoryBinding
    ) :
        RecyclerView.ViewHolder(
            binding.root
        ) {


        fun bind(
            session: Session
        ) {


            val date =
                SimpleDateFormat(
                    "MMM dd yyyy",
                    Locale.getDefault()
                )
                    .format(
                        Date(session.date)
                    )


            binding.dateText.text =
                date


            binding.resultText.text =
                "Wins: ${session.wins}  Losses: ${session.losses}"


            binding.rankText.text =
                "Rank: ${session.rank ?: "Unknown"}"


            val duration =
                session.durationMs
                    ?.div(1000)
                    ?: 0


            val hours =
    duration / 3600

val minutes =
    (duration % 3600) / 60

val seconds =
    duration % 60


binding.durationText.text =
    String.format(
        Locale.getDefault(),
        "Duration: %02d:%02d:%02d",
        hours,
        minutes,
        seconds
    )


            binding.typeText.text =
                if (session.isAutomatic)
                    "Automatic"
                else
                    "Manual"
        }
    }
}