package com.example.memoriesapp.Model_Classes

class Memories {
    private var memoryid: String = ""
    private var description: String = ""
    private var memoryimage: String = ""
    private var publisher: String = ""

    constructor(memoryid: String, description: String, memoryimage: String, publisher: String) {
        this.memoryid = memoryid
        this.description = description
        this.memoryimage = memoryimage
        this.publisher = publisher
    }
    constructor()

    fun getMemoryId(): String { return memoryid }
    fun setMemoryId(memoryid: String){ this.memoryid = memoryid }

    fun getDescription(): String { return description }
    fun setDescription(description: String){ this.description = description }

    fun getMemoryImage(): String { return memoryimage }
    fun setMemoryImage(memoryimage: String){ this.memoryimage = memoryimage }

    fun getPublisher(): String { return publisher }
    fun setPublisher(publisher: String){ this.publisher = publisher }
}