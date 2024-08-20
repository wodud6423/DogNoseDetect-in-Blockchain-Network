package com.example.go.myapplication03

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress

lateinit var pingRetrofit: Retrofit
lateinit var pingRetrofitAPI: RetrofitAPI
var smallestValueNode : String? = null

class nodePing {
    fun onCreate() {
        val nodes = listOf("16.16.91.160")
        val pingResults = mutableListOf<Double>()

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

        val baseUrl = listOf("http://16.16.91.160:5000")
        var n = 0
        for (baseURL in baseUrl) {
            pingRetrofit = Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            pingRetrofitAPI = pingRetrofit.create(RetrofitAPI::class.java)
            pingRetrofitAPI.token().enqueue(object :
                Callback<DataModel02.token> {
                override fun onResponse(
                    call: Call<DataModel02.token>,
                    response: Response<DataModel02.token>
                ) {
                    if (response.isSuccessful) {
                        val tokenResponse = response.body()
                        if (tokenResponse != null) {
                            val tokenMessage = 1.0 / tokenResponse.token!!
                            val result: Double = tokenMessage * pingResults[n]
                            pingResults[n] = result
                        }
                    }
                }

                override fun onFailure(call: Call<DataModel02.token>, t: Throwable) {
                    TODO()
                }
            })

            n++
        }

        val smallestValueIndex = pingResults.indexOf(pingResults.minOrNull())
        if (smallestValueIndex != -1 && smallestValueIndex < nodes.size) {
            smallestValueNode = baseUrl[smallestValueIndex]

        }
        smallestValueNode = "http://16.16.91.160:5000"
    }

}
