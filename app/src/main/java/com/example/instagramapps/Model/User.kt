package com.example.instagramapps.Model

class User {
    private var fullname: String = ""
    private var username: String = ""
    private var email: String = ""
    private var bio: String = ""
    private var image: String = ""
    private var uid: String = ""

    constructor()

    constructor(
        fullname: String,
        username: String,
        email: String,
        bio: String,
        image: String,
        uid: String
    ) {
        this.fullname = fullname
        this.username = username
        this.email = email
        this.bio = bio
        this.image = image
        this.uid = uid
    }

    fun getFullname(): String {
        return fullname
    }
    fun setFullname(fullname: String) {
        this.fullname = fullname
    }

    fun getUsername(): String {
        return username
    }
    fun setUsername(username: String) {
        this.username = username
    }

    fun getEmail(): String {
        return email
    }
    fun setEmail(email: String) {
        this.email = email
    }

    fun getBio(): String {
        return bio
    }
    fun setBio(bio: String) {
        this.bio = bio
    }

    fun getImage(): String {
        return image
    }
    fun setImage(image: String) {
        this.image = image
    }

    fun getUid(): String {
        return uid
    }
    fun setUid(uid: String) {
        this.uid = uid
    }
}