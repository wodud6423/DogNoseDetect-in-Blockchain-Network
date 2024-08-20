package com.example.go.myapplication03


import LoadingDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.random.Random



class RegisterActivity : AppCompatActivity() {
    private val loadingDialog: LoadingDialog = LoadingDialog(this@RegisterActivity)

    lateinit var mRetrofit: Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI

    // s3 이미지 버킷 서버의 접근에 필요한 객체들
    // val DATABASE_VERSION = 1
    var code_exist: String? = "00"

    // val DATABASE_NAME = "LocalDB.db"
    lateinit var registerresultcode: String

    // private lateinit var localDB: LocalDB
    override fun onCreate(savedInstanceState: Bundle?) {
        setRetrofit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        var register_code: EditText = findViewById(R.id.register_code)
        var input_code: TextView = findViewById(R.id.input_code)
        var check_email: EditText = findViewById(R.id.check_email)
        var join_pwck = findViewById<EditText>(R.id.join_pwck)
        var join_name: EditText = findViewById(R.id.join_name)
        var join_email: EditText = findViewById(R.id.join_email)

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")

        var idcheckanswer: String? = "NoCan"

        register_code.visibility = View.INVISIBLE
        register_code.isClickable = false;
        register_code.isFocusable = false;
        // 이메일 확인 입력란 비활성화
        check_email.visibility = View.INVISIBLE
        // 관리자 코드 입력란 비활성화
        input_code.visibility = View.INVISIBLE
        //관리자 코드 및 등록 번호 안내란 숨기기
        input_code.isClickable = false;
        input_code.isFocusable = false;
        // 이메일 확인 버튼 비활성화
        check_button.visibility = View.INVISIBLE
        check_button.isClickable = false;
        check_button.isFocusable = false;
        //관리자 코드 및 등록 번호 안내란 비활성화
        spinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.register_type,
            android.R.layout.simple_spinner_item
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //가입유형 콤보박스 생성(스피너)
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    //일반사용자
                    0 -> {
                        input_code.visibility = View.INVISIBLE
                        register_code.setFocusableInTouchMode(false);
                        register_code.setFocusable(false);
                        register_code.setText("0000000000")
                    }
                    //펫샵
                    1 -> {
                        input_code.visibility = View.VISIBLE
                        input_code.hint = "사업자 등록 번호를 입력하십시오(10자리)"

                        register_code.setFocusableInTouchMode(true);
                        register_code.setFocusable(true);
                        register_code.visibility = View.VISIBLE

                    }
                    //관리자
                    2 -> {
                        input_code.visibility = View.VISIBLE
                        input_code.hint = "관리자 등록 번호를 입력하십시오(10자리)"

                        register_code.isFocusableInTouchMode = true;
                        register_code.isFocusable = true;
                        register_code.visibility = View.VISIBLE
                    }
                    else -> {
                    }
                }
            }
        }
        when (spinner.selectedItemPosition) {
            1 -> {
                code_exist = "01"
            } // 선택된 가입 유형이 펫샵인 경우
            2 -> {
                code_exist = "11"
            } // 선택된 가입 유형이 관리자인 경우
            else -> {
                code_exist = "00"
            }
        } // 선택된 가입 유형이 일반 사용자인 경우
        registerresultcode = "0100" + code_exist + "0100"
        sendemailcheckmesage.setOnClickListener {
            var EmailTocheck = join_email.text.toString()
            var checkemailstring: String = sendEmail(EmailTocheck)
            // 이메일 인증 확인 문자열을 보내는 함수를 실행하면서 이메일 인증 문자열을 입력받음
            check_email.visibility = View.VISIBLE
            check_email.isClickable = true;
            check_email.isFocusable = true;
            // 이메일 인증 확인버튼 활성화
            check_button.visibility = View.VISIBLE
            check_button.isClickable = true;
            check_button.isFocusable = true;
            // Debug 목적으로 실행되는 명령줄
            //check_email.setText(checkemailstring)
            // ---------------------------
            check_button.setOnClickListener {
                if (check_email.text.toString() == checkemailstring) {
                    // 이메일 인증 확인란의 텍스트를 확인되었다는 텍스트로 바꾸고 나서 비활성화시킨다.
                    check_email.setText("인증이 확인되었습니다!")
                    // 이메일 인증 확인란 비활성화
                    check_email.isClickable = false
                    check_email.isFocusable = false
                    // 이메일 확인 버튼 비활성화
                    check_button.visibility = View.INVISIBLE
                    check_button.isClickable = false
                    check_button.isFocusable = false
                } else {
                    Toast.makeText(this, "이메일 인증이 실패하였습니다. 다시 확인해주십시오.", Toast.LENGTH_LONG)
                        .show()
                    check_email.visibility = View.INVISIBLE
                    check_email.isClickable = false
                    check_email.isFocusable = false
                    // 이메일 확인 버튼 비활성화
                    check_button.visibility = View.INVISIBLE
                    check_button.isClickable = false
                    check_button.isFocusable = false
                }
            }
        }
        // 이메일 인증 확인란을 눈에 보이게 설정한다. 그리고 포커스와 클릭이 가능하도록 하여 입력이 가능하도록 한다.

        // 관리자코드는 가입유형 + 입력코드(10자리)

        //레이아웃과 연결하여 화면 출력
        //localDB= LocalDB(this, DATABASE_NAME,null, DATABASE_VERSION) // SQLite 모듈 생성
        join_button.setOnClickListener { view ->
            if (register_code.text.isEmpty() || join_name.text.isEmpty() || join_email.text.isEmpty() || join_password.text.isEmpty() || join_pwck.text.isEmpty()) {// 값이 전부 입력되지 않은경우
                Toast.makeText(this, "값을 전부 입력해주세요..", Toast.LENGTH_LONG).show()
            } else {
                // 먼저 이메일 인증이 완료되어서 입력란이 비활성화되있는지 확인합니다.
                if (!check_email.isClickable && !check_email.isFocusable) {
                    if (join_pwck.text.toString() == join_password.text.toString()) {//패스워드/패스워드 확인이 일치
                        val sendPostdataregister = DataModel02.Registerid(
                            register_code.text.toString(),
                            join_name.text.toString(),
                            join_email.text.toString(),
                            join_pwck.text.toString(),
                            registerresultcode
                        )
                        val sendPostdatacheckid = DataModel02.Searchid(
                            join_email.text.toString()
                        )
                        // 위에서 입력을 정리한 모델 sendPostdata01을 API의 POST함수의 큐에 담아 전송.
                        mRetrofitAPI.checkid(sendPostdatacheckid).enqueue(object :
                            Callback<DataModel02.PostResult02> {
                            override fun onResponse(
                                call: Call<DataModel02.PostResult02>,
                                response: Response<DataModel02.PostResult02>
                            ) {
                                val result = response.body()
                                Log.d("log", response.toString())
                                Log.d("log", response.body().toString())
                                idcheckanswer = response.body()?.message
                                if (idcheckanswer == "Can") {
                                    // 아이디 중복 확인이 완료되어 아이디 가입이 가능해지면 해당 아이디를 트랜잭션으로
                                    // 가입 코드, 이름,이메일 아이디, 비밀번호를 등록시킵니다.
                                    loadingDialog.show()

                                    mRetrofitAPI.registerid(sendPostdataregister).enqueue(object :
                                        Callback<DataModel02.RegisterEncript> {
                                        override fun onResponse(
                                            call: Call<DataModel02.RegisterEncript>,
                                            response: Response<DataModel02.RegisterEncript>
                                        ) {
                                            loadingDialog.hide()
                                            Log.d("log", response.toString())
                                            Log.d("log", response.body().toString())
                                            val encript_String = response.body()?.encrypt
                                            val encryptedFileManager = EncryptedFileManager(this@RegisterActivity)
                                            val fileName = "myPrivateKey${join_email.text.toString()}_open.txt"
                                            println(encript_String)
                                            encript_String?.let {
                                                encryptedFileManager.saveEncryptedStringToFile(
                                                    it,fileName)
                                            }
                                            // 입력받은 AES로 암호화된 암호화키를 텍스트파일에 저장
                                            val intent0001 = Intent(this@RegisterActivity,MainHomePage::class.java)

                                            intent0001.putExtra("nodebaseurl",nodebaseurl)
                                            setResult(Activity.RESULT_OK,intent0001)
                                            // 메인 서비스 페이지 시작.
                                            startActivity(intent0001)
                                        }

                                        override fun onFailure(
                                            call: Call<DataModel02.RegisterEncript>,
                                            t: Throwable
                                        ) {
                                            loadingDialog.hide()
                                            // 실패
                                            Log.d("log", t.message.toString())
                                            Log.d("log", "블록 체인 서버 정보 입력에 실패하였습니다.")
                                        }
                                    })
                                    //Toast.makeText(this, "해당 아이디로 등록되었습니다.", Toast.LENGTH_LONG)
                                        //.show()

                                } else {// 존재하는 아이디
                                    //Toast.makeText(this, "아이디가 이미 존재합니다.", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(
                                call: Call<DataModel02.PostResult02>,
                                t: Throwable
                            ) {
                                // 실패
                                Log.d("log", t.message.toString())
                                Log.d("log", "블록 체인 서버 정보 입력에 실패하였습니다.")
                            }
                        })

                    } else { // 패스워드/패스워드 확인이 일치하지 않음
                        Toast.makeText(this, "패스워드가 틀렸습니다.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "이메일 인증이 필요합니다.", Toast.LENGTH_LONG).show()
                }
            }
        }

        Cancel_join.setOnClickListener{
            Toast.makeText(this, "이전 페이지로 이동합니다", Toast.LENGTH_LONG).show()
            val intent0001 = Intent(this@RegisterActivity,MainHomePage::class.java)

            intent0001.putExtra("nodebaseurl",nodebaseurl)
            setResult(Activity.RESULT_OK,intent0001)
            // 메인 서비스 페이지 시작.
            startActivity(intent0001)

        }
    }

    private fun sendEmail(toEmail: String): String {

        val fromEmail = "yeomjeayoung@gmail.com"
        val password = "knzvqjkscqnnvsqv"


        fun generateRandomString(length: Int): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') // 허용되는 문자 범위
            val random = Random(System.currentTimeMillis()) // 현재 시간을 시드로 사용하여 랜덤 생성기 초기화
            return (1..length)
                .map { allowedChars.random(random) } // 랜덤 문자 선택
                .joinToString("") // 문자열로 조합
        }

// 매번 요청할 때마다 랜덤 값을 생성하여 사용하는 예제
        val code = generateRandomString(10)



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
                "verfication code"                                              // 이메일 제목
            message.setText("저쪽 테이블에서 보낸 코드입니다. 아래 비밀번호를 인증창에 입력해주세요\n" + "<" + code + ">")                                               // 이메일 내용

            // 전송
            Transport.send(message)


        }
        return code
    }
    //메일전송메소드
    // 파일 저장 함수
    private fun setRetrofit() {

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        mRetrofit = Retrofit.Builder() // Retrofit2 인터페이스 빌더 생성
            .baseUrl(nodebaseurl) // 인터페이스와 연결될 서버 주소입력
            .addConverterFactory(GsonConverterFactory.create())
            .build() // 인터페이스 생성

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
        // 위의 설정을 기반으로 Retrofit 인터페이스 생성
    }
}


