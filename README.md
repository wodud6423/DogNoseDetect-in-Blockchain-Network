# Dog Nose-Print Inquiry Service From Distributed DB Using Blockchain

<!--배지-->
![MIT License][license-shield] ![Repository Size][repository-size-shield] ![Issue Closed][issue-closed-shield]

<!--프로젝트 대문 이미지-->
![Project Title](img/projecttitle.png)

<!--프로젝트 버튼-->
 [![Readme in English][readme-eng-shield]][readme-eng-url] [![View Demo][view-demo-shield]][view-demo-url] [![Report bug][report-bug-shield]][report-bug-url] [![Request feature][request-feature-shield]][request-feature-url]

<!--목차-->
# Table of Contents
- [[1] About the Project](#1-about-the-project)
  - [Features](#features)
  - [Technologies](#technologies)
- [[2] Getting Started](#2-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Configuration](#configuration)
- [[3] Usage](#3-usage)
- [[4] Contribution](#4-contribution)
- [[5] Acknowledgement](#5-acknowledgement)
- [[6] Contact](#6-contact)
- [[7] License](#7-license)



# [1] About the Project
## 🦮블록체인을 이용한 분산형 DB로부터의 강아지 비문 조회 서비스

### 1. 연구 배경 및 필요성🐕‍🦺


- 반려동물 시장 성장: 한국농촌경제연구원에 따르면, 2015년 1조 9천억 원 규모였던 국내 반려동물 시장이 2023년에는 4조 5,786억 원에 이르렀고, 2027년에는 6조 원 규모까지 성장할 것으로 전망됩니다.
![Project Title](img/선정이유사진01.png)
<연간 유기 및 유실 동물 개체수 증가 추이!>

![Project Title](img/선정이유사진02.png)
<서울시 유기견 피해 현황>

- 사회적 문제: 반려동물 인구 증가에 따라 불법 품종 개량, 유기 동물 증가 등의 사회적 문제가 대두되고 있습니다.

### 2. 기존 해결방안의 한계🐕‍🦺
- RFID 사용의 문제점: 무선식별 장치(RFID)를 삽입 또는 부착하는 방법은 큰 비용이 들며 낮은 등록률을 보이고 있습니다.
- 비문(코 무늬) 활용: 사람의 지문처럼 강아지의 비문은 고유한 특징을 가지고 있어 개체 구별에 효과적입니다.

### 3. 제안하는 시스템🐕‍🦺
- 블록체인 기반 분산형 DB: 블록체인의 위변조 방지 기능과 분산형 데이터베이스를 결합하여, 안전하고 투명한 데이터 저장 및 조회 시스템을 구축합니다.
- 비문 인식 및 등록: 강아지의 비문을 인식하여 개체의 정보와 소유주를 등록하고, 이를 통해 분양 및 추적 시스템을 구현합니다.

### 4. 시스템의 장점🐕‍🦺
- 보안성과 투명성: 블록체인의 특성을 이용하여 데이터의 투명성과 보안성을 높입니다.
- 분산 처리: 분산형 데이터베이스 구조를 통해 네트워크 처리 속도를 향상시킵니다.
- 효율적인 유기견 문제 해결: 비문을 이용한 개체 식별 시스템으로 유기견 문제를 효과적으로 해결할 수 있습니다.

### 5. 연구 의의 및 실제 구현🐕‍🦺
- 실제 애플리케이션 구현: 이론적인 제시를 넘어 실제로 블록체인을 이용한 분산형 DB와 강아지 비문 조회 서비스를 실행할 수 있는 애플리케이션을 구현하였습니다.
- 사회적 기여: 반려동물 문제 해결에 기여하고, 보다 나은 반려동물 문화를 구축하는 데 의의를 둡니다.

## Features
* 강조하고 싶은 **주요 기능**이나 **차별성 있는 특징**을 적으세요.*
- 최고 멋진 **README**를 쉽게 작성할 수 있도록 *이텔릭체*로 된 **가이드**를 제공
- 뱃지로 **언어 옵션**을 제공

## Technologies
***언어, 프레임워크, 주요 라이브러리**를 **버전**과 함께 나열하세요.*

- [Maven](https://maven.apache.org/) 3.6.3
- [MySQL](https://www.mysql.com/) 8.0
- [Spring](https://spring.io/) 2.4.3



# [2] Getting Started
*만약 운영체제에 따라 프로그램을 다르게 동작시켜야한다면, 운영체제별로 동작 방법을 설명하세요*

## Prerequisites
*프로젝트를 동작시키기 위해 필요한 소프트웨어와 라이브러리를 나열하고 어떻게 다운받을 수 있는지 설명하세요.*

- [OpenWeather API key](https://openweathermap.org/) (무료)
- npm
```bash
npm install npm@latest -g
```

## Installation
*어떻게 이 프로젝트의 소스코드를 다운받을 수 있는지 설명하세요.*
1. Repository 클론
```bash
git clone https://github.com/your-username/project-repository
```
2. NPM packages 설치
```bash
npm install
```

## Configuration
*코드의 어느 부분을 채우거나 수정해야하는지 설명하세요.*
- `config.js`에 Openweather API key를 입력
```bash
const API_KEY = "<Your API key>";
```



# [3] Usage
***스크린샷, 코드** 등을 통해 **사용 방법**과 **사용 예제**를 보여주세요. 사용 예제별로 h2 헤더로 나누어 설명할 수 있습니다.*

![usage](img/usage.png)

```java
// 몇 개의 API 사용 예제를 코드와 함께 보여주세요.
```



# [4] Contribution
기여해주신 모든 분들께 대단히 감사드립니다.[`contributing guide`][contribution-url]를 참고해주세요.
이 프로젝트의 기여하신 분들을 소개합니다! 🙆‍♀️
*이모티콘 쓰는 것을 좋아한다면, 버그 수정에 🐞, 아이디어 제공에 💡, 새로운 기능 구현에 ✨를 사용할 수 있습니다.*
- 🐞 [dev-ujin](https://github.com/dev-ujin): 메인페이지 버그 수정



# [5] Acknowledgement
***유사한 프로젝트의 레포지토리** 혹은 **블로그 포스트** 등 프로젝트 구현에 영감을 준 출처에 대해 링크를 나열하세요.*

- [Readme Template - Embedded Artistry](https://embeddedartistry.com/blog/2017/11/30/embedded-artistry-readme-template/)
- [How to write a kickass Readme - James.Scott](https://dev.to/scottydocs/how-to-write-a-kickass-readme-5af9)
- [Best-README-Template - othneildrew](https://github.com/othneildrew/Best-README-Template#prerequisites)
- [Img Shields](https://shields.io/)
- [Github Pages](https://pages.github.com/)



# [6] Contact
- 📧 dev.ujin518@gmail.com
- 📋 [https://dev-ujin.github.io/contact](https://dev-ujin.github.io/contact)



# [7] License
MIT 라이센스
라이센스에 대한 정보는 [`LICENSE`][license-url]에 있습니다.



<!--Url for Badges-->
[license-shield]: https://img.shields.io/github/license/dev-ujin/readme-template?labelColor=D8D8D8&color=04B4AE
[repository-size-shield]: https://img.shields.io/github/repo-size/dev-ujin/readme-template?labelColor=D8D8D8&color=BE81F7
[issue-closed-shield]: https://img.shields.io/github/issues-closed/dev-ujin/readme-template?labelColor=D8D8D8&color=FE9A2E

<!--Url for Buttons-->
[readme-eng-shield]: https://img.shields.io/badge/-readme%20in%20english-2E2E2E?style=for-the-badge
[view-demo-shield]: https://img.shields.io/badge/-%F0%9F%98%8E%20view%20demo-F3F781?style=for-the-badge
[view-demo-url]: https://dev-ujin.github.io
[report-bug-shield]: https://img.shields.io/badge/-%F0%9F%90%9E%20report%20bug-F5A9A9?style=for-the-badge
[report-bug-url]: https://github.com/dev-ujin/readme-template/issues
[request-feature-shield]: https://img.shields.io/badge/-%E2%9C%A8%20request%20feature-A9D0F5?style=for-the-badge
[request-feature-url]: https://github.com/dev-ujin/readme-template/issues

<!--URLS-->
[license-url]: LICENSE.md
[contribution-url]: CONTRIBUTION.md
[readme-eng-url]: ../README.md
