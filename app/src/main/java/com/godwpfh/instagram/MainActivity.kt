package com.godwpfh.instagram

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.godwpfh.instagram.navigation.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),  NavigationBarView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnItemSelectedListener(this)

        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

        //기본 화면 설정
        bottom_navigation.selectedItemId=R.id.action_home
        registerPushToken()

    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()
        when(item.itemId){
            R.id.action_home -> {
                var detailViewFragment=DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }
            R.id.action_search -> {
                var gridFragment=GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }
            R.id.action_add_photo -> {
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)==PERMISSION_GRANTED) {//storage 경로를 가져올 권한이 있는지 확인
                    startActivity(Intent(this,AddPhotoActivity::class.java))
                }
                return true
            }
            R.id.action_favorite_alarm -> {
                var alarmFragment=AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmFragment).commit()
                return true
            }
            R.id.action_account -> {
                var userFragment=UserFragment()
                var settingFragment= SettingFragment()
                var bundle =Bundle()
                var uid=FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid",uid)
                userFragment.arguments=bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_toolbar,settingFragment).commit()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()

                return true
            }
        }
        return false
    }

    fun setToolbarDefault(){
        //toolbar_username.visibility=View.GONE
        toolbar_back.visibility=View.GONE
        toolbar_logo.visibility=View.VISIBLE
    }
    fun registerPushToken(){
        //기기에 따라 토큰 보내기
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            val uid=FirebaseAuth.getInstance().currentUser?.uid
            val map= mutableMapOf<String,Any>()
            map["pushToken"]=token!!

            FirebaseFirestore.getInstance().collection("pushTokens").document("uid").set(map)

        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode== Activity.RESULT_OK){
            var imageUri=data?.data
            var uid=FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference
                    .child("profileUserImage").child(uid!!) //프로필 사진 저장
            storageRef.putFile(imageUri!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri -> //return된 uri값이 넘어옴
                var map=HashMap<String,Any>()
                map["images"]=uri.toString()
                FirebaseFirestore.getInstance().collection("profileUserImage").document(uid).set(map)

            }
        }
    }


}


