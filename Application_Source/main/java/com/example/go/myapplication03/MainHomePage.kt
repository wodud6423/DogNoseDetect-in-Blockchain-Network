package com.example.go.myapplication03

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_mainhomepage.*
import retrofit2.Call
import retrofit2.Retrofit
import java.io.File
import android.content.Intent
import com.example.nodetest.nodePing
import kotlinx.coroutines.*

class MainHomePage : AppCompatActivity() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    val nodePingInstance = nodePing()

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        var nodeurl = "http://13.48.76.117:5000"
        coroutineScope.launch {
            nodePingInstance.onCreate()
            delayUntilNodePingIsAvailable()
            nodeurl = nodePingInstance.largestValueNode.toString()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainhomepage)
        // 애니메이션 초기화 및 애니메이션 시작 변수 설정
        var animationDrawable: AnimationDrawable = mainhomepage.background as AnimationDrawable
        // 애니메이션이 들어갈 때 지속시간(2.5초)
        animationDrawable.setEnterFadeDuration(2500)
        // 애니메이션이 끝날때 지속시간(5초)
        animationDrawable.setExitFadeDuration(5000)
        // 애니메이션 시작
        animationDrawable.start()
        // 로그인 버튼 클릭시

        login_button.setOnClickListener {
            // 버튼 클릭시 로그인 페이지로 이동
            //-----------------------
            val intent01 = Intent(this, Loginpage::class.java)
            intent01.putExtra("nodebaseurl", nodeurl)
            // 해당 페이지를 시작
            startActivity(intent01)
        }
        // 회원가입 버튼 클릭시
        register_button.setOnClickListener {
            // 버튼 클릭시 회원가입 페이지로 이동
            val intent02 = Intent(this, RegisterActivity::class.java)
            // 해당 페이지를 시작
            intent02.putExtra("nodebaseurl", nodeurl)
            startActivity(intent02)
        }
    }

    private suspend fun delayUntilNodePingIsAvailable() {
        while (nodePingInstance.largestValueNode == null) {
            delay(10000) // 0.1초 대기
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancel the coroutine scope to avoid memory leaks
    }
}