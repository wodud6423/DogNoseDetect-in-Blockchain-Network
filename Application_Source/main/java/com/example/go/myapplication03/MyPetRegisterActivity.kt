package com.example.go.myapplication03
import LoadingDialog
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.TimeZone.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.internal.ContextUtils.getActivity
import kotlinx.android.synthetic.main.activity_mypetregister.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.mypetregisterdialog.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.security.AccessControlContext
import java.security.AccessController.getContext
import java.security.DigestException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.jar.*

class MyPetRegisterActivity : AppCompatActivity() {

    private val loadingDialog: LoadingDialog = LoadingDialog(this@MyPetRegisterActivity)
    //Manifest 에서 설정한 권한을 가지고 온다.
    val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val STORAGE_PERMISSION = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var dog_info: MutableMap<String, String?>? = null

    //권한 플래그값 정의
    val FLAG_PERM_CAMERA = 98
    val FLAG_PERM_STORAGE = 99

    //카메라와 갤러리를 호출하는 플래그
    val FLAG_REQ_CAMERA = 101
    val FLAG_REA_STORAGE = 102
    val TAG = "TAG_MyPetRegisterActivity"
    var imagePath: String? = null

    // 강아지 성별 레디오 입력 선택시 입력되는 문자열
    var malecheck: String? = null

    // 종 관련 선택시 입력되는 문자열
    var selectedItem_type: String? = null
    var currentPhotoPath: String? = null

    // 촬영한 사진을 경로에 저장하고
    lateinit var indogimage: File

    // flask - Retrofit통신을 위한 API객체 설정
    lateinit var mRetrofit: Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        setRetrofit()
        // 레트로핏 설정
        // 사용자의 이메일 아이디와 성함을 이전 페이지에서 넘겨받음.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypetregister)
        // 화면 출력

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        val emailid : String? = intent.getStringExtra("email_id")
        val idpw : String? = intent.getStringExtra("idpw")
        val idname : String? = intent.getStringExtra("idname")


