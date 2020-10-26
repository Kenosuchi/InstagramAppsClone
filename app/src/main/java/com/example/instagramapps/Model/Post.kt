package com.example.instagramapps.Model

class Post {
    private var postId:String=""
    private var postImage:String=""
    private var publisher:String=""
    private var Description:String=""

    constructor()
    constructor(postId: String, postImage: String, publisher: String, Description: String) {
        this.postId = postId
        this.postImage = postImage
        this.publisher = publisher
        this.Description = Description
    }


    fun getPostId():String{
        return this.postId
    }
    fun getPostImage():String{
        return this.postImage
    }
    fun getPublisher():String{
        return this.publisher
    }
    fun getDescription():String{
        return this.Description
    }
    fun setPostId(postId:String){
        this.postId=postId
    }
    fun setPostImage(postImage:String){
        this.postImage=postImage
    }
    fun setPublisher(publisher:String){
        this.publisher=publisher
    }
    fun setDescription(description:String){
        this.Description=description
    }
}