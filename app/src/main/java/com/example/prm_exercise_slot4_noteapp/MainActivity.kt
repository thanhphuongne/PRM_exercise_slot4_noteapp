package com.example.prm_exercise_slot4_noteapp

import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.android.material.snackbar.Snackbar
import com.example.prm_exercise_slot4_noteapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var editTextNote: EditText
    private lateinit var buttonAdd: Button
    private lateinit var linearLayoutNotes: LinearLayout
    private lateinit var imageViewTrash: ImageView
    private var lastDeletedNote: TextView? = null
    private var dragHighlight: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to set content view", e)
            return
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        if (toolbar == null) {
            Log.e("MainActivity", "Toolbar not found with ID R.id.toolbar")
            return
        }
        setSupportActionBar(toolbar)

        editTextNote = findViewById(R.id.editTextNote) ?: run {
            Log.e("MainActivity", "EditText not found with ID R.id.editTextNote")
            return
        }
        buttonAdd = findViewById(R.id.buttonAdd) ?: run {
            Log.e("MainActivity", "Button not found with ID R.id.buttonAdd")
            return
        }
        linearLayoutNotes = findViewById(R.id.linearLayoutNotes) ?: run {
            Log.e("MainActivity", "LinearLayout not found with ID R.id.linearLayoutNotes")
            return
        }
        imageViewTrash = findViewById(R.id.imageViewTrash) ?: run {
            Log.e("MainActivity", "ImageView not found with ID R.id.imageViewTrash")
            return
        }

        // TextWatcher for enabling/disabling Add button
        editTextNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                buttonAdd.isEnabled = !s.isNullOrBlank()
            }
        })

        // Button click to add note
        buttonAdd.setOnClickListener {
            val noteText = editTextNote.text.toString().trim()
            if (noteText.isNotEmpty()) {
                val noteView = TextView(this).apply {
                    text = noteText
                    setPadding(16)
                    try {
                        setBackgroundResource(R.drawable.note_background)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to set note background", e)
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(8, 8, 8, 8)
                    }
                    setOnLongClickListener { view ->
                        view.setBackgroundColor(Color.LTGRAY)
                        val clipData = ClipData.newPlainText("note", noteText)
                        view.startDragAndDrop(clipData, View.DragShadowBuilder(view), view, 0)
                        showDragHighlight()
                        true
                    }
                }
                linearLayoutNotes.addView(noteView)
                editTextNote.setText("")
            }
        }

        // Drag listener on trash
        imageViewTrash.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> {
                    imageViewTrash.setBackgroundColor(Color.RED)
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    imageViewTrash.setBackgroundColor(Color.TRANSPARENT)
                    true
                }
                DragEvent.ACTION_DROP -> {
                    imageViewTrash.setBackgroundColor(Color.TRANSPARENT)
                    val item = event.localState as View
                    lastDeletedNote = item as TextView
                    linearLayoutNotes.removeView(item)
                    showUndoSnackbar()
                    hideDragHighlight()
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    hideDragHighlight()
                    true
                }
                else -> false
            }
        }
    }

    private fun showDragHighlight() {
        dragHighlight = View(this).apply {
            setBackgroundColor(Color.argb(128, 255, 0, 0)) // Semi-transparent red
            layoutParams = FrameLayout.LayoutParams(
                80.dpToPx(this@MainActivity),
                80.dpToPx(this@MainActivity)
            ).apply {
                leftMargin = imageViewTrash.left - 16
                topMargin = imageViewTrash.top - 16
            }
        }
        val overlay = FrameLayout(this).apply {
            addView(dragHighlight)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        findViewById<ViewGroup>(android.R.id.content)?.addView(overlay)
            ?: Log.e("MainActivity", "Content view not found")
    }

    private fun hideDragHighlight() {
        dragHighlight?.let { view ->
            if (view.parent is ViewGroup) {
                val parent = view.parent as ViewGroup
                parent.removeView(view)
                (parent.parent as? ViewGroup)?.removeView(parent)
            } else {
                Log.e("MainActivity", "Parent is not a ViewGroup: ${view.parent}")
            }
        } ?: Log.w("MainActivity", "dragHighlight is null")
        dragHighlight = null
    }

    private fun showUndoSnackbar() {
        Snackbar.make(linearLayoutNotes, "Note deleted. Undo?", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                lastDeletedNote?.let { note ->
                    linearLayoutNotes.addView(note)
                    try {
                        note.setBackgroundResource(R.drawable.note_background)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to restore note background", e)
                    }
                }
            }
            .show()
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}