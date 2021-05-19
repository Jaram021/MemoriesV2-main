package com.example.memoriesapp

import android.app.Activity
import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import android.content.Intent
import android.widget.EditText
import android.widget.Toast
import androidx.core.net.toUri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class EditMemoryActivity : AppCompatActivity() {

    private var myUrl: String = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_memory)

        val editDescription = intent.getStringExtra("editdescription")
        val editImage = intent.getStringExtra("editimage")?.toUri()

        val addMemory: ImageView = findViewById<ImageView>(R.id.edit_add_memory_view)
        val quitBtn: ImageView = findViewById<ImageView>(R.id.edit_cancel_memory_view)
        val editMemoryImage: ImageView = findViewById<ImageView>(R.id.edit_image_post)
        val editMemoryDescription: EditText = findViewById<EditText>(R.id.edit_description)

        Picasso.get().load(editImage).placeholder(R.drawable.profile).into(editMemoryImage)

        editMemoryDescription.setText(editDescription)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Memory Images")


        addMemory.setOnClickListener { uploadImage() }
        quitBtn.setOnClickListener {finish()}

        editMemoryImage.setOnClickListener {
            CropImage.activity().setAspectRatio(2,1).start(this@EditMemoryActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK
                && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            findViewById<ImageView>(R.id.edit_image_post).setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        when {
            imageUri == null ->
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_LONG).show()

            findViewById<EditText>(R.id.edit_description).text.toString() == "" -> {
                Toast.makeText(this, "Please write description!", Toast.LENGTH_LONG).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Editing Memory")
                progressDialog.setMessage("Updating Your Memory Bank")
                progressDialog.show()

                val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if(!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener ( OnCompleteListener<Uri> { task ->
                    if(task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Memories")
                        val memoryId = intent.getStringExtra("memoryid") // create key for memory

                        val memoryMap = HashMap<String, Any>()
                        memoryMap["memoryid"] = memoryId!!
                        memoryMap["description"] = findViewById<EditText>(R.id.edit_description).text.toString()
                        memoryMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        memoryMap["memoryimage"] = myUrl

                        ref.child(memoryId).updateChildren(memoryMap)

                        Toast.makeText(this, "Your memory has been successfully posted!", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@EditMemoryActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }
}