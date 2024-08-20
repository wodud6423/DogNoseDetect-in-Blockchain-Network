package com.example.go.myapplication03

class DataModel02 {
    class DogData(
        val name: String,
        val gender: String,
        val breed: String,
        val image_url: String
    )

    data class Dog(
        val name: String,
        val gender: String,
        val breed: String,
        val imageUrl: String
    )
    data class DataModel02(val data: List<DogData>)
    // transaction/new/dog POST 요청시 보내는 데이터 모델(이미지 데이터와 함께 전송하므로 이미지에 대한 자세한 칼럼내용은 포함X)
    data class Registerdog(
        var doginfo: MutableMap<String?, String?>? = mutableMapOf(
                "ownerid" to null, // 이메일 아이디(로그인 정보를 담고있는 범용DB와 연결되는 칼럼)
                "owner" to null, // 소유자 이름
                "name" to null, // 강아지 이름
                "sex" to null, // 강아지 성별
                "species" to null, // 강아지 종
                "state" to null, // 강아지 상태(분양중, 일반(분양안하는 상태))
        ),
        var owner: String? = null, //소유자 이메일 id(블록체인 DB의 기본키 및 범용 DB의 기본키와 연결되는 기본키)
        var transactioncode: String? = null
    )
    // 강아지 정보를 변경시에 참조되는 데이터 클래스
    data class changeInfodog(
        var doginfo: DogItem02? = null,
        var owner: String? = null, //소유자 이메일 id(블록체인 DB의 기본키 및 범용 DB의 기본키와 연결되는 기본키)
        var transactioncode: String? = null
    )
    // 일반적으로 트랜잭션이 등록되어 해당 트랜잭션 등록에 대한 처리 메세지를 받는 데이터 클래스
    data class PostResult02(
        var message: String? = null
        // PostModel02에 대한 결과 출력 메세지(해당 메세지는 Retrofit2에 의해 app을 사용하는 사용자한테로 받게 됨)
    )
    // 강아지 정보를 등록시에 서명 전 강아지 등록 트랜잭션 요청에 대한 응답. 응답 트랜잭션은 스트링형태로 되있으며, 자신의 RSA공개키로 암호화 되어있다.
    data class Postdogregister(
        var message: String? = null,
        var transaction : String? = null// PostModel02에 대한 결과 출력 메세지(해당 메세지는 Retrofit2에 의해 app을 사용하는 사용자한테로 받게 됨)
    )

    // 서명 재확인 페이지의 요청으로 보내지는 데이터 클래스
    data class signtransaction(
        var emailid : String? = null,
        var transaction : String? = null
    )

    data class token(
        var token: Long? = null // PostModel02에 대한 결과 출력 메세지(해당 메세지는 Retrofit2에 의해 app을 사용하는 사용자한테로 받게 됨)
    )

