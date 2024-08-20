from flask import Flask, request, jsonify
from flask import send_file
from flask_restful import Resource, Api, reqparse, abort
import json
import os
from time import time
from textwrap import dedent
from uuid import uuid4
import random
# Our blockchain.py API
from blockchain import Blockchain
from makeRSA import makeRSA
from aes import AESCipher
from dogNoseprint import noseprintshot 
#import apscheduler
#from apscheduler.schedulers.background import BackgroundScheduler
#from apscheduler.jobstores.base import JobLookupError
import socket
import requests
import ast
import copy
#from Cryptodome.PublicKey import RSA
# 다수의 노드에서의 프로세스를 처리하기 위한 signal 라이브러리
import signal
from requests_toolbelt import MultipartEncoder
import base64
import numpy as np
import cv2
# /transactions/new : to create a new transaction to a block
# /mine : to tell our server to mine a new block.
# /chain : to return the full Blockchain.
# /nodes/register : to accept a list of new nodes in the form of URLs
# /nodes/resolve : to implement our Consensus Algorithm
blockchain = Blockchain() # 블록체인 생성
# 자신의 외부 ip주소를 플라스크 웹서버로 실행하기 위해 받는 변수
app = Flask(__name__)
# Universial Unique Identifier
# 플라스크 서버에서 받는 임시파일은 해당 디렉토리에 저장되도록 설정함
node_identifier = str(uuid4()).replace('-','')
count = 0
state = 0 # 트랜잭션이 있는지 확인하는 상태 변수 storage
# 트랜잭션들을 마이닝하여 실제 체인에 블록으로 등록시키는 마이닝 트랜잭션
# 어떠한 트랜잭션의 발생시 반드시 실행되도록 설정.
chain = []
# 다른 노드가 마이닝시, 마이닝하고 받을 최신 체인
@app.route('/mine/transaction', methods=['GET'])
def mine():
    # 노드의 마이닝을 접근된 시간에 따른 순차적인 마이닝을 수행하기 위해 불러오는 변수
    global state
    # 현재 마이닝 요청으로 접근된 시각을 해당 지역변수에 저장.
    # 블록체인에 마지막에 넣어진 블록.
    checkmychain = blockchain.resolve_conflicts()
    # 자신의 노드가 최신 노드인지 먼저 확인한 뒤 진행합니다.
    # Warning! input transaction Double
    # blockchain.current_transactions.append(check)
    print(len(blockchain.current_transactions))
    transactions_all = blockchain.current_transactions
    # 블록에 트랜잭션을 등록하기 전에
    for i in range(len(transactions_all)):
        try:
            checktransaction = transactions_all[i]
            # 블록에 트랜잭션을 등록하기 전에 블록으로 등록할 트랜잭션들을 검사한다.
        except:
            break
        try:
            transactiontype = (checktransaction['transactioncode'])[0:4]
            # 트랜잭션의 종류를 판별하는 앞의 4자리 코드 비트로 중복되어 검사해야할 키와 값들을 각각 확인한다.
        except:
            return 'No transactioncode have. Can not mine', 400
        if transactiontype == "0001":
            # 만약 트랜잭션 유형이 분양 거래였을 경우, 우리가 확인해야할 키와 값은 판매자, 강아지 정보이다.
            TROKAY=blockchain.search_transaction('seller',checktransaction['seller'],'dog_info',checktransaction['dog_info'])
            # 판매자와 강아지 정보를 통해 중복되는 다른 분양 정보가 있는지 확인한다.(Double Attatk check)
            if TROKAY != None:
                # 만약 이미 블록에 동일한 트랜잭션이 존재할 시,
                blockchain.current_transactions.remove(checktransaction)
                # 트랜잭션 큐에서 해당 중복되는 트랜잭션을 제거한다.
            checktransaction = {
                'seller' :  checktransaction['seller'],# 판매자
                'dog_info' :  checktransaction['dog_info'] # 강아지 정보
            }
            # 이제 트랜잭션 큐를 검사할때 이다. 트랜잭션 큐에 등록된 트랜잭션들은 모두 하나의 블록에 등록된다.
            # 그러기때문에 처리시 이중 분양과 같은 블록이 마이닝 될 수 있다. 이를 해결하기 위해서는 트랜잭션 큐를 확인하고
            # 먼저 등록된 트랜잭션만 처리해준다.
            checkpara = blockchain.check_attack_double_simple(checktransaction)
            # 해당 함수는 위에서 입력한 트랜잭션의 내용을 포함하는 트랜잭션들을 리스트로 추출해준다.
            if checkpara != None:
                # checkpara가 None이라면 중복되는 요소가 없다는 것이고 아니라면 있다는 것
                for a in range(1,len(checkpara)):
                    blockchain.current_transactions.remove(checkpara[-a])
        elif transactiontype == "0010":
        # 만약 트랜잭션 타입이 분양취소였을경우(분양계약 취소)
            # 만약 트랜잭션 유형이 분양 거래였을 경우, 우리가 확인해야할 키와 값은 구매자, 강아지 정보이다.
            TROKAY=blockchain.search_transaction('buyer',checktransaction['buyer'],'dog_info',checktransaction['dog_info'])
            # 판매자와 강아지 정보를 통해 분양 정보가 있는지 확인한다.(Double Attatk check)
            if TROKAY == None:
                # 만약 이미 블록에 동일한 트랜잭션이 존재할 시,
                blockchain.current_transactions.remove(checktransaction)
                # 트랜잭션 큐에서 해당 중복되는 트랜잭션을 제거한다.
            checktransaction = {
                'buyer' :  checktransaction['buyer'],# 판매자
                'dog_info' :  checktransaction['dog_info'] # 강아지 정보
            }
            # 이제 트랜잭션 큐를 검사할때 이다. 트랜잭션 큐에 등록된 트랜잭션들은 모두 하나의 블록에 등록된다.
            # 그러기때문에 처리시 이중 분양과 같은 블록이 마이닝 될 수 있다. 이를 해결하기 위해서는 트랜잭션 큐를 확인하고
            # 먼저 등록된 트랜잭션만 처리해준다.
            checkpara = blockchain.check_attack_double_simple(checktransaction)
            # 해당 함수는 위에서 입력한 트랜잭션의 내용을 포함하는 트랜잭션들을 리스트로 추출해준다.
            if checkpara != None:
                # checkpara가 None이라면 중복되는 요소가 없다는 것이고 아니라면 있다는 것
                for a in range(1,len(checkpara)):
                    blockchain.current_transactions.remove(checkpara[-a])

    if len(blockchain.current_transactions) == 0:
        # 현재의 트랜잭션 리스트의 길이가 0이다(즉, 들어있는 트랜잭션이 없다는 것)
        state = 0
        # state는 0으로 바꿔준다.

    if state == 0:
        return 'missing values', 400
    else:
        last_block= blockchain.last_block
        # 마지막 마이닝 요청의 POW증명에 대한 값
        last_proof= last_block['proof']
        # 마지막 블록의 증명 값
        # 블록 마이닝을 완료했다면 클라이언트에게도 등록이 완료되었다는 응답 메세지를 전송
        # 오직 트랜잭션이 있을때만 블록을 생성
        proof= blockchain.pow(last_proof)
        # 마지막 블록의 증명 값으로 실제 POW를 만족하여 마이닝할수 있는지 확인.
        previous_hash= blockchain.hash(last_block)
        # 블록 체인의 마지막 블록의 해쉬값
        in_addr = socket.gethostbyname(socket.gethostname())
        # 자신의 주소및 사용포트번호를 받아온다.
        block= blockchain.new_block(in_addr, proof, previous_hash)
        # 새로운 블록 추가
        response = {
        'message': 'new block found',
        'index': block['index'],
        'timestamp':block['timestamp'],
        'transaction': block['transactions'],
        'proof': block['proof'],
        'previous_hash': block['previous_hash'],
        'node': in_addr
        }
        updateallnode=blockchain.request_update_chain()
        # 모든 노드들을 자신이 방금 업데이트한 블록체인으로 업데이트시킴
        state = 0
        # 트랜잭션을 마이닝했으므로 남아있는 트랜잭션은 없게 된다. 따라서 state는 0이다.
        return jsonify(response) , 201
