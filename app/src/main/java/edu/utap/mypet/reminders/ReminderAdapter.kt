package edu.utap.mypet.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.mypet.AlarmReceiver
import edu.utap.mypet.MainActivity
import edu.utap.mypet.MainViewModel
import edu.utap.mypet.R
import edu.utap.mypet.petEdit.PetProfile
import edu.utap.mypet.petEdit.PetsAdapter
import java.util.*

class ReminderAdapter(private val viewModel: MainViewModel,
                      private val editReminder:(Int) -> Unit)
    : ListAdapter<Reminder, ReminderAdapter.VH>(ReminderAdapter.Diff()) {

    class Diff : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.reminderID == newItem.reminderID
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.ownerUid == newItem.ownerUid
                    && oldItem.petPicked == newItem.petPicked
                    && oldItem.pickedDate == newItem.pickedDate
                    && oldItem.pickedTime == newItem.pickedTime
                    && oldItem.checked  == newItem.checked
                    && oldItem.otherNotes == newItem.otherNotes
        }
    }

    inner class VH (itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        private var name: TextView = itemView.findViewById(R.id.re_petName)
        private var content: TextView = itemView.findViewById(R.id.reminder_content)
        private var date: TextView = itemView.findViewById(R.id.reminder_date)
        private var time: TextView = itemView.findViewById(R.id.reminder_time)

        init {
            itemView.setOnClickListener {
                editReminder(adapterPosition)
            }

        }

        fun bind(item: Reminder?) {
            if (item == null) return
            name.text = item.petPicked
            content.text = item.description
            date.text = item.pickedDate
            time.text = item.pickedTime
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderAdapter.VH {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.one_reminder, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: ReminderAdapter.VH, position: Int) {
        holder.bind(viewModel.getReminder(holder.adapterPosition))

    }

}