package com.findr.findr.entity;



public class Comment {

    private Long id = null;

    private String comment;

    private String author;

    private Long postID;


    public Comment(){}
    public Comment(String comment, String author, Long postID) {
        this.comment = comment;
        this.author = author;
        this.postID = postID;
    }

    private Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public Long getPostID() {
        return postID;
    }
    public void setPostID(Long postID) {
        this.postID = postID;
    }

}
