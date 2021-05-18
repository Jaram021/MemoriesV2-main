package com.example.memoriesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.Adapter_Classes.UserAdapter
import com.example.memoriesapp.Model_Classes.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DisplayUsersActivity : AppCompatActivity() {
    var id: String = ""
    var title: String = ""

    var userAdapter: UserAdapter? = null
    var userList: List<User>? = null
    var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_users)

        val intent = intent
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        var recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()

        when(title) {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun getViews() {
        val viewsRef = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(id!!)
            .child(intent.getStringExtra("storyid").toString())
            .child("views")

        viewsRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                (idList as ArrayList<String>).clear()

                for(snap in snapshot.children) {

                    (idList as ArrayList<String>).add(snap.key!!)
                }
                getUsers()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(id!!)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for(snap in snapshot.children) {
                        (idList as ArrayList<String>).add(snap.key!!)
                    }
                    getUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowing() {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow")
            .child(id!!)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for(snap in snapshot.children) {
                        (idList as ArrayList<String>).add(snap.key!!)
                    }
                    getUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getLikes() {
        val likeRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(id)

        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for(snap in snapshot.children) {
                        (idList as ArrayList<String>).add(snap.key!!)
                    }
                    getUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getUsers() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")

        userRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                    (userList as ArrayList<User>).clear()
                    for(snap in snapshot.children) {
                        val user = snap.getValue(User::class.java)
                        for(id in idList!!) {
                            if(user?.getUid() == id) {
                                (userList as ArrayList<User>).add(user!!)
                            }
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}