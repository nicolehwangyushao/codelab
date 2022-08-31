package com.udacity.notepad.data

import android.content.Context
import org.jetbrains.anko.doAsync

object DataStore {


    @JvmStatic
    lateinit var notes: NoteDatabase
        private set

    @JvmStatic
    fun init(context: Context?) {
        notes = NoteDatabase(context)
    }
    @JvmStatic
    fun execute(runnable: Runnable) {
        doAsync { runnable.run() }
    }
    @JvmStatic
    fun execute(fn: () -> Unit) {
        doAsync { fn() }
    }
}