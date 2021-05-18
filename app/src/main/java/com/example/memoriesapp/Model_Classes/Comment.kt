package com.example.memoriesapp.Model_Classes

class Comment {
    private var comment: String = ""
    private var publisher: String = ""

    constructor(comment: String, publisher: String) {
        this.comment = comment
        this.publisher = publisher
    }

    constructor()

    fun getComment(): String { return comment }
    fun setComment(comment: String) { this.comment = comment }
    fun getPublisher(): String { return publisher }
    fun setPublisher(publisher: String) { this.publisher = publisher }

}