package com.example.memoriesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.Adapter_Classes.CommentsAdapter
import com.example.memoriesapp.Model_Classes.Comment
import com.example.memoriesapp.Model_Classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsActivity : AppCompatActivity() {
    private var memoryId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentsAdapter? = null
    private var commentList: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val intent = intent
        memoryId = intent.getStringExtra("memoryId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser


        var recyclerView: RecyclerView = findViewById(R.id.recycler_view_comments)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        getUserImg()
        getComments()
        getMemoryImg()

        findViewById<TextView>(R.id.add_comment).setOnClickListener {
            if(findViewById<EditText>(R.id.comment)!!.text.toString() == "") {
                Toast.makeText(this@CommentsActivity, "Please write a comment before submitting", Toast.LENGTH_LONG).show()
            } else {
                addComment()
            }
        }
    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(memoryId!!)

        val commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = findViewById<EditText>(R.id.comment).text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        addNotification()

        findViewById<EditText>(R.id.comment)!!.text.clear()

    }

    private fun getUserImg() {
        val userRef = FirebaseDatabase.getInstance()
            .reference
            .child("Users")
            .child(firebaseUser!!.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(findViewById<CircleImageView>(R.id.profile_image_comments))
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getMemoryImg() {
        val memoryRef = FirebaseDatabase.getInstance()
            .reference
            .child("Memories")
            .child(memoryId!!)
            .child("memoryimage")
        memoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val image = snapshot.value.toString()
                    Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.profile)
                        .into(findViewById<CircleImageView>(R.id.memory_comment_image))
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getComments() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(memoryId)
        commentsRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    commentList!!.clear()

                    for(comments in snapshot.children) {
                        val comment = comments.getValue(Comment::class.java)
                        commentList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun addNotification() {
        val notificationRef = FirebaseDatabase.getInstance().reference
                .child("Notifications")
                .child(publisherId!!)

        val notificationMap = HashMap<String, Any>()


        notificationMap["userid"] = firebaseUser!!.uid
        notificationMap["text"] = "commented: " + findViewById<EditText>(R.id.comment)!!.text.toString()
        notificationMap["memoryid"] = memoryId
        notificationMap["ismemory"] = true

        notificationRef.push().setValue(notificationMap)
    }
}

