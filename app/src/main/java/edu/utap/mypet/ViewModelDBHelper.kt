package edu.utap.mypet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.mypet.petEdit.PetProfile


class ViewModelDBHelper(
        petsList: MutableLiveData<List<PetProfile>>
) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        dbFetchPets(petsList)
    }

    private fun dbFetchPets(petsList: MutableLiveData<List<PetProfile>>) {
        db.collection("allPetsProfiles")
                .orderBy("petName")//, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    Log.d(javaClass.simpleName, "allPets fetch ${result!!.documents.size}")
                    // NB: This is done on a background thread
                    petsList.postValue(result.documents.mapNotNull {
                        it.toObject(PetProfile::class.java)
                    })
                }
                .addOnFailureListener {
                    Log.d(javaClass.simpleName, "allPets fetch FAILED ", it)
                }
    }

    // After we successfully modify the db, we refetch the contents to update our
    // live data.  Hence the dbFetchNotes() calls below.
    fun updatePet(
            pet: PetProfile,
            petsList: MutableLiveData<List<PetProfile>>
    ) {
        val pictureUUIDs = pet.pictureUUIDs
        //SSS
        db.collection("allPetsProfiles")
                .document(pet.profileID)
                .set(pet)
                //EEE // XXX Writing a note
                .addOnSuccessListener {
                    Log.d(
                            javaClass.simpleName,
                            "pet update \"${pet.petName}\" len ${pet.pictureUUIDs} id: ${pet.profileID}"
                    )
                    dbFetchPets(petsList)
                }
                .addOnFailureListener { e ->
                    Log.d(javaClass.simpleName, "Pet update FAILED \"${pet.petName}\"")
                    Log.w(javaClass.simpleName, "Error ", e)
                }
    }

    fun createNewPet(
            pet: PetProfile,
            petsList: MutableLiveData<List<PetProfile>>
    ) {
        //SSS
        pet.profileID = db.collection("allPetsProfiles").document().id
        //EEE // XXX set note.noteID

        db.collection("allPetsProfiles")
                .document(pet.profileID)
                .set(pet)
                .addOnSuccessListener {
                    Log.d(
                            javaClass.simpleName,
                            "Pet create \"${pet.petName}\" id: ${pet.profileID}"
                    )
                    dbFetchPets(petsList)
                }
                .addOnFailureListener { e ->
                    Log.d(javaClass.simpleName, "Pet create FAILED \"${pet.profileID}\"")
                    Log.w(javaClass.simpleName, "Error ", e)
                }
    }

    fun removePet(
            pet: PetProfile,
            petsList: MutableLiveData<List<PetProfile>>
    ) {
        val uuid = pet.pictureUUIDs
        if(uuid.isNotEmpty()) {
            Storage.deleteImage(uuid)

        }
        db.collection("allPetsProfiles").document(pet.profileID).delete()
                .addOnSuccessListener {
                    Log.d(
                            javaClass.simpleName,
                            "Pet delete \"${pet.petName}\" id: ${pet.profileID}"
                    )
                    dbFetchPets(petsList)
                }
                .addOnFailureListener { e ->
                    Log.d(javaClass.simpleName, "Pet deleting FAILED \"${pet.profileID}\"")
                    Log.w(javaClass.simpleName, "Error adding document", e)
                }
    }
}