# 사용자의 관리자 코드, 이름, email 아이디와 비밀번호를 받고 아이디를 블록으로 등록시키는 함수
@app.route('/transactions/new/id', methods = ['POST'])
def new_transaction_registerid():
    global state
    values=request.get_json()
    ori_index = blockchain.last_block['index']
    required=["idcode","idname","emailid", "idpw","transactioncode"]
    if not all(k in values for k in required):
        return 'missing values', 400
    id_check=blockchain.mining_0100('transactioncode', '0100', 'emailid',values['emailid'])
    if id_check != None:
        response = {'message': 'Email id is duplicated. Please input different email id.'}
        return jsonify(response), 201
    p = makeRSA.generate_random_prime()
    q = makeRSA.generate_random_prime()
    while p == False:
        p = makeRSA.generate_random_prime()
    while q == False:
        q = makeRSA.generate_random_prime()
    pubkey, privkey = makeRSA.generate_keypair(p,q)
    keyid = values["emailid"]
    keypw = values["idpw"]
    key = keyid+keypw
    data = str(privkey)
    aes = AESCipher(key)
    encrypt = aes.encrypt(data)
    decrypt = aes.decrypt(encrypt)
    index = blockchain.new_transaction_registerid(values["idcode"],values["idname"],values["emailid"],values["idpw"],values["transactioncode"], pubkey, privkey, encrypt, decrypt)
    if ori_index == index:
        return 'Not upload TR', 400
    response = {'encrypt': encrypt}
    state = 1
    return jsonify(response), 201

# 가입자가 가입 성립시 기록되는 트랙잭션
# 분양시 성립되는 거래를 기록하는 트랜잭션
@app.route('/change/pw/check', methods = ['POST'])
def check_password():
    values=request.get_json()
    required=['emailid','idpw']
    # 이메일 아이디를 요청으로 입력받음.
    transaction=blockchain.mining_0100('0100', 'emailid',values['emailid'])

    if transaction == None:
        response = {'message': 'Please check your email id again.' }
        return jsonify(response), 201
    
    # 이메일 아이디와 비밀번호로 트랜잭션을 조회한다.        
    else:
        checking_pw = transaction['idpw']

        if values['idpw'] == checking_pw:
            response = {'message': 'OK. Now input your new password.' }
            return jsonify(response), 201

        else:
            response = {'message': 'Please check your password again.' }
            return jsonify(response), 201

