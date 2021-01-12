package edu.utap.mypet.todoList

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import edu.utap.mypet.MainActivity
import edu.utap.mypet.MainViewModel
import edu.utap.mypet.R
import kotlinx.android.synthetic.main.pet_fragment.*
import kotlinx.android.synthetic.main.todo_fragment.*


class TodoFragment :
    Fragment(R.layout.todo_fragment) {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var todoAdapter: TodoAdapter
    private var currentUser: FirebaseUser? = null

    companion object {
        fun newInstance(): TodoFragment {
            return TodoFragment()
        }
    }
    private fun initRecyclerView()  {
        todoAdapter = TodoAdapter(viewModel)
        todoRecyclerView.adapter = todoAdapter
        todoRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun initAuth() {
        viewModel.observeFirebaseAuthLiveData().observe(viewLifecycleOwner, Observer {
            currentUser = it
            Log.d(javaClass.simpleName, "current user: ${it?.displayName}")
        })
    }

    private fun clearCompose() {
        createToDo.text.clear()
    }

    private fun initsendToDoDb() {
        // add to do button
        addButton.setOnClickListener {
            if( createToDo.text.isNotEmpty()) {
                val toDoItem = ToDoItem().apply {
                    val cUser = currentUser
                    if(cUser == null) {
                        name = "unknown"
                        ownerUid = "unknown"
                        Log.d("TodoFragment", "XXX, currentUser null!")
                    } else {
                        name = cUser.displayName.toString()
                        ownerUid = cUser.uid
                    }
                    todoItem = createToDo.text.toString()
                    status = false
                    clearCompose()
                    (requireActivity() as MainActivity).hideKeyboard()
                }
                viewModel.addToDoItem(toDoItem)
            } else {
                Toast.makeText(context, "please add a to do", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getToDoItem()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAuth()
        initsendToDoDb()
        initRecyclerView()
        viewModel.setTitle("TO DO")

        viewModel.observeToDoList().observe(viewLifecycleOwner, Observer {
            Log.d(javaClass.simpleName, "Observe to do item $it")
            todoAdapter.submitList(it)
            toggleEmptyToDoList()
        })

        clearBut.setOnClickListener {
            createToDo.text.clear()
        }
    }

    private fun toggleEmptyToDoList() {
        if (viewModel.isToDoEmpty()) {
            noToDo.visibility = View.VISIBLE
        } else {
            noToDo.visibility = View.GONE
        }
    }

}
