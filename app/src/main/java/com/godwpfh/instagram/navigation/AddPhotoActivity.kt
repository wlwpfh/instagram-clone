 package com.godwpfh.instagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.godwpfh.instagram.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

 class AddPhotoActivity : AppCompatActivity() {
     var PICK_IMAGE_ALBUM=0;
     var storage: FirebaseStorage ?= null
     var photoUri : Uri?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //storage초기화하기
        storage= FirebaseStorage.getInstance()

        //사진첩 열기
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type="image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_ALBUM)

        //버튼에 기능 넣기
        add_photo_button.setOnClickListener {
            contentUpload();
        }

    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
            if(requestCode==PICK_IMAGE_ALBUM){
                if(resultCode==Activity.RESULT_OK) { //사진을 선택한 경우
                    photoUri=data?.data //경로 넣기
                    add_photo_image.setImageURI(photoUri)
                }else{
                    finish()
                }
                }
     }
      fun contentUpload() {
            //파일 이름 만들기
          var timestamp=SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
          var imageFileName="IMAGE_"+timestamp+"_.png"
          var storageRef=storage?.reference?.child("images")?.child(imageFileName)

          storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            //느낌표 두개로 null safety제거
              Toast.makeText(this,getString(R.string.upload_success),Toast.LENGTH_LONG).show()
          }
      }
 }