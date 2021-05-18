package com.example.memoriesapp.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.Adapter_Classes.MemoryAdapter
import com.example.memoriesapp.Model_Classes.Memories
import com.example.memoriesapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MemoryDetailsFragment : Fragment() {
    private var memoryAdapter: MemoryAdapter? = null
    private var memoryList: MutableList<Memories>? = null
    private var memoryId: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_memory_details, container, false)

        val preferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if(preferences != null) {
            memoryId = preferences.getString("memoryId", "none").toString()
        }

        var recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_memory_details)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        memoryList = ArrayList()
        memoryAdapter = context?.let { MemoryAdapter(it, memoryList as ArrayList<Memories>) }
        recyclerView.adapter = memoryAdapter

        getMemory()

        return view
    }

    private fun getMemory() {
        val memoryRef = FirebaseDatabase.getInstance().reference
            .child("Memories")
            .child(memoryId)

        memoryRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                memoryList?.clear()

                val memory = snapshot.getValue(Memories::class.java)
                memoryList!!.add(memory!!)
                memoryAdapter!!.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}