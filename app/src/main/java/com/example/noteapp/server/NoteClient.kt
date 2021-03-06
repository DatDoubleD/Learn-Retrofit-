package com.example.noteapp.server

import com.example.noteapp.model.Image
import com.example.noteapp.model.Note
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface NoteClient {
    @GET("/note")
    fun getAllNote(): Call<List<Note>>

    //do img k trả về string mà trả về object dạng json nên tạo data class Image
    @Multipart
    @POST("/upload")
    fun uploadImage(@Part img:MultipartBody.Part): Call<Image>

    @POST("/note")
    fun addNote(@Body note: Note):Call<Note>

    //update, kieu tra về là note đã updated
    @PUT("note/{id}")
    fun updateNote(@Path("id") id :Int, @Body note: Note): Call<Note>

    @DELETE("note/{id}")
    fun deleteNote(@Path("id") id: Int): Call<Note>
}