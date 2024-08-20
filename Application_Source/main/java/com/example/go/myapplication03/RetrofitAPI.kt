package com.example.go.myapplication03

import com.google.gson.JsonObject //JSON형태로 파일을 읽어들이기 위한 라이브러리
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call   //RETROFIT2의 Call라이브러리(요청에 대한 응답을 불러옴)
import retrofit2.http.* //RETROFIT2로 HTTP통신을 하기 위한 라이브러리
// retrofit API로 Block Chain 서버와 사용자의 어플리케이션을 연결해주는 인터페이스.
// 사용자는 Retrofit에 있는 함수를 사용해서 블록체인 서버에 요청을 보내고
// 각 함수의 형식에 따라 응답받을 수 있다.
interface RetrofitAPI {
    //서버에 요청할 주소를 입력하고 요청한 주소에 대한 함수를 등록.
    @GET("/mine/transaction")
    fun getMine() : Call<JsonObject> //MainActivity에서 불러와서 이 함수에 큐를 만들고 대기열에 콜백을 넣어주면 그거갖고 요청하는거임.

    @Multipart
    @POST("/transactions/new/dog") // 블록체인 서버에 나의 강아지 정보를 등록시에 POST요청으로 실행됨.
    fun savetransaction(
        //해당 함수 요청시 Body에 나의 강아지 정보를 파라미터로 전송함.
        @Part file: MultipartBody.Part, // 강아지 사진을 전송한다.
        @Part ("jsondata")jsonparams: DataModel02.Registerdog // 나의 강아지 정보 전송시에 DataModel02.PostModel02 데이터 형식의
    ) : Call<DataModel02.Postdogregister> // POST요청후 DatamODEL02.PostResult02의 데이터 형식으로 응답받음
    // 서비스 가입을 하는 정보를 보내고 가입 응답 메세지를 응답받는 객체

    @POST("/transactions/new/id")
    fun registerid(
        @Body jsonparams: DataModel02.Registerid // id중복을 확인하기 위해서 자신이 입력한 id를 요청으로 전송하는 객체
    ):Call<DataModel02.RegisterEncript>
    // 가입을 위한 아이디 중복을 확인하기 위해서 가입할 아이디를 보내고 가입할 수 있는지에 대한 응답을 받는 객체
    @POST("/chain/idsearch")
    fun checkid(
        @Body jsonparams: DataModel02.Searchid // id중복을 확인하기 위해서 자신이 입력한 id를 요청으로 전송하는 객체
    ):Call<DataModel02.PostResult02>
    // 로그인시 아이디와 비밀번호를 보내고 로그인이 되었는지에 대한 응답 메세지
    @POST("/chain/loginsearch")
    fun loginid(
        @Body jsonparams: DataModel02.Loginid
    ):Call<DataModel02.loginResult>
    // 나의 개 리스트를 조회할때 사용되는 함수

    @POST("/change/pw/check")
    fun checkmyinfo(
        @Body jsonparams: DataModel02.Loginid
    ):Call<DataModel02.checkmyinfoResult>

    @POST("/change/pw/new")
    fun changemyinfo(
        @Body jsonparams: DataModel02.Changemyinfo
    ):Call<DataModel02.changepwResult>

    @POST("/chain/mydogsearch")
    fun mydogallsearch(
        @Body jsonparams: DataModel02.Mydog_all_search
    ):Call<DataModel02.Mydog_all_search_result>
    // 분양 등록된 개 리스트를 조회하여 그 리스트를 출력해주는 함수
    @POST("/chain/adoptingdogsearch")
    fun adoptingdogallsearch(
    ):Call<DataModel02.Mydog_all_search_result>
    // 분양 신청시 실행되는 함수
    @POST("/chain/adoptrequest/dog")
    fun Adoptthispet(
        @Body jsonparams: DataModel02.Adoptthispet_request
    ):Call<DataModel02.PostResult02>
    //
    @GET("/download_img")
    @Streaming
    fun downloadFile(): Call<ResponseBody>
    //
    @POST("/Token")
    fun token(
        @Body jsonparams: DataModel02.requestToken
    ):Call<DataModel02.token>
    // 나의 강아지 리스트를 읽어오는 트랜잭션 함수

    @POST("/chain/missingdog")//서버에서 구현필요
    fun missing(
        @Body jsonparams: DataModel02.MissingDogImage
    ):Call<DataModel02.missingResult>

    // 분양 신청중인 강아지 목록을 읽어오는 트랜잭션 함수
    // 나의 등록된 강아지 정보를 수정하는 요청을 보내는 트랜잭션 함수
    @POST("/transactions/new/registerchangedog")
    fun changedoginfo(
        @Body jsonparams: DataModel02.changeInfodog // id중복을 확인하기 위해서 자신이 입력한 id를 요청으로 전송하는 객체
    ):Call<DataModel02.Postdogregister>
    // 서명페이지를 통해 자신이 복호화한 등록 트랜잭션과 자신의 이메일 아이디를 같이 전송한다.
    @POST("/transactions/confirm/registerdog")
    fun confirmregisterdog(
        @Body jsonparams: DataModel02.signtransaction
    ):Call<DataModel02.ReconfirmResult>

    @POST("/chain/mytransactiondog")
    fun mydogtransactionsearch(
        @Body jsonparams: DataModel02.Mydog_all_search
    ):Call<DataModel02.Transacion_all_search_result>

    @GET("/count")
    fun token(
    ):Call<DataModel02.token>
}