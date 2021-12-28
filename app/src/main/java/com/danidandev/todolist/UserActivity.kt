package com.danidandev.todolist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.danidandev.todolist.databinding.ActivityUserBinding
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    private lateinit var loadingDialog: AlertDialog
    private lateinit var items: ArrayList<TodoItem>
    private var auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val path = "users/${auth.currentUser!!.uid}/todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.userInclude.userRecyclerView.layoutManager =
            LinearLayoutManager(this)

        binding.fab.setOnClickListener {

            addTodoDialog().show()
        }

        loadingDialog = loadingDialog()

        fetchData()

        title = getString(R.string.user_activity_title_template).format(
            auth.currentUser?.displayName?.split(" ")?.get(0)
                ?: getString(R.string.title_activity_user)
        )
    }

    private fun loadingDialog(): AlertDialog = AlertDialog.Builder(this)
        .setCancelable(false)
        .setView(R.layout.content_loading_dialog)
        .create()

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
            .setPositiveButton(
                getString(R.string.fui_button_text_save)
            ) { _, _ ->
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


    private fun fetchData() {
        items = ArrayList()
        if (!loadingDialog.isShowing) loadingDialog.show()
        db.collection(path)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val item = TodoItem(
                        document.data["id"].toString(),
                        document.data["text"].toString(),
                        document["completed"] as Boolean
                    )
                    items.add(item)
                }
                updateRecyclerViewAdapter()
            }
            .addOnCompleteListener { if (loadingDialog.isShowing) loadingDialog.dismiss() }
    }

    private fun updateRecyclerViewAdapter() {
        binding.userInclude.userRecyclerView.adapter = CustomAdapter(items, { id ->
            deleteTodoItem(id)
        }, { id ->
            updateTodoItem(id)
        })
    }

    private fun addTodoItem(text: String) {
        if (!loadingDialog.isShowing) loadingDialog.show()
        val id = System.currentTimeMillis().toString()
        db.collection(path).document(id).set(
            hashMapOf(
                "id" to id,
                "completed" to false,
                "text" to text
            )
        ).addOnCompleteListener {
            if (loadingDialog.isShowing) loadingDialog.dismiss()
            items.add(TodoItem(id, text, false))
            updateRecyclerViewAdapter()
        }
    }

    private fun deleteTodoItem(id: String) {
        if (!loadingDialog.isShowing) loadingDialog.show()
        db.collection(path).document(id)
            .delete()
            .addOnSuccessListener {
                var indexToDelete: Int = -1
                for (i in items.indices) {
                    if (items[i].id.compareTo(id) == 0) {
                        indexToDelete = i
                        break
                    }
                }
                items.removeAt(indexToDelete)
                updateRecyclerViewAdapter()
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    binding.userInclude.contentUserRoot,
                    e.toString(),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            .addOnCompleteListener { if (loadingDialog.isShowing) loadingDialog.dismiss() }
    }

    private fun updateTodoItem(id: String) {
        if (!loadingDialog.isShowing) loadingDialog.show()
        db.collection(path).document(id)
            .get()
            .addOnSuccessListener { result ->
                db.collection(path).document(id)
                    .update("completed", !(result.data?.get("completed") as Boolean))
                    .addOnSuccessListener {
                        var indexToUpdate: Int = -1
                        for (i in items.indices) {
                            if (items[i].id.compareTo(id) == 0) {
                                indexToUpdate = i
                                break
                            }
                        }
                        val itemToUpdate = items[indexToUpdate]
                        items[indexToUpdate] = TodoItem(
                            itemToUpdate.id,
                            itemToUpdate.text,
                            !itemToUpdate.completed
                        )
                        updateRecyclerViewAdapter()
                    }
                    .addOnCompleteListener { if (loadingDialog.isShowing) loadingDialog.dismiss() }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnu_item_sign_out -> {
                signOut()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}