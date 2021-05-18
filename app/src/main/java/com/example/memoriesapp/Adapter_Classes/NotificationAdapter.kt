package com.example.memoriesapp.Adapter_Classes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.Fragments.MemoryDetailsFragment
import com.example.memoriesapp.Fragments.ProfileFragment
import com.example.memoriesapp.Model_Classes.Memories
import com.example.memoriesapp.Model_Classes.Notification
import com.example.memoriesapp.Model_Classes.User
import com.example.memoriesapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(private val mContext: Context,
                          private val mNotification: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notifications_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNotification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotification[position]

        if(notification.getText().equals("started following you")) {
            holder.text.text = "started following you"
        } else if(notification.getText().equals("liked your post")) {
            holder.text.text = "liked your post"
        } else if(notification.getText().contains("commented:")) {
            // replace with spaced string to display nicely
            holder.text.text = notification.getText().replace("commented:", "commented: ")
        } else {
            holder.text.text = notification.getText()
        }


        getUserInfo(holder.profileImage, holder.userName, notification.getUserId())

        if(notification.getIsMemory()) {
            holder.memoryImage.visibility = View.VISIBLE
            getMemoryImg(holder.memoryImage, notification.getMemoryId())
        } else {
            holder.memoryImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if(notification.getIsMemory()) {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("memoryId", notification.getMemoryId())
                editor.apply()
                (mContext as FragmentActivity)
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, MemoryDetailsFragment()).commit()
            } else {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("profileId", notification.getUserId())
                editor.apply()
                (mContext as FragmentActivity)
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var memoryImage: ImageView
        var profileImage: CircleImageView
        var userName: TextView
        var text: TextView

        init {
            memoryImage = itemView.findViewById(R.id.memory_image_notification)
            profileImage = itemView.findViewById(R.id.profile_image_notification)
            userName = itemView.findViewById(R.id.username_notification)
            text = itemView.findViewById(R.id.comment_notification)
        }
    }

    private fun getUserInfo(imageView: ImageView, userName: TextView, publisherId: String) {
        val userRef = FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(publisherId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get()
                            .load(user!!.getImage())
                            .placeholder(R.drawable.profile)
                            .into(imageView)
                    userName.text = user.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getMemoryImg(imageView: ImageView, memId: String) {
        val memoryRef = FirebaseDatabase.getInstance()
                .reference
                .child("Memories")
                .child(memId)

        memoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val memory = snapshot.getValue<Memories>(Memories::class.java)

                    Picasso.get()
                            .load(memory?.getMemoryImage())
                            .placeholder(R.drawable.profile)
                            .into(imageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}