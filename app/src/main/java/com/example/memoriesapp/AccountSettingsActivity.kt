package com.example.memoriesapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.example.memoriesapp.Fragments.ProfileFragment
import com.example.memoriesapp.Model_Classes.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView

// Class for editing users account
// Using Firebase Auth, FirebaseStorage, Firebase Database
class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl: String = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        val LogoutBtn: Button = findViewById<Button>(R.id.logout_btn_profile_frag)
        val changeImgView: TextView = findViewById<TextView>(R.id.change_image_text_btn)
        val saveInfoBtn: ImageView = findViewById<ImageView>(R.id.save_info_profile_btn)
        val quitBtn: ImageView = findViewById<ImageView>(R.id.close_profile_btn)
        LogoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            finish()
        }

        changeImgView.setOnClickListener {
            checker = "clicked"

            CropImage.activity()
                    .setAspectRatio(1,1)
                    .start(this@AccountSettingsActivity)
        }

        saveInfoBtn.setOnClickListener {
            if(checker == "clicked") {
                updateImageAndInfo()
            } else {
                updateUserInfoOnly()
            }
        }

        quitBtn.setOnClickListener{
            finish()
        }

        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK
                && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            findViewById<CircleImageView>(R.id.image_profile_frag).setImageURI(imageUri)
        }
    }
    // Function for updating all of the profile data including the profile image
    // Stores new information in Firebase Database
    private fun updateImageAndInfo() {
        when {
            findViewById<EditText>(R.id.full_name_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write full name first.", Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.username_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please enter user name first", Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.bio_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write bio name first.", Toast.LENGTH_LONG).show()
            }
            imageUri == null -> {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_LONG).show()
            }
            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("One moment please. . .")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")
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

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        val userMap = HashMap<String, Any>()
                        userMap["searchname"] = findViewById<EditText>(R.id.username_profile_frag).text.toString().toLowerCase()
                        userMap["fullname"] = findViewById<EditText>(R.id.full_name_profile_frag).text.toString()
                        userMap["username"] = findViewById<EditText>(R.id.username_profile_frag).text.toString()
                        userMap["bio"] = findViewById<EditText>(R.id.bio_profile_frag).text.toString()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Account Information has been successfully updated", Toast.LENGTH_LONG).show()
                        startActivity(intent)
                        finish()
                    } else {
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }

    // Function for updating user information excluding users profile image
    // Stores new information in Firebase Database
    private fun updateUserInfoOnly() {
        when {
            findViewById<EditText>(R.id.full_name_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write full name first.", Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.username_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please enter user name first", Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.bio_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write bio name first.", Toast.LENGTH_LONG).show()
            }
            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
                val userMap = HashMap<String, Any>()
                userMap["searchname"] = findViewById<EditText>(R.id.username_profile_frag).text.toString().toLowerCase()
                userMap["fullname"] = findViewById<EditText>(R.id.full_name_profile_frag).text.toString()
                userMap["username"] = findViewById<EditText>(R.id.username_profile_frag).text.toString()
                userMap["bio"] = findViewById<EditText>(R.id.bio_profile_frag).text.toString()
                usersRef.child(firebaseUser.uid).updateChildren(userMap)
                Toast.makeText(this, "Account Information has been successfully updated", Toast.LENGTH_LONG).show()

                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    // Function for getting users information from Firebase Database
    // Using picasso to get the image
    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get()
                            .load(user!!.getImage())
                            .placeholder(R.drawable.profile)
                            .into(findViewById<CircleImageView>(R.id.image_profile_frag))
                    findViewById<EditText>(R.id.username_profile_frag)?.setText(user.getUsername())
                    findViewById<EditText>(R.id.full_name_profile_frag)?.setText(user.getFullName())
                    findViewById<EditText>(R.id.bio_profile_frag)?.setText(user.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}