package ru.netology.dto

import Attachment

data class Post(
    val id: Long,
    val author: Long,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    var attachment: Attachment? = null,
)
