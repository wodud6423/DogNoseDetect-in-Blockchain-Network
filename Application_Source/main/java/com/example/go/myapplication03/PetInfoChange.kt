package com.example.go.myapplication03
import LoadingDialog
import android.util.Base64
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
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
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class PetInfoChange : AppCompatActivity() {

    private val loadingDialog: LoadingDialog = LoadingDialog(this@PetInfoChange)
    private lateinit var dogItems_list: MutableList<DataModel02.DogItem>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DogAdapter
    lateinit var mRetrofit: Retrofit
    lateinit var mRetrofitAPI: RetrofitAPI

    override fun onCreate(savedInstanceState: Bundle?) {

        setRetrofit()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_info_change)

        val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
        val emailid : String? = intent.getStringExtra("email_id")
        val idpw : String? = intent.getStringExtra("idpw")
        val idname : String? = intent.getStringExtra("idname")


        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        dogItems_list = mutableListOf()
        adapter = DogAdapter(dogItems_list)
        recyclerView.adapter = adapter

        val sendPostdatamydogserch = DataModel02.Mydog_all_search(emailid,idpw)
        loadingDialog.show()
        mRetrofitAPI.mydogallsearch(sendPostdatamydogserch).enqueue(object :
            Callback<DataModel02.Mydog_all_search_result> {
            override fun onResponse(
                call: Call<DataModel02.Mydog_all_search_result>,
                response: Response<DataModel02.Mydog_all_search_result>) {
                loadingDialog.hide()
                if (response.isSuccessful) {
                    val dogInfo = response.body()?.mydoglist
                    val mydogbytearray = response.body()?.imgbyteList
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
                        val dogItem = DataModel02.DogItem(ownerid,owner,name, sex, species, state,imgpath,imgnosepath,price,imageBytes)
                        dogItems_list.add(dogItem)

                        adapter.notifyItemInserted(dogItems_list.size - 1)
                    }
                }
            }
            override fun onFailure(call: Call<DataModel02.Mydog_all_search_result>, t: Throwable) {
                loadingDialog.hide()
                Toast.makeText(
                    this@PetInfoChange,
                    "!! 네트워크에 대한 응답을 받는데 실패하였습니다!! 메인서비스페이지로 돌아갑니다!",
                    Toast.LENGTH_SHORT
                ).show()
                // 응답을 받는데 에러가 발생한 경우 = > 메인 서비스 페이지로 돌아가게 설정
                val intent0001 = Intent(this@PetInfoChange,Mainservicepage::class.java)
                intent0001.putExtra("email_id", emailid)
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

    inner class DogAdapter(private val dogItems: List<DataModel02.DogItem>) : RecyclerView.Adapter<DogAdapter.DogViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
            // Inflate the layout for each item in the RecyclerView
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog, parent, false)
            return DogViewHolder(view)
        }

        override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
            val dogItem = dogItems[position]
            holder.bind(dogItem)
        }

        override fun getItemCount(): Int {
            return dogItems.size
        }

        inner class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val emailid : String? = intent.getStringExtra("email_id")
            val idpw : String? = intent.getStringExtra("idpw")
            val idname : String? = intent.getStringExtra("idname")

            val nodebaseurl : String? = intent.getStringExtra("nodebaseurl")
            private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            private val genderTextView: TextView = itemView.findViewById(R.id.genderTextView)
            private val breedTextView: TextView = itemView.findViewById(R.id.breedTextView)
            private val stateTextView: TextView = itemView.findViewById(R.id.stateTextView)
            private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
            private val imageView: ImageView = itemView.findViewById(R.id.imageView)
            private val dogButton: Button = itemView.findViewById(R.id.button)

            init {
                dogButton.setOnClickListener {
                    val context = itemView.context
                    var intent = Intent(context, ChangeDogInfoPage::class.java) // Replace with your new activity class

                    // Get the position of the clicked item
                    val position = adapterPosition

                    if (position != RecyclerView.NO_POSITION) {
                        // Retrieve the corresponding DogItem
                        val dogItem = dogItems[position]
                        // Pass the relevant data to the intent
                        intent.putExtra("email_id",emailid)
                        intent.putExtra("idpw",idpw)

                        intent.putExtra("idname",idname)
                        intent.putExtra("ownerid", dogItem.ownerid)
                        intent.putExtra("owner", dogItem.owner)
                        intent.putExtra("name", dogItem.name)
                        intent.putExtra("sex", dogItem.sex)
                        intent.putExtra("species", dogItem.species)
                        intent.putExtra("state", dogItem.state)
                        intent.putExtra("imgpath", dogItem.imgpath)
                        intent.putExtra("imgnosepath",dogItem.imgnosepath)
                        intent.putExtra("price",dogItem.price)
                        intent.putExtra("imageBytes", dogItem.imageBytes)

                        intent.putExtra("nodebaseurl",nodebaseurl)
                        // Start the activity
                        if (dogItem.state == "ReservedAdopting") {
                            Toast.makeText(
                                this@PetInfoChange,
                                "이미 예약신청되어있는 강아지 입니다!! 분양 확인 페이지로 이동합니다!",
                                Toast.LENGTH_SHORT
                            ).show()
                            intent = Intent(context, TransactionPage::class.java) // Replace with your new activity class
                            intent.putExtra("email_id",emailid)
                            intent.putExtra("idpw",idpw)

                            setResult(Activity.RESULT_OK,intent)

                            context.startActivity(intent)
                        }
                        else {

                            setResult(Activity.RESULT_OK,intent)
                            context.startActivity(intent)
                        }
                    }
                }
            }

            fun bind(dogItem: DataModel02.DogItem) {
                nameTextView.text = dogItem.name
                genderTextView.text = dogItem.sex
                breedTextView.text = dogItem.species
                stateTextView.text = dogItem.state
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
    // ImageView 객체를 Bitmap으로 변환하는 함수
    fun decodeByteArrayList(encodedList: List<String?>?): List<ByteArray?> {
        val byteArrayList = mutableListOf<ByteArray?>()
        encodedList?.forEach { encodedString ->
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            byteArrayList.add(decodedBytes)
        }
        return byteArrayList
    }

}
