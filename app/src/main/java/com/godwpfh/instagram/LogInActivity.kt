package com.godwpfh.instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_log_in.*


class LogInActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener {
            signinAndSignup();
        }
    }

    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener { task ->
            if (task.isSuccessful) { //id가 생성되었을 때
                moveMainPage(task.result?.user)
            } else if (task.exception?.message.isNullOrEmpty()) {
                //로그인 에러 메세지 출력
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            } else {
                //일반 로그인
                signinEmail()
            }
        }
    }

    fun signinEmail(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //로그인 성공
                moveMainPage(task.result?.user)
            } else {
                //로그인 실패
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun moveMainPage(user: FirebaseUser?){
        if(user!=null){ //firebase에 user가 존재한다면
            startActivity(Intent(this, MainActivity::class.java));
        }
    }

}