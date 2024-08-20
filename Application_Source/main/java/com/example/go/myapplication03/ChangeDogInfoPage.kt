package com.example.go.myapplication03

import LoadingDialog
import android.util.Base64
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.internal.ContextUtils
import kotlinx.android.synthetic.main.activity_change_dog_info_page.*
import kotlinx.android.synthetic.main.activity_mypetregister.*
import kotlinx.android.synthetic.main.activity_mypetregister.Petid
import kotlinx.android.synthetic.main.activity_mypetregister.Pettype
import kotlinx.android.synthetic.main.activity_mypetregister.camerabutton
import kotlinx.android.synthetic.main.activity_mypetregister.insert_info
import kotlinx.android.synthetic.main.activity_mypetregister.iv_pre
import kotlinx.android.synthetic.main.activity_mypetregister.malegroup01
import kotlinx.android.synthetic.main.mypetregisterdialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class ChangeDogInfoPage : AppCompatActivity() {
    private val loadingDialog: LoadingDialog = LoadingDialog(this@ChangeDogInfoPage)

    var dog_info: DataModel02.DogItem02? = null
    val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val STORAGE_PERMISSION = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val FLAG_PERM_CAMERA = 98
    val FLAG_PERM_STORAGE = 99
    val FLAG_REQ_CAMERA = 101

    var selectedItem_state: String? = null

    var selectedItem_type: String? = null

    lateinit var indogimage: File
    lateinit var mRetrofit: Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        setRetrofit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_dog_info_page)

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        var malecheck: String? = null
        var dog_price: String = "None"
        val idname : String? = intent.getStringExtra("idname")
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
        val imageBytes = intent.getStringExtra("imageBytes")
        val byteArray = Base64.decode(imageBytes, Base64.DEFAULT)
        val bitmap_mypet = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        println(emailid)
        println(idpw)
        // 해당 강아지 이미지 정보를 보여줄 코드
        iv_pre.setImageBitmap(bitmap_mypet)
        // 강아지 가격정보는 비활성화시켜둔다.
        // state 입력 값에 따라 가격 정보 입력란을 다시 활성화시킨다.
        // 이전 정보를 기준으로 성별 관련 레디오를 디폴트 세팅으로 기존 값으로 체크해놓기
        Petid.setText(name)
        when (sex) {
            "Male" -> {
                malegroup01.check(R.id.male)
                malecheck = "Male"
            }
            "Female" -> {
                malegroup01.check(R.id.female)
                malecheck = "Female"
            }
            "Nomale" -> {
                malegroup01.check(R.id.nomale)
                malecheck = "Nomale"
            }
        }
        // 강아지 종 정보 스피너 인터페이스 정보
        val spinner01 = findViewById<Spinner>(R.id.spinner01)

        // 어댑터 생성 및 항목 리스트 설정
        val adapter01 = ArrayAdapter.createFromResource(
            this,
            R.array.pet_type,
            android.R.layout.simple_spinner_item
        )
        adapter01.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner01.adapter = adapter01

        // 선택된 항목을 저장할 변수

        spinner01.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 항목도 선택되지 않은 경우에 대한 처리
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedItem_type = parent?.getItemAtPosition(position).toString()
                // 선택된 항목을 변수에 저장하거나 원하는 처리를 수행
            }
        }

        // 기본 선택 항목 지정
        val defaultValue = species
        val defaultIndex = adapter01.getPosition(defaultValue)
        spinner01.setSelection(defaultIndex)


        // 강아지 상태 정보 스피너 인터페이스 정보
        val spinner02 = findViewById<Spinner>(R.id.spinner02)

        // 어댑터 생성 및 항목 리스트 설정
        val adapter02 = ArrayAdapter.createFromResource(
            this,
            R.array.pet_state,
            android.R.layout.simple_spinner_item
        )
        adapter02.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner02.adapter = adapter02

        // 선택된 항목을 저장할 변수
        var state_send :String? = null
        spinner02.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 항목도 선택되지 않은 경우에 대한 처리
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                selectedItem_state = parent?.getItemAtPosition(position).toString()
                // 선택된 항목을 변수에 저장하거나 원하는 처리를 수행
                when (selectedItem_state) {
                    "입양상태" -> {
                        state_send = "Normal"
                        PetPrice.visibility = View.GONE
                        PetPrice.isClickable = false
                        PetPrice.isFocusableInTouchMode = false
                        dog_price = "None"
                    }
                    "분양등록중" -> {
                        state_send = "Adopting"
                        PetPrice.visibility = View.VISIBLE
                        PetPrice.isClickable = true
                        PetPrice.isFocusableInTouchMode = true
                        dog_price = PetPrice.text.toString()
                    }
                    "분실상태" -> {
                        state_send = "Missing"
                        PetPrice.visibility = View.GONE
                        PetPrice.isClickable = false
                        PetPrice.isFocusableInTouchMode = false
                        dog_price = "None"
                    }

                    else -> {
                        // 선택된 항목에 해당하는 처리를 추가하거나 기본 동작을 정의
                    }
                }
            }
        }
        // 기존에 설정된 상태에 따라 기본 세팅 선택이 달라지도록 설정
        var defaultValue02 = "입양상태"
        // 기본 선택 항목 지정
        when (state) {
            "Normal" -> defaultValue02 = "입양상태"
            "Adopting" ->  defaultValue02 = "분양등록중"
            "Missing" -> defaultValue02 = "분실상태"
        }

        val defaultIndex02 = adapter02.getPosition(defaultValue02)
        spinner02.setSelection(defaultIndex02)
        // 강아지 상태 스피너 기본 설정 완료

        // 종 텍스트 입력값을 기준으로 해당 입력 값을 포함한 제일 적합한 종 스피너 항목을 선택하게 하는 코드
        val textToMatch = Pettype.text.toString()
        var textLength = 0
        var selectedIndex = 0

        for (i in 0 until spinner01.count) {
            val currentItem = spinner01.getItemAtPosition(i).toString()
            if (currentItem.contains(textToMatch) && currentItem.length > textLength) {
                selectedIndex = i
                textLength = currentItem.length
            }
        }

        spinner01.setSelection(selectedIndex)

        // 종 텍스트 입력 설정 완료
        
        // 카메라 권한 설정관련 항목
        malegroup01.setOnCheckedChangeListener { group, checkid ->
            when (checkid) {
                R.id.male -> malecheck = "Male"

                R.id.female -> malecheck = "Female"

                R.id.nomale -> malecheck = "Nomale"
            }
        }
        // 정보 입력 버튼을 누를시 현재 새롭게 입력한 정보로 해당 비문정보를 가진 강아지 정보를 전송
        insert_info.setOnClickListener {
            dog_price = PetPrice.text.toString()
            // 입력받은 강아지 가격정보를
            val builder02 = AlertDialog.Builder(this)

            var dog_info: MutableMap<String,String?> = mutableMapOf(
                "ownerid" to ownerid,
                "owner" to owner,
                "name" to Petid.text.toString(),
                "sex" to malecheck,
                "species" to selectedItem_type,
                "state" to state_send,
                "imgpath" to imgpath,
                "imgnosepath" to imgnosepath,
                "price" to dog_price
            )
            builder02.setTitle("정보 저장 확인")
                .setMessage(
                            "\n강아지 이름: " + Petid.text.toString()
                            + "\n강아지 성별: " + malecheck
                            + "\n강아지 품종: " + selectedItem_type
                            + "\n강아지 현재 상태: " + selectedItem_state
                            + "해당 펫 정보를 저장하시겠습니까?"
                )
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                        val sendPostdata01 = DataModel02.changeInfodog(
                            dog_info,
                            emailid,
                            "1000001000"
                        )
                        loadingDialog.show()
                        mRetrofitAPI.changedoginfo(sendPostdata01)
                            .enqueue(object :
                                Callback<DataModel02.Postdogregister> {
                                override fun onResponse(
                                    call: Call<DataModel02.Postdogregister>,
                                    response: Response<DataModel02.Postdogregister>
                                ) {

                                    loadingDialog.hide()
                                    val resresult = response.body()?.message
                                    val resalt_transaction = response.body()?.transaction
                                    if (resresult == "SAVE OKAY") {
                                        Toast.makeText(
                                            this@ChangeDogInfoPage,
                                            "수정된 강아지 등록 정보가 서버에 전달되었습니다! 등록 서명페이지로 이동합니다! ",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent001 = Intent(this@ChangeDogInfoPage,ReconfirmPage::class.java)
                                        intent001.putExtra("transactioncode","1000001000")
                                        intent001.putExtra("idname",idname)
                                        intent001.putExtra("idpw",idpw)
                                        intent001.putExtra("email_id",emailid)

                                        intent001.putExtra("nodebaseurl",nodebaseurl)
                                        intent001.putIntegerArrayListExtra("transaction",
                                            resalt_transaction?.let { it1 -> ArrayList(it1) })
                                        // 상태값, 전달할려는 intent 설정
                                        setResult(Activity.RESULT_OK,intent001)
                                        // 메인 서비스 페이지 시작.
                                        startActivity(intent001)
                                    }

                                }
                                override fun onFailure(
                                    call: Call<DataModel02.Postdogregister>,
                                    t: Throwable
                                ) {

                                    loadingDialog.hide()
                                    Toast.makeText(
                                        this@ChangeDogInfoPage,
                                        "이미 등록된 강아지 정보가 확인되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent001 = Intent(this@ChangeDogInfoPage,Mainservicepage::class.java)
                                    intent001.putExtra("email_id",emailid)
                                    intent001.putExtra("idname",idname)
                                    intent001.putExtra("idpw",idpw)
                                    intent001.putExtra("nodebaseurl",nodebaseurl)
                                    // 상태값, 전달할려는 intent 설정
                                    setResult(Activity.RESULT_OK,intent001)
                                    // 메인 서비스 페이지 시작.
                                    startActivity(intent001)
                                }


                            })
                    })
                .setNegativeButton("취소",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog_message01.text = "취소 클릭"
                        val intent001 = Intent(this@ChangeDogInfoPage,Mainservicepage::class.java)
                        intent001.putExtra("email_id",emailid)
                        intent001.putExtra("idname",idname)
                        intent001.putExtra("idpw",idpw)

                        intent001.putExtra("nodebaseurl",nodebaseurl)
                        // 상태값, 전달할려는 intent 설정
                        setResult(Activity.RESULT_OK,intent001)
                        // 메인 서비스 페이지 시작.
                        startActivity(intent001)

                    })
            builder02.show()
        }

        missing.setOnClickListener{
        }
        Cancel_gobefore.setOnClickListener {
            Toast.makeText(
                this@ChangeDogInfoPage,
                "이전 페이지로 이동합니다!",
                Toast.LENGTH_SHORT
            ).show()
            val intent001 = Intent(this@ChangeDogInfoPage,PetInfoChange::class.java)
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

    private fun setRetrofit() {
        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        mRetrofit = Retrofit.Builder() // Retrofit2 인터페이스 빌더 생성
            .baseUrl(nodebaseurl) // 인터페이스와 연결될 서버 주소입력
            .addConverterFactory(GsonConverterFactory.create())
            .build() // 인터페이스 생성 얘가 위에있는 친구.

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
        // 위의 설정을 기반으로 Retrofit 인터페이스 생성
    }
    private fun setViews() {
        camerabutton.setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        if (checkPermission(CAMERA_PERMISSION, FLAG_PERM_CAMERA)) {
            val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, FLAG_REQ_CAMERA)
        }
    }
    fun checkPermission(permissions: Array<out String>, flag: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
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
                        Toast.makeText(this, "저장소 권한을 승인해야지만 앱을 사용할 수 있습니다..", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                        return
                    }
                }
                setViews()
            }
            FLAG_PERM_CAMERA -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "카메라 권한을 승인해야지만 카메라를 사용할 수 있습니다.", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }
                }
                openCamera()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                FLAG_REQ_CAMERA -> {
                    if (data?.extras?.get("data") != null) {
                        val bitmap = data.extras?.get("data") as Bitmap
                        indogimage = bitmapToFile(bitmap, File(filesDir, "image").toString())
                        iv_pre.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }
    // 비트맵 형식을 일반 파일 형식으로 변환하는 함수
    fun bitmapToFile(bitmap: Bitmap, path: String): File {
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

    fun bytesToHex(byteArray: ByteArray): String {
        val digits = "0123456789ABCDEF"
        val hexChars = CharArray(byteArray.size * 2)
        for (i in byteArray.indices) {
            val v = byteArray[i].toInt() and 0xff
            hexChars[i * 2] = digits[v shr 4]
            hexChars[i * 2 + 1] = digits[v and 0xf]
        }
        return String(hexChars)
    }


    @SuppressLint("RestrictedApi")
    fun requireActivity(): FragmentActivity {
        return (ContextUtils.getActivity(applicationContext)
            ?: throw IllegalStateException("Fragment $this not attached to an activity.")) as FragmentActivity
    }
}