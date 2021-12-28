package com.danidandev.todolist

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.danidandev.todolist.databinding.ActivityGuestBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import java.io.*

class GuestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuestBinding
    private lateinit var jsonArray: JSONArray
    private val items = ArrayList<TodoItem>()

    private val fileName = "TodoList.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGuestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.guestInclude.guestRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.fab.setOnClickListener {
            addTodoDialog().show()
        }

        updateRecyclerViewAdapter()
        fetchData()
    }

    private fun getDataFile(): File {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        if (!file.exists()) {
            file.createNewFile()
            val fileWriter = FileWriter(getDataFile())
            val bufferedWriter = BufferedWriter(fileWriter)
            bufferedWriter.write(JSONArray("[]").toString())
            bufferedWriter.close()
        }
        return file
    }

    private fun fetchData() {
        val fileReader = FileReader(getDataFile())
        val bufferedReader = BufferedReader(fileReader)
        val stringBuffer = StringBuffer()
        var line = bufferedReader.readLine()
        while (line != null) {
            stringBuffer.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close()

        jsonArray = JSONArray(stringBuffer.toString())
        for (i in 0 until jsonArray.length()) {
            val jsonArrayItem = jsonArray.getJSONObject(i)
            val id = jsonArrayItem.optString("id")
            val text = jsonArrayItem.optString("text")
            val completed = jsonArrayItem.optBoolean("completed")
            items.add(TodoItem(id, text, completed))
        }
    }

    private fun updateRecyclerViewAdapter() {
        binding.guestInclude.guestRecyclerView.adapter = CustomAdapter(items, { id ->
            deleteTodoItem(id)
        }, { id ->
            updateTodoItem(id)
        })
    }

    private fun addTodoDialog(): AlertDialog {
        val contentView = LayoutInflater.from(this)
            .inflate(R.layout.content_add_dialog, null, false)
        val textInputEditText: TextInputEditText =
            contentView.findViewById(R.id.contentAddDialogTIE)
        return AlertDialog.Builder(this)
            .setCancelable(false)
            .setNegativeButton(getString(R.string.fui_cancel), null)
            .setView(contentView)
            .setTitle(getString(R.string.new_todo))
            .setPositiveButton(getString(R.string.fui_button_text_save)) { _, _ ->
                if (!textInputEditText.text.isNullOrBlank()) {
                    addTodoItem(textInputEditText.text.toString())
                } else {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.blank_todo_not_allowed),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            .create()
    }

    private fun updateDataFile() {
        val fileWriter = FileWriter(getDataFile())
        val bufferedWriter = BufferedWriter(fileWriter)
        bufferedWriter.write(jsonArray.toString())
        bufferedWriter.close()
    }

    private fun addTodoItem(text: String) {
        val id = System.currentTimeMillis().toString()
        val newTodoItem = TodoItem(id, text, false)
        jsonArray.put(newTodoItem.toJsonObject())
        updateDataFile()
        items.add(newTodoItem)
        updateRecyclerViewAdapter()
    }

    private fun deleteTodoItem(id: String) {
        var indexToDelete: Int = -1
        for (i in items.indices) {
            if (items[i].id.compareTo(id) == 0) {
                indexToDelete = i
                break
            }
        }
        jsonArray.remove(indexToDelete)
        updateDataFile()
        items.removeAt(indexToDelete)
        updateRecyclerViewAdapter()
    }

    private fun updateTodoItem(id: String) {
        var indexToUpdate: Int = -1
        for (i in items.indices) {
            if (items[i].id.compareTo(id) == 0) {
                indexToUpdate = i
                break
            }
        }
        val itemToUpdate = items[indexToUpdate]
        val updatedItem = TodoItem(itemToUpdate.id, itemToUpdate.text, !itemToUpdate.completed)
        jsonArray.put(indexToUpdate, updatedItem.toJsonObject())
        updateDataFile()
        items[indexToUpdate] = updatedItem
        updateRecyclerViewAdapter()
    }
}