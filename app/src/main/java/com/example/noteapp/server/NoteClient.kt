package com.example.noteapp.server

import com.example.noteapp.model.Image
import com.example.noteapp.model.Note
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NoteClient {
    @GET("/note")
    fun getAllNote(): Call<List<Note>>

    //do img k trả về string mà trả về object dạng json nên tạo data class Image
    @Multipart
    @POST("/upload")
    fun uploadImage(@Part img:MultipartBody.Part): Call<Image>
}