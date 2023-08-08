package com.example.instagramclone.Model

class Post {
    private var postid: String? = null
    private var image: String? = null
    private var description: String? = null
    private var publisher: String? = null
    private var time: Long? = null
    private var userId: String? = null // Add the userId property

    // Empty constructor is required for Firebase
    constructor()

    constructor(
        postid: String?,
        image: String?,
        description: String?,
        publisher: String?,
        time: Long?,
        userId: String? // Add the userId parameter
    ) {
        this.postid = postid
        this.image = image
        this.description = description
        this.publisher = publisher
        this.time = time
        this.userId = userId // Assign the userId value
    }

    // Getter and setter methods for userId
    fun getUserId(): String? {
        return userId
    }

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    fun getPostid(): String? {
        return postid
    }

    fun setPostid(postid: String?) {
        this.postid = postid
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String?) {
        this.image = image
    }

    fun getDescription(): String? {
        return description
    }

    fun setDescription(description: String?) {
        this.description = description
    }

    fun getPublisher(): String? {
        return publisher
    }

    fun setPublisher(publisher: String?) {
        this.publisher = publisher
    }

    fun getTime(): Long? {
        return time
    }

    fun setTime(time: Long?) {
        this.time = time
    }
}
