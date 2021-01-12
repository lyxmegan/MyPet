package edu.utap.mypet.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.utap.mypet.MainActivity
import edu.utap.mypet.MainViewModel
import edu.utap.mypet.R
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseUser
import edu.utap.mypet.AlarmReceiver
import kotlinx.android.synthetic.main.reminder_frag.*
import java.util.*
import kotlin.random.Random.Default.nextInt


class ReminderFragment:  Fragment(R.layout.reminder_frag) {
    private val viewModel: MainViewModel by activityViewModels()
    var calList = listOf<Calendar>()
    private var currentUser: FirebaseUser? = null
    companion object {
        fun newInstance(): ReminderFragment {
            return ReminderFragment()
        }
    }

    private fun initAuth() {
        viewModel.observeFirebaseAuthLiveData().observe(viewLifecycleOwner, Observer {
            currentUser = it
            Log.d(javaClass.simpleName, "current user: ${it?.displayName}")
        })
    }


    // View exists in onViewCreated, so synthetic imports work.
    // They are null in onCreateView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAuth()
        viewModel.setTitle("REMINDERS")
        addReminderBut.setOnClickListener {
            parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_frame, ReminderEdit.newInstance(-1))
                    .addToBackStack(null)
                    .commit()
            if (activity is MainActivity) {
                val  mainActivity = activity as MainActivity
                mainActivity.setBottomNavigationVisibility(View.GONE)
            }

        }

        val adapter = ReminderAdapter(viewModel) { position ->
            parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_frame, ReminderEdit.newInstance(position))
                    .addToBackStack(null)
                    .commit()
            if (activity is MainActivity) {
                val  mainActivity = activity as MainActivity
                mainActivity.setBottomNavigationVisibility(View.GONE)
            }

        }

        val rv = view.findViewById<RecyclerView>(R.id.reminderListRV)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(context)
        val itemDecor = DividerItemDecoration(rv.context, LinearLayoutManager.VERTICAL)
        itemDecor.setDrawable(ContextCompat.getDrawable(rv.context, (R.drawable.divider))!!)
        rv.addItemDecoration(itemDecor)

        parentFragmentManager
                .addOnBackStackChangedListener {
                    Log.d(javaClass.simpleName, "backStack!")
                    adapter.notifyDataSetChanged()
                }


        viewModel.observeReminders().observe(viewLifecycleOwner, Observer {

            adapter.submitList(it)
            toggleEmptyReminder()
            viewModel.getReminderNotification(it).forEach {
                calList += it
            }
            for(cal in calList){
                val currentTime = Calendar.getInstance()
                Log.d(javaClass.simpleName, "current time $currentTime")
                if(cal > currentTime){
                    startAlarm(cal)
                }

            }
        })
        toggleEmptyReminder()
    }

    fun startAlarm(calendar: Calendar) {
        val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val code =  (0..10000).random()
        val pendingIntent = PendingIntent.getBroadcast(context, code, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Log.d("start alarm go through", "end")
    }

    private fun toggleEmptyReminder() {
        if (viewModel.isRemindersEmpty()) {
            no_reminder_image.visibility = View.VISIBLE
            no_reminder_text.visibility = View.VISIBLE

        } else {
            no_reminder_image.visibility = View.GONE
            no_reminder_text.visibility = View.GONE

        }
    }
}