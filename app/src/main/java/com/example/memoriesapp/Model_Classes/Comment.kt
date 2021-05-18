package com.example.memoriesapp.Model_Classes
// Class for representing a Comment
class Comment {
    private var comment: String = ""
    private var publisher: String = ""
    // Constructor for Comment class
    constructor(comment: String, publisher: String) {
        this.comment = comment
        this.publisher = publisher
    }

    constructor()
    // Function for getting comment
    fun getComment(): String { return comment }
    // Function for setting comment
    fun setComment(comment: String) { this.comment = comment }
    // Function for getting publisher
    fun getPublisher(): String { return publisher }
    // Function for setting publisher
    fun setPublisher(publisher: String) { this.publisher = publisher }

}