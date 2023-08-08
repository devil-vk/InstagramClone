package com.example.instagramclone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramclone.Model.User
import com.example.instagramclone.databinding.ActivityAccountSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var database: FirebaseDatabase
    private lateinit var profilePic: CircleImageView
    private lateinit var userName: EditText
    private lateinit var fullName: EditText
    private lateinit var reference: StorageReference
    private lateinit var bio: EditText
    private var checker = ""
    private var myUrl = ""
    private var imageUri:Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        profilePic = binding.profileImageViewProfileFrag
        userName = binding.usernameSettingsFrag
        fullName = binding.fullNameSettingsFragment
        bio = binding.bioSettingsFragment
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.LogoutBtn.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.closeProfileBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.changeImageTextBtn.setOnClickListener {
            checker = "clicked"
            var intent=Intent()
            intent.action=Intent.ACTION_GET_CONTENT
            intent.type="image/*"
            startActivityForResult(intent,27)
        }

        binding.saveInfoProfileBtn.setOnClickListener {
            if (checker == "clicked") {

            } else {
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    private fun updateUserInfoOnly() {
        val username = userName.text.toString().trim()
        val fullname = fullName.text.toString().trim()
        val userBio = bio.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (fullname.isEmpty()) {
            Toast.makeText(this, "Full name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (userBio.isEmpty()) {
            Toast.makeText(this, "Bio cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val usersRef = FirebaseDatabase.getInstance().reference.child("users")
        val userMap = HashMap<String, Any>()
        userMap["fullname"] = fullname.toLowerCase()
        userMap["username"] = username.toLowerCase()
        userMap["bio"] = userBio.toLowerCase()

        usersRef.child(firebaseUser.uid).updateChildren(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update account", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    return
                }
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    Picasso.get().load(it.getImage()).placeholder(R.drawable.profile).into(profilePic)
                    userName.setText(it.getUsername())
                    fullName.setText(it.getFullname())
                    bio.setText(it.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data?.data != null) {
            val sFile: Uri = data.data!!
            binding.profileImageViewProfileFrag.setImageURI(sFile)

            reference = storage.reference.child("profile_pic")
                .child(FirebaseAuth.getInstance().uid.toString())

            reference.putFile(sFile)
                .addOnSuccessListener {
                    reference.downloadUrl
                        .addOnSuccessListener { uri ->
                            database.reference
                                .child("users")
                                .child(FirebaseAuth.getInstance().uid.toString())
                                .child("image")
                                .setValue(uri.toString())
                        }
                }
        }
    }
}
