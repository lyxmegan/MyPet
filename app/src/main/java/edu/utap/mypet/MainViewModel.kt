package edu.utap.mypet

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import java.time.LocalDateTime
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.mypet.glide.Glide
import edu.utap.mypet.petEdit.PetProfile
import edu.utap.mypet.reminders.Reminder
import edu.utap.mypet.todoList.ToDoItem
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.time.measureTimedValue


class MainViewModel(application: Application,
        // Handle provided if framework kills application, but not if user kills it.
                    private val state: SavedStateHandle
) : AndroidViewModel(application){
    companion object {
        // Remember the uuid, and hence file name of file camera will create
        const val pictureUUIDKey = "pictureUUIDKey"
        const val photoSuccessKey = "photoSuccessKey"
        const val takePhotoIntentKey = "takePhotoIntentKey"
    }
    private val appContext = getApplication<Application>().applicationContext

    private var storageDir =
            getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var firebaseAuthLiveData = FirestoreAuthLiveData()
    private var toDoList = MutableLiveData<List<ToDoItem>>()
    private var petsList = MutableLiveData<List<PetProfile>>()
    private var reminderList = MutableLiveData<List<Reminder>>()
    private var title = MutableLiveData("PETS")
    private val dbHelp = ViewModelDBHelper(petsList)
    private val dbReminderHelper = VMDBHelperReminder(reminderList)
    private lateinit var auth: FirebaseUser
    private lateinit var crashMe: String
    private val widthPx = (appContext.resources.displayMetrics.widthPixels)
    val displayMetrics = appContext.resources.displayMetrics
    val height = (150 * displayMetrics.density).toInt()

    fun observeFirebaseAuthLiveData(): LiveData<FirebaseUser?> {
        return firebaseAuthLiveData
    }

    fun observeTitle(): LiveData<String> {
        return title
    }
    fun setTitle(_title: String) {
        title.value = _title
    }

    private fun noPhoto() {
        Log.d(javaClass.simpleName, "Function must be initialized to something that can start the camera intent")
        crashMe.plus(" ")
    }

    private var takePhotoIntent: () -> Unit = ::noPhoto

    private fun defaultPhoto(@Suppress("UNUSED_PARAMETER") path: String) {
        Log.d(javaClass.simpleName,"Function must be initialized to photo callback")
        crashMe.plus(" ")
    }
    private var photoSuccess: (path: String)->Unit = ::defaultPhoto

    fun isPetsEmpty(): Boolean {
        return petsList.value.isNullOrEmpty()
    }

    fun observePets(): LiveData<List<PetProfile>> {
        return petsList
    }

    fun getPet(position: Int) : PetProfile {
        val profile = petsList.value?.get(position)
        return profile!!
    }
     fun getPetLists(): List<PetProfile>?{
         return petsList.value
     }

    fun getPetNames(): List<String>{
        var petNameArray : List<String> = emptyList()
        petNameArray += "None"
        getPetLists()?.forEach {
               petNameArray += it.petName
        }
        return petNameArray
    }

    fun updatePetProfile(position: Int,picture: String , name: String, petSpecies: String,
                         petBreed: String, petGender: String, petBirthDate: String, petWeight: String,
                         petMicrochip: String, petRabiesTag: String, petOthers: String){
        val profile = getPet(position)
        profile.apply {
            pictureUUIDs = picture
            petName = name
            species = petSpecies
            breed = petBreed
            gender = petGender
            birthdate = petBirthDate
            weight = petWeight
            microchip = petMicrochip
            rabiesTag = petRabiesTag
            others = petOthers
        }
        dbHelp.updatePet(profile, petsList)
    }

    fun createPetProfile(picture: String, name: String, petSpecies: String,
                        petBreed: String, petGender: String, petBirthDate: String, petWeight: String,
                        petMicrochip: String, petRabiesTag: String, petOthers: String){
        auth = FirebaseAuth.getInstance().getCurrentUser()!!
        val profile = PetProfile(
                name = auth.displayName!!,
                ownerUid = auth.uid,
                pictureUUIDs = picture,
                petName = name,
                species = petSpecies,
                breed = petBreed,
                gender = petGender,
                birthdate = petBirthDate,
                weight = petWeight,
                microchip = petMicrochip,
                rabiesTag = petRabiesTag,
                others = petOthers
        )
        dbHelp.createNewPet(profile, petsList)
    }

    fun removePetAt(position: Int){
        val pet = getPet(position)
        Log.d(javaClass.simpleName, "remote pet at pos: $position id: ${pet.profileID}")
        dbHelp.removePet(pet, petsList)
    }

    fun getPetsProfile(){
        if(FirebaseAuth.getInstance().currentUser == null) {
            petsList.value = listOf()
            return
        }

        db.collection("allPetsProfiles")
                .orderBy("petName")
                .addSnapshotListener { querySnapshot, ex ->
                    if (ex != null) {

                        return@addSnapshotListener
                    }

                    if (querySnapshot != null) {
                        petsList.value = querySnapshot.documents.mapNotNull {
                            it.toObject(PetProfile::class.java)
                        }
                    }
                }

    }

    fun setPhotoIntent(_takePhotoIntent: () -> Unit) {
        takePhotoIntent = _takePhotoIntent
    }

    // Get callback for when camera intent returns.
    // Send intent to take picture
    fun takePhoto(_photoSuccess: (String) -> Unit) {
        photoSuccess = _photoSuccess
        takePhotoIntent.invoke()
    }

    fun getPhotoURI(): Uri {
        // Create an image file name
        state.set(pictureUUIDKey,  UUID.randomUUID().toString())
        Log.d(javaClass.simpleName, "pictureUUID ${state.get<String>(pictureUUIDKey)}")
        var photoUri: Uri? = null
        // Create the File where the photo should go
        try {
            val localPhotoFile = File(storageDir, "${state.get<String>(pictureUUIDKey)}.jpg")
            Log.d(javaClass.simpleName, "photo path ${localPhotoFile.absolutePath}")
            photoUri = FileProvider.getUriForFile(
                    appContext,
                    "edu.utap.mypet",
                    localPhotoFile
            )
        } catch (ex: IOException) {
            // Error occurred while creating the File
            Log.d(javaClass.simpleName, "Cannot create file", ex)
        }
        // CRASH.  Production code should do something more graceful
        return photoUri!!
    }

    fun pictureSuccess() {
        val pictureUUID = state.get(pictureUUIDKey) ?: ""
        val localPhotoFile = File(storageDir, "${pictureUUID}.jpg")
        // Wait until photo is successfully uploaded before calling back
        Storage.uploadImage(localPhotoFile, pictureUUID) {
            photoSuccess(pictureUUID)
            photoSuccess = ::defaultPhoto
            state.set(pictureUUIDKey, "")
        }
    }
    fun pictureFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        state.set(pictureUUIDKey, "")
        Log.d(javaClass.simpleName, "pictureFailure pictureUUID cleared")
    }

    //may change the pic size later
    fun glideFetch(pictureUUID: String, imageView: ImageView) {
        Glide.fetch(Storage.uuid2StorageReference(pictureUUID),
                imageView, widthPx, (1.1*widthPx).toInt() )
    }

    fun observeToDoList(): LiveData<List<ToDoItem>> {
        return toDoList
    }

    fun getList(): List<ToDoItem>? {
        return toDoList.value
    }

    fun isToDoEmpty(): Boolean {
        return toDoList.value.isNullOrEmpty()
    }


    fun myUid(): String? {
        return firebaseAuthLiveData.value?.uid
    }

    fun addToDoItem(toDoItem: ToDoItem) {
        Log.d(
            "ToDoViewModel",
            String.format(
                "saveToDoItemRow ownerUid(%s) name(%s) %s",
                toDoItem.ownerUid,
                toDoItem.name,
                toDoItem.todoItem,
                toDoItem.status
            )
        )
        // https://firebase.google.com/docs/firestore/manage-data/add-data#add_a_document
        // Remember to set the rowID of the chatRow before saving it
        toDoItem.rowID = db.collection("globalToDoItems").document().id
        db.collection("globalToDoItems")
            .document(toDoItem.rowID)
            .set(toDoItem)
            .addOnSuccessListener {
                Log.d(
                    javaClass.simpleName,
                    "To-do Item create \"id: ${toDoItem.rowID}"
                )

            }
            .addOnFailureListener { e ->
                Log.d(javaClass.simpleName, "Note create FAILED ")
                Log.w(javaClass.simpleName, "Error ", e)
            }

    }


    fun getToDoItem() {
        if(FirebaseAuth.getInstance().currentUser == null) {
            Log.d(javaClass.simpleName, "Can't get todo item, no one is logged in")
            toDoList.value = listOf()
            return
        }

        db.collection("globalToDoItems")
            .orderBy("status")
            .addSnapshotListener { querySnapshot, ex ->
                if (ex != null) {

                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    toDoList.value = querySnapshot.documents.mapNotNull {
                        it.toObject(ToDoItem::class.java)
                    }
                }
            }
    }

    fun deleteToDoItem(toDoItem: ToDoItem){
        Log.d("delete to to item", "remote chatRow id: ${toDoItem.rowID}")
        // XXX delete to do item
        db.collection("globalToDoItems").document(toDoItem.rowID)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

    }

    //reminder parts
    fun getReminder(position: Int) : Reminder {
        val reminder = reminderList.value?.get(position)
        return reminder!!
    }

    fun isRemindersEmpty(): Boolean {
        return reminderList.value.isNullOrEmpty()
    }

    fun observeReminders(): LiveData<List<Reminder>> {
        return reminderList
    }

    fun createReminder(name: String, text: String, date: String,
    time: String, setReminder: Boolean, others: String){
        auth = FirebaseAuth.getInstance().getCurrentUser()!!
        val reminder = Reminder(
                name = auth.displayName!!,
                ownerUid = auth.uid,
                petPicked = name,
                description = text,
                pickedDate = date,
                pickedTime = time,
                checked = setReminder,
                otherNotes = others
        )
        dbReminderHelper.createNewReminder(reminder, reminderList)
    }

    fun updateReminder(position: Int, name: String, text: String, date: String,
    time: String, setReminder: Boolean, others: String){
        val reminder = getReminder(position)
        reminder.apply {
            petPicked = name
            description = text
            pickedDate = date
            pickedTime = time
            checked = setReminder
            otherNotes = others
        }
        dbReminderHelper.updateReminder(reminder, reminderList)
    }

    fun removeReminderAt(position: Int){
        val reminder = getReminder(position)
        Log.d(javaClass.simpleName, "remote reminder at pos: $position id: ${reminder.reminderID}")
        dbReminderHelper.removeReminder(reminder, reminderList)
    }

    fun getAllReminders() {
        if(FirebaseAuth.getInstance().currentUser == null) {
            reminderList.value = listOf()
            return
        }

        db.collection("allReminders")
                .orderBy("petPicked")
                .addSnapshotListener { querySnapshot, ex ->
                    if (ex != null) {

                        return@addSnapshotListener
                    }

                    if (querySnapshot != null) {
                        reminderList.value = querySnapshot.documents.mapNotNull {
                            it.toObject(Reminder::class.java)
                        }
                    }
                }

    }

    fun getReminderNotification(list: List<Reminder>): List<Calendar>{
        var calList = listOf<Calendar>()

        list.filter {
            it.checked == true
        }.forEach {
            val cal = Calendar.getInstance()
            if(it.pickedDate == "mm/dd/yyyy"){
                val currentDate = LocalDate.now()
                val formatter  = DateTimeFormatter.ofPattern("yyyy MM dd")
                val text = currentDate.format(formatter)
                val parsedDate = LocalDate.parse(text, formatter).toString().split("-")
                Log.d("Local Date Time", "$parsedDate")
                val currentYear = parsedDate[0].toInt()
                val currentMonth = parsedDate[1].toInt()
                val currentDay = parsedDate[2].toInt()
                cal.set(Calendar.YEAR, currentYear)
                cal.set(Calendar.MONTH, currentMonth-1)
                cal.set(Calendar.DAY_OF_MONTH, currentDay)

            } else {
                val date = it.pickedDate.split(".")
                val month = date[0].toInt()
                val day = date[1].toInt()
                val year = date[2].toInt()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month-1)
                cal.set(Calendar.DAY_OF_MONTH, day)
            }
            if(it.pickedTime == "hh:mm"){
                val currentHour = 0
                val currentMin = 0
                cal.set(Calendar.HOUR_OF_DAY, currentHour)
                cal.set(Calendar.MINUTE, currentMin)
                cal.set(Calendar.SECOND, 0)
            } else{
                val time = it.pickedTime.split(":")
                val hour = time[0].toInt()
                val min = time[1].toInt()
                cal.set(Calendar.HOUR_OF_DAY,hour)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
            }

            Log.d("what is the calendar time in Model", "${cal.time}")
            calList += cal
        }
        Log.d("what is calList now in Model", "${calList.size}")
        return calList
    }

}