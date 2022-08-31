package com.udacity.notepad.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns._ID
import com.udacity.notepad.data.NotesContract.NoteTable
import com.udacity.notepad.data.NotesContract.NoteTable.Companion.CREATED_AT
import com.udacity.notepad.data.NotesContract.NoteTable.Companion.IS_PINNED
import com.udacity.notepad.data.NotesContract.NoteTable.Companion.TEXT
import com.udacity.notepad.data.NotesContract.NoteTable.Companion.UPDATED_AT
import com.udacity.notepad.data.NotesContract.NoteTable.Companion._TABLE_NAME
import org.jetbrains.anko.db.transaction
import java.util.*

class NoteDatabase(context: Context?) {
    private val helper: NotesOpenHelper = NotesOpenHelper(context)
    fun getAll(): List<Note> {
        return allFromCursor(
            helper.readableDatabase.query(
                _TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                CREATED_AT
            )
        )
    }

    fun loadAllByIds(vararg ids: Int): List<Note> {
        val questionMarks = ids.map { "?" }.joinToString { "," }
        val args = ids.map { it.toString() }
        val selection = "$_ID IN ($questionMarks)"
        val cursor = helper.readableDatabase.query(
            NoteTable._TABLE_NAME,
            null,
            selection,
            args.toTypedArray(),
            null,
            null,
            NoteTable.CREATED_AT
        )
        val retval = allFromCursor(cursor)
        cursor.close()
        return retval
    }

    fun insert(vararg notes: Note) {
        helper.writableDatabase.transaction {
            fromNotes(notes).forEach { insert(_TABLE_NAME, null, it) }
        }
    }

    fun update(note: Note) {
        val values = fromNote(note)
        helper.writableDatabase.update(
            _TABLE_NAME,
            values,
            "$_ID = ?", arrayOf("${note.id}")
        )
    }

    fun delete(note: Note) {
        helper.writableDatabase.delete(
            NoteTable._TABLE_NAME,
            "$_ID = ?", arrayOf("${note.id}")
        )
    }

    companion object {
        private fun fromCursor(cursor: Cursor): Note {
            var col = 0
            return Note().apply {
                id = cursor.getInt(col++)
                text = cursor.getString(col++)
                isPinned = cursor.getInt(col++) != 0
                createdAt = Date(cursor.getLong(col++))
                updatedAt = Date(cursor.getLong(col))
            }
        }

        private fun allFromCursor(cursor: Cursor): List<Note> {
            val retval: MutableList<Note> = ArrayList()
            while (cursor.moveToNext()) {
                retval.add(fromCursor(cursor))
            }
            return retval
        }

        private fun fromNote(note: Note): ContentValues {
            return ContentValues().apply {
                val id = note.id
                if (id != -1) {
                    put(_ID, id)
                }
                put(TEXT, note.text)
                put(CREATED_AT, note.createdAt.time)
                put(IS_PINNED, note.isPinned)
                put(UPDATED_AT, note.updatedAt!!.time)
            }
        }

        private fun fromNotes(notes: Array<out Note>): List<ContentValues> {
            return notes.map(this::fromNote)
        }
    }


}