package com.godwpfh.instagram.navigation.model

data class AlarmDTO(
        var destinationUid : String?=null,
        var userId: String?=null,
        var uid: String?=null,
        var kind: Int?=null,
        var message: String?=null,
        var timestamp : Long?=null

//        kind
//                0: like
//                1: comment
//                2: follow
)
