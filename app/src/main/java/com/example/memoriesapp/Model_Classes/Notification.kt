package com.example.memoriesapp.Model_Classes
// Class representing a notification in Memories
class Notification {
    private var userid: String = ""
    private var text: String = ""
    private var memoryid: String = ""
    private var ismemory: Boolean = false
    // Constructor for Notification class
    constructor(userid: String, text: String, memoryid: String, ismemory: Boolean) {
        this.userid = userid
        this.text = text
        this.memoryid = memoryid
        this.ismemory = ismemory
    }

    constructor()
    // Function for getting userid
    fun getUserId(): String { return userid }
    // Function for setting userid
    fun setUserId(userid: String) { this.userid = userid }
    // Function for getting notification text
    fun getText(): String { return text }
    // Function for setting notification text
    fun setText(text: String) { this.text = text }
    // Function for getting memoryid
    fun getMemoryId(): String { return memoryid }
    // Function for setting memoryid
    fun setMemoryId(memoryid: String) { this.memoryid = memoryid }
    // Function for checking if ismemory
    fun getIsMemory(): Boolean { return ismemory }
    // Function for setting if ismemory
    fun setIsMemory(ismemory: Boolean) { this.ismemory = ismemory }


}