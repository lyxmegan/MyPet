package edu.utap.mypet.funFacts

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import edu.utap.mypet.R
import kotlinx.android.synthetic.main.facts_frag.*
import androidx.lifecycle.Observer
import edu.utap.mypet.MainViewModel

class FactsFragment :
    Fragment(R.layout.facts_frag) {
    private val mainViewModel: MainViewModel by activityViewModels()

    companion object {
        fun newInstance(): FactsFragment {
            return FactsFragment()
        }
    }
    private val viewModel: FactsViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.setTitle("FUN FACTS")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        catFact.movementMethod = ScrollingMovementMethod()
        dogFact.movementMethod = ScrollingMovementMethod()
        viewModel.observeCatFact().observe(viewLifecycleOwner, Observer {
            catFact.text = it
        })
        catFact.setOnClickListener {
            viewModel.netRefreshCatFact()

        }

        viewModel.observeDogFact().observe(viewLifecycleOwner, Observer {
            dogFact.text = it
        })
        dogFact.setOnClickListener {
            viewModel.netRefreshDogFact()
            true
        }

        shareButton1.setOnClickListener {
            val message: String = catFact.text.toString()
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, message)
            intent.type = "text/plain"

            startActivity(Intent.createChooser(intent, "Please select app: "))
        }
        shareButton2.setOnClickListener {
            val message: String = dogFact.text.toString()
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, message)
            intent.type = "text/plain"

            startActivity(Intent.createChooser(intent, "Please select app: "))
        }
    }
}
