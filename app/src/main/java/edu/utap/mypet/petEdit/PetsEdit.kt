package edu.utap.mypet.petEdit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.utap.mypet.MainActivity
import edu.utap.mypet.MainViewModel
import edu.utap.mypet.R
import edu.utap.mypet.Storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.pet_edit.*


class  PetsEdit : Fragment(R.layout.pet_edit) {
    private lateinit var pictureUUIDs: String
    private var position = -1
    private val viewModel: MainViewModel by activityViewModels()

    companion object {
        private const val positionKey = "positionKey"
        // Argument is the database key, which is -1 for a not-yet-created
        // note and set by the database for every other note
        fun newInstance(position: Int) : PetsEdit {
            val frag = PetsEdit()
            frag.arguments = Bundle().apply {
                putInt(positionKey, position)
            }
            return frag
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // One class for two differently labeled actions
        position = arguments?.getInt(positionKey) ?: -1
        Log.d("what is the position", "$position")
        val listOfPets = viewModel.getPetLists()
        Log.d("list of pets size", "${listOfPets?.size}")

        if(position == -1) {
            viewModel.setTitle("ADD YOUR PET")
            changeBut.text = "Cancel"
            pictureUUIDs = ""
            Log.d("postion -1: pictureID", "$pictureUUIDs")


        } else {
            val profile = viewModel.getPet(position)
            pictureUUIDs = profile.pictureUUIDs
            petName.text.insert(0, profile.petName)
            petSpecies.text.insert(0, profile.species)
            petBreed.text.insert(0, profile.breed)
            petGender.text.insert(0, profile.gender)
            petBirthdate.text.insert(0, profile.birthdate)
            petWeight.text.insert(0, profile.weight)
            petMicrochip.text.insert(0, profile.microchip)
            petTag.text.insert(0, profile.rabiesTag)
            petOthers.text.insert(0, profile.others)
            changeBut.text = "Delete"
            viewModel.setTitle(profile.petName.toUpperCase())
            Log.d("position not -1, picId", "$pictureUUIDs")
            if(pictureUUIDs.isEmpty()){
                //do nothing
            }else {
                viewModel.glideFetch(pictureUUIDs, addPetImage)
                addText.visibility = View.GONE
            }

        }

        addPetImage.setOnClickListener {
            Log.d("what is the pictureUUID now? ", "$pictureUUIDs")
            val id = pictureUUIDs
            Log.d(javaClass.simpleName, "id: $id")
            viewModel.takePhoto {
                if (activity is MainActivity) {
                    val mainActivity = activity as MainActivity
                    mainActivity.setBottomNavigationVisibility(View.GONE)
                }
                if (pictureUUIDs.isNotEmpty()) {
                    Storage.deleteImage(pictureUUIDs)
                    pictureUUIDs = ""
                    pictureUUIDs = it
                    viewModel.glideFetch(pictureUUIDs, addPetImage)
                } else {
                    pictureUUIDs = it
                    Log.d(javaClass.simpleName, "picUUID_1: $pictureUUIDs")
                    viewModel.glideFetch(it, addPetImage)
                    addText.visibility = View.GONE
                }
                Log.d(javaClass.simpleName, "picUUID_2: $pictureUUIDs")
            }

        }


        petSaveBut.setOnClickListener {
            if(petName.text.isNullOrEmpty() || petSpecies.text.isNullOrEmpty() || petBreed.text.isNullOrEmpty()) {
                Toast.makeText(activity,
                        "Please enter the required information!",
                        Toast.LENGTH_LONG).show()
            } else {
                if(position == -1) {
                    Log.d(javaClass.simpleName, " pos $position")
                    //create pet profile in the database
                    Log.d("when save picId", "$pictureUUIDs")
                    viewModel.createPetProfile(pictureUUIDs, petName.text.toString(), petSpecies.text.toString(),petBreed.text.toString(),
                    petGender.text.toString(), petBirthdate.text.toString(), petWeight.text.toString(), petMicrochip.text.toString(),
                    petTag.text.toString(), petOthers.text.toString())
                } else {
                    Log.d(javaClass.simpleName, " pos $position")
                    //update pet profile in the database
                    Log.d("when save picId", "$pictureUUIDs")
                    viewModel.updatePetProfile(position,pictureUUIDs,petName.text.toString(), petSpecies.text.toString(),petBreed.text.toString(),
                            petGender.text.toString(), petBirthdate.text.toString(), petWeight.text.toString(), petMicrochip.text.toString(),
                            petTag.text.toString(), petOthers.text.toString())
                }
               parentFragmentManager.popBackStack()
            }
        }

        changeBut.setOnClickListener {
            if(position == -1) {
                parentFragmentManager.popBackStack()
            } else {
                //delete pet profile from the data base
                viewModel.removePetAt(position)
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("Pets Edit on pause", "being called ")
        if (activity is MainActivity) {
            val  mainActivity = activity as MainActivity
            mainActivity.setBottomNavigationVisibility(View.VISIBLE)
        }
    }

}

