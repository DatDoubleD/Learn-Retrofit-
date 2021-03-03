package com.example.noteapp.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp.AppConfig
import com.example.noteapp.R
import com.example.noteapp.model.Image
import com.example.noteapp.model.Note
import com.example.noteapp.server.NoteClient
import com.example.noteapp.ultis.Utils
import com.example.noteapp.viewmodel.NoteViewModel
import kotlinx.android.synthetic.main.activity_add_note.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddNoteActivity : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        //set event
        btn_add.setOnClickListener {
            uploadImg()
        }
        img_note_add.setOnClickListener {
            Toast.makeText(this,"check!!!",Toast.LENGTH_LONG).show()
            requestPermissionAndPickImage()
        }

    }

    private fun requestPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            pickImage()
            return
        }
        val result = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)

        if (result == PackageManager.PERMISSION_GRANTED) {
            pickImage()
        } else {
            // trả về request
            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image to upload"), PICK_IMAGE_REQUEST
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            img_note_add.setImageURI(data.data)
            uriImg = data.data!!
        }
    }


    private val client: NoteClient = AppConfig.retrofit.create(NoteClient::class.java)
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
                    if (response.body() != null) {
                        var imagePath = response.body()!!.imagePath
                        Log.d("ADD_NOTE", imagePath)
                    }
                }
            }

            override fun onFailure(call: Call<Image>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
}
