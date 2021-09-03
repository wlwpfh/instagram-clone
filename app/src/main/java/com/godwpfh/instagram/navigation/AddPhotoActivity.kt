 package com.godwpfh.instagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.godwpfh.instagram.R
import com.godwpfh.instagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

 class AddPhotoActivity : AppCompatActivity() {
     var PICK_IMAGE_ALBUM=0;
     var storage: FirebaseStorage ?= null
     var photoUri : Uri?= null

     var auth : FirebaseAuth? =null
     var firestore:FirebaseFirestore?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //storage초기화하기
        storage= FirebaseStorage.getInstance()
        auth= FirebaseAuth.getInstance()
        firestore=FirebaseFirestore.getInstance()
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
      fun contentUpload() { //upload방식 1.promise 2.callback
            //파일 이름 만들기
          var timestamp=SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
          var imageFileName="IMAGE_"+timestamp+"_.png"
          var storageRef=storage?.reference?.child("images")?.child(imageFileName)

          //1.promise
          storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
              return@continueWithTask storageRef.downloadUrl
          }?.addOnSuccessListener { uri ->
              var contentDTO= ContentDTO()
              contentDTO.imageUrl=uri.toString()
              contentDTO.uid=auth?.currentUser?.uid
              contentDTO.userId=auth?.currentUser?.email
              contentDTO.explain=add_photo_edit.text.toString()
              contentDTO.timestamp=System.currentTimeMillis()
              firestore?.collection("images")?.document()?.set(contentDTO)
              setResult(Activity.RESULT_OK)
              finish()
          }


          //2.callback
//          storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
//           storageRef.downloadUrl.addOnSuccessListener { uri ->
//               var contentDTO= ContentDTO()
//               contentDTO.imageUrl=uri.toString()
//               contentDTO.uid=auth?.currentUser?.uid
//               contentDTO.userId=auth?.currentUser?.email
//               contentDTO.explain=add_photo_edit.text.toString()
//               contentDTO.timestamp=System.currentTimeMillis()
//               firestore?.collection("images")?.document()?.set(contentDTO)
//               setResult(Activity.RESULT_OK)
//               finish()
//           }
//          }
      }
 }