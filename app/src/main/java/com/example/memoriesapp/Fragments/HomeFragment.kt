package com.example.memoriesapp.Fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu

import android.widget.Toast
import androidx.core.app.ActivityCompat.recreate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.Adapter_Classes.MemoryAdapter
import com.example.memoriesapp.Adapter_Classes.StoryAdapter
import com.example.memoriesapp.MainActivity
import com.example.memoriesapp.Model_Classes.Memories
import com.example.memoriesapp.Model_Classes.Story
import com.example.memoriesapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.logging.Logger

class HomeFragment : Fragment() {
    private var memoryAdapter: MemoryAdapter? = null
    private var memoryList: MutableList<Memories>? = null
    private var followingList: MutableList<String>? = null

    private var storyAdapter: StoryAdapter? = null
    private var storyList: MutableList<Story>? = null

    private val LOG = Logger.getLogger(HomeFragment::class.java.name)
    val currentUser = FirebaseAuth.getInstance().currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        var recyclerView: RecyclerView? = null
        var recyclerViewStory: RecyclerView? = null

        recyclerView = view.findViewById(R.id.recycler_view)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true // display new posts at the top
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        memoryList = ArrayList()
        memoryAdapter = context?.let { MemoryAdapter(it, memoryList as ArrayList<Memories>) }
        recyclerView.adapter = memoryAdapter

        recyclerViewStory = view.findViewById(R.id.recycler_view_story)
        recyclerViewStory.setHasFixedSize(true)
        val linearLayoutManagerStory = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewStory.layoutManager = linearLayoutManagerStory

        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }
        recyclerViewStory.adapter = storyAdapter

        checkFollowing()

        //updateAdapters()
        LOG.warning("!!!!!!!!!!!!!!!!!on create called!!!!!!!!!!!!!!!!!")
        return view
    }


    private fun checkFollowing() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    (followingList as ArrayList<String>).clear()

                    for(mem in snapshot.children) {
                        mem.key?.let {
                            (followingList as ArrayList<String>).add(it)
                        }
                    }
                    getMemories()
                    getStories()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getMemories() {
        val memoryRef = FirebaseDatabase.getInstance().reference.child("Memories")

        memoryRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                memoryList?.clear()

                for(mem in snapshot.children) {
                    val memory = mem.getValue(Memories::class.java)
                    for(uid in (followingList as ArrayList<String>)) {
                        if(memory!!.getPublisher() == uid) {
                            memoryList!!.add(memory)
                        }
                    }
                }
                memoryAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getStories() {
        val storyRef = FirebaseDatabase.getInstance().reference
                .child("Story")

        storyRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()

                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).add(Story("", 0, 0, "", FirebaseAuth.getInstance().currentUser!!.uid))

                for(id in followingList!!) {

                    var countStory = 0

                    var story: Story?= null

                    for(snap in snapshot.child(id).children) {

                        story = snap.getValue(Story::class.java)

                        if(timeCurrent>story!!.getTimeStart() && timeCurrent<story!!.getTimeEnd()) {
                            countStory++
                        }
                    }
                    if(countStory>0) {
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}