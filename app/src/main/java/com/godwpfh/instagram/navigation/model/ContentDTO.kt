package com.godwpfh.instagram.navigation.model

data class ContentDTO(var explain: String? = null, var imageUrl:String? = null, var uid:String? = null,
                      var userId: String? = null, var timestamp: Long? = null, var favoriteCount: Int =0, var favorites:MutableMap<String,Boolean> = HashMap()){
                                    //favorite는 중복 좋아요 누른 것을 확인하기 위해서
    data class Comment(var uid:String? = null,
                                    var userId: String?= null,
                                    var comment: String?= null,
                                    var timestamp: Long?= null)
}