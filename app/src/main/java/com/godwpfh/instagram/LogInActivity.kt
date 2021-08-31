package com.godwpfh.instagram


import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64.encode
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.zxing.aztec.encoder.Encoder.encode
import com.google.zxing.qrcode.encoder.Encoder.encode
import kotlinx.android.synthetic.main.activity_log_in.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.net.URLEncoder
import java.io.UnsupportedEncodingException
import java.net.URLEncoder.encode

class LogInActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener {
            signinAndSignup();
        }
        google_sign_in.setOnClickListener {
            googleLogIn(); //1
        }
        var gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        printHashKey()
    }
    fun printHashKey() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)

            for (signature in info.signatures) {
                val md= MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey= String(Base64.encode(md.digest(),0));
                Log.i(TAG, "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "printHashKey()", e)
        } catch (e: Exception) {
            Log.e(TAG, "printHashKey()", e)
        }
    }
    fun googleLogIn(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==GOOGLE_LOGIN_CODE){
            var result= Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess){
                var account=result.signInAccount
                //2
                firebaseAuthWithGoogle(account)
            }
        }
    }
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    //login
                    moveMainPage(task.result?.user)
                }else{
                    //error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }
    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )?.addOnCompleteListener { task ->
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
        auth?.createUserWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )?.addOnCompleteListener { task ->
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