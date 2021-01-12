package edu.utap.mypet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.mypet.petEdit.PetProfile
import edu.utap.mypet.reminders.Reminder

class VMDBHelperReminder(reminderList: MutableLiveData<List<Reminder>>) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        dbFetchReminders(reminderList)
    }
    
    private fun dbFetchReminders(reminderList: MutableLiveData<List<Reminder>>) {
        db.collection("allReminders")
                .orderBy("petPicked")//, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    Log.d(javaClass.simpleName, "allReminders fetch ${result!!.documents.size}")
                    // NB: This is done on a background thread
                    reminderList.postValue(result.documents.mapNotNull {
                        it.toObject(Reminder::class.java)
                    })
                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "allReminders FAILED ", it)
                }
    }

    fun updateReminder(
            reminder: Reminder,
            reminderList: MutableLiveData<List<Reminder>>
    ) {

        db.collection("allReminders")
                .document(reminder.reminderID)
                .set(reminder)
                //EEE // XXX Writing a note
                .addOnSuccessListener {
                    Log.d(
                            javaClass.simpleName,
                            "Reminder update \"${reminder.description}\"  id: ${reminder.reminderID}"
                    )
                    dbFetchReminders(reminderList)
                }
                .addOnFailureListener { e ->
                    Log.d(javaClass.simpleName, "Reminder update FAILED \"${reminder.reminderID}\"")
                    Log.w(javaClass.simpleName, "Error ", e)
                }
    }

    fun createNewReminder(
            reminder: Reminder,
            reminderList: MutableLiveData<List<Reminder>>
    ) {
        //SSS
        reminder.reminderID = db.collection("allReminders").document().id

        db.collection("allReminders")
                .document(reminder.reminderID)
                .set(reminder)
                .addOnSuccessListener {
                    Log.d(
                            javaClass.simpleName,
                            "reminder create \"${reminder.petPicked}\" id: ${reminder.reminderID}"
                    )
                    dbFetchReminders(reminderList)
                }
                .addOnFailureListener { e ->
                    Log.d(javaClass.simpleName, "Reminder create FAILED \"${reminder.reminderID}\"")
                    Log.w(javaClass.simpleName, "Error ", e)
                }
    }

    fun removeReminder(
            reminder: Reminder,
            reminderList: MutableLiveData<List<Reminder>>
    ) {

        db.collection("allReminders").document(reminder.reminderID).delete()
                .addOnSuccessListener {
                    Log.d(
                            javaClass.simpleName,
                            "Reminder delete \"${reminder.petPicked}\" id: ${reminder.reminderID}"
                    )
                    dbFetchReminders(reminderList)
                }
                .addOnFailureListener { e ->
                    Log.d(javaClass.simpleName, "Reminder deleting FAILED \"${reminder.reminderID}\"")
                    Log.w(javaClass.simpleName, "Error adding document", e)
                }
    }
}
