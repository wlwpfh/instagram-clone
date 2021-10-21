package com.godwpfh.instagram.navigation.model

import java.net.URL

data class InfoDTO(
    var uid: String?= null,
    var userID : String?= null,
    var nickname: String?=null,
    var image: String?= null,
    var website: URL?=null,
    var introduce: String?=null
)
