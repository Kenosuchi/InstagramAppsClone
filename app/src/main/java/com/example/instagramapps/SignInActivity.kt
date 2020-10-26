package com.example.instagramapps

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        login_signup_btn.setOnClickListener{
            startActivity(Intent(this,SignUpActivity::class.java))
        }
        login_login_btn.setOnClickListener{
            loginUser()
        }

    }

    private fun loginUser() {
        val email = login_email.text.toString()
        val password = login_password.text.toString()
        when{
            TextUtils.isEmpty(email)-> Toast.makeText(this,"email is require",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password)-> Toast.makeText(this,"password is require",Toast.LENGTH_LONG).show()

            else-> {
                val progressdialog = ProgressDialog(this@SignInActivity)
                progressdialog.setTitle("Login")
                progressdialog.setMessage("Please wait...")
                progressdialog.setCanceledOnTouchOutside(false)
                progressdialog.show()

                val mAuth:FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                    if(it.isSuccessful){
                        progressdialog.dismiss()

                        val intent = Intent(this@SignInActivity,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
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

    }

    override fun onStart() {
        super.onStart()

        if(FirebaseAuth.getInstance().currentUser!=null){
            val intent = Intent(this@SignInActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}