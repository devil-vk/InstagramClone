package com.example.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramclone.databinding.ActivityAddPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseDatabase
    private var imageUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

        binding.closeAddPostBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.saveAddPostBtn.setOnClickListener {
            uploadImage()
        }

        binding.imagePost.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun uploadImage() {
        val description = binding.descriptionPost.text.toString()
        val postid = database.reference.child("posts").push().key

        if (description.isEmpty()) {
            binding.descriptionPost.error = "Please enter a description"
            return
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = System.currentTimeMillis()

        val storageReference = storage.reference.child("posts/$postid.jpg")

        storageReference.putFile(imageUri!!)
            .addOnSuccessListener {
                progressDialog = ProgressDialog(this)
                progressDialog.setTitle("")
                progressDialog.setMessage("Uploading Picture")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val postMap = HashMap<String, Any>()
                    postMap["description"] = description
                    postMap["image"] = uri.toString()
                    postMap["postid"] = postid!!
                    postMap["time"] = timestamp
                    postMap["userId"] = FirebaseAuth.getInstance().currentUser?.uid ?:""

                    database.reference.child("posts")
                        .child(postid)
                        .setValue(postMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this,"Post Uploaded successfully",Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                }
            }
            .addOnFailureListener {
                // Handle upload failure
                Toast.makeText(this,"Post Uploading Failed",Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data?.data != null) {
            imageUri = data.data!!
            binding.imagePost.setImageURI(imageUri)
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
