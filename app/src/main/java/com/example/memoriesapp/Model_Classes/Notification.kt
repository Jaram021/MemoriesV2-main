package com.example.memoriesapp.Model_Classes

class Notification {
    private var userid: String = ""
    private var text: String = ""
    private var memoryid: String = ""
    private var ismemory: Boolean = false

    constructor(userid: String, text: String, memoryid: String, ismemory: Boolean) {
        this.userid = userid
        this.text = text
        this.memoryid = memoryid
        this.ismemory = ismemory
    }

    constructor()

    fun getUserId(): String { return userid }
    fun setUserId(userid: String) { this.userid = userid }

    fun getText(): String { return text }
    fun setText(text: String) { this.text = text }

    fun getMemoryId(): String { return memoryid }
    fun setMemoryId(memoryid: String) { this.memoryid = memoryid }

    fun getIsMemory(): Boolean { return ismemory }
    fun setIsMemory(ismemory: Boolean) { this.ismemory = ismemory }


}