    // 체인 조회시 객체 구조
    data class ChainResult01(
        var chain: MutableList<Block?>? , // 체인을 조회할 시 블록 체인의 체인
        var length: Int? = null // 체인의 길이
    )
    // 체인의 블록 객체 구조
    data class Block(
        var index:Int? = null, // 해당 체인의 인덱스
        var timestamp:Float? = null,
        var transaction:MutableMap<String,String?>? = null,
        var proof: Int? = null,
        var previous_hash: String? = null
    )
    // 서비스에 아이디를 가입시 등록시키는 객체 구조
    data class Registerid(
        var idcode : String? = null,
        var idname : String? = null,
        var emailid : String? = null,
        var idpw : String? = null,
        var transactioncode : String = "0100000100"
    )
    // 가입시, 아이디 중복을 확인하기 위해 요청으로 보내는 객체 구조
    data class Searchid(
        var emailid : String? = null
    )
    // 로그인시, 아이디와 비밀번호를 전송하는 객체 구조
    data class Loginid(
        var emailid : String? = null,
        var idpw : String? = null
    )
    data class doginfo(
        var name: String? = null, // 펫 이름
        var sex: String? = null, // 펫 성별
        var species: String? = null, // 펫 종류
        var state: Int? = null,// 펫 상태정보
        val imageBytes: ByteArray
        )
    // DogItem의 imagekey를 정의하기 위한 데이터 클래스
    data class KeyPointData(
        val pt: Pair<Float, Float>,
        val size: Float,
        val angle: Float,
        val response: Float,
        val octave: Int,
        val classId: Int
    )
    // 실제 블록체인 내부에서 다뤄지는 강아지 정보 칼럼. 하지만 각 페이지의 경우에 따라 넣어지는 칼럼의 수와 값이 달라지기 때문에
    // 여러 개로 나눠졌다(위에서의 dog_info가 있음에도 이것도 정의된 이유)
    data class DogItem(
        var ownerid: String? = null,
        var owner: String? = null,
        var name: String? = null,
        var sex: String? = null,
        var species: String? = null,
        var state: String? = null,
        var imgpath: String? = null,
        var imgnosepath: String? = null,
        var imagekey: List<KeyPointData>? = null,
        var imagedes: List<List<Float>>? = null,
        var price : String? = null,
        var imageBytes: ByteArray? =null
    )
    // 분양 강아지 목록을 조회할때 사용되는 데이터 클래스, 위와 달리 이미지에 대한 자세한 정보는 제외되어있다.
    data class DogItem02(
        var ownerid: String? = null,
        var owner: String? = null,
        var name: String? = null,
        var sex: String? = null,
        var species: String? = null,
        var state: String? = null,
        var imgpath: String? = null,
        var imgnosepath: String? = null,
        var imagekey: List<KeyPointData>? = null,
        var imagedes: List<List<Float>>? = null,
        var price : String? = null
    )

    data class adopt(
        var emailid : String? = null,
        var idpw : String? = null,
        var emailid2 : String?=null,
        var name: String? = null
    )
    // 나의 강아지 리스트를 검색할 때 요청으로 보내는 데이터 클래스 
    data class Mydog_all_search(
        var emailid : String? = null,
        var idpw : String? = null
    )
    // 나의 강아지 리스트를 검색하여 그 응답으로 받는 데이터 클래스
    data class Mydog_all_search_result(
        var message : String? = null,
        var mydoglist: MutableList<DogItem?>? = mutableListOf(null),
        var imgbyteList :MutableList<ByteArray?>? = null
    )
    // 트랜잭션 페이지 접근시 요청되는 데이터 클래스
    // 세세히 나누면 다음과 같다
    // 1) 분양 등록한 나의 강아지 리스트 출력시 - 각 RecycleView에 강아지 정보가 나온다.
    // 2) 분양 신청한 강아지 리스트 출력시 - 각 RecycleView에 소유자 강아지 정보 출력 및 상태 출력
    // 3) 분양 등록하고 예약된 경우의 강아지 리스트 출력 - 각 RecycleView에 신청자에 대한 정보(해당 설계의 경우 emailid만 출력되게 설정하였다) 및 상태 출력
    // 4) 분양 신청하고 분양자가 싸인서명을 한 경우 강아지 리스트 출력 - 각 RecycleView에 소유자 강아지 정보 출력 및 상태 출력
    data class Transacion_all_search_result(
        var message : String? = null,
        var mydoglist: MutableList<DogItem?>? = mutableListOf(null),
        var imgbyteList :MutableList<ByteArray?>? = null,
        var buyerinfo : MutableList<MutableMap<String?,String?>?>? = null
    )

    // 분양 신청시에 요청으로 보내지는 데이터 클래스
    data class Adoptthispet_request(
        var buyer : String? = null,
        var owner : String? =null,
        var dogItem: DogItem02? = null
    )
    data class Changemyinfo(
        var emailid: String? = null,
        var lastidpw : String? = null,
        var idpw : String? = null,
        var transactioncode : String? = null
    )
    // 서비스 가입시 응답받는 AES 암호화된 RSA 개인키를 받게 된다.
    data class RegisterEncript (
        var encript: String
    )

    data class MissingDogImage(
        var imageBytes: ByteArray? =null
    )
}
