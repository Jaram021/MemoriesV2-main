package com.example.memoriesapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.*
import com.example.memoriesapp.Adapter_Classes.UserImageAdapter
import com.example.memoriesapp.Model_Classes.Memories
import com.example.memoriesapp.Model_Classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    private  var profileId: String = ""
    private  var firebaseUser: FirebaseUser? = null

    var memoryList: List<Memories>? = null
    var userImageAdapter: UserImageAdapter? = null

    var savedMemoryAdapter: UserImageAdapter? = null
    var savedMemoryList: List<Memories>? = null
    var userSavedMemories: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if(pref != null) {
            this.profileId = pref.getString("profileId", "none").toString() // toString()
        }

        // User on own profile
        if(profileId == firebaseUser!!.uid) {
            view.findViewById<Button>(R.id.edit_account_settings_btn).text = "Edit Profile"
        }
        // User going on other user's page
        else if(profileId != firebaseUser!!.uid) {
            checkFollowAndFollowingButtonStatus()
        }


        // recycler view for user's profile memories
        var recyclerViewProfile: RecyclerView = view.findViewById(R.id.recycler_view_profile)
        recyclerViewProfile.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewProfile.layoutManager = linearLayoutManager

        memoryList = ArrayList()
        userImageAdapter = context?.let { UserImageAdapter(it, memoryList as ArrayList<Memories>)}
        recyclerViewProfile.adapter = userImageAdapter

        // recycler view for saved memories
        var recyclerViewSaved: RecyclerView = view.findViewById(R.id.recycler_view_saved)
        recyclerViewSaved.setHasFixedSize(true)
        val linearLayoutManagerSaved: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewSaved.layoutManager = linearLayoutManagerSaved

        savedMemoryList = ArrayList()
        savedMemoryAdapter = context?.let { UserImageAdapter(it, savedMemoryList as ArrayList<Memories>)}
        recyclerViewSaved.adapter = savedMemoryAdapter


        var userMemoryBtn: ImageButton = view.findViewById(R.id.images_grid_view_btn)
        userMemoryBtn.setOnClickListener {
            recyclerViewSaved.visibility = View.GONE
            recyclerViewProfile.visibility = View.VISIBLE
        }

        var userSavedMemoryBtn: ImageButton = view.findViewById(R.id.images_save_btn)
        userSavedMemoryBtn.setOnClickListener {
            recyclerViewProfile.visibility = View.GONE
            recyclerViewSaved.visibility = View.VISIBLE
        }

        view.findViewById<TextView>(R.id.total_followers).setOnClickListener {
            val intent = Intent(context, DisplayUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        view.findViewById<TextView>(R.id.total_following).setOnClickListener {
            val intent = Intent(context, DisplayUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }


        view.findViewById<Button>(R.id.edit_account_settings_btn).setOnClickListener {
            val getButtonTxt = view.findViewById<Button>(R.id.edit_account_settings_btn).text.toString()

            when {
                getButtonTxt == "Edit Profile" ->
                    startActivity(Intent(context, AccountSettingsActivity::class.java))

                getButtonTxt == "Follow" -> {
                    firebaseUser!!.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(profileId)
                                .setValue(true)
                    }

                    firebaseUser!!.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(it1.toString())
                                .setValue(true)
                    }
                    addNotification()
                }

                getButtonTxt == "Following" -> {
                    firebaseUser!!.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(profileId)
                                .removeValue()
                    }

                    firebaseUser!!.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(it1.toString())
                                .removeValue()
                    }
                    val intent = Intent(context, MainActivity::class.java)
                    context?.startActivity(intent)
                }
            }
        }

        getFollowers()
        getFollowing()
        userInfo()
        getUserImages()
        getMemoryNum()
        getSavedMemories()

        return view
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser!!.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                    .child("Follow").child(it1)
                    .child("Following")
        }
        if(followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child(profileId).exists()) {
                        // Change button to following if user is following
                        view?.findViewById<Button>(R.id.edit_account_settings_btn)?.text = "Following"
                    } else {
                        // Change button to Follow if user is not following
                        view?.findViewById<Button>(R.id.edit_account_settings_btn)?.text = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
                    .child("Follow").child(profileId)
                    .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    view?.findViewById<TextView>(R.id.total_followers)?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getFollowing() {
        val followingRef = FirebaseDatabase.getInstance().reference
                    .child("Follow").child(profileId)
                    .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    view?.findViewById<TextView>(R.id.total_following)?.text = snapshot.childrenCount.toString()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getUserImages() {
        val memoriesRef = FirebaseDatabase.getInstance().reference
            .child("Memories")
        memoriesRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    (memoryList as ArrayList<Memories>).clear()
                    for(memories in snapshot.children) {
                        val memory = memories.getValue(Memories::class.java)!!
                        if(memory.getPublisher().equals(profileId)) {
                            (memoryList as ArrayList<Memories>).add(memory)
                        }
                        Collections.reverse(memoryList)
                        userImageAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(profileId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get()
                            .load(user!!.getImage())
                            .placeholder(R.drawable.profile)
                            .into(view?.findViewById<CircleImageView>(R.id.image_page_frag))
                    view?.findViewById<TextView>(R.id.username_page_frag)?.text = user.getUsername()//.capitalize()
                    view?.findViewById<TextView>(R.id.full_name_page_frag)?.text = user.getFullName()//.capitalize()
                    view?.findViewById<TextView>(R.id.bio_page_frag)?.text = user.getBio()//.capitalize()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getSavedMemories() {
        userSavedMemories = ArrayList()

        val saveRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(firebaseUser!!.uid)

        saveRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    for(saves in snapshot.children) {
                        (userSavedMemories as ArrayList<String>).add(saves.key!!)
                    }
                    readSavedMemoriesData()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun readSavedMemoriesData() {
        val memoryRef = FirebaseDatabase.getInstance().reference
            .child("Memories")

        memoryRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    (savedMemoryList as ArrayList<Memories>).clear()

                    for(snap in snapshot.children) {
                        val memory = snap.getValue(Memories::class.java)

                        for(key in userSavedMemories!!) {
                            if(memory?.getMemoryId() == key) {
                                (savedMemoryList as ArrayList<Memories>).add(memory!!)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getMemoryNum() {
        val memoriesRef = FirebaseDatabase.getInstance().reference
            .child("Memories")

        memoriesRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    var memoryCounter = 0

                    for(memory in snapshot.children) {
                        val memory = memory.getValue(Memories::class.java)
                        if(memory!!.getPublisher() == profileId) {
                            memoryCounter++
                        }
                    }
                    view?.findViewById<TextView>(R.id.total_memories)?.text = " " + memoryCounter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser!!.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser!!.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser!!.uid)
        pref?.apply()
    }

    private fun addNotification() {
        val notificationRef = FirebaseDatabase.getInstance().reference
                .child("Notifications")
                .child(profileId)

        val notificationMap = HashMap<String, Any>()

        notificationMap["userid"] = firebaseUser!!.uid // publisher
        notificationMap["text"] = "started following you"
        notificationMap["memoryid"] = ""
        notificationMap["ismemory"] = false

        notificationRef.push().setValue(notificationMap)
    }
}