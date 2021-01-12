package edu.utap.mypet.petEdit


import android.os.Bundle
import android.util.Log
import android.view.View

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import edu.utap.mypet.MainActivity
import edu.utap.mypet.MainViewModel
import edu.utap.mypet.R
import kotlinx.android.synthetic.main.pet_fragment.*


class PetsFragment :
    Fragment(R.layout.pet_fragment) {
    private val viewModel: MainViewModel by activityViewModels()
    private var currentUser: FirebaseUser? = null

    companion object {
        fun newInstance(): PetsFragment {
            return PetsFragment()
        }
    }

    private fun initAuth() {
        viewModel.observeFirebaseAuthLiveData().observe(viewLifecycleOwner, Observer {
            currentUser = it
            Log.d(javaClass.simpleName, "current user: ${it?.displayName}")
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAuth()
        viewModel.setTitle("PETS")
        addPet.setOnClickListener {
            parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_frame, PetsEdit.newInstance(-1))
                    .addToBackStack(null)
                    .commit()
            if (activity is MainActivity) {
                val  mainActivity = activity as MainActivity
                mainActivity.setBottomNavigationVisibility(View.GONE)
            }

        }
        val adapter = PetsAdapter(viewModel) {position ->
            parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_frame, PetsEdit.newInstance(position))
                    .addToBackStack(null)
                    .commit()
            if (activity is MainActivity) {
                val  mainActivity = activity as MainActivity
                mainActivity.setBottomNavigationVisibility(View.GONE)
            }

        }
        val rv = view.findViewById<RecyclerView>(R.id.petListRV)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(context)

       parentFragmentManager
               .addOnBackStackChangedListener {
                    Log.d(javaClass.simpleName, "backStack!")
                    //toggleEmptyPets()
                    adapter.notifyDataSetChanged()
               }
        viewModel.observePets().observe(viewLifecycleOwner, Observer {
            Log.d("what is list look like new", "$it")
            adapter.submitList(it)
            toggleEmptyPets()
           //adapter.notifyDataSetChanged()
        })
        toggleEmptyPets()
    }

    override fun onPause() {
        super.onPause()
        Log.d("pets frag on pause", "being called")

    }

    private fun toggleEmptyPets() {
        if (viewModel.isPetsEmpty()) {
            dog_cat_pic.visibility = View.VISIBLE
            no_pet.visibility = View.VISIBLE
            no_pet_2.visibility = View.VISIBLE
        } else {
            dog_cat_pic.visibility = View.GONE
            no_pet.visibility = View.GONE
            no_pet_2.visibility = View.GONE
        }
    }
}
