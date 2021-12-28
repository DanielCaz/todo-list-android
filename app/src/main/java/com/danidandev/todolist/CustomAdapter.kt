package com.danidandev.todolist

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(
    private val dataSet: ArrayList<TodoItem>,
    private val customOnDelete: (itemId: String) -> Unit,
    private val customOnDone: (itemId: String) -> Unit
) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: ConstraintLayout = view.findViewById(R.id.textRowItemConstraint)
        val textView: TextView = view.findViewById(R.id.textViewTextRow)
        val buttonDelete: Button = view.findViewById(R.id.buttonTextRowDelete)
        val buttonDone: Button = view.findViewById(R.id.buttonTextRowDone)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.apply {
            textView.text = dataSet[position].text
            if (dataSet[position].completed) {
                textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                root.setBackgroundColor(Color.argb(.25f, 0f, 0f, 0f))
            }
            buttonDelete.setOnClickListener { customOnDelete(dataSet[position].id) }
            buttonDone.setOnClickListener { customOnDone(dataSet[position].id) }
        }
    }

    override fun getItemCount() = dataSet.size
}
