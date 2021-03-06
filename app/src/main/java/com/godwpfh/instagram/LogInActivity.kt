package com.godwpfh.instagram


import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.godwpfh.instagram.navigation.model.InfoDTO
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_log_in.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class LogInActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager : CallbackManager? = null
    var user: FirebaseUser?=null
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
        facebook_sign_in.setOnClickListener {
            facebookLogIn(); //1
        }
        var gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        //printHashKey()
        callbackManager= CallbackManager.Factory.create()
    }
    override fun onStart(){ //?????? ?????????
        super.onStart()
        moveMainPage(auth?.currentUser)

    }
    fun printHashKey() {
        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)

            for (signature in info.signatures) {
                val md= MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = Base64Util.encode(md.digest());
                //val hashKey= Base64.encode(md.digest(),0);
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
    fun facebookLogIn(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("public_profile","email"))

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    //2
                   handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }

            })
    }
    fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)

        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    //login 3
                    makeProfile(auth?.currentUser)
                    moveMainPage(task.result?.user)
                }else{
                    //error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode,resultCode,data)
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
                    makeProfile(auth?.currentUser)
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
            if (task.isSuccessful) { //id??? ??????????????? ???
                makeProfile(auth?.currentUser)
                moveMainPage(task.result?.user)
            } else if (task.exception?.message.isNullOrEmpty()) {
                //????????? ?????? ????????? ??????
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            } else {
                //?????? ?????????
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
                //????????? ??????
                moveMainPage(task.result?.user)
            } else {
                //????????? ??????
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    fun moveMainPage(user: FirebaseUser?){
        if(user!=null){ //firebase??? uvar infoDTO= InfoDTO()
            startActivity(Intent(this, MainActivity::class.java));

        }
    }

    fun makeProfile(user:FirebaseUser?){
        Toast.makeText(this, user?.email, Toast.LENGTH_LONG).show();
        var infoDTO = InfoDTO()
        infoDTO.uid=user?.uid
        infoDTO.userID=user?.email
        infoDTO.nickname=user?.email
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            infoDTO.image=getDrawable(R.drawable.ic_account).toString()
        }
        FirebaseFirestore.getInstance().collection("info").document(user?.uid!!).set(infoDTO)
    }

}