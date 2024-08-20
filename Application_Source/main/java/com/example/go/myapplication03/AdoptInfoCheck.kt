package com.example.go.myapplication03


import LoadingDialog
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_adopt_info_check.*
import kotlinx.android.synthetic.main.mypetregisterdialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AdoptInfoCheck : AppCompatActivity() {

    private val loadingDialog: LoadingDialog = LoadingDialog(this@AdoptInfoCheck)
    lateinit var mRetrofit : Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        setRetrofit()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adopt_info_check)

        val idname : String? = intent.getStringExtra("idname")
        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        var id : String? = null
        val emailid: String? = intent.getStringExtra("email_id")
        val idpw: String? = intent.getStringExtra("idpw")
        val ownerid: String? = intent.getStringExtra("ownerid")
        val owner: String? = intent.getStringExtra("owner")
        val name: String? = intent.getStringExtra("name")
        val sex: String? = intent.getStringExtra("sex")
        val species: String? = intent.getStringExtra("species")
        val state: String? = intent.getStringExtra("state")
        val imgpath: String? = intent.getStringExtra("imgpath")
        val imgnosepath: String? = intent.getStringExtra("imgnosepath")
        val price: String? = intent.getStringExtra("price")
        val imageBytes = intent.getStringExtra("imageBytes")
        val byteArray = Base64.decode(imageBytes, Base64.DEFAULT)
        val bitmap_mypet = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        println(emailid)
        println(idpw)

        // 분양 페이지에서 클릭한 강아지 사진을 보여줌
        iv_pre_checkinfo.setImageBitmap(bitmap_mypet)
        // 로그인 확인 버튼을 누를시
        PetNamecheckinfo.text = PetNamecheckinfo.text .toString() + name
        Petgendercheckinfo.text = Petgendercheckinfo.text.toString() + sex
        Pettypecheckinfo.text = Pettypecheckinfo.text.toString() + species
        PetOwnercheckinfo.text = PetOwnercheckinfo.text.toString() + ownerid
        // 분양 펫 정보를 이전 선택했던 해당 강아지 정보에서 intent 형식으로 가져온다
        PetPrice.text = PetPrice.text.toString() + price
        // 분양 신청 버튼을 눌렀을 경우
        Adopt_Request.setOnClickListener {
            var dog_info: MutableMap<String,String?> = mutableMapOf(
                "ownerid" to ownerid,
                "owner" to owner,
                "name" to name,
                "sex" to sex,
                "species" to species,
                "state" to "ReservedAdopting",
                "imgpath" to imgpath,
                "imgnosepath" to imgnosepath,
                "price" to price
            )
            val senddata = DataModel02.Adoptthispet_request(
                emailid,
                ownerid,
                dog_info
            // 분양 등록 트랜잭션의 첫번째 과정인 분양 신청 트랜잭션임을 의미하는 트랜잭션 코드
            )
            loadingDialog.show()
            mRetrofitAPI.Adoptthispet(senddata).enqueue(object :
                Callback<DataModel02.PostResult02> {
                override fun onResponse(
                    call: Call<DataModel02.PostResult02>,
                    response: Response<DataModel02.PostResult02>
                ) {
                    loadingDialog.hide()
                    Log.d("log", response.toString())
                    Log.d("log", response.body().toString())
                    val message_result = response.body()?.message
                    if(message_result == "Adopting Request OKAY!"){
                        // 로그인 성공
                        val builder02 = AlertDialog.Builder(this@AdoptInfoCheck)
                        builder02.setTitle("분양 예약 신청이 완료되었습니다!")
                            .setMessage("분양 예약 신청완료!")
                            .setPositiveButton("확인",
                                DialogInterface.OnClickListener { dialog_message01, Okay_button ->
                                    // 확인버튼으로 메인 서비스 페이지로 넘어갈때, 해당 사용자의 id 정보는 다음 페이지에도 그 값을 넘겨준다.
                                    val intent001 = Intent(this@AdoptInfoCheck,Mainservicepage::class.java)
                                    intent001.putExtra("email_id",emailid)
                                    intent001.putExtra("idpw",idpw)

                                    intent001.putExtra("idname",idname)
                                    intent001.putExtra("nodebaseurl",nodebaseurl)
                                    // 상태값, 전달할려는 intent 설정
                                    setResult(Activity.RESULT_OK,intent001)
                                    // 메인 서비스 페이지 시작.
                                    startActivity(intent001)
                                }
                            )
                            .setNegativeButton("취소",
                                DialogInterface.OnClickListener { dialog, id ->
                                    dialog_message01.text = "확인되었습니다."
                                })
                        // 다이얼로그를 띄워주기
                        builder02.show()
                        //val intent01 = Intent(this, Loginpage::class.java)
                        //startActivity(intent01)
                    }
                }

                override fun onFailure(
                    call: Call<DataModel02.PostResult02>,
                    t: Throwable
                ) {
                    loadingDialog.hide()
                    // 실패
                    Log.d("log", t.message.toString())
                    Log.d("log", "블록 체인 서버 정보 입력에 실패하였습니다.")
                }
            })


        }
        Cancel_and_gobeforepage.setOnClickListener{
            Toast.makeText(
                this@AdoptInfoCheck,
                "이전 페이지인 분양 페이지로 이동합니다.",
                Toast.LENGTH_SHORT
            ).show()
            val intent001 = Intent(this@AdoptInfoCheck,AdoptInfoPage::class.java)
            intent001.putExtra("email_id",emailid)
            intent001.putExtra("idpw",idpw)

            intent001.putExtra("idname",idname)
            intent001.putExtra("nodebaseurl",nodebaseurl)
            // 상태값, 전달할려는 intent 설정
            setResult(Activity.RESULT_OK,intent001)
            // 메인 서비스 페이지 시작.
            startActivity(intent001)

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

    private fun setRetrofit(){
        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        mRetrofit = Retrofit.Builder() // Retrofit2 인터페이스 빌더 생성
            .baseUrl(nodebaseurl) // 인터페이스와 연결될 서버 주소입력
            .addConverterFactory(GsonConverterFactory.create())
            .build() // 인터페이스 생성

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
        // 위의 설정을 기반으로 Retrofit 인터페이스 생성
    }
}