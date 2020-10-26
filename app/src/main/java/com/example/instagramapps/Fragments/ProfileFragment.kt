package com.example.instagramapps.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapps.AccountSettingActivity
import com.example.instagramapps.Adapter.MyImageAdapter
import com.example.instagramapps.Model.Post
import com.example.instagramapps.Model.User
import com.example.instagramapps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postListUploadImg: MutableList<Post>?=null
    var myUploadImageAdapte:MyImageAdapter?=null

    var postListSaveImg: MutableList<Post>?=null
    var mySaveImageAdapte:MyImageAdapter?=null
    var mySaveImage:MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            if (pref.getString("FromSearch", "FALSE") == "TRUE") {
                this.profileId = pref.getString("profileId", "none")!!
                val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
                pref?.putString("FromSearch", "FALSE")
                pref?.apply()
            } else
                profileId = "none"
        }
        if (profileId == "none")
            profileId = firebaseUser.uid
        if (profileId == firebaseUser.uid) {
            view.profile_edit_btn.text = "Edit Profile"
        } else {
            CheckFollowAndFollowing()
        }

        var recyclerViewUploadImage:RecyclerView = view.findViewById(R.id.recycler_view_upload_img)
        recyclerViewUploadImage.setHasFixedSize(true)
        var upLoadImgLinearLayoutManager:LinearLayoutManager = GridLayoutManager(context,3)
        recyclerViewUploadImage.layoutManager = upLoadImgLinearLayoutManager

        var recyclerViewSaveImage:RecyclerView = view.findViewById(R.id.recycler_view_save_img)
        recyclerViewSaveImage.setHasFixedSize(true)
        var saveImgLinearLayoutManager:LinearLayoutManager = GridLayoutManager(context,3)
        recyclerViewSaveImage.layoutManager = saveImgLinearLayoutManager

        postListUploadImg = ArrayList()
        postListSaveImg = ArrayList()
        myUploadImageAdapte = context?.let { MyImageAdapter(it,postListUploadImg as ArrayList<Post>) }
        mySaveImageAdapte = context?.let { MyImageAdapter(it,postListSaveImg as ArrayList<Post>) }
        recyclerViewUploadImage.adapter = myUploadImageAdapte
        recyclerViewSaveImage.adapter = mySaveImageAdapte

        view.profile_edit_btn.setOnClickListener {
            val getBtnText = view.profile_edit_btn.text.toString()
            when {
                getBtnText == "Edit Profile" -> startActivity(
                    Intent(
                        context,
                        AccountSettingActivity::class.java
                    )
                )
                getBtnText == "Follow" -> FollowHandler("Follow")
                getBtnText == "Following" -> FollowHandler("Following")
            }
        }

        getUserInfo()

        return view
    }

    private fun getUserInfo() {
        getFollowersAndFollowing("Follower")
        getFollowersAndFollowing("Following")
        getTotalPosts()
        userInfo()
        myUploadImage()
        mySavedImage()
    }


    private fun FollowHandler(type: String) {
        if (type == "Follow") {
            firebaseUser?.uid.let { itw ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(itw.toString())
                    .child("Following").child(profileId).setValue(true)
            }
            firebaseUser?.uid.let { itw ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(profileId)
                    .child("Followers").child(itw.toString()).setValue(true)
            }
        } else if (type == "Following") {
            firebaseUser?.uid.let { itw ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(itw.toString())
                    .child("Following").child(profileId).removeValue()
            }
            firebaseUser?.uid.let { itw ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(profileId)
                    .child("Followers").child(itw.toString()).removeValue()
            }
        }
    }

    private fun CheckFollowAndFollowing() {
        val followRef = firebaseUser?.uid.let { itw ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(itw.toString())
                .child("Following")
        }

        if (followRef != null) {
            followRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(profileId).exists()) {
                        view?.profile_edit_btn?.text = "Following"
                    } else {
                        view?.profile_edit_btn?.text = "Follow"
                    }
                }

            })
        }
    }

    private fun getFollowersAndFollowing(type: String) {
        val followerRef: DatabaseReference
        followerRef = if (type == "Follower") {
            profileId.let { itw ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(itw.toString())
                    .child("Followers")
            }
        } else {
            profileId.let { itw ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(itw.toString())
                    .child("Following")
            }
        }

        followerRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if (type == "Follower")
                        view?.total_followers?.text = snapshot.childrenCount.toString()
                    else
                        view?.total_following?.text = (snapshot.childrenCount - 1).toString()
                }
            }

        })


    }

    private fun getTotalPosts() {
        var totalPosts = 0;
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(child in snapshot.children){
                        var post = child.getValue(Post::class.java)
                        if(post!!.getPublisher()==profileId)
                            ++totalPosts
                    }
                }
                if(totalPosts>0){
                    view?.total_posts?.text = totalPosts.toString()
                }
            }
        })
    }

    private fun userInfo() {
        Log.i("ProfileID", profileId)
        val userRef = FirebaseDatabase.getInstance().getReference().child("User").child(profileId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<User>(User::class.java)

                Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                    .into(view?.profile_image)
                view?.profile_username?.text = user.getUsername()
                view?.profile_full_name?.text = user.getFullname()
                view?.profile_bio?.text = user.getBio()
            }
        })
    }

    private fun myUploadImage(){
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    postListUploadImg!!.clear()

                    for(child in snapshot.children){
                        val post = child.getValue(Post::class.java)
                        if(post!!.getPublisher().equals(profileId)){
                            postListUploadImg!!.add(post)
                        }
                        postListUploadImg!!.reverse()
                        myUploadImageAdapte!!.notifyDataSetChanged()
                    }
                }
            }

        })
    }
    private fun mySavedImage() {
        mySaveImage = ArrayList()

        var saveRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(firebaseUser!!.uid)
        saveRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(child in snapshot.children){
                        mySaveImage!!.add(snapshot.key!!)
                    }
                    readSaveImageData()
                }
            }

        })
    }

    private fun readSaveImageData() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    postListUploadImg!!.clear()

                    for(child in snapshot.children){
                        val post = child.getValue(Post::class.java)
                        if(post!!.getPublisher().equals(profileId)){
                            postListUploadImg!!.add(post)
                        }
                        postListUploadImg!!.reverse()
                        myUploadImageAdapte!!.notifyDataSetChanged()
                    }
                }
            }

        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}