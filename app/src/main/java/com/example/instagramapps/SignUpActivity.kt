package com.example.instagramapps

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signup_signin_btn.setOnClickListener{
            startActivity(Intent(this,SignInActivity::class.java))
        }

        signup_signup_btn.setOnClickListener{
            CreateAccount()
        }
    }

    private fun CreateAccount() {
        val fullname = signup_fullname.text.toString()
        val username = signup_username.text.toString()
        val email = signup_email.text.toString()
        val password = signup_password.text.toString()

        when{
            TextUtils.isEmpty(fullname)-> Toast.makeText(this,"Full name is require",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(username)-> Toast.makeText(this,"username is require",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email)-> Toast.makeText(this,"email is require",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password)-> Toast.makeText(this,"password is require",Toast.LENGTH_LONG).show()

            else->{
                val progressdialog = ProgressDialog(this@SignUpActivity)
                progressdialog.setTitle("SignUp")
                progressdialog.setMessage("Please wait...")
                progressdialog.setCanceledOnTouchOutside(false)
                progressdialog.show()

                val mAuth:FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener{
                        if(it.isSuccessful){
                            saveUserInfo(fullname,username,email,progressdialog)

                        }else{
                            val message =it.exception!!.toString()
                            Toast.makeText(this,"ERROR: $message",Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressdialog.dismiss()
                        }
                    }
            }
        }
        

    }

    private fun saveUserInfo(fullname: String, username: String, email: String,progressdialog:ProgressDialog) {
        val currentUserId =FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef:DatabaseReference = FirebaseDatabase.getInstance().reference.child("User")

        val userMap = HashMap<String,Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullname.toLowerCase()
        userMap["username"] = username.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "TOANG VLLLLL"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagramapps-79247.appspot.com/o/Default%20Image%2Fprofile.png?alt=media&token=d0cdbb64-c465-4446-b1ad-59ee1804bafe"

        usersRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    progressdialog.dismiss()
                    Toast.makeText(this,"Account has been created successfully",Toast.LENGTH_LONG).show()

                    FirebaseDatabase.getInstance().reference.child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId).setValue(true)

                    val intent = Intent(this@SignUpActivity,MainActivity::class.java)
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    val message =it.exception!!.toString()
                    Toast.makeText(this,"ERROR: $message",Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressdialog.dismiss()
                }

            }

    }
}