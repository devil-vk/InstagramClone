package com.example.instagramclone.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.PostAdapter
import com.example.instagramclone.Model.Post
import com.example.instagramclone.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        var recyclerView: RecyclerView = binding.recyclerViewHome
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.adapter = postAdapter

        postList = ArrayList()
        postAdapter = PostAdapter(requireContext(), postList!!)

        checkFollowing()
        retrievePosts()

        return view
    }

    private fun checkFollowing() {
        followingList = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followingList?.clear()

                for (dataSnapshot in snapshot.children) {
                    val followingId = dataSnapshot.key
                    followingList?.add(followingId.toString())
                }
                postAdapter?.notifyDataSetChanged() // Notify the adapter of data changes
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }


    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val post = dataSnapshot.getValue(Post::class.java)
                    val publisherId = post?.getPublisher().toString()
                        postList?.add(post!!)
                    }
                postAdapter?.notifyDataSetChanged() // Notify the adapter of data changes
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

}
