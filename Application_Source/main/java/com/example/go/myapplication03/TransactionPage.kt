package com.example.go.myapplication03

import LoadingDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_mypetregister.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class TransactionPage : AppCompatActivity() {

    private val loadingDialog: LoadingDialog = LoadingDialog(this@TransactionPage)
    private lateinit var dogItems_list: MutableList<DataModel02.DogItem>
    private lateinit var recyclerView: RecyclerView
    private lateinit var buyerinfo : MutableList<MutableMap<String,String?>>
    private lateinit var adapter: DogAdapter
    lateinit var mRetrofit: Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI

    override fun onCreate(savedInstanceState: Bundle?) {

        setRetrofit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_page)

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        val emailid : String? = intent.getStringExtra("email_id")
        val idpw : String? = intent.getStringExtra("idpw")
        val idname : String? = intent.getStringExtra("idname")
        val buyer_none : MutableMap<String,String?> = mutableMapOf(
            "state" to "None", //
            "buyer" to "None", //
            "buyername" to "None", //
            "imgpath" to "None"
        )
        println(emailid)
        println(idpw)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        dogItems_list = mutableListOf()
        buyerinfo = mutableListOf()
        adapter = DogAdapter(dogItems_list,buyerinfo)
        recyclerView.adapter = adapter


        val sendPostdatamydogserch = DataModel02.Mydog_all_search(emailid,idpw)
        loadingDialog.show()
        mRetrofitAPI.mydogtransactionsearch(sendPostdatamydogserch).enqueue(object :
            Callback<DataModel02.Transacion_all_search_result> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(
                call: Call<DataModel02.Transacion_all_search_result>,
                response: Response<DataModel02.Transacion_all_search_result>) {
                loadingDialog.hide()
                if (response.isSuccessful) {
                    val dogInfo = response.body()?.mydoglist
                    val mydogbytearray = response.body()?.imgbyteList
                    val buyerinfo01 = response.body()?.buyerinfo
                    for (index in dogInfo?.indices!!) {
                        var dog = dogInfo[index]
                        val ownerid: String? = dog["ownerid"]
                        val owner: String? = dog["owner"]
                        val name = dog["name"]
                        val sex = dog["sex"]
                        val species = dog["species"]
                        val state = dog["state"]
                        val price = dog["price"]
                        val imgpath = dog["imgpath"]
                        val imgnosepath = dog["imgnosepath"]
                        val imageBytes = mydogbytearray?.getOrNull(index)
                            if (dog["state"] == "ReservedAdopting" && emailid == dog["ownerid"]){
                                for (index0 in buyerinfo01?.indices!!){
                                    if (buyerinfo01[index0]["imgpath"] == dog["imgpath"]){
                                        val dogItem = DataModel02.DogItem(ownerid,owner,name, sex, species, state,imgpath,imgnosepath,price,imageBytes)
                                        dogItems_list.add(dogItem)
                                        buyerinfo.add(buyerinfo01[index0])
                                        adapter.notifyItemInserted(dogItems_list.size - 1)
                                    }
                                }
                            }
                            else if (dog["state"] == "OwnerSignAdioting" && emailid != dog["ownerid"]){
                                for (index0 in buyerinfo01?.indices!!){
                                    if (buyerinfo01[index0]["imgpath"] == dog["imgpath"]){
                                        val dogItem = DataModel02.DogItem(ownerid,owner,name, sex, species, state, imgpath,imgnosepath,price,imageBytes)
                                        dogItems_list.add(dogItem)
                                        buyerinfo.add(buyerinfo01[index0])
                                        adapter.notifyItemInserted(dogItems_list.size - 1)
                                    }
                                }
                            }
                        else {
                                val dogItem = DataModel02.DogItem(
                                    ownerid,
                                    owner,
                                    name,
                                    sex,
                                    species,
                                    state,
                                    imgpath,
                                    imgnosepath,
                                    price,
                                    imageBytes
                                )
                                dogItems_list.add(dogItem)
                                buyerinfo.add(buyer_none)
                                adapter.notifyItemInserted(dogItems_list.size - 1)
                            }
                        }
                    }
                }

            override fun onFailure(call: Call<DataModel02.Transacion_all_search_result>, t: Throwable) {
                loadingDialog.hide()
                Toast.makeText(
                    this@TransactionPage,
                    "!! 네트워크에 대한 응답을 받는데 실패하였습니다!! 메인서비스페이지로 돌아갑니다!",
                    Toast.LENGTH_SHORT
                ).show()
                // 응답을 받는데 에러가 발생한 경우 = > 메인 서비스 페이지로 돌아가게 설정
                val intent0001 = Intent(this@TransactionPage,Mainservicepage::class.java)
                intent0001.putExtra("emailid", emailid)
                intent0001.putExtra("idpw", idpw)

                intent0001.putExtra("idname",idname)
                intent0001.putExtra("nodebaseurl",nodebaseurl)
                setResult(Activity.RESULT_OK,intent0001)
                // 메인 서비스 페이지로 이동
                startActivity(intent0001)
            }

        })

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

    inner class DogAdapter(private val dogItems: List<DataModel02.DogItem>, private val buyerInfos: MutableList<MutableMap<String, String?>>) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
            // Inflate the layout for each item in the RecyclerView
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog, parent, false)
            return DogViewHolder(view)
        }

        override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
            val dogItem = dogItems[position]
            val buyerInfo = buyerInfos[position]
            holder.bind(dogItem,buyerInfo)
        }

        override fun getItemCount(): Int {
            return dogItems.size
        }

        inner class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            private val genderTextView: TextView = itemView.findViewById(R.id.genderTextView)
            private val breedTextView: TextView = itemView.findViewById(R.id.breedTextView)
            private val stateTextView: TextView = itemView.findViewById(R.id.stateTextView)
            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            private val buyerTextView :TextView= itemView.findViewById(R.id.buyerTextView)
            private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
            private val dogButton: Button = itemView.findViewById(R.id.button)

            val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
            val emailid : String? = intent.getStringExtra("email_id")
            val idpw : String? = intent.getStringExtra("idpw")
            val idname : String? = intent.getStringExtra("idname")
            init {
                dogButton.setOnClickListener {
                    val context = itemView.context
                    // Replace with your new activity class
                    // Get the position of the clicked item
                    val position = adapterPosition

                    if (position != RecyclerView.NO_POSITION) {
                        val dogItem = dogItems[position]
                        val buyerInfo = buyerInfos[position]
                        if ((dogItem.state == "ReservedAdopting" && emailid == dogItem.ownerid)) {

                            var dog_info: MutableMap<String, String?> = mutableMapOf(
                                "ownerid" to dogItem.ownerid,
                                "owner" to dogItem.owner,
                                "name" to dogItem.name,
                                "sex" to dogItem.sex,
                                "species" to dogItem.species,
                                "state" to "OwnerSignAdioting",
                                "imgpath" to dogItem.imgpath,
                                "imgnosepath" to dogItem.imgnosepath,
                                "price" to dogItem.price
                            )
// 이와같은 경우, 서명 페이지로 이동하기전 사인하여 등록할 암호화된 트랜잭션을 받는 코드가 있어야 한다.
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
                                                this@TransactionPage,
                                                "수정된 강아지 등록 정보가 서버에 전달되었습니다! 등록 서명페이지로 이동합니다! ",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent001 = Intent(
                                                this@TransactionPage,
                                                ReconfirmPage::class.java
                                            )
                                            intent001.putIntegerArrayListExtra("transaction",
                                                resalt_transaction?.let { it1 ->
                                                    java.util.ArrayList(
                                                        it1
                                                    )
                                                })

                                            intent001.putExtra("transactioncode", "1000001000")
                                            intent001.putExtra("idpw", idpw)
                                            intent001.putExtra("email_id", emailid)
                                            // 상태값, 전달할려는 intent 설정

                                            intent001.putExtra("idname", idname)
                                            intent001.putExtra("nodebaseurl", nodebaseurl)
                                            setResult(Activity.RESULT_OK, intent001)
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
                                            this@TransactionPage,
                                            "요청에 대해 실패하였습니다! 메인 서비스 페이지로 돌아갑니다!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent001 = Intent(
                                            this@TransactionPage,
                                            Mainservicepage::class.java
                                        )
                                        intent001.putExtra("email_id", emailid)

                                        intent001.putExtra("idname", idname)
                                        intent001.putExtra("nodebaseurl", nodebaseurl)
                                        intent001.putExtra("idpw", idpw)
                                        // 상태값, 전달할려는 intent 설정
                                        setResult(Activity.RESULT_OK, intent001)
                                        // 메인 서비스 페이지 시작.
                                        startActivity(intent001)
                                    }
                                })
                        } else if (dogItem.state == "OwnerSignAdioting" && emailid != dogItem.ownerid) {
                                var dog_info: MutableMap<String, String?> = mutableMapOf(
                                    "ownerid" to buyerInfo["buyer"],
                                    "owner" to buyerInfo["buyername"],
                                    "name" to dogItem.name,
                                    "sex" to dogItem.sex,
                                    "species" to dogItem.species,
                                    "state" to "Normal",
                                    "imgpath" to dogItem.imgpath,
                                    "imgnosepath" to dogItem.imgnosepath,
                                    "price" to dogItem.price
                                )

// 이와같은 경우, 서명 페이지로 이동하기전 사인하여 등록할 암호화된 트랜잭션을 받는 코드가 있어야 한다.
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
                                                    this@TransactionPage,
                                                    " 입양자의 등록 서명페이지로 이동합니다! ",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                val intent001 = Intent(
                                                    this@TransactionPage,
                                                    ReconfirmPage::class.java
                                                )
                                                intent001.putIntegerArrayListExtra("transaction",
                                                    resalt_transaction?.let { it1 ->
                                                        java.util.ArrayList(
                                                            it1
                                                        )
                                                    })

                                                intent001.putExtra("transactioncode", "1000001000")
                                                intent001.putExtra("idpw", idpw)
                                                intent001.putExtra("email_id", emailid)
                                                // 상태값, 전달할려는 intent 설정

                                                intent001.putExtra("idname", idname)
                                                intent001.putExtra("nodebaseurl", nodebaseurl)
                                                setResult(Activity.RESULT_OK, intent001)
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
                                                this@TransactionPage,
                                                "요청에 대해 실패하였습니다! 메인 서비스 페이지로 돌아갑니다!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent001 = Intent(
                                                this@TransactionPage,
                                                Mainservicepage::class.java
                                            )
                                            intent001.putExtra("email_id", emailid)

                                            intent001.putExtra("idpw", idpw)

                                            intent001.putExtra("idname", idname)
                                            intent001.putExtra("nodebaseurl", nodebaseurl)
                                            // 상태값, 전달할려는 intent 설정
                                            setResult(Activity.RESULT_OK, intent001)
                                            // 메인 서비스 페이지 시작.
                                            startActivity(intent001)
                                        }
                                    })
                            }
                         else {
                            val intent = Intent(
                                this@TransactionPage,
                                Mainservicepage::class.java
                            ) // Replace with your new activity class
                            intent.putExtra("email_id", emailid)
                            intent.putExtra("idpw", idpw)

                            intent.putExtra("idname", idname)
                            intent.putExtra("nodebaseurl", nodebaseurl)
                            // Start the activity
                            Toast.makeText(
                                this@TransactionPage,
                                "현재 해당 분양과정중의 강아지에 접근이 불가합니다! 다음 과정을 기달려주십시오.",
                                Toast.LENGTH_SHORT
                            ).show()

                            setResult(Activity.RESULT_OK, intent)
                            startActivity(intent)


                        }

                    }
                }
            }
            fun bind(dogItem: DataModel02.DogItem,buyerinfo: MutableMap<String,String?>) {
                nameTextView.text = dogItem.name
                genderTextView.text = dogItem.sex
                breedTextView.text = dogItem.species
                stateTextView.text = dogItem.state
                buyerTextView.text = buyerinfo["buyer"]
                priceTextView.text = dogItem.price
                val imageByteResult = Base64.decode(dogItem.imageBytes, Base64.DEFAULT)
                if (dogItem.imageBytes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(imageByteResult, 0, imageByteResult!!.size)
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.drawable.ic_dog) // Replace with your default image resource
                }
            }
        }
    }
    private fun <T> List<T>.toArrayList(): ArrayList<T> {
        return ArrayList(this)
    }
    // Retrofit 인터페이스 설정
    private fun setRetrofit() {

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        mRetrofit = Retrofit.Builder() // Retrofit2 인터페이스 빌더 생성
            .baseUrl(nodebaseurl) // 인터페이스와 연결될 서버 주소입력
            .addConverterFactory(GsonConverterFactory.create())
            .build() // 인터페이스 생성 얘가 위에있는 친구.

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
        // 위의 설정을 기반으로 Retrofit 인터페이스 생성
    }
    // ImageView 객체를 Bitmap으로 변환하는 함수
}
