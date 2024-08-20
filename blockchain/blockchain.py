import hashlib
import json
from time import time
from time import sleep
import urllib.parse
import requests
from dogNoseprint import noseprintshot
from merkleTree import get_merkle_root
import random
from collections import Counter
from dataclasses import dataclass, asdict, astuple
from Cryptodome.PublicKey import RSA
from Cryptodome.Hash import SHA256
from Cryptodome.Signature import PKCS1_v1_5
import socket
import cv2
import numpy as np
import zipfile
import os
from requests_toolbelt.multipart.encoder import MultipartEncoder
import base64
import copy
#from AESCipher import AESCipher
####### block generation & its principle
class Blockchain:
    def __init__(self):
        self.chain = []
        self.current_transactions = []
        self.nodes = []
        self.new_block(in_addr = '0', proof=100,previous_hash='1')

    def new_block(self,in_addr, proof, previous_hash=None):
        if previous_hash == '1':
            # GENESIS BLOCK은 반드시 해당 블록의 모든 값이 동일해야한다. 즉, 정해져있는 값이여야한다. 
            # 따라서 각 노드에서 구동되는 변동적인 값이 들어가면 제네시스 블록의 해시값은 반드시 달라지게 된다.
            # 따라서 제네시스 블록의 경우 반드시 정해줘야한다!
            block = {
                'index' : 1,
                'timestamp': 20230520,
                'transactions': [],
                'proof': proof,
                'previous_hash': previous_hash,
                'merkle_root': get_merkle_root([]),
                'node': in_addr
            }
        else:
            block = {
                'index' : len(self.chain) + 1,
                'timestamp': time(),
                'transactions': self.current_transactions,
                'proof': proof,
                'previous_hash': previous_hash,
                'merkle_root': get_merkle_root(self.current_transactions),
                'node': in_addr
            }
        self.current_transactions = []
        self.chain.append(block)
        return block
    # 이미지 파일들은 압축을 해서 전송해야한다. 압축파일 2개를 만들고 이 압축파일의 경로를 반환하는 함수
    def myimgdir_zip(self):
        dognose_zip_path = './dognosedir.zip'
        dognoseimg_zip = zipfile.ZipFile(dognose_zip_path, 'w')
        for folder, subfolders, files in os.walk('./dognosedict'):
            # 강아지 코 이미지 저장파일안에 있는 파일을 모두 꺼낸다.
            for file in files:
                dognoseimg_zip.write(os.path.join(folder, file), os.path.relpath(os.path.join(folder, file), './dognosedict'), compress_type=zipfile.ZIP_DEFLATED)
        dognoseimg_zip.close()

        dogimg_zip_path = './dogimg.zip'
        dogimg_zip = zipfile.ZipFile(dogimg_zip_path, 'w')
        for folder, subfolders, files in os.walk('./templates'):
            # 강아지 이미지 저장파일안에 있는 파일을 모두 꺼낸다
            for file in files:
                dogimg_zip.write(os.path.join(folder, file), os.path.relpath(os.path.join(folder, file), './templates'), compress_type=zipfile.ZIP_DEFLATED)
        dogimg_zip.close()

        return (dognose_zip_path, dogimg_zip_path)
    # 압축해제시 이름이 같은 경우 해제된 파일들로 덮어씌우는 작업을 진행해야한다.
    def overwrite_files(self,extract_dir):
        for root, _, files in os.walk(extract_dir):
            for file in files:
                src_path = os.path.join(root, file)
                dst_path = os.path.join(extract_dir, file)
                os.replace(src_path, dst_path)
                
    # 트랜잭션 키와 값을 받아서 해당 트랜잭션을 출력하는 함수(키는 두개를 받을수 있고, 두개를 받을 경우 각각의 입력값이 
    # 등록된 값과 동일해야 트랜잭션을 반환. 아님 None을 반환한다.)
    def search_transaction(self,insertkey,insertvalues,insertkey2=None,insertvalues2=None,insertkey3=None,insertvalues3=None):
        for i in range(1,len(self.chain)+1):
            # block들의 transaction을 조회
            block=self.chain[-i]
            transaction=block['transactions']
            for n in range(1,len(transaction)+1):
                value01=transaction[-n][insertkey]
                if value01 == insertvalues:
                    if insertkey2 == None:
                        return transaction[-n]
                    else:
                        if transaction[-n][insertkey2] == insertvalues2:
                            if insertkey3 == None:
                                return transaction[-n]
                            else:
                                if transaction[-n][insertkey3] == insertvalues3:
                                    return transaction[-n]
                        else:
                            continue
        return None

    def mining_0100(self, transactioncode_value, insertkey1=None, insertvalues1=None, insertkey2=None,insertvalues2=None):
        for i in range(1,len(self.chain)+1):
            # block들의 transaction을 조회
            block=self.chain[-i]
            transaction=block['transactions']
            for j in range(1, len(transaction)+1):
                transactiontype_full = transaction[-j]
                transactiontype = transactiontype_full['transactioncode'][0:4]
                if transactiontype == transactioncode_value:
                    if insertkey1 == None:
                        return transaction[-j]
                    else:
                        if transaction[-j][insertkey1] == insertvalues1:
                            if insertkey2 == None:
                                return transaction[-j]
                            else:
                                if transaction[-j][insertkey2] == insertvalues2:
                                    return transaction[-j]
                        else:
                            continue
        return None

    # 강아지 트랜잭션 조회시, 해당 하나의 키와 강아지 고유정보(IMG,KEY,DES)로 가장 최근의 등록된 해당 강아지 트랜잭션
    # 넘버를 블록 넘버와 트랜잭션 큐 넘버로 출력하는 함수
    def transactionnum(self, transactioncode_value,dog_infoTR,insertkey1=None,insertvalues1=None):
        for i in range(1,len(self.chain)+1):
            # block들의 transaction을 조회
            num= [None,None]
            # 실제 해당 트랜잭션 위치가 어디인지를 출력하기 위한 변수
            # index 0: Block위치
            # index 1: 트랜잭션 큐 위치
            block=self.chain[-i]
            num[0] = i
            transaction=block['transactions']
            for j in range(1, len(transaction)+1):
                transactiontype_full = transaction[-j]
                num[1] = j
                transactiontype = transactiontype_full['transactioncode'][0:4]
                if transactiontype == transactioncode_value:
                    if insertkey1 == None:
                        checkimg,checkkey,checkdes=self.img_check(transactiontype_full['dog_info'])
                        # 찾은 강아지 트랜잭션에서 이미지,key,des를 추출
                        inputimg,inputkey,inputdes=self.img_check(dog_infoTR)
                        # 내가 입력한 강아지 트랜잭션에서 이미지,key,des를 추출
                        check=noseprintshot.matcher_twoimage_knn(inputkey,inputdes,checkkey,checkdes,inputimg,checkimg)
                        # 현재 출력할려는 강아지 정보가 내가 찾는 정보랑 일치한지 찾는 과정
                        if check:
                            # 만약 일치한 경우
                            return num
                        else:
                            continue
                    else:
                        if transactiontype_full[insertkey1] == insertvalues1:
                            checkimg,checkkey,checkdes=self.img_check(transaction[-j]['dog_info'])
                            # 찾은 강아지 트랜잭션에서 이미지,key,des를 추출
                            inputimg,inputkey,inputdes=self.img_check(dog_infoTR)
                            # 내가 입력한 강아지 트랜잭션에서 이미지,key,des를 추출
                            check=noseprintshot.matcher_twoimage_knn(inputkey,inputdes,checkkey,checkdes,inputimg,checkimg)
                            # 현재 출력할려는 강아지 정보가 내가 찾는 정보랑 일치한지 찾는 과정
                            if check:
                                # 만약 일치한 경우
                                return num
                            else:
                                continue
                        else:
                            continue
        return None
    # 강아지 트랜잭션을 입력하여 IMG,KEY,DES을 반환해주는 함수
    def img_check(self,dog_infoTR):
        result = []
        img=dog_infoTR.get('imgnosepath')
        key_dict=dog_infoTR.get('imagekey')
        key = []
        for kp_dict in key_dict:
            kp = cv2.KeyPoint(x=kp_dict['pt'][0], y=kp_dict['pt'][1], _size=kp_dict['size'], _angle=kp_dict['angle'], _response=kp_dict['response'], _octave=kp_dict['octave'], _class_id=kp_dict['class_id'])
            key.append(kp)
        des_serialize=dog_infoTR.get('imagedes')
        des= np.array(des_serialize)
        print(img,key,des)
        #debug용
        return img,key,des
        
    # 해당 key에 대한 values가 존재하면 해당 모든 트랜잭션들을 리스트형태로 출력.
    def search_transaction_all(self,insertkey,insertvalues,insertkey2=None,insertvalues2=None):
        transaction_list = []
        # 트랜잭션의 모음으로 동적 리스트를 생성
        for i in range(1,len(self.chain)+1):
            # block들의 transaction을 조회
            block=self.chain[-i]
            transaction=block['transactions']
            for n in range(1,len(transaction)+1):
                if transaction[-n][insertkey] == insertvalues:
                    if insertkey2 == None:
                        transaction_list.append(transaction[-n])
                        # insertkey2가 None으로 오직 하나의 키로 찾는 경우 insertkey에 입력한 값이 맞으면 해당 트랜잭션을 
                        # 리스트에 추가
                    else:
                        if transaction[-n][insertkey2] == insertvalues2:
                                transaction_list.append(transaction[-n])
                                # 두 개의 키로 찾는 경우 insertkey02에 입력한 값이 맞는것까지 확인하여 해당 트랜잭션을 추가
                        else :
                            continue

        return transaction_list
    # 해당 key에 대한 values가 존재하면 해당 트랜잭션들을 출력. 
    
    # 트랜잭션을 입력받아 체인에서 해당 완전히 동일한 트랜잭션을 찾으면 True을 출력하는 함수
    def search_transaction_by_transaction(self, search_transaction):
        for i in range(1,len(self.chain)+1):
            block = self.chain[-i]
            transaction=block['transactions']
            for n in range(1,len(transaction)+1):
                if transaction[-n] == search_transaction:
                    # 입력받은 트랜잭션과 완전히 동일한 경우
                    return True
        # 블록을 검사하고 나서 현재 등록되있는 트랜잭션 큐를 확인한다.
        for a in range(1,len(self.current_transactions)+1):
            if self.current_transactions[-a] == search_transaction:
                return True
        return False
        # 찾지 못한 경우
            
    def search_mydoglist(self, ownerid):
        mydoginfo_list = []
        checkformydoginfo_list = []

        # 최종적으로 나의 개 등록 트랜잭션 리스트에서 나의 개 정보만 리스트 형태로 뽑아 출력하는 함수   
        register_dog_list_all = self.search_transaction_all("owner", ownerid, 'transactioncode', "1000000100")

        # 중복 개 트랜잭션을 처리하기 위한 리스트
        processed_transactions = []

        for transaction_all in register_dog_list_all:
            transaction = copy.deepcopy(transaction_all)
            # 실제 현재 확인하는 트랜잭션들의 키가 모두 삭제되면 안되기에 copy를 한다.
            img_check, key_check, des_check = self.img_check(transaction_all['dog_info'])
            
            if len(mydoginfo_list) == 0:
                check_adopt = self.transactionnum("1000", transaction_all['dog_info'])
                # 전체 조회되는 해당 강아지 블록 및 트랜잭션 번호
                check_info = self.transactionnum("1000", transaction_all['dog_info'], "owner", transaction_all['dog_info']['ownerid'])
                # 내가 등록한 강아지의 블록및 트랜잭션 번호
                # 해당 작업은 내가 분양을 보냈다면 현재 조회가 되는 해당 강아지 등록 트랜잭션은 내가 등록한 트랜잭션 번호보다
                # 더 최신 블록및 최신 트랜잭션큐로 들어가 있을것이다. 즉, 이는 조회하는 나의 강아지가 진짜 현재 내가 소유한
                # 게 맞는지 확인하는 작업이다.
                if check_adopt:
                    if check_adopt[0] < check_info[0] or (check_adopt[0] == check_info[0] and check_adopt[1] < check_info[1]):
                        continue
                    else:
                        checkformydoginfo_list.append(transaction_all['dog_info'])
                        del transaction['dog_info']['imagekey']
                        del transaction['dog_info']['imagedes']
                        mydoginfo_list.append(transaction['dog_info'])
            else:
                checkpara = 0
                for check_dog_info in checkformydoginfo_list:
                    # 내가 등록했던 내 소유 강아지들중에 혹시 지금 등록시키는 강아지 정보가 겹치는지를 확인하는 작업
                    img_check01, key_check01, des_check01 = self.img_check(check_dog_info)
                    check01 = noseprintshot.matcher_twoimage_knn(key_check01, des_check01, key_check, des_check, img_check01, img_check)
                    if check01:
                        checkpara = 1
                        break

                if checkpara != 1:
                    check_adopt = self.transactionnum("1000", transaction_all['dog_info'])
                    check_info = self.transactionnum("1000", transaction_all['dog_info'], "owner", transaction_all['dog_info']['ownerid'])
                    if check_adopt:
                        if check_adopt[0] < check_info[0] or (check_adopt[0] == check_info[0] and check_adopt[1] < check_info[1]):
                            continue
                        else:
                            checkformydoginfo_list.append(transaction_all['dog_info'])
                            del transaction['dog_info']['imagekey']
                            del transaction['dog_info']['imagedes']
                            mydoginfo_list.append(transaction['dog_info'])

        return mydoginfo_list

    
        # 소유자의 이메일 아이디로 해당 소유자가 등록한 혹은 분양받은 강아지 리스트를 출력하는 함수
    def search_adoptdoglist(self,state,emailid = None):
        mydoginfo_list = []
        checkformydoginfo_list = []
        register_dog_list_all = []
        # 최종적으로 나의 개 등록 트랜잭션 리스트에서 나의 개 정보만 리스트 형태로 뽑아 출력하는 함수
        if emailid == None:
            chektransaction = {
                'state':state
            }
            register_dog_list_all = self.dog_info_search(chektransaction,"1000000100")
        else:
            if state == "Adopting":
                chektransaction = {
                    'ownerid':emailid,
                    'state':state
                }
                register_dog_list_all = self.dog_info_search(chektransaction,"1000000100")
                # 1번의 경우인 내가 등록한 강아지 리스트
            elif state == "ReservedAdopting":
                register_dog_list_all = self.search_transaction_all('buyer',emailid,'transactioncode','0001000100')
                # 2의 경우인 내가 예약신청한 강아지 리스트
                chektransaction = {
                    'ownerid':emailid,
                    'state':state
                }
                register_dog_list_all += self.dog_info_search(chektransaction,"1000000100")
                # 3번의 경우인 내가 등록했던 강아지중 현재 예약되있는 강아지 리스트
                        # 만약 해당 state값에 해당 되지않는 강아지일 경우 - 제거
            elif state == "OwnerSignAdioting":
                # 4번의 경우인 분양자가 싸인한 경우
                check_all_transaction = self.search_transaction_all('buyer',emailid,'transactioncode','0001000100')
                for t in check_all_transaction:
                    if t['dog_info']['state'] == state:
                        register_dog_list_all.append(t)
                        # 만약 해당 state값에 해당 되지않는 강아지일 경우 - 제거 
        # 해당 전체 트랜잭션에서 중복되는 강아지 트랜잭션들이 올라가게 된다. 따라서 이런 중복들을 제거해줘야한다.
        for transaction_all in register_dog_list_all:
            checkpara = 0
            # 추가할 트랜잭션에 대한 변수를 리셋한다. 확인을 하는 작업시 계속 초기화해줘야한다.
            transaction = copy.deepcopy(transaction_all)
            # 출력 리스트에 추가할 트랜잭션이 현재 리스트에 이미 추가되어져 있는지 , 혹은 더 최근 블록에 해당 강아지
            # 에 대한 분양 관련 트랜잭션이 있는지 확인하기 위해 해당 작업을 수행
            # 일부 정보가 다른 것의 중복성을 방지하기 위해 해당 확인 리스트에 추가
            img_check,key_check,des_check=self.img_check(transaction_all['dog_info'])
            # 중복된 같은 강아지 정보를 제거하기 위한 변수
            if len(mydoginfo_list) == 0:
                # 출력한 강아지 트랜잭션 리스트를 순차적으로 추가.
                # 중복되게 등록된 강아지 정보가 아닌 것은 확인했다. 이제 최근 해당 강아지 관련 분양 사실이 있었는지 확인한다.
                check_adopt=self.transactionnum("1000",transaction_all['dog_info'])
                # 먼저 해당 강아지에 대한 분양관련 트랜잭션의 블록넘버, 트랜잭션 큐 넘버를 조회한다.
                check_info=self.transactionnum("1000",transaction_all['dog_info'],"owner",transaction_all['dog_info']['ownerid'])
                if check_adopt:
                    # 분양 트랜잭션이 조회되었다.
                    if check_adopt[0] < check_info[0]:
                        continue
                    # 만약 해당 강아지에 대한 더 최근 분양관련 트랜잭션 블록이 확인된 경우
                    # 리스트에 추가하지 않고 넘어간다.
                    elif (check_adopt[0] == check_info[0]) and (check_adopt[1]<check_info[1]):
                        continue
                    else:
                        checkformydoginfo_list.append(transaction_all['dog_info'])
                        del transaction['dog_info']['imagekey']
                        del transaction['dog_info']['imagedes']
                        mydoginfo_list.append(transaction['dog_info'])
            else :
                for j in range(len(checkformydoginfo_list)):
                    img_check01,key_check01,des_check01=self.img_check(checkformydoginfo_list[j])
                    check01=noseprintshot.matcher_twoimage_knn(key_check01,des_check01,key_check,des_check,img_check01,img_check)
                    # 현재 추가할 강아지 정보가 일부 정보만 다른 같은 강아지 정보가 아님을 증명하는 과정
                    if check01 == True:
                    # 개 이름이 같은 나의 강아지 트랜잭션의 경우, 해당 트랜잭션은 이미 가장 최신 정보가 넣어진걸로 간주하고 
                        checkpara = 1
                    # 추가하지않고 넘어간다.
                        break
                if checkpara != 1:
                    check_adopt=self.transactionnum("1000",transaction_all['dog_info'])
                    # 먼저 해당 강아지에 대한 분양관련 트랜잭션의 블록넘버, 트랜잭션 큐 넘버를 조회한다.
                    check_info=self.transactionnum("1000",transaction_all['dog_info'],"owner",transaction_all['dog_info']['ownerid'])
                    if check_adopt:
                        # 분양 트랜잭션이 조회되었다.
                        if check_adopt[0] < check_info[0]:
                            continue
                        # 만약 해당 강아지에 대한 더 최근 분양관련 트랜잭션 블록이 확인된 경우
                        # 리스트에 추가하지 않고 넘어간다.
                        elif (check_adopt[0] == check_info[0]) and (check_adopt[1]<check_info[1]):
                            continue
                        else:
                            checkformydoginfo_list.append(transaction_all['dog_info'])
                            del transaction['dog_info']['imagekey']
                            del transaction['dog_info']['imagedes']
                            mydoginfo_list.append(transaction['dog_info'])
        return mydoginfo_list
    
    
    
    # Double Spending, 여기서는 이중 트랜잭션 생성 관련 문제를 해결하기 위한 함수
    def check_attack_double_standing(self,checktransactions):
        updatemychain=self.resolve_conflicts()
        # 자신의 체인을 최신 체인으로 업데이트한다.
        transactionlist =[]
        # 빈 트랜잭션 리스트를 생성
        for i, (key, value) in enumerate(checktransactions.items()):
            # 입력받은 트랜잭션 검증을 위한 키와 그에 해당되는 값을 순차대로 출력한다.
            transactionlist.extend(self.search_transaction_all(key,value))
            # 키와 값에 해당 되는 값들을 출력한다.
            for d in range(0,len(self.current_transactions)):
                print(self.current_transactions[d])
                # 키와 값이 중복되는 트랜잭션을 트랜잭션 큐안에서 찾는다.
                if value==self.current_transactions[d][key]:
                    # 입력된 키에 대한 값이 존재하면  
                    transactionlist.append(self.current_transactions[d])
                    # 트랜잭션리스트에 추가한다.
            if len(transactionlist) < 1:
                return True
            # 중복된 트랜잭션이 없는것이므로 True를 반환
        b_count = []# 각 원소의 등장 횟수를 카운팅할 리스트
        b_tr = [] # 실제 카운팅 리스트 인덱스에 맞춰 넣어지는 트랜잭션 리스트
        for a in range(len(transactionlist)):
            if b_tr.count(transactionlist[a]) >= 1:
                b_count[b_tr.index(transactionlist[a])] += 1
            else :
                b_count.insert(a,1)
                b_tr.insert(a,transactionlist[a])
        new_b = [] # 중복 원소만 넣을 리스트
        for b in range(len(b_tr)):
            if b_count[b] >= len(checktransactions): # n회 이상 등장한 원소로도 변경 가능
                new_b.append(b_tr[b])
        if len(new_b) == 0:
            return True
        else:
            return False
            # 어떠한 트랜잭션도 중복되지 않을 때, True를 반환한다.   
    # 트랜잭션 코드와 특정한 강아지 정보값을 받고 해당 트랜잭션들을 찾아 리스트로 출력하는 함수 
    def dog_info_search(self,checktransactions,code):
        # 입력받은 코드를 가지고 조회
        result_list = []
        # 출력될 결과 리스트 
        search_all = self.search_transaction_all("transactioncode",code)
        # 찾는 트랜잭션 코드로 먼저 모든 블록안의 트랜잭션 리스트를 추출
        for i in range(len(search_all)):
            checkpara = 0
            transaction_dog_info= search_all[i]['dog_info']
            # 해당 트랜잭션의 강아지 정보
            for key,values in checktransactions.items():
                if transaction_dog_info[key] == values:
                    checkpara = 1
                else:
                    checkpara = 0
                    break
            if checkpara == 1:
                result_list.append(search_all[i])
        # 이제 트랜잭션 큐를 검사
        for d in range(1,len(self.current_transactions)+1):
            # 현재 트랜잭션 큐도 확인.
            if self.current_transactions[-d]['transactioncode'] == code:
                result_list.append(self.current_transactions[-d])
                # 입력된 종과 성별값이 다른 같은 강아지 입력일수 있기때문에 일단 트랜잭션 코드만 같으면 출력 리스트에 집어넣는다.
        return result_list
    # 결과 강아지 리스트 출력
                    
        
    def check_attack_double_simple(self,checktransactions):
        updatemychain=self.resolve_conflicts()
        # 자신의 체인을 최신 체인으로 업데이트한다.
        transactionlist =[]
        # 빈 트랜잭션 리스트를 생성
        for i, (key, value) in enumerate(checktransactions.items()):
            # 입력받은 트랜잭션 검증을 위한 키와 그에 해당되는 값을 순차대로 출력한다.
            transactionlist.extend(self.search_transaction_all(key,value))
            # 키와 값에 해당 되는 값들을 출력한다.
            for d in range(0,len(self.current_transactions)):
                print(self.current_transactions[d])
                # 키와 값이 중복되는 트랜잭션을 트랜잭션 큐안에서 찾는다.
                if value==self.current_transactions[d][key]:
                    # 입력된 키에 대한 값이 존재하면  
                    transactionlist.append(self.current_transactions[d])
                    # 트랜잭션리스트에 추가한다.
            if len(transactionlist) < 1:
                return None
        b_count = []# 각 원소의 등장 횟수를 카운팅할 리스트
        b_tr = [] # 실제 카운팅 리스트 인덱스에 맞춰 넣어지는 트랜잭션 리스트
        for a in range(len(transactionlist)):
            if b_tr.count(transactionlist[a]) >= 1:
                b_count[b_tr.index(transactionlist[a])] += 1
            else :
                b_count.insert(a,1)
                b_tr.insert(a,transactionlist[a])
        new_b = [] # 확인할려는 조건을 만족하는 중복 트랜잭션만 넣을 리스트
        for b in range(len(b_tr)):
            if b_count[b] >= len(checktransactions): # n회 이상 등장한 원소로도 변경 가능
                new_b.append(b_tr[b])
        if len(new_b) == 0:
            return None
        else:
            return new_b
            # 어떠한 트랜잭션도 중복되지 않을 때, True를 반환한다. 
        # 사용자가 해당 서비스를 이용한 분양시, 그 거래에 대한 트랜잭션
    def new_transaction_transaction(self, buyer, seller, dog_info,transactioncode,state):
        checktransaction = {
            'seller' :  seller,# 판매자
            'dog_info' :  dog_info # 강아지 정보 
        }
        if state ==  "ReservedAdopting" or state == "OwnerSignAdioting":
            data = {
            'dog_info': dog_info,
            'owner':seller,
            'transactioncode': "1000000100",

        }
        # 만약 아직 분양의 마지막 과정이 아닌 상태의 경우, 생성되는 등록 트랜잭션의 소유자는 반드시 소유권자가 된다.
        elif state == "Normal":
            data = {
                'dog_info': dog_info,
                'owner':buyer,
                'transactioncode': "1000000100",

            }
        # 추가적으로 등록 트랜잭션 발생시 json형식으로 들어갈 Json데이터
        sleep(random.randrange(1, 3))
        # 1~10사이의 sleep시간을 가진다.이는 랜덤하게 시간을 두고 검증하여 이중 트랜잭션 공격을 감지하기 위함이다.
        checkpara = self.check_attack_double_standing(checktransaction)
        # Double Spending Attack 을 검증한다
        if checkpara:
            createtransaction = {
            'buyer':buyer,
            'seller':seller,
            'dog_info':dog_info,
            'price': dog_info['price'],
            'transactioncode':transactioncode,
            'owner':"None",
            'idcode':"None",
            'idname':"None",
            'emailid':"None",
            'idpw':"None",
            'img_hash':"None",
            'hash_transaction_id':"None",
            'CheckTRCode': "None"
            }
            # 만약 검증하여 해당 값이 True로 반환된 경우
            owner_pubkey = self.search_transaction('emailid',data['owner'],'transactioncode',"0100000100")['pubkey']
            # 등록된 사용자의 RSA퍼블릭 키를 받는다. 이것으로 트랜잭션을 암호화한다.
            ownername_search = self.mining_0100('0100','emailid',data['owner'])['idname']
            # 사용자의 이름과 등록코드로 사용자가 등록한 정보를 조회하여 그 트랜잭션에서 해당 사용자의 이름을 출력
            checktransactions = {
                'imgnosepath': data['dog_info']['imgnosepath']
            }
            # 저장된 강아지 코 이미지 경로 역시 강아지 정보의 기본키가 된다. 따라서 해당 경로를 통해 조회되는 강아지를 추출한다.
            transactions_doginfo = self.dog_info_search(checktransactions,"1000000100")
            result_doginfo = transactions_doginfo[0]['dog_info']
            # 가장 최근에 등록한 강아지 정보를 가지고 옴(imgkey와 imgdes만 가져올 것이기 때문에 크게 상관x)
            img, key, des=self.img_check(result_doginfo)
            # 추출해낸 강아지 정보 딕셔너리에서 원래의 key,des형태를 추출해낸다 = > 이것은 get_dog_information을 이용한 
            # 함수를 생성할 때, 반드시 key,des는 원래의 상태 (List of Dict,ArrayList)
            change_dog_info = self.get_dog_information(data["owner"],ownername_search,data["dog_info"]["name"],data["dog_info"]["sex"], data["dog_info"]["species"],data["dog_info"]["state"],data["dog_info"]["imgpath"],data["dog_info"]["imgnosepath"],key,des,data['dog_info']['price'])
            # 분양 관련 트랜잭션을 큐에 등록했으므로 이제 등록 트랜잭션을 요청하여 등록 트랜잭션 역시 큐에 등록되도록
            # 한다
            index = self.new_registration_dog(data['owner'], change_dog_info, data['transactioncode'],"0000000000"+data['dog_info']['ownerid'])
            return self.last_block['index']+1
        else :
            # 만약 검증에 실패한 경우, 공격이 들어온것으로 감지한다.
            return self.last_block['index']
            # 검증에 실패하였기 때문에 해당 트랜잭션은 무시된다.
   
    # 사용자가 서비스 가입시 사용자의 id와 비밀번호를 네트워크에 등록하는 함수
    def new_transaction_registerid(self,idcode,idname,emailid ,idpw,transactioncode, pubkey, privkey, encrypt, decrypt, okaykey=False,setkey=None): 
        checktransaction = {
            'emailid' :  emailid,# 이메일 아이디
            'transactioncode' : transactioncode # 트랜잭션 코드
        }
        sleep(random.uniform(1, 3))
        # 1~10사이의 sleep시간을 가진다.이는 랜덤하게 시간을 두고 검증하여 이중 트랜잭션 공격을 감지하기 위함이다.
        checkpara = self.check_attack_double_standing(checktransaction)
        # Double Spending Attack 을 검증한다
        if checkpara:
            createtransaction = {
            'buyer':"None",
            'seller':"None",
            'dog_info':"None",
            'price': "None",
            'transactioncode':transactioncode,
            'owner':"None",
            'idcode':idcode,
            'idname':idname,
            'emailid':emailid,
            'idpw':idpw,
            'img_hash':"None",
            'privkey': privkey,
            'pubkey': pubkey,
            'encryptkey': encrypt,
            'decryptkey': decrypt,
            'hash_transaction_id':"None",
            'CheckTRCode': "None"
            }
            # 만약 검증하여 해당 값이 True로 반환된 경우
            self.current_transactions.append(createtransaction)
            # 해당 트랜잭션을 트랜잭션 큐에 등록한다.
            # 만약 어떠한 중복된 트랜잭션도 발견되지않았다면 
            return self.last_block['index']+1
        else :
            # 만약 검증에 실패한 경우, 공격이 들어온것으로 감지한다.
            return self.last_block['index']

    def new_transaction_changepw(self,last_tranasaction,emailid ,idpw, transactioncode,encrypt,decrypt): 
        createtransaction = {
            'buyer':last_tranasaction['buyer'],
            'seller':last_tranasaction['seller'],
            'dog_info':last_tranasaction['dog_info'],
            'price': last_tranasaction['price'],
            'transactioncode':transactioncode,
            'owner':last_tranasaction['owner'],
            'idcode':last_tranasaction['idcode'],
            'idname':last_tranasaction['idname'],
            'emailid':emailid,
            'idpw':idpw,
            'img_hash':last_tranasaction['img_hash'],
            'privkey': last_tranasaction['privkey'],
            'pubkey': last_tranasaction['pubkey'],
            'encryptkey': encrypt,
            'decryptkey': decrypt,
            'hash_transaction_id':last_tranasaction['hash_transaction_id'],
            'CheckTRCode': last_tranasaction['CheckTRCode']
        }
            # 만약 검증하여 해당 값이 True로 반환된 경우
        self.current_transactions.append(createtransaction)
            # 해당 트랜잭션을 트랜잭션 큐에 등록한다.
            # 만약 어떠한 중복된 트랜잭션도 발견되지않았다면 
        return self.last_block['index']+1
    # 개의 정보로 저장하기 위한 함수 
    def get_dog_information(self,email_id, owner,name, sex, species,state,imgpath,imgnosepath,key1,des1,price = "None"):
        key_dict = []
        for kp in key1:
            kp_dict = {
                'pt': (kp.pt[0], kp.pt[1]),# 특징점의 위치를 나타내는 (x, y) 좌표입니다. 'pt' 키는 (kp.pt[0], kp.pt[1])로 설정되어 있다.
                'size': kp.size, # 특징점의 크기를 나타내는 값입니다. 'size' 키는 kp.size로 설정되어 있다.
                'angle': kp.angle, # 특징점의 방향을 나타내는 값입니다. 'angle' 키는 kp.angle로 설정되어 있다.
                'response': kp.response, # 특징점의 응답(특징점으로 간주되는 정도)을 나타내는 값입니다. 'response' 키는 kp.response로 설정되어 있다.
                'octave': kp.octave,# 특징점이 속한 옥타브(Octave)를 나타내는 값. 'octave' 키는 kp.octave로 설정되어 있다.
                'class_id': kp.class_id # 특징점의 클래스 식별자를 나타내는 값. 즉 고유한 특정 상수값과 비슷하다. 'class_id' 키는 kp.class_id로 설정되어 있습니다.
            }
            
            key_dict.append(kp_dict)
        # Save des as a numpy array
        des_serialize = des1.tolist()
        dog_info = {
        'ownerid':email_id,#이메일 아이디(로그인 정보를 담고있는 범용DB와 연결되는 칼럼)
        'owner':owner, # 소유자 이름
        'name':name, # 강아지 이름
        'sex' : sex, # 강아지 성별
        'species': species, # 강아지 종
        'state':state, # 강아지 상태(분양중, 일반(분양안하는 상태))
        'imgpath':imgpath, # 강아지 이미지
        'imgnosepath': imgnosepath, # 이미지가 저장된 절대 경로,
        'imagekey': key_dict, # 이미지에 대한 특이점 key정보
        'imagedes': des_serialize, #  특이점 key정보에 대한 key descriptor
        'price' : price # 강아지 가격 정보로 스트링 형태임에 주의해야한다! 정수형이 아닌 스트링 형태로 받아 처리한다.
        }
        # 입력한 강아지 정보가 실제 체인에서 중복되는 정보가 있는지 확인
        # 해당 강아지 정보에 대한 중복성을 검사
        # GET으로 넘겨주는 정보 출력
        print('%s' %email_id) 
        print('%s' %owner)
        print('%s' %sex)
        print('%s' %species)
        return dog_info
        # 개 정보 등록 함수
    def new_registration_dog (self, owner, dog_info,transactioncode,CheckTRCode):
        checktransaction = {
            'owner' :  owner,# 소유권자
            'dog_info' :  dog_info, # 강아지 정보
            # 중복된 정보가 블록에 등록되지 않도록 함.(공격이 아닌 전송과정중의 에러로 인한 중복 요청을 받아 처리시 확인작업)
        }
        sleep(random.uniform(1, 3))
        # 1~10사이의 sleep시간을 가진다.이는 랜덤하게 시간을 두고 검증하여 이중 트랜잭션 공격을 감지하기 위함이다.
        checkpara = self.check_attack_double_standing(checktransaction)
        # 요청에 대한 에러발생으로 인한 이중요청, 혹은 의도된 중복 트랜잭션 검사
        if checkpara:
            createtransaction = {
            'buyer':"None",
            'seller':"None",
            'dog_info':dog_info,
            'price': dog_info['price'],
            'transactioncode':transactioncode,
            'owner':owner,
            'idcode':"None",
            'idname':"None",
            'emailid':"None",
            'idpw':"None",
            'img_hash':"None",
            'hash_transaction_id':"None",
            'CheckTRCode': CheckTRCode
            }
            # 만약 검증하여 해당 값이 True로 반환된 경우
            self.current_transactions.append(createtransaction)
            # 해당 트랜잭션을 트랜잭션 큐에 등록한다.
            # 만약 어떠한 중복된 트랜잭션도 발견되지않았다면 
            return self.last_block['index'] + 1 
        else:
            return self.last_block['index']
    
    # 해당 노드를 블록 체인 서버에 등록(풀노드)
    def register_node(self, ip_address):
        self.nodes.append(ip_address)
        # 입력받은 등록 노드의 ip주소를 받아온다.
        if len(self.nodes) > 0:
            # 만약 현재 나에게 등록된 노드들의 수가 현재 등록되어있는 등록 노드의수가 1개 이상이라면
            for i in range(len(self.nodes)):
                # 나머지 등록된 노드에게 방금 내가 추가한 노드에 대한 노드추가를 알려야한다.
                data = {
                    'registerip':ip_address
                }
                tmp_url = 'http://' + str(self.nodes[i]) + ':5000/nodes/registerbroadcast'
                # 각 노드에게 해당 노드의 등록을 알려야한다. 즉, 나머지 노드들도 해당 자신의 네트워크 노드로 등록시켜야한다.
                response = requests.post(tmp_url,json = data) 
                # 해당 ip주소를 데이터에 담아 똑같이 모든 나의 등록된 노드들에게 전송한다.
                
    # 유효한 체인인지 검사하는 함수.
    def valid_chain(self,chain):
        # 큐로 생각하여 가장 처음에 넣어진 체인의 블록은 체인의 맨 처음에 위치함.
        # 현재 블록(last_block)의 해쉬값과 다음 블록의 이전 해쉬값(previous_hash)값을 비교하여 해당 체인이 유효한지
        # 검사.
        last_block = self.chain[0]
        # 맨 처음에 제네시스 블록의 해시값과 이전 블록에서의 해시값을 비교하는 작업으로 시작됨으로 체인의 제네시스 블록을 
        # 해시값을 비교할 마지막 블록으로 설정
        current_index = 1
        # 해당 체인의 길이만큼 순차대로 검사.
        while current_index < len(chain):
            # 순차대로 체인의 블록
            block = chain[current_index]
            print(type(last_block))
            print("\n---------\n")
            print(block)
            print(type(block))
            print("\n---------\n")
            print("\n---------\n")
            print(block['previous_hash'])
            print(type(block['previous_hash']))
            print("\n---------\n")
            print(self.hash(last_block))
            print(type(self.hash(last_block)))
            print("\n---------\n")
            print(block['previous_hash'] != self.hash(last_block))
            # check that the hash of the block is correct(해당 블록의 이전 해쉬값과 실제 업데이트되있는 마지막 블록의 
            # 해쉬값을 비교) 만약 맞지 않을 경우, 해당 체인은 유효하지 않음.
            if block['previous_hash'] != self.hash(last_block):
                return False
            # 현재 블록을 마지막 블록으로 바꾸고 다음 블록의 이전 해쉬값과 비교하며 검사
            last_block = block
            # 현재 체인의 인덱스를 1 높임.
            current_index += 1
        return True

    def request_update_chain(self):
        neighbours = self.nodes
        mychain = self.chain
        mydognosedirzip_path, mydogdirzip_path = self.myimgdir_zip()

        for node in neighbours:
            data = {
                'myupdatechain': mychain,
            }
            fields = {
                'data': json.dumps(data),
                'dognose_zip': (mydognosedirzip_path, open(mydognosedirzip_path, 'rb'), 'application/zip'),
                'dogimg_zip': (mydogdirzip_path, open(mydogdirzip_path, 'rb'), 'application/zip')
            }

            multipart_encoder = MultipartEncoder(fields=fields)
            headers = {'Content-Type': multipart_encoder.content_type}

            try:
                url = f'http://{node}:5000/nodes/checkupdate'
                response = requests.post(url, data=multipart_encoder, headers=headers)
                response.raise_for_status()
                print(f"요청이 성공적으로 전송되었습니다. (노드: {node})")
            except requests.exceptions.RequestException as e:
                print(f"요청 전송 중 오류가 발생했습니다: {e} (노드: {node})")

        print("모든 노드가 자신의 체인과 이미지 파일로 업데이트되었습니다.")


    
    # 나의 체인과 상대방에게 받은 체인의 길이를 비교하고, 상대방 체인이 유효하다면 
    # 상대방 체인으로 나의 체인및 나의 강아지 코,강아지 이미지를 업데이트한다.
    def mychain_update(self,receive_chain,receivedognose,receivedogimg):
        # 처음에는 나의 체인이 제일 최신 체인으로 생각하여 None으로 초기화
        my_length = len(self.chain) 
        # 응답이 정상적으로 수행되었을 시, 조건문 진입
        length = len(receive_chain)
        # 응답받은 json형식의 출력에서 해당 노드의 체인 길이를 length 지역 변수에 할당.
        chain = receive_chain
        # 응답받은 json형식의 출력에서 해당 노드의 체인을 지역 변수에 할당
        target_directory_nose = './dognosedict'
        # 해당 디렉토리에 받은 강아지 코 이미지 파일을 압축을 풀며 업데이트하기 위해 경로 설정
        target_directory_dog = './templates'
        # 해당 디렉토리에 받은 강아지 코 이미지 파일을 압축을 풀며 업데이트하기 위해 경로 설정
        print(self.valid_chain(chain))
        print(length > my_length)
        if length > my_length and self.valid_chain(chain):
            # 만약 검사하는 노드의 체인 길이가 가장 최신의 체인이여서 해당 체인의 길이가 함수를 수행하는 노드의 
            self.chain = receive_chain
            # 해당 체인으로 업데이트할 체인에 할당.
            dognose_zip_path = os.path.join(target_directory_nose, 'dognosedir.zip')
            # 받은 강아지  코 이미지 '파일'을 설정한 강아지 코 이미지 디렉토리에 압축파일로 저장하고
            # 이를 다시 압축을 해제해줘야한다. 
            dogimg_zip_path = os.path.join(target_directory_dog, 'dogimg.zip')
            # 받은 강아지 이미지 파일 역시 같은 작업이 필요하다.
            receivedognose.save(dognose_zip_path)
            # 각 저장되어야할 경로에 압축 파일들을 저장한다.
            receivedogimg.save(dogimg_zip_path)
            # 각 저장되어야할 경로에 압축 파일들을 제공한다.
            with zipfile.ZipFile(dognose_zip_path, 'r') as zip_ref:
                zip_ref.extractall(target_directory_nose)
                # 먼저 생성한 강아지 코 이미지 압축 파일을 해당 디렉토리에서 풀어준다. 파일은 덮어씌워진다.
                self.overwrite_files(target_directory_nose)
            with zipfile.ZipFile(dogimg_zip_path, 'r') as zip_ref:
                zip_ref.extractall(target_directory_dog)
                # 그 다음 생성한 강아지 이미지 압축 파일을 해당 디렉토리에서 풀어준다. 파일은 덮어씌워진다.
                self.overwrite_files(target_directory_dog)
            # 각 압축해제하는 파일들로 덮어씌운다.
            os.remove(dognose_zip_path)
            # 사용한 압축파일은 삭제한다.
            os.remove(dogimg_zip_path)
            return True
            # 해당 체인으로 대체되었으므로 True를 반환.
        return False
            # 만약 입력받은 체인이 유효하지 않는 경우 False를 반환

    def resolve_conflicts(self):
        # 블록 생성후 체인에 블록을 넣고나서 해당 노드에서의 체인이 유효한지를 검사하고 
        # 각 노드들의 체인을 검사하여 해당 노드의 체인의 길이가 더 길고, 유효한 체인이 검증되면
        neighbours = self.nodes
        # 해당 블록체인 네트워크에 등록된 다른 노드들
        new_chain = None
        # 업데이트될 체인
        # 처음에는 나의 체인이 제일 최신 체인으로 생각하여 None으로 초기화
        print(neighbours)
        for node in neighbours:
            max_length = len(self.chain) 
            # Our chain length 
            # 각 다른 노드들의 체인을 비교해가며 다른 노드의 체인의 길이가 더 길고,
            # 그 노드의 체인이 유효하다면 해당 노드의 체인으로 업데이트한뒤, 응답으로 True를 return
            tmp_url = 'http://' + str(node) + ':5000/chain'
            # 다른 노드들을 순차적으로 server파일에 있는 함수를 호출하여 해당 노드의 체인을 검사 것이며, 
            # 체인을 응답받는 url
            response = requests.get(tmp_url)
            # 해당 노드의 체인의 길이를 응답받음.
            if response.status_code == 200:
                # 응답이 정상적으로 수행되었을 시, 조건문 진입
                length = response.json()['length']
                # 응답받은 json형식의 출력에서 해당 노드의 체인 길이를 length 지역 변수에 할당.
                chain = response.json()['chain']
                # 응답받은 json형식의 출력에서 해당 노드의 체인을 지역 변수에 할당
                if length > max_length and self.valid_chain(chain):
                    # 만약 검사하는 노드의 체인 길이가 가장 최신의 체인이여서 해당 체인의 길이가 함수를 수행하는 노드의 
                    # 체인 길이보다 길어진 경우, 그리고 해당 노드의 체인이 유효한 경우
                    max_length = length
                    # 가장 긴 길이를 해당 길이로 업데이트함.
                    new_chain = chain
                    # 해당 체인으로 업데이트할 체인에 할당.
                    continue
                    # new_chain이 바뀌었다면 다시 반복문으로 돌아감.
            if new_chain:
                # 최종적으로 나의 체인의 길이가 가장 긴 최신 체인을 new_chain에 할당한 경우
                self.chain = new_chain
                # new_chain의 체인을 나의 체인으로 업데이트함.
                return True
                # 해당 체인으로 대체되었으므로 True를 반환.
        return False
            # 만약 나의 체인이 가장 최신이였어서 new_chain이 None으로 남게된 경우
            # 나의 체인은 가장 최신의 체인으로 인증된 것이므로 False를 반환.

    # directly access from class, share! not individual instance use it
    @staticmethod
    # 위의 staticmethod는 blockchain이라는 클래스 밖의 전역에서도 해당 함수를 사용할 수 있도록 정의하기위해서 
    # 사용한 것이다.
    def hash(block):
        block_serialized = json.dumps(block, sort_keys=True)
        return hashlib.sha256(block_serialized.encode()).hexdigest()
        # sha256 : 단방향 암호화 해시함수, 64자리(256bit) 암호화 문자열로 출력해준다.
    @property
    # 데코레이션 property : 해당 데코레이션의 함수는 자동으로 set과 get의 속성을 부여받는 객체가 된다.
    # 즉, 어떤 값을 출력할 때는 get함수, 어떤 값을 입력할 때는 set함수가 사용된다.
    def last_block(self):
        # 마지막 블록에 대한 객체 생성
        return self.chain[-1]
        # 체인의 마지막으로 넣어진 블록을 출력.
    def pow(self, last_proof):
        # 블록을 마이닝할 노드는 반드시 해당 노드가 마이닝할 능력이 됨을 증명해야한다. 
        # 즉, 이에 대한 증명방식이 필요한데 이중하나가 pow(작업증명방식)이다.
        # pow(작업증명방식)은 마이닝을 요청후 해당 마이닝 노드에서 임의의 값들로 컴퓨터 자원을 이용하여 
        # 해당 블록 체인 네트워크에서 문제내는 어떠한 해시값을 추리할때, 해당 해시값을 맞추면
        # 해당 노드가 블록을 생성할 수 있다는 것을 증명했다는것으로 생각하여 해당 노드는 pow을 통과
        # 마이닝할 수 있게되는 것이다.
        proof = 0
        # 여기서 proof는 논스로 pow과정중엣 pow를 만족시키기 위해 계속 값이 올라간다. 
        while self.valid_proof(last_proof, proof) is False:
            proof += 1
        return proof
    @staticmethod
    def valid_proof(last_proof, proof):
        # 고정된 블록의 해시 입력값 + 논스값을 입력하여 pow증명을 해내가는 과정의 함수
        guess = str(last_proof + proof).encode()
        # pow을 하는 노드는 먼저 블록의 해시 입력값 + 논스값을 문자열로 인코딩한다.
        guess_hash = hashlib.sha256(guess).hexdigest()
        # 위에서 인코딩한 문자열 값을 sha256해시함수에 입력값으로 입력하여 64자리 문자열을 입력받고 다시 hexdigest로
        # 해당 64자리 문자열을 16진수로 변환하여 추측pow값을 추출한다.
        return guess_hash[:4] == "0000" 
    # 추측한 64자리가 만약 마지막 4자리가 0000이 되었을때,  