@app.route('/change/pw/new', methods = ['POST'])
def change_password():
    values = request.get_json()
    required = ['emailid','lastpw', 'idpw', 'transactioncode']
    transaction = blockchain.mining_0100('0100', 'emailid', values['emailid'], 'idpw', values['idpw'])
    last_transaction = blockchain.mining_0100('0100', 'emailid', values['emailid'], 'idpw', values['lastpw'])    
    global state
    if not all(k in values for k in required):
        return 'missing values ', 400
    if transaction:
        response = {'message': 'Your new password is same with last password.',
                   'encrypt' : transaction['encryptkey']
                   }
        return jsonify(response), 201
    else:
        key = last_transaction['emailid']+values['idpw']
        data = str(last_transaction['privkey'])
        aes = AESCipher(key)
        encrypt = aes.encrypt(data)
        decrypt = aes.decrypt(encrypt)
        index = blockchain.new_transaction_changepw(last_transaction,values['emailid'], values['idpw'], values['transactioncode'],encrypt,decrypt)
        response = {'message': 'Upload your new password. Please wait. Your new password is %s' %values['idpw'],
                   'encrypt': encrypt
                   }
        state = 1
        return jsonify(response), 201

@app.route('/count', methods=['GET'])
def count_mining():
    all_block = blockchain.chain
    token = 1
    my_ip = socket.gethostbyname(socket.gethostname())
    
    for i in range (len(all_block)):
        if all_block[i]["node"] == my_ip:
            token += 1
        else:
            token = token
            
    float_token = float(token)
    
    return jsonify({'token': float_token}), 201


# 분양의 마지막과정까지 와서 마지막으로 서명을 받았을때 이를 분양 트랜잭션을 등록하는 함수
@app.route('/transactions/new/transaction', methods = ['POST']) 
def new_transaction_transaction():
    values= request.get_json()
    ori_index = blockchain.last_block['index']
    required = ['buyer', 'seller', 'dog_info']
    global state
    if not all(k in values for k in required):
        return 'missing values 1', 400
    index = blockchain.new_transaction_transaction(values['buyer'], values['seller'], values['dog_info'],"0001000100",values['dog_info']['state'])
    if ori_index == index:
        return 'Not upload TR', 400
    response = { 'message':'Check Seller Sign {%s}' %index}
    state = 1
    return jsonify(response), 201

# 특정 강아지에 대해 분양 신청을 하는 함수. 
# 해당 과정이 매우 복잡하기에 발표전에 개발자는 반드시 확인할 것!
# 해당 함수는 강아지 분양 과정에서 첫번째 과정인 분양 신청을 받는 함수
# 분양 신청중인 강아지가 된다.
@app.route("/chain/adoptrequest/dog",methods=['POST'])
def new_transaction_requestdog():
    values= request.get_json()
    ori_index = blockchain.last_block['index']
    required = ['buyer', 'owner', 'dog_info']
    # 구매자 아이디, 분양 신청자(판매자) 아이디, 강아지 정보를 입력받아야한다. 
    global state
    if not all(k in values for k in required):
        return 'missing values 1', 400
    index = blockchain.new_transaction_transaction(values['buyer'], values['owner'], values['dog_info'],"0001000100",values['dog_info']['state'])
    # 자동으로 현재 상태가 변한 강아지에 대한 등록 트랜잭션을 생성한다.
    if ori_index == index:
        return 'Not upload TR', 400
    response = { 'message': "Adopting Request OKAY!" }
    state = 1
    return jsonify(response), 201

# 강아지 정보 변경시 실행되는 트랜잭션
@app.route('/transactions/new/registerchangedog', methods = ['POST'])
def new_transaction_by_transaction():
    values= request.get_json()
    print(values)
    ori_index = blockchain.last_block['index']
    required = ['dog_info','owner','transactioncode']
    global state
    if not all(k in values for k in required):
        return 'missing values 1', 400
    owner_pubkey = blockchain.search_transaction('emailid',values['owner'],'transactioncode',"0100000100")['pubkey']
    # 등록된 사용자의 RSA퍼블릭 키를 받는다. 이것으로 트랜잭션을 암호화한다.
    ownername_search = blockchain.mining_0100('0100','emailid',values['owner'])['idname']
    # 사용자의 이름과 등록코드로 사용자가 등록한 정보를 조회하여 그 트랜잭션에서 해당 사용자의 이름을 출력
    checktransactions = {
        'imgnosepath': values['dog_info']['imgnosepath']
    }
    # 저장된 강아지 코 이미지 경로 역시 강아지 정보의 기본키가 된다. 따라서 해당 경로를 통해 조회되는 강아지를 추출한다.
    transactions_doginfo = blockchain.dog_info_search(checktransactions,"1000000100")

    result_doginfo = transactions_doginfo[0]['dog_info']

    # 가장 최근에 등록한 강아지 정보를 가지고 옴(imgkey와 imgdes만 가져올 것이기 때문에 크게 상관x)
    img, key, des=blockchain.img_check(result_doginfo)
    # 추출해낸 강아지 정보 딕셔너리에서 원래의 key,des형태를 추출해낸다 = > 이것은 get_dog_information을 이용한 
    # 함수를 생성할 때, 반드시 key,des는 원래의 상태 (List of Dict,ArrayList)
    change_dog_info = blockchain.get_dog_information(values["owner"],ownername_search,values["dog_info"]["name"],values["dog_info"]["sex"], values["dog_info"]["species"],values["dog_info"]["state"],values["dog_info"]["imgpath"],values["dog_info"]["imgnosepath"],key,des,values['dog_info']['price'])
    # 소유권자의 이름및 아이디을 변경하여 해당 강아지 dog_info딕셔너리를 생성
    random_stream = ''.join(random.choice('0123456789') for _ in range(10))
    random_stream += values['dog_info']["ownerid"]
    # CheckTRCode로 넣게될 정수문자열 12자리를 넣는다.
    index = blockchain.new_registration_dog(values['owner'], change_dog_info, values['transactioncode'],random_stream)
    if ori_index == index:
        return 'Not upload TR', 400
    response = {
        'message' : "SAVE OKAY",
        'transaction': makeRSA.encrypt(owner_pubkey,str(random_stream))
        # 사용자의 공개키로 암호화하여 전송한다.
    }
    state = 1
    return jsonify(response), 201


