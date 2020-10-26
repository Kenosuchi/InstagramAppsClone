package com.example.instagramapps.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapps.Fragments.ProfileFragment
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
import kotlinx.android.synthetic.main.user_item_layout.view.*

class UserAdapter(
    private var mContext: Context,
    private var mUser: List<User>, private var isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTextview: TextView = itemView.findViewById(R.id.user_name_item_seach)
        var userFullNameTextView: TextView = itemView.findViewById(R.id.user_full_name_item_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_image_item_search)
        var followBtn: Button = itemView.findViewById(R.id.user_follow_btn_item_search)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        holder.userNameTextview.text = user.getUsername()
        holder.userFullNameTextView.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile)
            .into(holder.userProfileImage)

        CheckFollowingStatus(user.getUid(), holder.followBtn)

        holder.itemView.setOnClickListener {
            itemViewOnClickListener(user)

        }

        holder.followBtn.setOnClickListener {
            FollowUserHandler(holder, user)
        }
    }

    private fun itemViewOnClickListener(user: User) {
        val pref = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
        pref.putString("profileId",user.getUid())
        pref.putString("FromSearch","TRUE")
        pref.apply()

        (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,ProfileFragment()).commit()
    }

    private fun FollowUserHandler(holder: ViewHolder, user: User) {
        if (holder.followBtn.text.toString() == "Follow") {
            firebaseUser?.uid.let { itw ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(itw.toString())
                    .child("Following").child(user.getUid())
                    .setValue(true).addOnCompleteListener {
                        if (it.isSuccessful) {
                            firebaseUser?.uid.let { itw ->
                                FirebaseDatabase.getInstance().reference
                                    .child("Follow").child(user.getUid())
                                    .child("Followers").child(itw.toString())
                                    .setValue(true).addOnCompleteListener {

                                    }
                            }
                        }
                    }
            }

        } else {
            firebaseUser?.uid.let { itw ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(itw.toString())
                    .child("Following").child(user.getUid())
                    .removeValue().addOnCompleteListener {
                        if (it.isSuccessful) {
                            firebaseUser?.uid.let { itw ->
                                FirebaseDatabase.getInstance().reference
                                    .child("Follow").child(user.getUid())
                                    .child("Followers").child(itw.toString())
                                    .removeValue().addOnCompleteListener {

                                    }
                            }
                        }
                    }
            }
        }
    }

    private fun CheckFollowingStatus(uid: String, followBtn: Button) {
        val followingRef = firebaseUser?.uid.let { itw ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(itw.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(uid).exists())
                    followBtn.text = "Following"
                else
                    followBtn.text = "Follow"
            }

        })
    }

}