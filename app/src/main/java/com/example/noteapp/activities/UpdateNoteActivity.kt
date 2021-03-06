package com.example.noteapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.noteapp.AppConfig
import com.example.noteapp.R
import com.example.noteapp.model.Image
import com.example.noteapp.model.Note
import com.example.noteapp.server.NoteClient
import com.example.noteapp.ultis.Utils
import com.example.noteapp.viewmodel.NoteViewModel
import kotlinx.android.synthetic.main.activity_add_note.*
import kotlinx.android.synthetic.main.activity_update_note.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UpdateNoteActivity : AppCompatActivity() {
    private val noteViewModel: NoteViewModel by lazy {
        ViewModelProvider(
            this,
            NoteViewModel.NoteViewModelFactory(this.application)
        )[NoteViewModel::class.java]
    }
    companion object {
        const val PICK_IMAGE_REQUEST = 123
        const val REQUEST_PERMISSION_CODE = 234
    }
    private lateinit var uriImg: Uri

    private lateinit var imgPath: String
    private val client: NoteClient = AppConfig.retrofit.create(NoteClient::class.java)

    private lateinit var note: Note
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_note)

        note=intent.getSerializableExtra("UPDATE_NOTE") as Note
        edt_note_title.setText(note.title)
        edt_note_des.setText(note.description)
        Glide.with(this).load(note.imgPath).into(img_note_update)
        addEvents()
    }

    private fun addEvents() {
        btn_update.setOnClickListener {
            //gọi uploadImage lên sever + update lại note, ảnh cũ của note đó vẫn chưa xóa
            //upload ảnh trùng, vẫn là 1 ảnh, còn ảnh khác -> 2 ảnh, chhưa xóa dc ảnh cũ trước đó của note
            uploadImg()
           /* noteViewModel.updateNote(note)
            finish()*/
        }
        img_note_update.setOnClickListener {
            requestPermissionAndPickImage()
        }
    }

    private fun updateNote(imgPath: String) {
        note.title=edt_note_title.text.toString()
        note.description=edt_note_des.text.toString()
        note.imgPath = imgPath
        val call:Call<Note> = client.updateNote(note.id, note)
        call.enqueue(object : Callback<Note> {
            override fun onResponse(call: Call<Note>, response: Response<Note>) {
                if (response.isSuccessful) {
                    Log.d("UPDATE_NOTE",  response.body().toString())
                    finish()
                }
            }

            override fun onFailure(call: Call<Note>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun requestPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            pickImage()
            return
        }
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (result == PackageManager.PERMISSION_GRANTED) {
            pickImage()
        } else {
            // trả về request
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                AddNoteActivity.REQUEST_PERMISSION_CODE
            )
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddNoteActivity.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            img_note_update.setImageURI(data.data)
            uriImg = data.data!!
            Log.d("TAG", uriImg.toString())
        }
    }
    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select image to upload"),
            AddNoteActivity.PICK_IMAGE_REQUEST
        )
    }

    private fun uploadImg() {
        //đọc kỹ api và doc của phần backend để làm phần này
        val file = File(Utils.getPathFromUri(this, uriImg)!!)
        val requestBody =
            RequestBody.create(MediaType.parse(contentResolver.getType(uriImg)!!), file)
        val imagePart = MultipartBody.Part.createFormData("picture", file.name, requestBody)

        val call: Call<Image> = client.uploadImage(imagePart)
        call.enqueue(object : Callback<Image> {
            override fun onResponse(call: Call<Image>, response: Response<Image>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        imgPath = it.imagePath
                        Log.d("ADD_NOTE", imgPath)
                        updateNote(imgPath)
                    }
                }
            }

            override fun onFailure(call: Call<Image>, t: Throwable) {
                Log.d("ERR", t.toString())
            }
        })
    }
}