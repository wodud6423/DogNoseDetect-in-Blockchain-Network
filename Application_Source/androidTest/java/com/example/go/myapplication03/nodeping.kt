package com.example.nodetest

import com.example.go.myapplication03.DataModel02
import com.example.go.myapplication03.RetrofitAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress

lateinit var pingRetrofit: Retrofit
lateinit var pingRetrofitAPI: RetrofitAPI

class nodePing {

    var largestValueNode: String? = null

    suspend fun onCreate() = withContext(Dispatchers.IO) {
        val nodes = listOf("13.51.87.180", "3.37.158.247","13.48.76.117")
        val pingResults = mutableListOf<Double>()
        val Results = mutableListOf<Float>()

        for (node in nodes) {
            val startTime: Double = System.currentTimeMillis().toDouble()
            val address = InetAddress.getByName(node)
            if (address.isReachable(5000)) {
                val endTime: Double = System.currentTimeMillis().toDouble()
                val pingTime: Double = endTime - startTime
                pingResults.add(pingTime)
            } else {
                pingResults.add(5000.0)
            }
        }
        val baseUrl = listOf("http://13.51.87.180:5000", "http://3.37.158.247:5000","http://13.48.76.117:5000")
        for (baseURL in baseUrl.withIndex()) {
            pingRetrofit = Retrofit.Builder()
                .baseUrl(baseURL.value)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            pingRetrofitAPI = pingRetrofit.create(RetrofitAPI::class.java)
            pingRetrofitAPI.token().enqueue(object : Callback<DataModel02.token> {
                override fun onResponse(
                    call: Call<DataModel02.token>,
                    response: Response<DataModel02.token>
                ) {
                    if (response.isSuccessful) {
                        val tokenResponse = response.body()
                        if (tokenResponse != null) {
                            Results.add(tokenResponse.token!!)
                            val tokenMessage = tokenResponse.token!!
                            val result: Double = tokenMessage * pingResults[baseURL.index]
                            pingResults[baseURL.index] = result
                        }
                        else{
                            val tokenMessage = 5000.0
                            val result: Double = tokenMessage * pingResults[baseURL.index]
                            pingResults[baseURL.index] = result
                        }
                    }
                    // 모든 호출이 완료되었을 때 결과 출력
                    if (Results.size == baseUrl.size) {
                        val largestValueIndex = pingResults.indexOf(pingResults.min())
                        largestValueNode = "0"
                        if (largestValueIndex != -1 && largestValueIndex < nodes.size) {
                            largestValueNode = baseUrl[largestValueIndex]
                        } else {
                            largestValueNode = "0"
                        }
                    }
                }

                override fun onFailure(call: Call<DataModel02.token>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }
    }
}
