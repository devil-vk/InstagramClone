package com.example.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramclone.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.alreadyRegistered.setOnClickListener {
            var intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
        }

        binding.signupBtn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullName = binding.fullnameSignup.text.toString()
        val userName = binding.usernameSignup.text.toString()
        val email = binding.emailSignup.text.toString()
        val password = binding.passwordSignup.text.toString()

        when{
            TextUtils.isEmpty(fullName)->Toast.makeText(this,"Fullname is required",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(userName)->Toast.makeText(this,"Username is required",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email)->Toast.makeText(this,"Email is required",Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password)->Toast.makeText(this,"Password is required",Toast.LENGTH_SHORT).show()

            else->{
                progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Creating Account")
                progressDialog.setMessage("We're creating your account.")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                val mAuth:FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful){
                            saveUserInfo(fullName,userName,email)
                        }
                        else{
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error: $message",Toast.LENGTH_SHORT).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String) {

        val currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef:DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

        val userMap = HashMap<String,Any>()
        userMap["uid"]= currentUid
        userMap["fullname"]= fullName.toLowerCase()
        userMap["username"]= userName.toLowerCase()
        userMap["email"]= email
        userMap["bio"]= "Hey! I'm using Instagram Clone app"
        userMap["image"]="https://firebasestorage.googleapis.com/v0/b/instagram-clone-vamsi-krishna.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=854007fd-56df-4020-a4f6-6852a81d5070"

        usersRef.child(currentUid).setValue(userMap)
            .addOnCompleteListener {task->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account Created Successfully",Toast.LENGTH_SHORT).show()

                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(currentUid)
                            .child("Following").child(currentUid)
                            .setValue(true)

                    val intent = Intent(this,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error: $message",Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }



    }
}