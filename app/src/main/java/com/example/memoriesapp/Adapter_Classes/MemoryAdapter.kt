package com.example.memoriesapp.Adapter_Classes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.*
import com.example.memoriesapp.Fragments.MemoryDetailsFragment
import com.example.memoriesapp.Fragments.ProfileFragment
import com.example.memoriesapp.Model_Classes.Memories
import com.example.memoriesapp.Model_Classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

// Memory Adapter to attach views to respective memories as well as controls for their functionality.
class MemoryAdapter
    (private val mContext: Context, private val mMemory: List<Memories>) : RecyclerView.Adapter<MemoryAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    // Inflates the layout on creation
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.memories_layout, parent, false)
        return ViewHolder(view)
    }

    // Returns the memory list, mMemory size
    override fun getItemCount(): Int {
        return mMemory.size
    }

    // onBindViewHolder for MemoryAdapter.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val memory = mMemory[position]

        Picasso.get().load(memory.getMemoryImage()).into(holder.memoryImage)

        if (memory.getDescription().equals("")) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = memory.getDescription()
        }

        publisherInfo(holder.profileImage, holder.userName, holder.publisher, memory.getPublisher())
        isLiked(memory.getMemoryId(), holder.likeBtn)
        likeNum(holder.likes, memory.getMemoryId())
        commentNum(holder.comments, memory.getMemoryId())
        checkIfMemorySaved(memory.getMemoryId(), holder.saveBtn)

        holder.likeBtn.setOnClickListener {
            if (holder.likeBtn.tag == "Like") {
                FirebaseDatabase.getInstance().reference
                        .child("Likes")
                        .child(memory.getMemoryId())
                        .child(firebaseUser!!.uid)
                        .setValue(true)

                addNotification(memory.getPublisher(), memory.getMemoryId())
            } else {
                FirebaseDatabase.getInstance().reference
                        .child("Likes")
                        .child(memory.getMemoryId())
                        .child(firebaseUser!!.uid)
                        .removeValue()

                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }
        }

        holder.saveBtn.setOnClickListener {
            if (holder.saveBtn.tag == "Save") {
                FirebaseDatabase.getInstance().reference
                        .child("Saves")
                        .child(firebaseUser!!.uid)
                        .child(memory.getMemoryId())
                        .setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference
                        .child("Saves")
                        .child(firebaseUser!!.uid)
                        .child(memory.getMemoryId())
                        .removeValue()
            }
        }

        holder.commentBtn.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("memoryId", memory.getMemoryId())
            intent.putExtra("publisherId", memory.getPublisher())
            mContext.startActivity(intent)
        }

        holder.comments.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("memoryId", memory.getMemoryId())
            intent.putExtra("publisherId", memory.getPublisher())
            mContext.startActivity(intent)
        }

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, DisplayUsersActivity::class.java)
            intent.putExtra("id", memory.getMemoryId())
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        holder.memoryImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("memoryId", memory.getMemoryId())
            editor.apply()
            (mContext as FragmentActivity)
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, MemoryDetailsFragment()).commit()
        }
        holder.publisher.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("profileId", memory.getPublisher())
            editor.apply()
            (mContext as FragmentActivity)
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.profileImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("profileId", memory.getPublisher())
            editor.apply()
            (mContext as FragmentActivity)
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
        }
        if (firebaseUser?.uid == memory.getPublisher()) {
            holder.optionsBtn.visibility = View.VISIBLE
            holder.optionsBtn.setOnClickListener {
                val currentUserReference = FirebaseDatabase.getInstance().reference
                        .child("Memories").child(FirebaseAuth.getInstance().currentUser!!.uid)

                holder.optionsBtn.setOnClickListener {
                    val popupMenu: PopupMenu = PopupMenu(mContext, holder.optionsBtn)
                    popupMenu.menuInflater.inflate(R.menu.memory_options, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.memory_edit -> mContext.startActivity(Intent(mContext, EditMemoryActivity::class.java))

                            R.id.memory_delete -> deleteMemory(memory)
                        }
                        true
                    })
                    popupMenu.show()
                }
            }
        }
    }

    // Delete Memory and Memory Image from Firebase
    private fun deleteMemory(memory: Memories) {
        FirebaseStorage.getInstance().getReferenceFromUrl(memory.getMemoryImage()).delete()

        FirebaseDatabase.getInstance().reference.child("Memories").child(memory.getMemoryId()).removeValue()
    }

    // Check if the memory is saved for user
    private fun checkIfMemorySaved(memoryId: String, imageView: ImageView) {
        val saveRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(firebaseUser!!.uid)

        saveRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(memoryId).exists()) {
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                } else {
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    // Updates the number of comments on the memory
    private fun commentNum(comments: TextView, memoryId: String) {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(memoryId)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    comments.text = "view all " + snapshot.childrenCount.toString() + " comments"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // updates the amount of likes on a Memory
    private fun likeNum(likes: TextView, memoryId: String) {
        val likeRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(memoryId)

        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    likes.text = snapshot.childrenCount.toString() + " likes"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // checks if Memory is liked by current user
    private fun isLiked(memoryId: String, likeBtn: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(memoryId)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(firebaseUser!!.uid).exists()) {
                    likeBtn.setImageResource(R.drawable.heart_clicked)
                    likeBtn.tag = "Liked"
                } else {
                    likeBtn.setImageResource(R.drawable.heart_not_clicked)
                    likeBtn.tag = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // inner class to ViewHolder to hold itemView
    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var memoryImage: ImageView
        var likeBtn: ImageView
        var commentBtn: ImageView
        var saveBtn: ImageView
        var userName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView
        var optionsBtn: ImageView
        // initialize itemView to respective view
        init {
            profileImage = itemView.findViewById(R.id.profile_image_home)
            memoryImage = itemView.findViewById(R.id.memory_image_home)
            likeBtn = itemView.findViewById(R.id.memory_like_btn)
            commentBtn = itemView.findViewById(R.id.memory_image_comment_btn)
            saveBtn = itemView.findViewById(R.id.memory_save_comment_btn)
            userName = itemView.findViewById(R.id.user_name_home)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)
            optionsBtn = itemView.findViewById(R.id.dropdown_menu)
        }
    }

    // Attaches publisher to a Memory: profile picture, username, and publisher
    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherId: String) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
        usersRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(profileImage)
                    userName.text = user!!.getUsername()
                    publisher.text = user!!.getFullName()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // Add notification to the publisher
    private fun addNotification(userId: String, memoryId: String) {
        val notificationRef = FirebaseDatabase.getInstance().reference
                .child("Notifications")
                .child(userId) // online user

        val notificationMap = HashMap<String, Any>()


        notificationMap["userid"] = firebaseUser!!.uid // publisher
        notificationMap["text"] = "liked your memory"
        notificationMap["memoryid"] = memoryId
        notificationMap["ismemory"] = true

        notificationRef.push().setValue(notificationMap)
    }
}