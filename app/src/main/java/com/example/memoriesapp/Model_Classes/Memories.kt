package com.example.memoriesapp.Model_Classes
// Class representing a Memory
class Memories {
    private var memoryid: String = ""
    private var description: String = ""
    private var memoryimage: String = ""
    private var publisher: String = ""
    // Constructor for creating Memory
    constructor(memoryid: String, description: String, memoryimage: String, publisher: String) {
        this.memoryid = memoryid
        this.description = description
        this.memoryimage = memoryimage
        this.publisher = publisher
    }
    constructor()
    // Function for getting memoryid
    fun getMemoryId(): String { return memoryid }
    // Function for setting memoryid
    fun setMemoryId(memoryid: String){ this.memoryid = memoryid }
    // Function for getting description
    fun getDescription(): String { return description }
    // Function for setting description
    fun setDescription(description: String){ this.description = description }
    // Function for getting memoryimage
    fun getMemoryImage(): String { return memoryimage }
    // Function for setting memoryimage
    fun setMemoryImage(memoryimage: String){ this.memoryimage = memoryimage }
    // Function for getting publisher
    fun getPublisher(): String { return publisher }
    // Function for setting publisher
    fun setPublisher(publisher: String){ this.publisher = publisher }
}