package com.example.instagramapps.Model

class Comment {
    private var comments:String = ""
    private var publisher:String = ""

    constructor()
    constructor(comments: String, publisher: String) {
        this.comments = comments
        this.publisher = publisher
    }
    fun getComments():String{
        return comments
    }
    fun getPublisher():String{
        return publisher
    }
    fun setComments(comments: String){
        this.comments = comments
    }
    fun setPublisher(publisher: String){
        this.publisher = publisher
    }
}