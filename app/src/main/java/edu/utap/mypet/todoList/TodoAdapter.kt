package edu.utap.mypet.todoList

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import edu.utap.mypet.MainViewModel
import edu.utap.mypet.R

class TodoAdapter (private var viewModel: MainViewModel)
    : ListAdapter<ToDoItem, TodoAdapter.ToDoItemViewHolder>(Diff()){
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    class Diff : DiffUtil.ItemCallback<ToDoItem>() {
        override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
            return oldItem.rowID == newItem.rowID
        }

        override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.ownerUid == newItem.ownerUid
                    && oldItem.status == newItem.status
                    && oldItem.todoItem == newItem.todoItem
        }
    }
    inner class ToDoItemViewHolder (itemView: View)
        : RecyclerView.ViewHolder(itemView) {
        private var deleteBut = itemView.findViewById<ImageButton>(R.id.deleteToDoItem)
        private var cardView = itemView.findViewById<CardView>(R.id.toDoCardView)
        private var checkBox = itemView.findViewById<CheckBox>(R.id.itemCheckBox)
        private var textView = itemView.findViewById<TextView>(R.id.todoText)
        private var imagebutton = itemView.findViewById<ImageButton>(R.id.deleteToDoItem)


        init {
            deleteBut.setOnClickListener{
                val item = viewModel.getList()?.get(adapterPosition)
                if (item != null) {
                    viewModel.deleteToDoItem(item)
                }
            }
            checkBox.setOnCheckedChangeListener { button, b ->
                val item = viewModel.getList()?.get(adapterPosition)
                val ref = item?.rowID?.let {
                    db.collection("globalToDoItems")
                        .document(it)
                }
                if (ref != null) {
                    ref.update("status", b)
                        .addOnSuccessListener {
                            Log.d("update item", "DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.d(javaClass.simpleName, "To do item update FAILED")
                            Log.w(javaClass.simpleName, "Error ", e)
                        }
                }
                Log.d("is checked", "$b")
            }
        }

        fun bind(item: ToDoItem?) {
            if (item == null) return
            textView.text = item.todoItem
            checkBox.isChecked = item.status!!
            cardView.visibility = View.VISIBLE
            imagebutton.setImageResource(R.drawable.ic_baseline_delete_24)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.one_todo_list, parent, false)
        return ToDoItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ToDoItemViewHolder, position: Int) {
        holder.bind(getItem(holder.adapterPosition))
    }
}


