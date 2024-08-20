package com.example.go.myapplication03

import LoadingDialog
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyInfoChange : AppCompatActivity() {

    private val loadingDialog: LoadingDialog = LoadingDialog(this@MyInfoChange)
    private lateinit var mRetrofit: Retrofit
    private lateinit var mRetrofitAPI: RetrofitAPI
    private lateinit var joinName: TextView
    private lateinit var joinEmail: TextView
    private lateinit var lastidpw : EditText
    private lateinit var joinPassword: EditText
    private lateinit var joinPwck: EditText
    private lateinit var joinButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        setRetrofit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info_change)

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        val emailid: String? = intent.getStringExtra("email_id")
        val idname : String? = intent.getStringExtra("idname")

        joinName = findViewById(R.id.join_name)
        joinEmail = findViewById(R.id.join_email)
        lastidpw = findViewById(R.id.last_idpw)
        joinPassword = findViewById(R.id.join_password)
        joinPwck = findViewById(R.id.join_pwck)
        joinButton = findViewById(R.id.join_button)

        joinName.text = idname
        joinEmail.text = emailid

        joinButton.setOnClickListener {
            val lastpw = lastidpw.text.toString()
            val password = joinPassword.text.toString()
            val pwck = joinPwck.text.toString()

            if (lastpw.isEmpty()|| password.isEmpty() || pwck.isEmpty()) {
                Toast.makeText(this, "값을 전부 입력해주세요.", Toast.LENGTH_LONG).show()
            } else {
                if (password == pwck) {
                    val CheckPwData = DataModel02.Loginid(emailid, lastpw)
                    loadingDialog.show()
                    mRetrofitAPI.checkmyinfo(CheckPwData).enqueue(object : Callback<DataModel02.checkmyinfoResult> {
                        override fun onResponse(call: Call<DataModel02.checkmyinfoResult>, response: Response<DataModel02.checkmyinfoResult>) {
                            loadingDialog.hide()
                            if (response.body()?.message == "OK. Now input your new password.") {
                                val changeMyInfoData = DataModel02.Changemyinfo(emailid, lastpw, password, "0100000100")
                                loadingDialog.show()
                                mRetrofitAPI.changemyinfo(changeMyInfoData).enqueue(object : Callback<DataModel02.changepwResult> {
                                    override fun onResponse(call: Call<DataModel02.changepwResult>, response: Response<DataModel02.changepwResult>) {
                                        loadingDialog.hide()
                                        if (response.isSuccessful) {
                                            Toast.makeText(this@MyInfoChange, "정보수정 성공!", Toast.LENGTH_LONG).show()
                                            val encript_String = response.body()?.encrypt
                                            val encryptedFileManager = EncryptedFileManager(this@MyInfoChange)
                                            val fileName = "myPrivateKey${emailid}_open.txt"
                                            println(encript_String)
                                            encript_String?.let {
                                                encryptedFileManager.saveEncryptedStringToFile(
                                                    it,fileName)
                                            }
                                            val alertDialogBuilder = AlertDialog.Builder(this@MyInfoChange)

                                            // 다이얼로그 제목 설정
                                            alertDialogBuilder.setTitle("정보 수정 확인!")

                                            // 다이얼로그 메시지 설정
                                            alertDialogBuilder.setMessage("입력된 비밀번호로 정보가 수정되었습니다!!")

                                            // 확인 버튼 추가
                                            alertDialogBuilder.setPositiveButton("확인") { dialog, _ ->
                                                // 확인 버튼을 클릭했을 때 수행할 동작
                                                val intent0001 = Intent(this@MyInfoChange,Mainservicepage::class.java)
                                                intent0001.putExtra("idname",idname)
                                                intent0001.putExtra("idpw",password)
                                                intent0001.putExtra("email_id",emailid)
                                                intent0001.putExtra("nodebaseurl",nodebaseurl)
                                                // 아직 완전하게 등록되지않은 트랜잭션을 나의 싸인 서명까지하여 등록시키기 위해 다음 페이지로 넘겨준다.
                                                print(emailid)
                                                print(password)
                                                setResult(Activity.RESULT_OK,intent0001)
                                                // 메인 서비스 페이지로 이동
                                                startActivity(intent0001)
                                                dialog.dismiss() // 다이얼로그 닫기
                                            }

                                            // 다이얼로그 생성 및 표시
                                            val alertDialog = alertDialogBuilder.create()
                                            alertDialog.show()


                                        } else {
                                            Toast.makeText(this@MyInfoChange, "현재 비밀번호와 변경한 비밀번호가 서로 같습니다.", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<DataModel02.changepwResult>, t: Throwable) {
                                        loadingDialog.hide()
                                        Toast.makeText(this@MyInfoChange, "블록체인 네트워크와의 통신에 실패하였습니다.", Toast.LENGTH_LONG).show()
                                    }
                                })
                            } else {
                                Toast.makeText(this@MyInfoChange, "이전에 등록된 비밀번호와 같습니다.", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: Call<DataModel02.checkmyinfoResult>, t: Throwable) {
                            // Handle failure here
                            // Display an error message or perform necessary actions

                            loadingDialog.hide()
                            Toast.makeText(this@MyInfoChange, "블록체인 네트워크와의 통신에 실패하였습니다.", Toast.LENGTH_LONG).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "The password does not match.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        val idname: String? = intent.getStringExtra("idname")
        val nodebaseurl: String? = intent.getStringExtra("nodebaseurl")
        val emailid: String? = intent.getStringExtra("email_id")
        val idpw: String? = intent.getStringExtra("idpw")

        val returnIntent = Intent()
        returnIntent.putExtra("email_id", emailid)
        returnIntent.putExtra("idpw", idpw)
        returnIntent.putExtra("idname", idname)
        returnIntent.putExtra("nodebaseurl", nodebaseurl)

        setResult(Activity.RESULT_OK, returnIntent)
        super.onBackPressed()
    }


    private fun setRetrofit() {
        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        mRetrofit = Retrofit.Builder()
            .baseUrl(nodebaseurl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
    }
    fun showAlert(context: Context, title: String?, message: String?) {
        val alertDialogBuilder = AlertDialog.Builder(context)

        // 다이얼로그 제목 설정
        alertDialogBuilder.setTitle(title)

        // 다이얼로그 메시지 설정
        alertDialogBuilder.setMessage(message)

        // 확인 버튼 추가
        alertDialogBuilder.setPositiveButton("확인") { dialog, _ ->
            // 확인 버튼을 클릭했을 때 수행할 동작

            dialog.dismiss() // 다이얼로그 닫기
        }

        // 다이얼로그 생성 및 표시
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