# 펫 정보 입력란에서 해당 개의 정보를 입력하여 새로운 개의 정보를 입력하는 트랜잭션
# 사진 파일과 json형식의 입력 양식을 따로 받아서 이를 처리함
@app.route('/transactions/new/dog', methods = ['POST'])
def new_transaction_dog():
    ori_index = blockchain.last_block['index']
    try:
        file = request.files['file']
    except:
        return 'missing values', 400
    print(request.form['jsondata'])
    # 받은 요청에서 파일형식의 파일은 파일로 저장
    values = json.loads(request.form['jsondata'])
    # 입력받은 값중 jsondata라는 데이터를 json형식으로 읽어들임
    print("check01")
    required = ['dog_info','owner','transactioncode']
    if not all(k in values for k in required):
        return 'missing values in json', 400
    print("check02")
    # 만약 요청받은 json정보에 위의 키중 하나라도 없다면 위의 에러가 메세지로 출력되어 응답으로 보내진다.
    owner_pubkey = blockchain.search_transaction('emailid',values['owner'],'transactioncode',"0100000100")['pubkey']
    # 등록된 사용자의 RSA퍼블릭 키를 받는다. 이것으로 트랜잭션을 암호화한다.
    dog_info_dict = values['dog_info']
    # 요청받은 정보중 강아지 정보에 대한 딕셔너리를 조회한다.
    required = ['ownerid','owner','name','sex','species','state']
    for a in range(10000):
        # 다수의 해당 트랜잭션 요청받을시, 파일 생성에서 중복되지않게 파일 생성하기 위한 반복문. 
        receive_file_name = './templates/'+str(a)+file.filename
        result_file_name='./dognosedict/nosearea_dog_{0}'.format(str(a)+file.filename)
        if os.path.exists(receive_file_name)or os.path.exists(result_file_name):
            continue
        else :
            break
    # 요청받은 강아지 정보 딕셔너리에 필요한 정보가 다 들어가 있는지 확인한다.
    if not all(k in dog_info_dict for k in required):
        return 'missing values in dog_info_dict', 400
    # 딕셔너리에 해당 키에 대한 값이 존재한지를 확인한다.
    file.save(receive_file_name)
    # 이미지처리를 위해 templates 디렉토리에 파일을 저장
    img_nose=noseprintshot.find_dog_nose(receive_file_name,debug=True)
    # 임시 디렉토리에 저장된 해당 강아지 파일로 강아지의 코부분을 특정지어 추출
    KEY1,DES1=noseprintshot.noseprint_SIFT(img_nose)
    # 이미지에 대한 특이점과 그 특이점에 대한 디스크립터 
    dog_nose_check = {
            'species':dog_info_dict['species'],
            'sex':dog_info_dict['sex']
    }
    check_dog_duplicate=blockchain.dog_info_search(dog_nose_check,"1000000100")
    # 종, 성별이 같은 강아지로 범주를 줄여 해당 강아지의 비문 정보와 동일한 강아지 정보가 있는지를 조회한다.
    for i in range(1,len(check_dog_duplicate)+1):
        # 입력한 강아지의 종,성별과 같은 강아지들을 조회
        transaction = check_dog_duplicate[-i]
        # 각각의 트랜잭션을 조회하며 해당 트랜잭션에 기록된 강아지와 같은지 검사한다.
        img2,key2,des2 = blockchain.img_check(transaction['dog_info'])
        # 비교할 강아지에 대한 이미지, 이미지의 키와디스크립터에 대한 정보를 추출한다.
        # 비교할 강아지에 대한 사진을 받는다.
        check=noseprintshot.matcher_twoimage_knn(KEY1,DES1,key2,des2,img_nose,img2)
        # 각 강아지의 정보를 비교하며 같으면 True 다르면 False 
        if check == True:
            response = {'message': 'Duplication Info'}
            os.remove(receive_file_name)
            return jsonify(response), 201
    img_nose_path = os.path.abspath(img_nose)
    # 생성한 코 이미지에 대한 절대 경로를 저장
    img_path = os.path.abspath(receive_file_name)
    # 받은 이미지에 대한 절대 경로를 지정
    dog_info = blockchain.get_dog_information(dog_info_dict["ownerid"],dog_info_dict["owner"],dog_info_dict["name"],dog_info_dict["sex"], dog_info_dict["species"],dog_info_dict["state"],img_path,img_nose_path,KEY1,DES1)
    global state
    # 아직 등록되지 않은 트랜잭션이기때문에 실제 서명을 받은 트랜잭션을 등록완료 트랜잭션이라고 올릴것이다.
    # 서명을 받는 요청에서 서명 싸인자 ID,Transaction을 받는데 사실 이것만 받아서는 실제 서명을 받은것인지 알수없다.
    # 따라서 암호화하는 해당 트랜잭션에 실제 사용자에 의해 생성되고 서명된 트랜잭션이라는
    # 해당 트랜잭션만 가지는 고유의 부여된 임의의 코드가 필요하다. 이것이 해당 random_stream, 트랜잭션의 칼럼에서는 
    # "CheckTRCode"이 되게 된다.
    random_stream = ''.join(random.choice('0123456789') for _ in range(10))
    random_stream += dog_info_dict["ownerid"]
    # CheckTRCode로 넣게될 정수문자열 10자리를 넣는다.
    index = blockchain.new_registration_dog(dog_info_dict["ownerid"],dog_info,values['transactioncode'],random_stream)
    print(ori_index == index)
    if ori_index == index:
        return 'Not upload TR', 400
    print("okay01")
    print(makeRSA.encrypt(owner_pubkey,str(random_stream)))
    print(type(makeRSA.encrypt(owner_pubkey,str(random_stream))))
    print("okay02")
    response = {
        'message' : "SAVE OKAY",
        'transaction': makeRSA.encrypt(owner_pubkey,str(random_stream))
        # 사용자의 공개키로 암호화하여 전송한다.
    }
    state = 1
    return jsonify(response), 201


