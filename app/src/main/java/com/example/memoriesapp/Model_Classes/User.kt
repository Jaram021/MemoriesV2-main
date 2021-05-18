package com.example.memoriesapp.Model_Classes
// Class to represent User's in Memories
class User {
    private var username: String = ""
    private var fullname: String = ""
    private var bio: String = ""
    private var image: String = ""
    private var uid: String = ""
    // Constructor for User class
    constructor(username: String, fullname: String, bio: String, image: String, uid: String) {
        this.username = username
        this.fullname = fullname
        this.bio = bio
        this.image = image
        this.uid = uid
    }

    constructor()
    // Function for getting User's username
    fun getUsername(): String { return username }
    // Function for setting User's username
    fun setUsername(username: String) { this.username = username}
    // Function for getting User's fullname
    fun getFullName(): String { return fullname }
    // Function for setting User's fullname
    fun setFullName(fullname: String) { this.fullname = fullname}
    // Function for getting User's bio
    fun getBio(): String { return bio }
    // Function for setting User's bio
    fun setBio(bio: String) { this.bio = bio}
    // Function for getting User's image
    fun getImage(): String { return image }
    // Function for setting User's image
    fun setImage(image: String) { this.image = image}
    // Function for getting User's uid
    fun getUid(): String { return uid }
    // Function for setting User's uid
    fun setUid(uid: String) { this.uid = uid }
}