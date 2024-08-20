package com.example.go.myapplication03
import LoadingDialog
import MakeRSA
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_loginpage.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReconfirmPage : AppCompatActivity() {

    private val loadingDialog: LoadingDialog = LoadingDialog(this@ReconfirmPage)
    lateinit var mRetrofit : Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        setRetrofit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reconfirm_page)

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        var id : String? = null
        var idcheckanswer : String? = null
        val emailid : String? = intent.getStringExtra("email_id")
        val idname : String? = intent.getStringExtra("idname")
        // 이메일 AES 암호화된 나의 RSA싸인 서명(즉, 나의 RSA개인키로 해당 트랜잭션의 내용을 암호화하는 것이다.)
        val transactioncode : String? = intent.getStringExtra("transactioncode")
        val transaction : ArrayList<Int>? = intent.getIntegerArrayListExtra("transaction")


        check_button.setOnClickListener{
        // 서명 확인 버튼을 누를시
        val passwd = join_password.text.toString()
        // 입력한 비밀번호를 토대로
        val encryptedFileManager = EncryptedFileManager(this@ReconfirmPage)
        val EncryptedMyRSAkey = encryptedFileManager.readEncryptedStringFromFile("myPrivateKey${emailid}_open.txt")

        // AES로 암호화된 나의 RSA키를 읽어와
        val OPENAES = AESCipher(emailid + passwd)
        var decryptedMYRSAKEY = OPENAES.decrypt(EncryptedMyRSAkey!!)
            .trim('(', ')') // 양쪽의 대괄호 제거
            .split(", ") // 쉼표와 공백을 기준으로 분리하여 리스트 생성
            .map { it.toInt() } // 각 요소를 정수로 변환하여 리스트 생성
        println(decryptedMYRSAKEY)
        // AES 복호화하여 나의 RSA개인키를 얻는다.
        println(transaction)
        var trasaction_decrpt: String = MakeRSA().decrypt(decryptedMYRSAKEY,
            transaction!!
        )
        println(trasaction_decrpt)
        // 나의 RSA개인키를 통해 입력받은 TRANSACTION을 복호화하고 이를 다시 서버로 전송한다.
        val senddata = DataModel02.signtransaction(
            emailid,
            // 나의 이메일 아이디
            trasaction_decrpt,
            transactioncode
           // 복호화한 트랜잭션
        )
        loadingDialog.show()
        mRetrofitAPI.confirmregisterdog(senddata).enqueue(object :
            Callback<DataModel02.ReconfirmResult> {
            override fun onResponse(
                call: Call<DataModel02.ReconfirmResult>,
                response: Response<DataModel02.ReconfirmResult>
            ) {
                loadingDialog.hide()
                Log.d("log", response.toString())
                Log.d("log", response.body().toString())
                idcheckanswer = response.body()?.message
                if (idcheckanswer == "Transaction Register Okay!") {
                    Toast.makeText(
                        this@ReconfirmPage,
                        "정보 등록이 완료되었습니다! 서버에서 해당 데이터를 업데이트할 것 입니다! ",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent0001 = Intent(this@ReconfirmPage,Mainservicepage::class.java)
                    intent0001.putExtra("idpw",passwd)
                    intent0001.putExtra("email_id",emailid)

                    intent0001.putExtra("idname",idname)
                    intent0001.putExtra("nodebaseurl",nodebaseurl)
                    // 나의 이메일 아이디와 비밀번호 값을 넘겨준다.
                    setResult(Activity.RESULT_OK,intent0001)
                    // 나의 강아지 리스트 페이지로 이동
                    startActivity(intent0001)
                }
            }
            override fun onFailure(
                call: Call<DataModel02.ReconfirmResult>,
                t: Throwable
            ) {
                loadingDialog.hide()
                Toast.makeText(
                    this@ReconfirmPage,
                    "블록 체인내의 문제발생으로 정보입력에 실패하였습니다. 다시 시도해주십시오. ",
                    Toast.LENGTH_SHORT
                ).show()
                // 실패
                Log.d("log", t.message.toString())
                Log.d("log", "블록 체인 서버 정보 입력에 실패하였습니다.")
            }
        })
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

    fun stringToTuple(str: String): Pair<Int, Int> {
        val values = str.trim('(', ')').split(',').map { it.trim() }
        val first = values[0].toInt()
        val second = values[1].toInt()
        return Pair(first, second)
    }
    private fun setRetrofit(){

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        mRetrofit = Retrofit.Builder() // Retrofit2 인터페이스 빌더 생성
            .baseUrl(nodebaseurl) // 이 부분을 node Ping으로 바꿀 필요가 있음
            //.baseUrl(smallestValueNode!!)
            .addConverterFactory(GsonConverterFactory.create())
            .build() // 인터페이스 생성

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
        // 위의 설정을 기반으로 Retrofit 인터페이스 생성
    }
}