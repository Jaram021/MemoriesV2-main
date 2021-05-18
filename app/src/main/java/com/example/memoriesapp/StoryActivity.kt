package com.example.memoriesapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.memoriesapp.Adapter_Classes.StoryAdapter
import com.example.memoriesapp.Model_Classes.Story
import com.example.memoriesapp.Model_Classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import jp.shts.android.storiesprogressview.StoriesProgressView

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    var currentUserId: String = ""
    var userId: String = ""
    var imageList: List<String>? = null
    var storyIdList: List<String>? = null
    var storiesProgressView: StoriesProgressView? = null
    var counter = 0
    var pressTime = 0L
    var limit = 500L

    private var onTouchListener = View.OnTouchListener { v, event ->
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView!!.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storiesProgressView!!.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userid").toString()

        storiesProgressView = findViewById(R.id.progress_bar)

        findViewById<LinearLayout>(R.id.layout_seen).visibility = View.GONE
        findViewById<TextView>(R.id.story_delete).visibility = View.GONE

        if(userId == currentUserId) {
            findViewById<LinearLayout>(R.id.layout_seen).visibility = View.VISIBLE
            findViewById<TextView>(R.id.story_delete).visibility = View.VISIBLE
        }

        getStories(userId!!)
        userInfo(userId!!)

        val reverse: View = findViewById(R.id.reverse)
        reverse.setOnClickListener { storiesProgressView!!.reverse() }
        reverse.setOnTouchListener(onTouchListener)

        val skip: View = findViewById(R.id.skip)
        skip.setOnClickListener { storiesProgressView!!.skip() }
        skip.setOnTouchListener(onTouchListener)

        findViewById<TextView>(R.id.view_number).setOnClickListener {
            val intent = Intent(this@StoryActivity, DisplayUsersActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("storyid", storyIdList!![counter])
            intent.putExtra("title", "views")
            startActivity(intent)
        }

        findViewById<TextView>(R.id.story_delete).setOnClickListener{
            val ref = FirebaseDatabase.getInstance().reference
                .child("Story")
                .child(userId!!)
                .child(storyIdList!![counter])

            ref.removeValue().addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Toast.makeText(this@StoryActivity, "Memory Story Deleted", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@StoryActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun getStories(userId: String) {
        imageList = ArrayList()
        storyIdList = ArrayList()

        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId!!)

        storyRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (imageList as ArrayList<String>).clear()
                (storyIdList as ArrayList<String>).clear()

                for(snap in snapshot.children) {
                    val story: Story? = snap.getValue<Story>(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()

                    if(timeCurrent>story!!.getTimeStart() && timeCurrent<story.getTimeEnd()) {
                        (imageList as ArrayList<String>).add(story.getImageUrl())
                        (storyIdList as ArrayList<String>).add(story.getStoryId())
                    }
                }
                storiesProgressView!!.setStoriesCount((imageList as ArrayList<String>).size)
                storiesProgressView!!.setStoryDuration(8000L)
                storiesProgressView!!.setStoriesListener(this@StoryActivity)
                storiesProgressView!!.startStories(counter)
                Picasso.get().load(imageList!!.get(counter)).into(findViewById<ImageView>(R.id.view_story_image))

                addView(storyIdList!!.get(counter))
                viewNumber(storyIdList!!.get(counter))
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun userInfo(userId: String) {
        val userRef = FirebaseDatabase.getInstance()
            .reference
            .child("Users")
            .child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(findViewById<CircleImageView>(R.id.story_profile_image))

                    findViewById<TextView>(R.id.story_username).text = user.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addView(storyId: String) {
        FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId!!)
            .child(storyId)
            .child("views")
            .child(currentUserId)
            .setValue(true)
    }

    private fun viewNumber(storyId: String) {
        val viewRef = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId!!)
            .child(storyId)
            .child("views")

        viewRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                findViewById<TextView>(R.id.view_number).text = "" + snapshot.childrenCount
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        storiesProgressView!!.destroy()
    }

    override fun onResume() {
        super.onResume()
        storiesProgressView!!.resume()
    }

    override fun onPause() {
        super.onPause()
        storiesProgressView!!.pause()
    }

    override fun onComplete() {
        finish()
    }

    override fun onPrev() {
        if(counter - 1 < 0) return
        Picasso.get().load(imageList!![--counter]).into(findViewById<ImageView>(R.id.view_story_image))
        viewNumber(storyIdList!![counter])
    }

    override fun onNext() {
        Picasso.get().load(imageList!![++counter]).into(findViewById<ImageView>(R.id.view_story_image))
        addView(storyIdList!![counter])
        viewNumber(storyIdList!![counter])
    }
}