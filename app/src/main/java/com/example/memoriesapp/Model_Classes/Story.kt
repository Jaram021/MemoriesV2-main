package com.example.memoriesapp.Model_Classes
// Class representing a Story for Memories
class Story {
    private var imageurl: String = ""
    private var timestart: Long = 0
    private var timeend: Long = 0
    private var storyid: String = ""
    private var userid: String = ""
    // Constructor for Story
    constructor(imageurl: String, timestart: Long, timeend: Long, storyid: String, userid: String) {
        this.imageurl = imageurl
        this.timestart = timestart
        this.timeend = timeend
        this.storyid = storyid
        this.userid = userid
    }

    constructor()
    // Function for getting imageurl for Story
    fun getImageUrl(): String { return imageurl }
    // Function for setting imageurl for Story
    fun setImageUrl(imageurl: String){ this.imageurl = imageurl }
    // Function for getting start time for story
    fun getTimeStart(): Long { return timestart }
    // Function for setting start time for story
    fun setTimeStart(timestart: Long){ this.timestart = timestart }
    // Function for getting end time for Story
    fun getTimeEnd(): Long { return timeend }
    // Function for setting end time for Story
    fun setTimeEnd(timeend: Long){ this.timeend = timeend }
    // Function for getting storyId
    fun getStoryId(): String { return storyid }
    // Function for setting storyid
    fun setStoryId(storyid: String){ this.storyid = storyid }
    // Function for getting userid
    fun getUserId(): String { return userid }
    // Function for setting userid
    fun setUserId(userid: String){ this.userid = userid }
}