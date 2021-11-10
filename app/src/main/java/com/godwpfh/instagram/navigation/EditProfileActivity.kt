package com.godwpfh.instagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.godwpfh.instagram.MainActivity
import com.godwpfh.instagram.R
import com.godwpfh.instagram.navigation.model.InfoDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    var firestore: FirebaseFirestore? = null
    var auth: FirebaseAuth? = null

    var PICK_IMAGE_ALBUM = 1;
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)


        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        var uid = auth?.currentUser?.uid


        btn_cancel_edit_profile.setOnClickListener { //취소를 누른 경우
//            var userFragment=UserFragment()
//            var settingFragment= SettingFragment()
//            var bundle =Bundle()
//            bundle.putString("destinationUid",uid)
//            userFragment.arguments=bundle
            //startActivity(Intent(this,MainActivity::class.java))
            //supportFragmentManager.beginTransaction().replace(R.id.main_toolbar,settingFragment).commit()
            //supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
            onBackPressed()
            //이걸 해도 MainActivity Home으로 간다... (ㄱ-)
        }
        btn_profile_image.setOnClickListener {  //사진 바꾸기 : 갤러리 들어가서 사진 하나 바꾸기!
            var photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            photoPickerIntent.type = "image_profile/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_ALBUM)
        }
        btn_update_edit_profile.setOnClickListener { //업데이트를 한 경우
            //사지을 firebase storage에 저장하고
            //그 다음에 info 정보 바꾸고
                contentUpload()

            firestore?.collection("info")?.document(uid!!)?.update(mapOf(
                    "image" to photoUri,
                    "name" to edit_profile_name?.text.toString(),
                    "introduce" to edit_profile_intro?.text.toString(),
                    "nickname" to edit_profile_nickname?.text.toString(),
                    "website" to edit_profile_website?.text.toString()
            )
            )
//            var userFragment = UserFragment()
//            var settingFragment = SettingFragment()
//            var bundle = Bundle()
//            bundle.putString("destinationUid", uid)
//            userFragment.arguments = bundle
//            supportFragmentManager.beginTransaction().replace(R.id.main_toolbar, settingFragment).commit()
//            supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE_ALBUM){
            if(resultCode==Activity.RESULT_OK) { //사진을 선택한 경우
                photoUri=data?.data //경로 넣기
                image_profile.setImageURI(photoUri)

            }else{
                finish()
            }
        }
    }

    fun contentUpload() { //upload방식 1.promise 2.callback
        //파일 이름 만들기
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("images_profile")?.child(imageFileName)

        //1.promise
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var infoDTO = InfoDTO()
            infoDTO.image = uri.toString()

            firestore?.collection("images_profile")?.document()?.set(infoDTO)
            setResult(Activity.RESULT_OK)
            finish()
        }

    }
}
