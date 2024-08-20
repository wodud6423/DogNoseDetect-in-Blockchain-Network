package com.example.go.myapplication03
import LoadingDialog
import android.app.AlertDialog
import android.content.Context
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64.encodeToString
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import java.util.*
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.util.Base64

class MissingDogPage : AppCompatActivity() {

    private val loadingDialog: LoadingDialog = LoadingDialog(this@MissingDogPage)
    private lateinit var mRetrofit: Retrofit
    private lateinit var mRetrofitAPI: RetrofitAPI
    private lateinit var imageViewDogNose: ImageView
    private val REQUEST_CAMERA_PERMISSION = 1
    private val REQUEST_IMAGE_CAPTURE = 2

    override fun onCreate(savedInstanceState: Bundle?) {

        setRetrofit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missing_dog_page)


        imageViewDogNose = findViewById(R.id.imageViewDogNose)

        val buttonShoot: Button = findViewById(R.id.buttonShoot)
        buttonShoot.setOnClickListener {
            if (hasCameraPermission()) {
                captureImage()
            } else {
                requestCameraPermission()
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

    private fun hasCameraPermission(): Boolean {
        val permission = Manifest.permission.CAMERA
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        val permission = Manifest.permission.CAMERA
        ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CAMERA_PERMISSION)
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val pictureByteArray = convertBitmapToByteArray(imageBitmap)
            sendPictureToRetrofit(pictureByteArray)
        }
    }

    @SuppressLint("ResourceType")
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun sendPictureToRetrofit(pictureByteArray: ByteArray) {
        val string_byte = Base64.encodeToString(pictureByteArray, Base64.DEFAULT)
        val emailId: String? = intent.getStringExtra("email_id")
        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        val sendPostData = DataModel02.MissingDogImage(string_byte)
        loadingDialog.show()
        mRetrofitAPI.missing(sendPostData).enqueue(object : Callback<DataModel02.missingResult> {
            override fun onResponse(
                call: Call<DataModel02.missingResult>,
                response: Response<DataModel02.missingResult>
            ) {
                loadingDialog.hide()
                if (response.isSuccessful) {
                    showAlert(this@MissingDogPage," 실종 발견 안내 "," -- 해당 강아지 주인 연락처 -- \n"+
                    response.body()!!.emailid)
                    sendEmail(emailId!!, response.body()!!.emailid!!)
                    val postResult = response.body()?.message
                    if (postResult == "SAVE OKAY") {
                        Toast.makeText(
                            this@MissingDogPage,
                            "해당 강아지 주인 정보입니다! 이메일로 꼭 연락을 해주십시오.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else {
                        Toast.makeText(
                            this@MissingDogPage,
                            "조회되는 강아지 정보가 없습니다!!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }

            override fun onFailure(call: Call<DataModel02.missingResult>, t: Throwable) {
                loadingDialog.hide()
                Toast.makeText(
                    this@MissingDogPage,
                    "전송에 실패하였습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
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
    private fun sendEmail(findEmail: String,toEmail : String){

        val fromEmail = "yeomjeayoung@gmail.com"
        val password = "knzvqjkscqnnvsqv"

        CoroutineScope(Dispatchers.IO).launch {
            val props = Properties()
            props.setProperty("mail.transport.protocol", "smtp")
            props.setProperty("mail.host", "smtp.gmail.com")
            props.put("mail.smtp.auth", "true")
            props.put("mail.smtp.port", "465")
            props.put("mail.smtp.socketFactory.port", "465")
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            props.put("mail.smtp.socketFactory.fallback", "false")
            props.put("mail.smtp.ssl.enable", "true")
            props.setProperty("mail.smtp.quitwait", "false")
            // 구글에서 지원하는 smtp 정보를 받아와 MimeMessage 객체에 전달
            val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(fromEmail, password);
                }
            })
            // 메시지 객체 만들기
            val message = MimeMessage(session)
            message.sender = InternetAddress(fromEmail)                                 // 보내는 사람 설정
            message.addRecipient(Message.RecipientType.TO, InternetAddress(toEmail))    // 받는 사람 설정
            message.subject =
                "  분실 발견 안내 이메일  "                                              // 이메일 제목
            message.setText("현재 사용자님의 강아지가 실종 발견되었습니다!! 확인해주십시오.\n" +
                    "발견자의 이메일 연락처 \n" + "<" + findEmail + ">")                                               // 이메일 내용

            // 전송
            Transport.send(message)


        }
    }
}
