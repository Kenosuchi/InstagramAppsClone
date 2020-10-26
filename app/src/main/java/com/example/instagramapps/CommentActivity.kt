package com.example.instagramapps

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapps.Adapter.CommentAdapter
import com.example.instagramapps.Model.Comment
import com.example.instagramapps.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {
    private var postId:String? = ""
    private var publisherId:String? = ""
    private var firebaseUser:FirebaseUser? = null
    private var commentAdapter:CommentAdapter? = null
    private var commentList:MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")

        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView:RecyclerView= findViewById(R.id.comments_recycler)
        var linearLayoutManager = object : LinearLayoutManager(this) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                // force height of viewHolder here, this will override layout_height from xml
                lp.height = height / 3
                return true
            }
        }
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager
        commentList = ArrayList()
        commentAdapter = CommentAdapter(this,commentList)
        recyclerView.adapter = commentAdapter

        getUserInfo()
        readComment()
        post_comment.setOnClickListener{
            addComment()
        }
        getCommentPost()
    }

    private fun readComment(){
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comment").child(postId!!)
        commentRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    commentList!!.clear()
                    for(child in snapshot.children){
                        val comment = child.getValue(Comment::class.java)
                        commentList!!.add(comment!!)
                    }

                    commentAdapter!!.notifyDataSetChanged()
                }
            }

        })
    }
    private fun addComment() {
        if(comment_write_comment_edt.text.toString()==""){
            Toast.makeText(this@CommentActivity,"Please write your comments",Toast.LENGTH_LONG).show()
            return
        }

        val commentRef = FirebaseDatabase.getInstance().getReference()
            .child("Comment")
            .child(postId!!)

        val commentsMap = HashMap<String,Any>()
        commentsMap["comments"] = comment_write_comment_edt.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentRef.push().setValue(commentsMap)

        comment_write_comment_edt.text.clear()

    }
    private fun getCommentPost() {
        val postRef = FirebaseDatabase.getInstance().getReference()
            .child("Posts")
            .child(postId!!)
            .child("postImage")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val image = snapshot.getValue().toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile)
                        .into(post_image_comment)
                }
            }
        })
    }
    private fun getUserInfo() {
        val userRef = FirebaseDatabase.getInstance().getReference().child("User").child(firebaseUser!!.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<User>(User::class.java)

                Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                    .into(profile_image_comment)
            }
        })
    }
}