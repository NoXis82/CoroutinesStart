package ru.netology.dto

data class PostWithAuthors(
    val post: Post,
    val authorPost: Author
)