        spinner01.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.pet_type,
            android.R.layout.simple_spinner_item
        )
        spinner01.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //품종 유형 콤보박스 생성(스피너)
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedItem_type = parent?.getItemAtPosition(position).toString()
            }
        }
        // 왼쪽의 텍스트를 입력하면 오른쪽의 스피너의 목록에서 입력된 텍스트와 근접한 목록이 선택되도록 구현.
        // pet_type의 배열이 스피너의 목록이므로 해당 배열을 가져옴.
        val array_type: Array<String> = arrayOf(R.array.pet_type.toString())
        var textlength: Int = 0 // 텍스트와 비슷한 입력의 길이 초기값 0 설정

        // 스피너의 첫번째 인덱스 목록부터 순차대로 반복문을 통해 0부터 해당 스피너 길이만큼 진행됨.
        for (i in array_type.indices) {
            var check_spinner: String = spinner01.getItemAtPosition(i).toString()
            if (check_spinner.indexOf(Pettype.text.toString()) >= textlength) {
                spinner01.setSelection(i) //입력된 텍스트와 가장 가까운 목록을 발견시 해당 항목 선택
                textlength =
                    check_spinner.indexOf(Pettype.text.toString()) // 현재 가장 텍스트와 비슷한 항목의 동일 길이로 리셋
            }
        }
        // 화면이 만들어 지면서 저장소 권한을 체크 합니다.
        // 권한이 승인되어 있으면 카메라를 호출하는 메소드를 실행합니다.
        if (checkPermission(STORAGE_PERMISSION, FLAG_PERM_STORAGE)) {
            setViews()
        }

        malegroup01.setOnCheckedChangeListener { group, checkid ->
            when (checkid) {
                R.id.male -> malecheck = "Male"

                R.id.female -> malecheck = "Female"

                R.id.nomale -> malecheck = "Nomale"

            }
            // 정보 입력 확인 버튼
            insert_info.setOnClickListener {
                val builder02 = AlertDialog.Builder(this)
                // 다이얼 로그 메세지의 타이틀로 해당 메세지가 나옴.
                dog_info = mutableMapOf(
                    "ownerid" to emailid,
                    "owner" to idname,
                    "name" to Petid.text.toString(),
                    "sex" to malecheck,
                    "species" to selectedItem_type,
                    "state" to "Normal"
                )
                builder02.setTitle("정보 저장 확인")
                    .setMessage(
                        "\n강아지 이름: " + Petid.text.toString()
                                + "\n강아지 성별: " + malecheck
                                + "\n강아지 품종: " + selectedItem_type
                                + "해당 펫 정보를 저장하시겠습니까?"
                    )
                    .setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog, id ->
                            // 업로드한 이미지를 s3버킷 서버로 전송후 생성된 이미지 url을 가져옴.
                            // val stringimageUrl : String? = uploadWithTransferUtility(Petid.text.toString() + newJpgFileName(), indogimage)
                            // 입력한 펫 이름,성별,품종,입력받은 이미지 url,이미지 해싱 데이터를 BlockChain서버에 전송.
                            val sendPostdata01 = DataModel02.Registerdog(
                                dog_info,
                                //name.toString(), // 넘겨받은 사용자 성함
                                emailid,
                                "1000001000"
                            // 트랜잭션 코드의 마지막 4자리는 0100(등록완료)가 아닌 1000(서명확인전)이 된다.
                            // 이는 강아지 등록의 경우 자신의 서명까지 받아야 등록완료로 취급하기 때문에 다음과 같이 코드를 전송한다.
                            )

                            //-----debug용 확인 코드---------------indogimage
                            //val inputStream: InputStream = resources.openRawResource(R.drawable.dog_picture02)
                            //val bitmap01: Bitmap = BitmapFactory.decodeStream(inputStream)
                            //val bitmap01toFile = bitmapToFile(bitmap01, File(filesDir, "image").toString())
                            //---------------------------------------
                            val requestBodyProfile = RequestBody.create(
                                MediaType.parse("image/jpeg"),indogimage
                            )
                            // 실제 전송할 수 있는 데이터 형태를 만든다.
                            val multipartBodyProfile = MultipartBody.Part.createFormData(
                                "file", indogimage!!.name, requestBodyProfile
                            )
                            // 위에서 입력을 정리한 모델 sendPostdata01을 API의 POST함수의 큐에 담아 전송.
                            loadingDialog.show()
                            mRetrofitAPI.savetransaction(multipartBodyProfile, sendPostdata01)
                                .enqueue(object : Callback<DataModel02.Postdogregister> {
                                    override fun onResponse(
                                        call: Call<DataModel02.Postdogregister>,
                                        response: Response<DataModel02.Postdogregister>
                                    ) {
                                        loadingDialog.hide()
                                        Log.d("log", response.toString())
                                        Log.d("log", response.body().toString())
                                        val resalt_message = response.body()?.message
                                        val resalt_transaction = response.body()?.transaction
                                        if (resalt_message == "SAVE OKAY") {
                                            Toast.makeText(
                                                this@MyPetRegisterActivity,
                                                "강아지 등록 정보가 서버에 전달되었습니다! 등록 서명페이지로 이동합니다! ",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent0001 = Intent(this@MyPetRegisterActivity,ReconfirmPage::class.java)
                                            intent0001.putExtra("pagenumber","MyPetRegisterActivity")
                                            intent0001.putIntegerArrayListExtra("transaction",
                                                resalt_transaction?.let { it1 -> ArrayList(it1) })

                                            intent0001.putExtra("transactioncode","1000001000")
                                            intent0001.putExtra("idname",idname)
                                            intent0001.putExtra("idpw",idpw)
                                            intent0001.putExtra("email_id",emailid)
                                            intent0001.putExtra("nodebaseurl",nodebaseurl)
                                            // 아직 완전하게 등록되지않은 트랜잭션을 나의 싸인 서명까지하여 등록시키기 위해 다음 페이지로 넘겨준다.
                                            setResult(Activity.RESULT_OK,intent0001)
                                            // 메인 서비스 페이지로 이동
                                            startActivity(intent0001)

                                        } else if (resalt_message == "Duplication Info") {
                                            Toast.makeText(
                                                this@MyPetRegisterActivity,
                                                "이미 등록된 강아지 정보가 확인되었습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    }

                                    override fun onFailure(
                                        call: Call<DataModel02.Postdogregister>,
                                        t: Throwable
                                    ) {
                                        loadingDialog.hide()
                                        Toast.makeText(
                                            this@MyPetRegisterActivity,
                                            "블록 체인내의 문제로 정보입력에 실패하였습니다!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // 실패
                                        Log.d("log", t.message.toString())
                                        Log.d("log", "블록 체인 서버 정보 입력에 실패하였습니다.")
                                    }

                                })
                        })
                    .setNegativeButton("취소",
                        DialogInterface.OnClickListener { dialog, id ->
                            dialog_message01.text = "취소 클릭"
                        })
                // 다이얼로그를 띄워주기
                builder02.show()
                // 링크가 없는 경우 기본 이미지를 보여주기 위함
                // 링크가 있는 경우 링크에서 이미지를 가져와서 보여준다.
            }
            // 나의 정보 조회하기를 누를시
            Cancel_Registerdog.setOnClickListener {
                Toast.makeText(
                    this@MyPetRegisterActivity,
                    "취소 버튼이 확인되었습니다. 메인 서비스 페이지로 돌아갑니다.",
                    Toast.LENGTH_SHORT
                ).show()
                val intent0001 = Intent(this@MyPetRegisterActivity,Mainservicepage::class.java)
                intent0001.putExtra("idname",idname)
                intent0001.putExtra("idpw",idpw)
                intent0001.putExtra("email_id",emailid)

                intent0001.putExtra("nodebaseurl",nodebaseurl)
                // 아직 완전하게 등록되지않은 트랜잭션을 나의 싸인 서명까지하여 등록시키기 위해 다음 페이지로 넘겨준다.
                setResult(Activity.RESULT_OK,intent0001)
                // 메인 서비스 페이지로 이동
                startActivity(intent0001)
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

        private fun setViews() {
            //카메라 버튼 클릭
            camerabutton.setOnClickListener {
                //카메라 호출 메소드
                openCamera()
            }
        }

        private fun openCamera() {
            //카메라 권한이 있는지 확인
            if (checkPermission(CAMERA_PERMISSION, FLAG_PERM_CAMERA)) {
                //권한이 있으면 카메라를 실행시킵니다.
                val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, FLAG_REQ_CAMERA)
            }
        }

        //권한이 있는지 체크하는 메소드
        private fun checkPermission(permissions: Array<out String>, flag: Int): Boolean {
            //안드로이드 버전이 마쉬멜로우 이상일때
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (permission in permissions) {
                    //만약 권한이 승인되어 있지 않다면 권한승인 요청을 사용에 화면에 호출합니다.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(this, permissions, flag)
                        return false
                    }
                }
            }
            return true
        }

        //checkPermission() 에서 ActivityCompat.requestPermissions 을 호출한 다음 사용자가 권한 허용여부를 선택하면 해당 메소드로 값이 전달 됩니다.
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            when (requestCode) {
                FLAG_PERM_STORAGE -> {
                    for (grant in grantResults) {
                        if (grant != PackageManager.PERMISSION_GRANTED) {
                            //권한이 승인되지 않았다면 return 을 사용하여 메소드를 종료시켜 줍니다
                            Toast.makeText(
                                this,
                                "저장소 권한을 승인해야지만 앱을 사용할 수 있습니다..",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                            return
                        }
                    }
                    //카메라 호출 메소드
                    setViews()
                }
                FLAG_PERM_CAMERA -> {
                    for (grant in grantResults) {
                        if (grant != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(
                                this,
                                "카메라 권한을 승인해야지만 카메라를 사용할 수 있습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                    openCamera()
                }
            }
        }


        //startActivityForResult 을 사용한 다음 돌아오는 결과값을 해당 메소드로 호출합니다.
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == RESULT_OK) {
                when (requestCode) {
                    FLAG_REQ_CAMERA -> {
                        if (data?.extras?.get("data") != null) {
                            //카메라로 방금 촬영한 이미지를 미리 만들어 놓은 이미지뷰로 전달 합니다.
                            val bitmap = data.extras?.get("data") as Bitmap
                            indogimage = bitmapToFile(bitmap, File(filesDir, "image").toString())
                            // 촬영한 비트맵 이미지를 일반 파일 형식의 변수인 indogimage에 할당
                            iv_pre.setImageBitmap(bitmap)
                            // 촬영한 이미지를 이미지 뷰에 보여줌
                        }
                    }
                }
            }
        }

        // 시간에 따른 랜덤 JPG파일 이름 생성 함수
        private fun newJpgFileName(): String {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
            val filename = sdf.format(System.currentTimeMillis())
            return "${filename}.jpg"
        }

        // 비트맵 형식을 일반 파일 형식으로 변환하는 함수
        private fun bitmapToFile(bitmap: Bitmap, path: String): File {
            var file = File(path)
            var out: OutputStream? = null
            try {
                file.createNewFile()
                out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            } finally {
                out?.close()
            }
            return file
        }

        private fun bytesToHex(byteArray: ByteArray): String {
            val digits = "0123456789ABCDEF"
            val hexChars = CharArray(byteArray.size * 2)
            for (i in byteArray.indices) {
                val v = byteArray[i].toInt() and 0xff
                hexChars[i * 2] = digits[v shr 4]
                hexChars[i * 2 + 1] = digits[v and 0xf]
            }
            return String(hexChars)
        }

        // sha256 해시함수. 해당 함수를 이용해 이미지url을 해싱한다.
        private fun hashSHA256(msg: String?): String {
            val hash: ByteArray
            try {
                val md = MessageDigest.getInstance("SHA-256")
                if (msg != null) {
                    md.update(msg.toByteArray())
                }
                hash = md.digest()
            } catch (e: CloneNotSupportedException) {
                throw DigestException("couldn't make digest of partial content");
            }

            return bytesToHex(hash)
        }
        @SuppressLint("RestrictedApi")
        private fun requireActivity(): FragmentActivity {
        return (getActivity(applicationContext)
            ?: throw IllegalStateException("Fragment $this not attached to an activity.")) as FragmentActivity
    }

        private fun createImageFile(): File {
            val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File? =
                requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile("JPEG_${timestamp}_", ".jpeg", storageDir).apply {
                currentPhotoPath = absolutePath
            }
        }

        private fun requireContext(): AccessControlContext {
            return getContext()
                ?: throw java.lang.IllegalStateException("Fragment $this not attached to a context.")
        }



        //http요청을 보냈고 이건 응답을 받을 콜벡메서드
        private fun setRetrofit() {

            val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
            mRetrofit = Retrofit.Builder() // Retrofit2 인터페이스 빌더 생성
                .baseUrl(nodebaseurl) // 인터페이스와 연결될 서버 주소입력
                .client(
                    OkHttpClient.Builder()
                        .readTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(60, TimeUnit.SECONDS) // Connection Timeout 값을 30초로 설정
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .build() // 인터페이스 생성 얘가 위에있는 친구.

            mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
            // 위의 설정을 기반으로 Retrofit 인터페이스 생성
        }
}
