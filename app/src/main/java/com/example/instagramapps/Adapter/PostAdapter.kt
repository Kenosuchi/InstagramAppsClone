package com.example.instagramapps.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapps.CommentActivity
import com.example.instagramapps.Model.Post
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

class PostAdapter(private val mContext:Context,
                  private val mPost:List<Post>):RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser:FirebaseUser?=null
    inner class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView){
        var publisherImage:CircleImageView
        var postImage: ImageView
        var commentBtn:ImageView
        var saveBtn:ImageView
        var liketBtn:ImageView
        var username:TextView
        var likes:TextView
        var publisher:TextView
        var comments:TextView
        var description:TextView

        init {
            publisherImage=itemView.findViewById(R.id.user_profile_image_post)
            postImage=itemView.findViewById(R.id.post_image_home)
            commentBtn=itemView.findViewById(R.id.post_image_comment_btn)
            saveBtn=itemView.findViewById(R.id.post_save_comment_btn)
            liketBtn=itemView.findViewById(R.id.post_image_like_btn)
            username=itemView.findViewById(R.id.user_name_post)
            likes=itemView.findViewById(R.id.likes)
            publisher=itemView.findViewById(R.id.publisher)
            comments=itemView.findViewById(R.id.comments)
            description=itemView.findViewById(R.id.description)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser=FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostImage()).into(holder.postImage)

        CheckDescriptionPost(post,holder.description)
        CheckUserLikePost(post.getPostId(),holder.liketBtn)
        CheckNumberOfTotalLikesPost(holder.likes,post.getPostId())
        CheckNumberOfTotalCommentsPost(holder.comments,post.getPostId())
        CheckSavedPostStatus(holder.saveBtn,post.getPostId())
        publisherInfo(holder.publisherImage,holder.username,holder.publisher,post.getPublisher())

        holder.liketBtn.setOnClickListener{
            LikeAndUnlikePostHandle(holder.liketBtn,post)
        }
        holder.commentBtn.setOnClickListener{
            MoveToCommmentActivity(post)
        }
        holder.saveBtn.setOnClickListener{
            UserSaveImage(holder.saveBtn,post)
        }
        holder.comments.setOnClickListener{
            MoveToCommmentActivity(post)
        }
    }

    private fun MoveToCommmentActivity(post:Post) {
        val intent = Intent(mContext,CommentActivity::class.java)
        intent.putExtra("postId",post.getPostId())
        intent.putExtra("publisherId",post.getPublisher())

        mContext.startActivity(intent)
    }

    private fun CheckDescriptionPost(post: Post, description: TextView) {
        if(post.getDescription()==""){
            description.visibility = View.GONE
        }else{
            description.visibility= View.VISIBLE
            description.text = post.getDescription()
        }
    }

    private fun CheckNumberOfTotalLikesPost(likes: TextView, postId: String) {
        var likeRef = FirebaseDatabase.getInstance().reference.child("Like").child(postId)

        likeRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    likes.visibility = View.VISIBLE
                    likes.text = snapshot.childrenCount.toString() + "  likes"
                }
                else{
                    likes.visibility = View.GONE
                }
            }

        })
    }

    private fun CheckNumberOfTotalCommentsPost(comments: TextView, postId: String) {
        var commentRef = FirebaseDatabase.getInstance().reference.child("Comment").child(postId)

        commentRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    comments.visibility = View.VISIBLE
                    comments.text = "View all " + snapshot.childrenCount.toString() + "  comments"
                }
                else{
                    comments.visibility = View.GONE
                }
            }

        })
    }

    private fun CheckUserLikePost(postId: String, liketBtn: ImageView) {
        var likeRef = FirebaseDatabase.getInstance().reference.child("Like").child(postId)

        likeRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(firebaseUser!!.uid).exists()){
                    liketBtn.setImageResource(R.drawable.heart_clicked)
                    liketBtn.tag = "Like"
                }else{
                    liketBtn.setImageResource(R.drawable.heart_not_clicked)
                    liketBtn.tag = "UnLike"
                }
            }

        })

    }

    private fun LikeAndUnlikePostHandle(liketBtn: ImageView,post:Post) {
        if(liketBtn.tag=="UnLike"){
            FirebaseDatabase.getInstance().reference
                .child("Like")
                .child(post.getPostId())
                .child(firebaseUser!!.uid).setValue(true)

        }else{
            FirebaseDatabase.getInstance().reference
                .child("Like")
                .child(post.getPostId())
                .child(firebaseUser!!.uid).removeValue()
        }
    }

    private fun CheckSavedPostStatus(saveBtn: ImageView, postId: String) {
        var saveRef = FirebaseDatabase.getInstance().reference
            .child("Saves").child(firebaseUser!!.uid)
        saveRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(postId).exists()){
                    saveBtn.setImageResource(R.drawable.save_large_icon)
                    saveBtn.tag = "Saved"
                }else{
                    saveBtn.setImageResource(R.drawable.save_unfilled_large_icon)
                    saveBtn.tag = "Unsaved"
                }
            }

        })
    }

    private fun UserSaveImage(
        saveBtn: ImageView,
        post: Post
    ) {
        if(saveBtn.tag == "Unsaved") {
            FirebaseDatabase.getInstance().reference
                .child("Saves")
                .child(firebaseUser!!.uid)
                .child(post.getPostId()).setValue(true)
        }else{
            FirebaseDatabase.getInstance().reference
                .child("Saves")
                .child(firebaseUser!!.uid)
                .child(post.getPostId()).removeValue()
        }

    }

    private fun publisherInfo(publisherImage: CircleImageView, username: TextView, publisher: TextView, postPublisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("User").child(postPublisher)

        userRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(publisherImage)
                    username.text = user.getUsername()
                    publisher.text = user.getFullname()

                }
            }

        })
    }

}