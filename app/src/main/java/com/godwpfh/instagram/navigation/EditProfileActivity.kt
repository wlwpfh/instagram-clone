package com.godwpfh.instagram.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.godwpfh.instagram.R

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
    }
    //일단 닉네임, 이름, 소개, 웹사이트가 정보를 넣고 업데이트하는 그런 거
    //프로필 사진 바꾸기 버튼을 누르면 사진첩으로 이동해서 하나의 사진 고르기
    // 취소하면 다시 userfragment로 돌아가고 완료를 누르면 정보를 업데이트시키고 다시 userfragment로
}