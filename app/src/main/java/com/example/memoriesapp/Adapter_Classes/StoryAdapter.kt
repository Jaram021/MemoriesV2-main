package com.example.memoriesapp.Adapter_Classes

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.CreateStoryActivity
import com.example.memoriesapp.Model_Classes.Story
import com.example.memoriesapp.Model_Classes.User
import com.example.memoriesapp.R
import com.example.memoriesapp.StoryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter (private val mContext: Context,
                    private val mStory: List<Story>)
                    : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if(viewType == 0) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item_layout, parent, false)
            return ViewHolder(view)
        } else {
            val view = LayoutInflater.from(mContext).inflate(R.layout.story_item_layout, parent, false)
            return ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = mStory[position]

        userInfo(holder, story.getUserId(), position)

        if(holder.adapterPosition !== 0) {
            viewedStory(holder, story.getUserId())
        }

        if(holder.adapterPosition === 0) {
            userStories(holder.addStoryText!!, holder.storyPlusBtn!!, false)
        }

        holder.itemView.setOnClickListener {
            if(holder.adapterPosition === 0) {
                userStories(holder.addStoryText!!, holder.storyPlusBtn!!, true)
            } else {
                val intent = Intent(mContext, StoryActivity::class.java)
                intent.putExtra("userid", story.getUserId())
                mContext.startActivity(intent)
            }
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        // viewHolder for story items
        var storyImage: CircleImageView? = null
        var storyImageSeen: CircleImageView? = null
        var storyUserName: TextView? = null

        // viewHolder for add story items
        var storyPlusBtn: ImageView? = null
        var addStoryText: TextView? = null



        init {
            // initialize story items
            storyImage = itemView.findViewById(R.id.story_image)
            storyImageSeen = itemView.findViewById(R.id.story_image_seen)
            storyUserName = itemView.findViewById(R.id.story_username)

            // initialize add story items
            storyPlusBtn = itemView.findViewById(R.id.story_add)
            addStoryText = itemView.findViewById(R.id.add_story_text)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0) {
            return 0
        } else {
            return 1
        }
    }

    private fun userInfo(viewHolder: ViewHolder, userId: String, position: Int) {
        val userRef = FirebaseDatabase.getInstance()
            .reference
            .child("Users")
            .child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(viewHolder.storyImage)

                    if(position != 0) {
                        Picasso.get()
                            .load(user!!.getImage())
                            .placeholder(R.drawable.profile)
                            .into(viewHolder.storyImageSeen)
                        viewHolder.storyUserName?.text = user.getUsername()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun userStories(textView: TextView, imageView: ImageView, click: Boolean) {
        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        storyRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var counter = 0

                val timeCurrent = System.currentTimeMillis()

                for(snap in snapshot.children) {
                    val story = snap.getValue(Story::class.java)
                    if(timeCurrent>story!!.getTimeStart() && timeCurrent<story!!.getTimeEnd()) {
                        counter++
                    }
                }
                if(click) {
                    if(counter > 0) {
                        val alertDialog = AlertDialog.Builder(mContext).create()

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story"){

                            dialogInterface, which ->

                            val intent = Intent(mContext, StoryActivity::class.java)
                            intent.putExtra("userid", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialogInterface!!.dismiss()
                        }

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story") {
                            dialogInterface, which ->

                            val intent = Intent(mContext, CreateStoryActivity::class.java)
                            intent.putExtra("userid", FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialogInterface!!.dismiss()
                        }
                        alertDialog.show()
                    } else {
                        val intent = Intent(mContext, CreateStoryActivity::class.java)
                        intent.putExtra("userid", FirebaseAuth.getInstance().currentUser!!.uid)
                        mContext.startActivity(intent)
                    }
                } else {
                    if(counter > 0) {
                        textView.text = "My Story"
                        imageView.visibility = View.GONE
                    } else {
                        textView.text = "Add Story"
                        imageView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun viewedStory(viewHolder: ViewHolder, userId: String) {
        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)

        storyRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var counter = 0
                for(snap in snapshot.children) {
                    if(!snap.child("views").child(FirebaseAuth.getInstance().currentUser!!.uid).exists()
                        && System.currentTimeMillis() < snap.getValue(Story::class.java)!!.getTimeEnd()) {
                        counter++
                        if(counter>0) {
                            viewHolder.storyImage!!.visibility = View.VISIBLE
                            viewHolder.storyImageSeen!!.visibility = View.GONE
                        } else {
                            viewHolder.storyImage!!.visibility = View.GONE
                            viewHolder.storyImageSeen!!.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}