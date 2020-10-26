package com.example.instagramapps

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.instagramapps.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_setting.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var mUrl = ""
    private var imageUri: Uri? = null
    private var storageProfileRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfileRef = FirebaseStorage.getInstance().reference.child("Profile Images")

        profile_setting_logout_btn.setOnClickListener {
            signOutHandler()
        }

        profile_setting_change_image_text.setOnClickListener {
            checker = "clicked"
            CropImage.activity().setAspectRatio(1, 1)
                .start(this@AccountSettingActivity)
        }

        save_profile_setting_btn.setOnClickListener {
            updateUserInfo()
        }

        getUserInfo()
    }

    private fun updateUserInfo() {
        if (TextUtils.isEmpty(profile_setting_fullname.text.toString())) {
            Toast.makeText(this, "Please write full name", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(profile_setting_username.text.toString())) {
            Toast.makeText(this, "Please write user name", Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(profile_setting_bio.text.toString())) {
            Toast.makeText(this, "Please write bio", Toast.LENGTH_LONG).show()
        } else {
            if (checker == "clicked") {
                UpdateUserInfoWithImage()
            } else
                UpdateUserInfoWithoutImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_setting_image.setImageURI(imageUri)

        }
    }

    private fun UpdateUserInfoWithoutImage() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Account Settings")
        progressDialog.setMessage("Updating your profile...")
        progressDialog.show()

        UpdateUserDatabase(
            profile_setting_fullname.text.toString().toLowerCase(),
            profile_setting_username.text.toString().toLowerCase(),
            profile_setting_bio.text.toString(), null
        )

        progressDialog.dismiss()
    }

    private fun UpdateUserInfoWithImage() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Account Settings")
        progressDialog.setMessage("Updating your profile...")
        progressDialog.show()

        if (imageUri == null) {
            Toast.makeText(this, "Please select image", Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
            return
        }
        val fileRef = storageProfileRef!!.child(firebaseUser.uid + ".jpg")
        var uploadTask: StorageTask<*>
        uploadTask = fileRef.putFile(imageUri!!)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                    progressDialog.dismiss()
                }
            }
            return@Continuation fileRef.downloadUrl
        }).addOnCompleteListener {
            if (it.isSuccessful) {
                val downloadUrl = it.result
                mUrl = downloadUrl.toString()

                UpdateUserDatabase(
                    profile_setting_fullname.text.toString().toLowerCase(),
                    profile_setting_username.text.toString().toLowerCase(),
                    profile_setting_bio.text.toString(), mUrl
                )
            }
            progressDialog.dismiss()
        }
    }

    private fun UpdateUserDatabase(
        fullName: String,
        userName: String,
        bio: String,
        image: String?
    ) {

        val userRef = FirebaseDatabase.getInstance().getReference().child("User")
        val userMap = HashMap<String, Any>()
        userMap["fullname"] = fullName
        userMap["username"] = userName
        userMap["bio"] = bio
        if (image != null)
            userMap["image"] = image
        userRef.child(firebaseUser.uid).updateChildren(userMap)

        Toast.makeText(this, "Account info has been updated", Toast.LENGTH_LONG).show()

        val intent = Intent(this@AccountSettingActivity, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun signOutHandler() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this@AccountSettingActivity, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun getUserInfo() {
        val userRef =
            FirebaseDatabase.getInstance().getReference().child("User").child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<User>(User::class.java)

                Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                    .into(profile_setting_image)
                profile_setting_fullname.setText(user.getUsername())
                profile_setting_username.setText(user.getFullname())
                profile_setting_bio.setText(user.getBio())
            }
        })
    }
}