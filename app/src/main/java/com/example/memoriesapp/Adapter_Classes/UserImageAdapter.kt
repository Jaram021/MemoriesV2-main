package com.example.memoriesapp.Adapter_Classes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.memoriesapp.Fragments.MemoryDetailsFragment
import com.example.memoriesapp.Model_Classes.Memories
import com.example.memoriesapp.R
import com.squareup.picasso.Picasso

class UserImageAdapter(private val mContext: Context, mMemory: List<Memories>)
    : RecyclerView.Adapter<UserImageAdapter.ViewHolder?>() {

    private var mMemory: List<Memories>? = null

    init {
        this.mMemory = mMemory
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.image_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mMemory!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memory: Memories = mMemory!![position]
        Picasso.get().load(memory.getMemoryImage()).into(holder.userImage)

        holder.userImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("memoryId", memory.getMemoryId())
            editor.apply()
            (mContext as FragmentActivity)
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, MemoryDetailsFragment()).commit()
        }
    }

    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView) {
        var userImage: ImageView

        init {
            userImage = itemView.findViewById(R.id.memory_image)
        }
    }
}