package ru.netology.dto

data class CommentsWithAuthors(
    val post: Post,
    val authorPost: Author,
    val comment: Comment,
    val authorComment: Author
)