# 전체 블록체인의 블록들과 그 길이를 가져오는 트랜잭션
@app.route('/chain', methods=['GET'])
def full_chain():
    print("okayfull_chain")
    response = {
        'chain' : blockchain.chain,
        'length': len(blockchain.chain),
    }
    return jsonify(response), 200

# 로그인을 확안하는 함수. 아이디와 비밀번호를 받고 해당 아이디와 
@app.route('/chain/loginsearch', methods = ['POST'])
def login_id():
    values=request.get_json()
    required=['emailid','idpw']
    # 이메일 아이디를 요청으로 입력받음.
    transaction=blockchain.mining_0100('0100', 'emailid',values['emailid'])
    # 이메일 아이디와 비밀번호로 트랜잭션을 조회한다.
    if transaction:
        checking_pw = transaction['idpw']
        if checking_pw == values['idpw']:
            response = {'message': 'LoginOK' ,
                       'idname':transaction['idname']
                       }
            return jsonify(response), 201
        else:
            response = {'message': 'LoginNOOK02',
                       'idname':transaction['idname']
                       }
            return jsonify(response), 201
    else:
        response = {'message': 'LoginNOOK01',
                   'idname': "None"
                   }
        return jsonify(response), 201
    return "Error: Please supply a valid list of nodes", 400
# 소유권자의 아이디와 비밀번호를 입력받고 소유권자의 강아지 리스트를 응답으로 보내주는 함수
@app.route('/chain/mydogsearch', methods = ['POST'])
def mydog_all_search():
    imgbytelist = []
    # 출력될 강아지 이미지들을 ByteArray형식으로 담을 리스트
    values = request.get_json()
    # 필요한 json : 소유권자 id, 비밀번호
    required=['emailid','idpw']
    # 이메일 아이디를 요청으로 입력받음.
    if not all(k in values for k in required):
        # 필요로 한 json형식이 도착하지 않은 경우
        return 'missing values in json', 400
    transaction=blockchain.search_transaction('emailid',values['emailid'],'idpw',values['idpw'])
    # 이메일 아이디와 비밀번호로 트랜잭션을 조회한다.이는 현재 요청이 실제 존재하는 유저가 요청한 것인지에 대한 확인작업이다.
    if transaction:
        # 위의 아이디에 해당되는 비밀번호로 확인되면 나의 강아지들 리스트를 출력하는 작업 실행
        mydoglist = blockchain.search_mydoglist(values['emailid'])
        for i in range(len(mydoglist)):
            with open(mydoglist[i]['imgpath'], 'rb') as file:
                image_data = file.read()
            # 바이트 배열을 base64 인코딩
            encoded_image = base64.b64encode(image_data).decode('utf-8')
            # ByteArray형식 - > base64인코딩 
            imgbytelist.append(encoded_image)
        if len(mydoglist) == 0 :
        	# 만약 해당 소유권자가 소유한 개 목록이 없는 경우
            response = {'message': 'NOREGDOG' ,
                    'mydoglist': mydoglist, 
                    'imgbyteList' : imgbytelist
                   }
            return jsonify(response), 201
        else:
        	# 만약 소유권자의 개 목록이 있고, 그것을 잘 추출한 경우
            response = {'message': 'ListOK',
                        'mydoglist': mydoglist,
                        'imgbyteList' : imgbytelist
                       }
            return jsonify(response), 201
    else:
    	# 입력한 아이디에 대한 정보가 없는 경우
        response = {'message': 'NOID',
                    'mydoglist': [], 
                    'imgbyteList' : []
                   }
        return jsonify(response), 201
    return "Error: Please Check Code or Node Sitilation", 400

