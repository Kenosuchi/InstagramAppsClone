package com.example.instagramapps.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapps.Model.Comment
import com.example.instagramapps.Model.User
import com.example.instagramapps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(private val mContext:Context,
                     private val mComment:MutableList<Comment>?): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    private var firebaseUser:FirebaseUser? = null
    inner class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView){
        var imageProfile:CircleImageView
        var userNametxt:TextView
        var commenttxt:TextView

        init {
            imageProfile = itemView.findViewById(R.id.user_profile_image_item_comment)
            userNametxt = itemView.findViewById(R.id.user_name_item_comment)
            commenttxt = itemView.findViewById(R.id.comment_text_item_comment)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_view_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mComment!!.size
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val comment = mComment!![position]
        holder.commenttxt.text = comment.getComments()
        getUserInfo(holder.imageProfile,holder.userNametxt,comment.getPublisher())
    }

    private fun getUserInfo(imageProfile: CircleImageView, userNametxt: TextView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance().getReference().child("User").child(publisher)
        userRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imageProfile)
                    userNametxt.text=user.getUsername()

                }
            }

        })

    }
}