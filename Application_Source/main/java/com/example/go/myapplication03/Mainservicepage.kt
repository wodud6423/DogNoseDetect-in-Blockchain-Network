package com.example.go.myapplication03

import LoadingDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class Mainservicepage : AppCompatActivity() {
    private val loadingDialog: LoadingDialog = LoadingDialog(this@Mainservicepage)
    lateinit var mRetrofit: Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        setRetrofit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainservicepage)

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        val emailid : String? = intent.getStringExtra("email_id")
        val idname : String? = intent.getStringExtra("idname")
        val idpw : String? = intent.getStringExtra("idpw")

        println(emailid)
        println(idpw)
        val button1 = findViewById<Button>(R.id.button1)
        button1.setOnClickListener {
            val intent = Intent(this@Mainservicepage, MyPetRegisterActivity::class.java)

            intent.putExtra("email_id", emailid)
            intent.putExtra("idpw", idpw)
            intent.putExtra("idname",idname)

            intent.putExtra("nodebaseurl",nodebaseurl)
            setResult(Activity.RESULT_OK,intent)
            startActivity(intent)
        }
        // 강아지 등록 화면으로 접근
        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            val sendMydog_all_search = DataModel02.Mydog_all_search(
                emailid,
                idpw
            )
            loadingDialog.show()
            mRetrofitAPI.mydogallsearch(sendMydog_all_search).enqueue(object :
                Callback<DataModel02.Mydog_all_search_result> {
                override fun onResponse(
                    call: Call<DataModel02.Mydog_all_search_result>,
                    response: Response<DataModel02.Mydog_all_search_result>
                ) {
                    loadingDialog.hide()
                    println(response.body()?.message)
                    println(response.body()?.mydoglist)
                    println(response.body()?.imgbyteList)
                    Log.d("log", response.toString())
                    Log.d("log", response.body().toString())
                    when (response.body()?.message) {
                        "NOREGDOG" -> {
                            Toast.makeText(this@Mainservicepage, "등록되있는 강아지 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Mainservicepage, MyPetRegisterActivity::class.java)
                            intent.putExtra("email_id", emailid)
                            intent.putExtra("idpw", idpw)
                            intent.putExtra("idname",idname)

                            intent.putExtra("nodebaseurl",nodebaseurl)
                            setResult(Activity.RESULT_OK,intent)
                            // 메인 서비스 페이지 시작.
                            startActivity(intent)
                        }
                        "NOID" -> {
                            Toast.makeText(this@Mainservicepage, "Warning! Recheck Your Input ID&PW", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Mainservicepage, MainHomePage::class.java)
                            intent.putExtra("email_id", emailid)
                            intent.putExtra("idpw", idpw)
                            intent.putExtra("idname",idname)
                            intent.putExtra("nodebaseurl",nodebaseurl)
                            setResult(Activity.RESULT_OK,intent)
                            // 메인 서비스 페이지 시작.
                            startActivity(intent)
                        }
                        "ListOK" -> {
                            val intent001 = Intent(this@Mainservicepage, PetInfoChange::class.java)
                            intent001.putExtra("email_id", emailid)
                            intent001.putExtra("idpw", idpw)
                            intent001.putExtra("idname",idname)
                            intent001.putExtra("nodebaseurl",nodebaseurl)
                            setResult(Activity.RESULT_OK,intent001)
                            // 메인 서비스 페이지 시작.


                            startActivity(intent001)
                        }
                    }
                }

                override fun onFailure(
                    call: Call<DataModel02.Mydog_all_search_result>,
                    t: Throwable
                ) {
                    loadingDialog.hide()
                    Toast.makeText(this@Mainservicepage, "요청중 문제가 발생하였습니다! 다시한번 요청해주십시오.", Toast.LENGTH_SHORT).show()
                }
            })
        }
        // 내가 보유한 강아지 리스트에 따라
        // 1) 내가 가진 강아지 리스트가 0인 경우 => 1번버튼과 마찬가지로 나의 강아지 등록 화면으로 이동됨
        // 2) 나의 아이디가 등록된 아이디로 조회되지 않는 경우 => 다시 로그인하도록 메인 홈페이지 화면으로 이동
        // 3) 나의 강아지 리스트가 1이상인 경우 => 탭 아이템 형태로 나의 강아지 리스트가 나오는 페이지 화면으로 이동
        val button3 = findViewById<Button>(R.id.button3)
        button3.setOnClickListener {
            loadingDialog.show()
            mRetrofitAPI.adoptingdogallsearch().enqueue(object :
                Callback<DataModel02.Mydog_all_search_result> {
                override fun onResponse(
                    call: Call<DataModel02.Mydog_all_search_result>,
                    response: Response<DataModel02.Mydog_all_search_result>
                ) {
                    loadingDialog.hide()
                    println(response.body()?.message)
                    println(response.body()?.mydoglist)
                    println(response.body()?.imgbyteList)
                    Log.d("log", response.toString())
                    Log.d("log", response.body().toString())
                    when (response.body()?.message) {
                        "NOADOPTINGDOG" -> {
                            Toast.makeText(this@Mainservicepage, "등록되있는 분양 강아지 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Mainservicepage, Mainservicepage::class.java)
                            intent.putExtra("email_id", emailid)
                            intent.putExtra("idpw", idpw)
                            intent.putExtra("idname",idname)

                            intent.putExtra("nodebaseurl",nodebaseurl)
                            setResult(Activity.RESULT_OK,intent)
                            startActivity(intent)
                        }
                        "ListOK" -> {
                            Toast.makeText(this@Mainservicepage, "분양 페이지로 이동합니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Mainservicepage, AdoptInfoPage::class.java)
                            intent.putExtra("email_id", emailid)
                            intent.putExtra("idpw", idpw)
                            intent.putExtra("idname",idname)

                            intent.putExtra("nodebaseurl",nodebaseurl)
                            setResult(Activity.RESULT_OK,intent)

                            startActivity(intent)
                        }
                    }
                }

                override fun onFailure(
                    call: Call<DataModel02.Mydog_all_search_result>,
                    t: Throwable
                ) {

                    loadingDialog.hide()
                    Toast.makeText(this@Mainservicepage, "요청중 문제가 발생하였습니다! 다시한번 요청해주십시오.", Toast.LENGTH_SHORT).show()
                }
            })
        }
        // 내가 보유한 강아지 리스트에 따라
        // 1) 내가 가진 강아지 리스트가 0인 경우 => 1번버튼과 마찬가지로 나의 강아지 등록 화면으로 이동됨
        // 2) 나의 아이디가 등록된 아이디로 조회되지 않는 경우 => 다시 로그인하도록 메인 홈페이지 화면으로 이동
        // 3) 나의 강아지 리스트가 1이상인 경우 => 탭 아이템 형태로 나의 강아지 리스트가 나오는 페이지 화면으로 이동
        // 분양 정보 페이지로 이동
        val button4 = findViewById<Button>(R.id.button4)
        button4.setOnClickListener {
            val intent = Intent(this@Mainservicepage, MyInfoChange::class.java)
            intent.putExtra("email_id", emailid)
            intent.putExtra("idpw", idpw)

            intent.putExtra("idname",idname)
            intent.putExtra("nodebaseurl",nodebaseurl)
            setResult(Activity.RESULT_OK,intent)

            startActivity(intent)
        }
        // 내가 보유한 강아지 리스트에 따라
        // 1) 내가 가진 강아지 리스트가 0인 경우 => 1번버튼과 마찬가지로 나의 강아지 등록 화면으로 이동됨
        // 2) 나의 아이디가 등록된 아이디로 조회되지 않는 경우 => 다시 로그인하도록 메인 홈페이지 화면으로 이동
        // 3) 나의 강아지 리스트가 1이상인 경우 => 탭 아이템 형태로 나의 강아지 리스트가 나오는 페이지 화면으로 이동
        // 분양 정보 페이지로 이동
        val button5 = findViewById<Button>(R.id.button5)
        button5.setOnClickListener {
            val intent = Intent(this@Mainservicepage, TransactionPage::class.java)
            intent.putExtra("email_id", emailid)
            intent.putExtra("idpw", idpw)
            intent.putExtra("idname",idname)

            intent.putExtra("nodebaseurl",nodebaseurl)
            setResult(Activity.RESULT_OK,intent)

            startActivity(intent)
        }
        // 분양 신청중 화면으로 이동
        val button6 = findViewById<Button>(R.id.button6)
        button6.setOnClickListener {
            val intent = Intent(this@Mainservicepage, MissingDogPage::class.java)
            intent.putExtra("email_id", emailid)
            intent.putExtra("idpw", idpw)
            intent.putExtra("idname",idname)
            intent.putExtra("nodebaseurl",nodebaseurl)

            setResult(Activity.RESULT_OK,intent)
            startActivity(intent)
        }
    }
    // 분실 신청중 화면으로 이동
    //http요청을 보냈고 이건 응답을 받을 콜벡메서드
    private fun setRetrofit() {
        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        mRetrofit = Retrofit.Builder()
            .baseUrl(nodebaseurl)
            .client(
                OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS) // Connection Timeout 값을 30초로 설정
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
    }

}