# 소유권자의 아이디와 비밀번호를 입력받고 소유권자의 나의 분양 강아지 리스트를 응답으로 보내주는 함수
# 즉, 출력 리스트에는 네 종류의 강아지 리스트가 출력되어야한다.
# 1) 분양 예약 등록된 나의 강아지 리스트 2) 내가 분양 예약 신청한 강아지 리스트 3) 분양자가 등록하고 예약된 강아지 리스트
# 4) 분양 신청하고 분양자가 싸인 서명한 경우
@app.route('/chain/mytransactiondog', methods = ['POST'])
def transactiondog_search():
    imgbytelist = []
    # 출력될 강아지 이미지들을 ByteArray형식으로 담을 리스트
    search_adoptdoglist =[]
    # 출력될 분양 강아지 리스트
    buyer_info = []
    search_03case = {
        'state':"None",
        'buyer': "None",
        'buyername':"None",
        'imgpath' :"None"
    }
    values = request.get_json()
    # 필요한 json : 소유권자 id, 비밀번호
    required=['emailid','idpw']
    # 이메일 아이디를 요청으로 입력받음.
    print("okay00")
    if not all(k in values for k in required):
        # 필요로 한 json형식이 도착하지 않은 경우
        return 'missing values in json', 400
    transaction=blockchain.search_transaction('emailid',values['emailid'],'idpw',values['idpw'])
    # 이메일 아이디와 비밀번호로 트랜잭션을 조회한다.이는 현재 요청이 실제 존재하는 유저가 요청한 것인지에 대한 확인작업이다.
    if transaction:
        # 위의 아이디에 해당되는 비밀번호로 확인되면 나의 강아지들 리스트를 출력하는 작업 실행
        search_adoptdoglist = blockchain.search_adoptdoglist("Adopting",values['emailid'])
        # 1번의 강아지 리스트
        search_adoptdoglist += blockchain.search_adoptdoglist("ReservedAdopting",values['emailid'])
        # 2번,3번의 강아지 리스트
        
        search_adoptdoglist += blockchain.search_adoptdoglist("OwnerSignAdioting",values['emailid'])
        
        # 4번의 강아지 리스트 추가
        print("okay01")
        for i in range(len(search_adoptdoglist)):
            check04 = (search_adoptdoglist[i]['state'] == "OwnerSignAdioting")and(search_adoptdoglist[i]['ownerid'] != values['emailid']) 
            if (search_adoptdoglist[i]['state'] == "ReservedAdopting" and search_adoptdoglist[i]['ownerid'] == values['emailid']) or check04:
                search_03case['state'] = search_adoptdoglist[i]['state'] 
                search_03case['buyer'] = search_adoptdoglist[i]['buyer']
                search_03case['buyername'] = blockchain.mining_0100('0100','emailid',search_03case['buyer'])['idname']
                search_03case['imgpath'] = search_adoptdoglist[i]['imgpath']
                buyer_info.append(search_03case)
                # 여기서 우리는 위에서 3번의 경우에서 우리는 분양 신청자에 대한 정보가 
                # 분양자에게도 보일수 있도록 해야함을 알 수 있다. 따라서 우리는 ReservedAdopting의 경우, 
                # 요청자에 따라 구매자의 정보를 보이거나 분양자의 정보가 보이게 해야한다.
                # 즉, buyer_info라는 리스트를 같이 전송하도록 한다.
                # 강아지 정보의 기본키가 될수 있는 또 하나의 것은 바로 이미지 경로. 생성된 이미지 경로는
                # 오직 그 강아지만의 이미지 경로가 됨을 생각해야한다.
            with open(search_adoptdoglist[i]['imgpath'], 'rb') as file:
                image_data = file.read()
            # 바이트 배열을 base64 인코딩
            encoded_image = base64.b64encode(image_data).decode('utf-8')
            # ByteArray형식 - > base64인코딩 
            imgbytelist.append(encoded_image)
            # 바이트어레이를 
            print("okay02")
            print(search_adoptdoglist)
        if len(search_adoptdoglist) == 0 :
        	# 만약 해당 소유권자가 소유한 개 목록이 없는 경우
            response = {'message': 'NOREGDOG' ,
                    'mydoglist': search_adoptdoglist , 
                    'imgbyteList' : imgbytelist,
                    'buyerinfo':buyer_info
                   }
            return jsonify(response), 201
        else:
        	# 만약 소유권자의 개 목록이 있고, 그것을 잘 추출한 경우
            response = {'message': 'ListOK' ,
                        'mydoglist': search_adoptdoglist ,
                        'imgbyteList' : imgbytelist,
                        'buyerinfo':buyer_info
                       }
            return jsonify(response), 201
    else:
    	# 입력한 아이디에 대한 정보가 없는 경우
        response = {'message': 'NOID',
                    'mydoglist': [],
                    'imgbyteList' :[],
                    'buyerinfo':[]
                   }
        return jsonify(response), 201
    return "Error: Please Check Code or Node Sitilation", 400

