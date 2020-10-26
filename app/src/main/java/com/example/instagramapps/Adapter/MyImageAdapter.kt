package com.example.instagramapps.Adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapps.Fragments.PostDetailFragment
import com.example.instagramapps.Fragments.ProfileFragment
import com.example.instagramapps.Model.Post
import com.example.instagramapps.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_item_layout.view.*

class MyImageAdapter(private var mContext:Context,
                     private var mPost:List<Post>):RecyclerView.Adapter<MyImageAdapter.ViewHolder>() {

    inner class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView){
        var postImage:ImageView = itemView.findViewById(R.id.post_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.image_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post:Post = mPost!![position]
        Picasso.get().load(post.getPostImage()).into(holder.postImage)

        holder.itemView.setOnClickListener {
            itemOnClickListener(post)
        }
    }

    private fun itemOnClickListener(post:Post) {
        val pref = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
        pref.putString("postId",post.getPostId())
        pref.apply()

        (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PostDetailFragment()).commit()
    }
}