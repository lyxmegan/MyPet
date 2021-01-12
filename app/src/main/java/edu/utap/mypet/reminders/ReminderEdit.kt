package edu.utap.mypet.reminders

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import edu.utap.mypet.MainActivity
import edu.utap.mypet.MainViewModel
import edu.utap.mypet.R
import edu.utap.mypet.petEdit.PetsEdit
import kotlinx.android.synthetic.main.one_reminder.*
import kotlinx.android.synthetic.main.pet_edit.*
import kotlinx.android.synthetic.main.reminder_edit.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class ReminderEdit: Fragment(R.layout.reminder_edit) {
    private val viewModel: MainViewModel by activityViewModels()
    private var position = -1
    var cal = Calendar.getInstance()
    companion object {
        private const val positionKey = "positionKey"
        // Argument is the database key, which is -1 for a not-yet-created
        // note and set by the database for every other note
        fun newInstance(position: Int) : ReminderEdit {
            val frag = ReminderEdit()
            frag.arguments = Bundle().apply {
                putInt(positionKey, position)
            }
            return frag
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        position = arguments?.getInt(positionKey) ?: -1
        Log.d("what is the position in the reminder list", "$position")
        //get pet names from database
        val petNames = viewModel.getPetNames()
        Log.d("pet names in reminder edit", "$petNames")
        val aa = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, petNames) }
        // Set layout to use when the list of choices appear
        aa?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        pet_spinner.adapter = aa
        pet_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                Log.d("which pet is being selected? ", "$selectedItem")

            }
        }

        if(position == -1) {
            viewModel.setTitle("ADD A REMINDER")
            reminderChangeBut.text = "Cancel"

        } else {
            viewModel.setTitle("EDIT REMINDER")
            val reminder = viewModel.getReminder(position)
            val pet = reminder.petPicked
            val position = petNames.indexOf(pet)
            pet_spinner.setSelection(position)
            reminder_description.text.insert(0, reminder.description)
            pickedDate.text = reminder.pickedDate
            pickedTime.text = reminder.pickedTime
            notificationCheckBox.isChecked = reminder.checked == true
            additional_notes.text.insert(0, reminder.otherNotes)
            reminderChangeBut.text = "Delete"
        }

        //handling the date picked and time picked
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "MM.dd.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            pickedDate.text = sdf.format(cal.time)
        }
        pick_date.setOnClickListener {
            context?.let { it1 ->
                DatePickerDialog(it1, dateSetListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        }

        pick_time.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                pickedTime.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        //handle save button
        reminderSaveBut.setOnClickListener {
            if(pet_spinner.selectedItem == "None" || reminder_description.text.isNullOrEmpty()){
                Toast.makeText(activity,
                        "Please enter the required information!",
                        Toast.LENGTH_LONG).show()
            }else {
                if(position == -1) {
                    Log.d(javaClass.simpleName, " pos $position")
                    //create reminder in the database
                    if(pickedDate.text == "mm/dd/yyyy" && pickedTime.text =="hh:mm"){
                        notificationCheckBox.isChecked = false
                        Toast.makeText(context, "Has to pick the date or time to set the notification", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.createReminder(pet_spinner.selectedItem.toString(),reminder_description.text.toString(),
                    pickedDate.text.toString(),pickedTime.text.toString(), notificationCheckBox.isChecked,
                    additional_notes.text.toString())


                } else {
                    Log.d(javaClass.simpleName, " pos $position")
                    //update reminder in the database
                    if(pickedDate.text == "mm/dd/yyyy" && pickedTime.text =="hh:mm"){
                        notificationCheckBox.isChecked = false
                        Toast.makeText(context, "Has to pick the date or time to set the notification", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.updateReminder(position,pet_spinner.selectedItem.toString(),reminder_description.text.toString(),
                            pickedDate.text.toString(),pickedTime.text.toString(), notificationCheckBox.isChecked,
                            additional_notes.text.toString())

                }

                parentFragmentManager.popBackStack()
            }
        }

        //handle cancel or delete button
        reminderChangeBut.setOnClickListener {
            if(position == -1) {
                parentFragmentManager.popBackStack()
            } else {
                //delete reminder from the data base
               viewModel.removeReminderAt(position)
                parentFragmentManager.popBackStack()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        if (activity is MainActivity) {
            val  mainActivity = activity as MainActivity
            mainActivity.setBottomNavigationVisibility(View.VISIBLE)
        }
    }

}