package edu.utap.mypet.petEdit

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.mypet.MainActivity
import edu.utap.mypet.MainViewModel
import edu.utap.mypet.R
import edu.utap.mypet.todoList.ToDoItem

class PetsAdapter (private val viewModel: MainViewModel,
private val editProfile:(Int) -> Unit)
    : ListAdapter<PetProfile, PetsAdapter.VH>(Diff()){

    class Diff : DiffUtil.ItemCallback<PetProfile>() {
        override fun areItemsTheSame(oldItem: PetProfile, newItem: PetProfile): Boolean {
            return oldItem.profileID == newItem.profileID
        }

        override fun areContentsTheSame(oldItem: PetProfile, newItem: PetProfile): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.ownerUid == newItem.ownerUid
                    && oldItem.pictureUUIDs == newItem.pictureUUIDs
                    && oldItem.petName == newItem.petName
                    && oldItem.species == newItem.species
                    && oldItem.breed == newItem.breed
                    && oldItem.gender == newItem.gender
                    && oldItem.birthdate == newItem.birthdate
                    && oldItem.weight == newItem.weight
                    && oldItem.microchip == newItem.microchip
                    && oldItem.rabiesTag == newItem.rabiesTag
                    && oldItem.others == newItem.others
        }
    }
    inner class VH (itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        private var title: TextView = itemView.findViewById(R.id.petTitle)
        private var gender: TextView = itemView.findViewById(R.id.gender)
        private var image: ImageView = itemView.findViewById(R.id.image)
        private var addText: TextView = itemView.findViewById(R.id.add_text)

        init {
            image.setOnClickListener {
                editProfile(adapterPosition)
                Log.d("adapter position", "$adapterPosition")
            }

        }

        fun bind(item: PetProfile?) {
            if (item == null) return
            title.text = item.petName
            gender.text = item.gender
            //bind pic
            if(item.pictureUUIDs.isNotEmpty()){
                viewModel.glideFetch(item.pictureUUIDs, image)
                addText.visibility = View.GONE
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.one_profile, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(viewModel.getPet(holder.adapterPosition))
    }
}


