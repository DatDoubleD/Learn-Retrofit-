package com.example.noteapp.server

import com.example.noteapp.model.Note
import retrofit2.Call
import retrofit2.http.GET

interface NoteClient {
    @GET("/note")
    fun getAllNote(): Call<List<Note>>

}