# 요청을 받으면 분양중인 강아지 목록을 보여주는 함수
@app.route('/chain/adoptingdogsearch', methods = ['POST'])
def adoptdog_all_search():
    imgbytelist = []
    # 출력될 강아지 이미지들을 ByteArray형식으로 담을 리스트
    # 위의 아이디에 해당되는 비밀번호로 확인되면 나의 강아지들 리스트를 출력하는 작업 실행
    adoptingdoglist = []
    # 출력될 분양등록된 강아지 리스트
    allregisteradoptingdoglist = blockchain.search_adoptdoglist("Adopting")
    # 모든 등록된 강아지 리스트를 출력해냅니다.
    for i in range(len(allregisteradoptingdoglist)):
        with open(allregisteradoptingdoglist[i]['imgpath'], 'rb') as file:
            image_data = file.read()
        # 바이트 배열을 base64 인코딩
        encoded_image = base64.b64encode(image_data).decode('utf-8')
        # ByteArray형식 - > base64인코딩 
        imgbytelist.append(encoded_image)
        # 바이트어레이를 
    if len(allregisteradoptingdoglist) == 0 :
        # 만약 해당 소유권자가 소유한 개 목록이 없는 경우
        response = {'message': 'NOADOPTINGDOG' ,
                    'mydoglist': [], 
                    'imgbyteList' : []

               }
        return jsonify(response), 201
    else:
        # 만약 분양중인 강아지 목록들이 있다면? => 목록과 함께 리스트가 출력됬다는 메세지를 전송한다.
        response = {'message': 'ListOK' ,
                    'mydoglist': allregisteradoptingdoglist ,
                    'imgbyteList' : imgbytelist
                   }
        return jsonify(response), 201
    return "Error: Please Check Code or Node Sitilation", 400

# 트랜잭션을 등록완료시키기위해 요청받는 함수. 결과로 트랜잭션 완료 코드로 변경되어 트랜잭션이 등록되게 된다.
@app.route('/transactions/confirm/registerdog',methods=['POST'])
def confirm_registerdog():
    global state
    values = request.get_json()
    required=['emailid','randomcode','transactioncode']
    # 이메일 아이디와 트랜잭션을 요청으로 입력받음.
    if not all(k in values for k in required):
        # 필요로 한 json형식이 도착하지 않은 경우
        return 'missing values in json', 400
    transaction= blockchain.search_transaction("CheckTRCode",values['randomcode'],"transactioncode",values['transactioncode'])
    check_transaction = copy.deepcopy(transaction)
    # 먼저 해당 트랜잭션이 실제 생성되었던 트랜잭션인지를 확인한다. 
    print(check_transaction)
    if check_transaction:
        # 만약 있었던 트랜잭션의 경우
        check_transaction['transactioncode'] = check_transaction['transactioncode'][:6]+"0100"
        # 해당 트랜잭션의 코드를 트랜잭션 등록 완료 코드로 마지막 4자리를 변경하여
        # 등록한다.
        blockchain.current_transactions.append(check_transaction)
        state = 1
        # 등록 완료 트랜잭션을 트랜잭션 큐에 올린다.
        response = {'message': "Transaction Register Okay!"}
        return jsonify(response), 201
    else:
        return "Error:Not a valid transaction!", 400        
        
        
# 사용자로부터 개의 이미지를 받아 처리하고, 트랜잭션에 동일한 정보가 있는지 조회하는 함수
@app.route('/chain/missingdog',methods=['POST'])
def match_missing_dog():
    image_data = request.get_data()
    np_arr = np.frombuffer(image_data, dtype=np.uint8)
    # 입력받은 바이트 어레이를 디코딩
    image = cv2.imdecode(np_arr, cv2.IMREAD_GRAYSCALE)
    # cv2모듈을 이용해 이미지로 변환(스케일은 그레이스케일)
    KEY1,DES1=noseprintshot.noseprint_SIFT(image)
    # 이미지에 대한 특이점과 그 특이점에 대한 디스크립터
    check_missinglist = {
        'state':"Missing"
    }
    checktransaction = blockchain.dog_info_search(check_missinglist,"1000000100")
    # 먼저 실종신고되있는 강아지 목록중에서 일치하는 강아지를 찾도록한다.
    for i in range(1,len(checktransaction)+1):
        # 입력한 강아지의 종,성별과 같은 강아지들을 조회
        transaction = checktransaction[-i]
        # 각각의 트랜잭션을 조회하며 해당 트랜잭션에 기록된 강아지와 같은지 검사한다.
        img2,key2,des2 = blockchain.img_check(transaction['dog_info'])
        # 비교할 강아지에 대한 이미지, 이미지의 키와디스크립터에 대한 정보를 추출한다.
        # 비교할 강아지에 대한 사진을 받는다.
        check=noseprintshot.matcher_twoimage_knn(KEY1,DES1,key2,des2,img_nose,img2)
        # 각 강아지의 정보를 비교하며 같으면 True 다르면 False 
        if check == True:
            response = {'message': 'Found! Send Email!',
                        "emailid":transaction['ownerid'] }
            return jsonify(response), 201
    # 만약 실종신고되있는 강아지목록에서 못찾았을 경우 , 전체 목록을 전부 확인한다.
    for a in range(1,len(blockchain.chain)+1):
        # 입력한 강아지의 종,성별과 같은 강아지들을 조회
        for b in range(1,len(blockchain.chain[-a]['transactions'])+1):
            transaction = blockchain.chain[-a]['transactions'][-b]
            # 각각의 트랜잭션을 조회하며 해당 트랜잭션에 기록된 강아지와 같은지 검사한다.
            img2,key2,des2 = blockchain.img_check(transaction['dog_info'])
            # 비교할 강아지에 대한 이미지, 이미지의 키와디스크립터에 대한 정보를 추출한다.
            # 비교할 강아지에 대한 사진을 받는다.
            check=noseprintshot.matcher_twoimage_knn(KEY1,DES1,key2,des2,img_nose,img2)
        # 각 강아지의 정보를 비교하며 같으면 True 다르면 False 
            if check == True:
                response = {'message': 'Found! Send Email!',
                            "emailid":transaction['ownerid'] }
                return jsonify(response), 201
    response = {
        'message': "Not Found!",
        "emailid":"None" 
               }
    return jsonify(response),201

