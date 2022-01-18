package com.sunayanpradhan.notesnap.repository

import com.sunayanpradhan.notesnap.db.NoteDatabase
import com.sunayanpradhan.notesnap.model.Note

class NoteRepository(private val db:NoteDatabase) {

    fun getNote()=db.getNoteDao().getAllNote()

    fun searchNote(query:String)=db.getNoteDao().searchNote(query)

    suspend fun addNote(note:Note)=db.getNoteDao().addNote(note)

    suspend fun updateNote(note:Note)=db.getNoteDao().updateNote(note)

    suspend fun deleteNote(note:Note)=db.getNoteDao().deleteNote(note)

}