package com.godwpfh.instagram.navigation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.godwpfh.instagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {
    var firestore: FirebaseFirestore?=null
    var auth : FirebaseAuth?=null

    var PICK_IMAGE_ALBUM=0;
    var storage: FirebaseStorage?= null
    var photoUri : Uri?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)


        storage= FirebaseStorage.getInstance()
        firestore= FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()
        var uid=auth?.currentUser?.uid

        //사진첩 열기
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type="image_profile/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_ALBUM)

        btn_cancel_edit_profile.setOnClickListener { //취소를 누른 경우
            var userFragment=UserFragment()
            var settingFragment= SettingFragment()
            var bundle =Bundle()
            bundle.putString("destinationUid",uid)
            userFragment.arguments=bundle
            supportFragmentManager.beginTransaction().replace(R.id.main_toolbar,settingFragment).commit()
            supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
        }
        btn_profile_image.setOnClickListener {  //사진 바꾸기 : 갤러리 들어가서 사진 하나 바꾸기!

        }
        btn_update_edit_profile.setOnClickListener { //업데이트를 한 경우

            firestore?.collection("info")?.document(uid!!)?.update(mapOf(
                    "image" to "A",
                    "name" to edit_profile_name,
                    "introduce" to edit_profile_intro,
                    "nickname" to edit_profile_nickname,
                    "website" to edit_profile_website
            )

            )
        }
    }
    //일단 닉네임, 이름, 소개, 웹사이트가 정보를 넣고 업데이트하는 그런 거
    //프로필 사진 바꾸기 버튼을 누르면 사진첩으로 이동해서 하나의 사진 고르기
    // 취소하면 다시 userfragment로 돌아가고 완료를 누르면 정보를 업데이트시키고 다시 userfragment로

}