# 사용자가 로그인시에 아이디,비밀번호를 입력받아 체인의 트랜잭션에서 해당 정보를 조회하여 메세지를 전송하는 함수
@app.route('/chain/idsearch', methods = ['POST'])
def search_id():
    values=request.get_json()
    required=['emailid']
    # 이메일 아이디를 요청으로 입력받음.
    transaction=blockchain.search_transaction('emailid',values['emailid'])
    # 해당 이메일 아이디로 ID를 검색한다.
    if transaction:
        response = {'message': 'NoCan' }
        return jsonify(response), 201
    else:
        response = {'message': 'Can' }
        return jsonify(response), 201
    return "Error: Please supply a valid list of nodes", 400

# IP노드를 블록체인 네트워크에 가입시키는 함수
@app.route('/nodes/register', methods=['POST'])
def register_nodes():
    values = request.get_json()
    node_ip = values.get('myip')
    # 새로 등록될 노드의 ip주소를 받아옴
    if node_ip is None: # Bad Request 400
        return "Error: NO IP INPUT! CAN NOT APPEND", 400
    # 만약 값이 IP주소입력값을 제대로 받았을시,
    blockchain.register_node(node_ip)
    if node_ip in blockchain.nodes:
        # 만약 해당 ip주소가 이미 등록되있다면
        response = {
            'message' : 'New nodes already added',
            'total_nodes': list(blockchain.nodes)
        }
        return jsonify(response), 201
    # 제대로 받고 현재 나의 현재 나의 nodes에 해당 노드의 ip 주소를 저장한다.
    response = {
        'message' : 'New nodes have been added',
        'total_nodes': list(blockchain.nodes)
    }
    return jsonify(response), 201

# 하나의 노드가 새로 등록될 노드의 ip주소를 받고 그 ip주소를 다른 노드들에게 등록 노드라고 브로드캐스트시
# 이 브로드캐스트 요청에 대한 함수. 요청을 받아 자신의 네트워크 노드에 받은 ip주소를 추가시키고 메세지 응답을 보냄
@app.route('/nodes/registerbroadcast',methods=['POST'])
def register_node_broadcast():
    values = request.get_json()
    required = ['registerip']
    # 이메일 아이디를 요청으로 입력받음.
    if values['registerip'] not in blockchain.nodes:
        blockchain.nodes.append(values['registerip'])
        # 만약 현재 나의 등록 노드 리스트에 해당 노드 ip주소가 없을 경우
    # 이메일 아이디와 비밀번호로 트랜잭션을 조회한다.
    response = {'message': 'OKAYREGISTER' }
    return jsonify(response), 201
    
# 상대방의 CHAIN을 받아 내 체인과 비교하여 받은 체인을 기준으로 나의 체인을 업데이트 시키는     
@app.route('/nodes/checkupdate', methods=['POST'])
def nodeupdatecheck():
    myupdatechain = json.loads(request.form['data'])
    myupdatechain_result = myupdatechain['myupdatechain']
    # myupdatechain을 원하는 형식으로 사용
    dognose_zip_file = request.files['dognose_zip']
    # 받은 강아지 코 압축파일을 읽어옵니다.
    dogimg_zip_file = request.files['dogimg_zip']
    # 받은 강아지 이미지 압축파일을 읽어옵니다.
    print(myupdatechain_result)
    print(blockchain.valid_chain(myupdatechain_result))
    print(len(myupdatechain_result) > len(blockchain.chain))
    # 해당 json데이터의 각 값을 딕셔너리 형태로 읽습니다.
    updatecheck = blockchain.mychain_update(myupdatechain_result,dognose_zip_file,dogimg_zip_file)
    # 나의 체인을 입력받은 체인을 확인하여 업데이트
    if updatecheck:
        response = {
            'message' : 'Our chain was replaced',
            'new_chain' : blockchain.chain
        }
    # 만약 체인이 유효하지 않다면 기존의 체인을 그대로 유지한다.
    else:
        response = {
            'message' : 'NOT ABLE CHAIN. CHECK RECEIVE CHAIN!!(NOT UPDATE)',
            'chain' : blockchain.chain
        }
    return jsonify(response), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0')
# 마이닝시 해당 트랜잭션을 블록에 올릴것인지 합의를 거친뒤 그 결과를 반환하는 트랜잭션
@app.route('/nodes/resolve', methods=['GET'])
def consensus():
    # 해당 체인이 유효한지 검사하여 유효하면 해당 체인으로 블록 체인의 체인을 업데이트
    replaced = blockchain.resolve_conflicts() # True False return
    # 해당 함수를 호출하므로써 호출한 노드는 최신 체인으로 업데이트되거나 
    # 혹은 자신이 최신 체인이였을 경우, 나의 노드가 최신 체인임을 확인 가능.
    # 만약 체인이 유효하다면 합의가 완료->해당 체인을 새로운 블록 체인의 체인으로 등록
    if replaced:
        response = {
            'message' : 'Our chain was replaced',
            'new_chain' : blockchain.chain
        }
    # 만약 체인이 유효하지 않다면 기존의 체인을 그대로 유지한다.
    else:
        response = {
            'message' : 'Our chain is authoritative',
            'chain' : blockchain.chain
        }
    return jsonify(response), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0')
