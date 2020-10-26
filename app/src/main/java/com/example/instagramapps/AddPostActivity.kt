package com.example.instagramapps

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {
    private var mUrl = ""
    private var imageUri: Uri? = null
    private var storageProfileRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storageProfileRef = FirebaseStorage.getInstance().reference.child("Post Images")

        CropImage.activity().setAspectRatio(2,1)
            .start(this@AddPostActivity)

        save_add_post_btn.setOnClickListener{
            uploadImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE&&resultCode==Activity.RESULT_OK){
            var result = CropImage.getActivityResult(data)
            imageUri = result.uri
            image_post.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        when{

            imageUri==null-> Toast.makeText(this,"Please select image",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(description_post.text.toString())->Toast.makeText(this,"Please write description",Toast.LENGTH_LONG).show()

            else->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait...")
                progressDialog.show()

                var fileRef = storageProfileRef!!.child(System.currentTimeMillis().toString()+".jpg")

                var uploadTask: StorageTask<*>
                uploadTask= fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{
                    if(!it.isSuccessful){
                        it.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener{
                    if(it.isSuccessful){
                        val downloadUrl = it.result
                        mUrl = downloadUrl.toString()
                        AddNewPost(description_post.text.toString().toLowerCase(),
                            FirebaseAuth.getInstance().currentUser!!.uid,mUrl)
                        progressDialog.dismiss()
                    }
                    else{
                        progressDialog.dismiss()
                    }
                }
            }
        }

    }

    private fun AddNewPost(description:String,publisher:String,postImage:String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
        val postId = ref.push().key

        val postMap = HashMap<String,Any>()
        postMap["postId"] = postId!!
        postMap["Description"] = description
        postMap["publisher"] = publisher
        postMap["postImage"] = postImage

        ref.child(postId).updateChildren(postMap)

        Toast.makeText(this@AddPostActivity,"Post uploaded successfully.",Toast.LENGTH_LONG).show()

        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
        startActivity(intent)
        finish()


    }

}