package com.example.noteapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppConfig {
    const val BASE_URL = "http://192.168.1.9:8080"
    private val builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
    val retrofit = builder.build()
}