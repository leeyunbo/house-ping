**Open API 활용가이드**


![](Aspose.Words.b858af45-a8d2-42a8-bcfd-280cfe2f97c4.001.png)![](Aspose.Words.b858af45-a8d2-42a8-bcfd-280cfe2f97c4.002.png)



![](Aspose.Words.b858af45-a8d2-42a8-bcfd-280cfe2f97c4.003.png)

**공공데이터 오픈API 활용가이드**
















목 차
![](Aspose.Words.b858af45-a8d2-42a8-bcfd-280cfe2f97c4.004.png)<a name="_toc277943240"></a><a name="_toc336008741"></a><a name="_toc366165668"></a><a name="_toc208129623"></a>

[**1. 서비스 명세	**3****](#_toc14797317)

[**1.1 분양임대공고문 조회 서비스**	3](#_toc14797318)

[가. API 서비스 개요	3](#_toc14797319)

[나. 상세기능 목록	4](#_toc14797320)

[다. 상세기능내역	4](#_toc14797321)

[1) \[분양임대공고문 조회\] 상세기능명세	4](#_toc14797322)

[**2. OpenAPI 에러 코드정리	**7****](#_toc14797323)





<a name="_toc14797317"></a>**1. 서비스 명세**

<a name="_toc14797318"></a>**1.1 분양임대공고문 조회 서비스**

<a name="_toc14797319"></a>가. API 서비스 개요

|**API 서비스 정보**|**API명(영문)**|lhLeaseNoticeInfo1|||
| :-: | :-: | :- | :- | :- |
||**API명(국문)**|분양임대공고문 조회 서비스|||
||**API 설명**|광역시도 코드, 공고유형코드, 공고상태코드, 공고명으로 분양.임대공고문을 이용하여 공고유형, 공고명, 지역, 공고게시일 정보조회 서비스|||
|<p>**API 서비스**</p><p>**보안적용**</p><p>**기술 수준**</p>|**서비스 인증/권한**|<p>[O] serviceKey    [ ] 인증서 (GPKI/NPKI)</p><p>[ ] Basic (ID/PW)  [ ] 없음</p>|||
||<p>**메시지 레벨**</p><p>**암호화**</p>|[ ] 전자서명   [ ] 암호화   [O] 없음|||
||**전송 레벨 암호화**|[ ] SSL   [O] 없음|||
||**인터페이스 표준**|<p>[ ] SOAP 1.2</p><p>(RPC-Encoded, Document Literal, Document Literal Wrapped)</p><p>[O] REST (GET)</p><p>[ ] RSS 1.0   [ ] RSS 2.0   [ ] Atom 1.0   [ ] 기타</p>|||
||<p>**교환 데이터 표준**</p><p>**(중복선택가능)**</p>|[] XML   [O] JSON   [ ] MIME   [ ] MTOM|||
|<p>**API 서비스**</p><p>**배포정보**</p>|**서비스 URL**|http://apis.data.go.kr/B552555/lhLeaseNoticeInfo1|||
||<p>**서비스 명세 URL**</p><p>**(WSDL 또는 WADL)**</p>|[http://apis.data.go.kr/B552555/lhLeaseNoticeInfo1?_wadl&type=json](http://apis.data.go.kr/B552555/lhLeaseNoticeInfo?_wadl&type=json) |||
||**서비스 버전**|1\.0|||
||**서비스 시작일**|2019-08-01|**서비스 배포일**|2019-08-01|
||**서비스 이력**|2019-08-01 : 서비스 시작|||
||**메시지 교환유형**|<p>[O] Request-Response   [ ] Publish-Subscribe</p><p>[ ] Fire-and-Forgot   [ ] Notification</p>|||
||**서비스 제공자**|  |||
||**데이터 갱신주기**|수시|||

<a name="_toc14797320"></a>나. 상세기능 목록

|**번호**|**API명(국문)**|**상세기능명(영문)**|**상세기능명(국문)**|
| :-: | :-: | :-: | :-: |
|1|분양임대공고문 조회 서비스|LhLeaseNoticeInfo1|분양임대공고문 조회|

<a name="_toc14797321"></a>다. 상세기능내역

<a name="_toc14797322"></a>1) [분양임대공고문 조회] 상세기능명세

a) 상세기능정보

|**상세기능 번호**|1|**상세기능 유형**|조회 (목록)|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|분양임대공고문 조회|||
|**상세기능 설명**|광역시도 코드, 공고유형코드, 공고상태코드, 공고명으로 분양.임대공고문을 이용하여 공고유형, 공고명, 지역, 공고게시일 정보를 조회하는 목록 조회 기능|||
|**Call Back URL**|http://apis.data.go.kr/B552555/lhLeaseNoticeInfo1/lhLeaseNoticeInfo1|||
|**최대 메시지 사이즈**|[4000] byte|||
|**평균 응답 시간**|[500] ms|**초당 최대 트랙잭션**|[30] tps|

b) 요청 메시지 명세 

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|serviceKey|인증키|100|1|<p>인증키</p><p>(URL Encode)</p>|공공데이터포털에서 발급받은 인증키|
|PG\_SZ|한 페이지 결과 수|4|1|10|한 페이지 결과 수|
|PAGE|페이지 번호|4|1|1|페이지 번호|
|PAN\_NM|공고명|50|0|대전|공고명으로 조회|
|UPP\_AIS\_TP\_CD|공고유형코드|10|0|01|공고유형코드|
|CNP\_CD|지역코드|10|0|11|지역코드|
|PAN\_SS|공고상태코드|10|0|공고중|공고상태코드|
|PAN\_NT\_ST\_DT|공고게시일|8|1|20190723||
|CLSG\_DT|공고마감일|8|1|20190822||
※ 항목구분 : 필수(1), 옵션(0) 



공고유형코드(UPP\_AIS\_TP\_CD)

|**코드**|**설명**|
| :-: | :-: |
|01|토지|
|05|분양주택|
|06|임대주택|
|13|주거복지|
|22|상가|
|39|신혼희망타운|

지역코드(CNP\_CD)

|**코드**|**설명**|
| :-: | :-: |
|11|서울특별시|
|26|부산광역시|
|27|대구광역시|
|28|인천광역시|
|29|광주광역시|
|30|대전광역시|
|31|울산광역시|
|36110|세종특별자치시|
|41|경기도|
|42|강원도|
|43|충청북도|
|44|충청남도|
|52|전북특별자치도|
|46|전라남도|
|47|경상북도|
|48|경상남도|
|50|제주특별자치도|

공고상태코드(PAN\_SS)

|**코드**|**설명**|
| :-: | :-: |
|공고중|공고중|
|접수중|접수중|
|접수마감|접수마감|
|상담요청|상담요청|
|정정공고중|정정공고중|

c) 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|SS\_CODE|결과코드|2|1|Y|결과코드|
|RS\_DTTM|출력일시|20|1|20241010045535|출력일시|
|RNUM|순번|10|1|1|데이터순번|
|UPP\_AIS\_TP\_NM|공고유형명|10|1|임대주택|공고유형명|
|AIS\_TP\_CD\_NM|공고세부유형명|10|1|행복주택|공고세부유형명|
|PAN\_NM|공고명|100|1|행복도시3-1M5블록 10년 공공임대주택리츠 |데이터 조회를 위한 키값|
|CNP\_CD\_NM|지역명|50|1|전국|지역명|
|PAN\_SS|공고상태|10|1|공고중|공고상태|
|ALL\_CNT|전체조회건수|10|1|21710|전체조회건수|
|DTL\_URL|공고상세URL|100|1|https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_0050.xfdl&gv\_menuId=1010203&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:02,PAN\_ID:0000059133,LCC:Y|공고상세URL|

※ 항목구분 : 필수(1), 옵션(0)

d) 요청/응답 메시지 예제

|**요청메시지**|
| :-: |
|http://apis.data.go.kr/B552555/lhLeaseNoticeInfo1/lhLeaseNoticeInfo1?serviceKey=인증키(URL Encode)&PG\_SZ=10&PAGE=1|
|**응답메시지**|
|[{"dsSch":[{"PAN\_ED\_DT":"20200508","PAGE":"1","PAN\_ST\_DT":"20200308","PG\_SZ":"10"}]},{"resHeader":[{"SS\_CODE":"Y","RS\_DTTM":"20200508031340"}],"dsList":[{"PAN\_SS":"접수중","RNUM":"1","PAN\_NT\_ST\_DT":"2020.04.27","AIS\_TP\_CD":"05","SPL\_INF\_TP\_CD":"050","CNP\_CD\_NM":"경기도","PAN\_ID":"0000059250","UPP\_AIS\_TP\_NM":"분양주택","AIS\_TP\_CD\_NM":"분양주택","CLSG\_DT":"2020.04.28","PAN\_DT":"20200427","UPP\_AIS\_TP\_CD":"05","PAN\_NM":"지역우선100호이상오피스텔 테스트1","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_0050.xfdl&gv\_menuId=1010202&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:02,PAN\_ID:0000059250,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"02"},{"PAN\_SS":"접수중","RNUM":"2","PAN\_NT\_ST\_DT":"2020.04.27","AIS\_TP\_CD":"05","SPL\_INF\_TP\_CD":"050","CNP\_CD\_NM":"경기도","PAN\_ID":"0000059252","UPP\_AIS\_TP\_NM":"분양주택","AIS\_TP\_CD\_NM":"분양주택","CLSG\_DT":"2020.04.27","PAN\_DT":"20200427","UPP\_AIS\_TP\_CD":"05","PAN\_NM":"지역우선미적용오피스텔 테스트1","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_0050.xfdl&gv\_menuId=1010202&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:02,PAN\_ID:0000059252,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"02"},{"PAN\_SS":"접수중","RNUM":"3","PAN\_NT\_ST\_DT":"2020.04.27","AIS\_TP\_CD":"05","SPL\_INF\_TP\_CD":"050","CNP\_CD\_NM":"경기도","PAN\_ID":"0000059253","UPP\_AIS\_TP\_NM":"분양주택","AIS\_TP\_CD\_NM":"분양주택","CLSG\_DT":"2020.04.27","PAN\_DT":"20200427","UPP\_AIS\_TP\_CD":"05","PAN\_NM":"지역우선100호미만오피스텔테스트2","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_0050.xfdl&gv\_menuId=1010202&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:02,PAN\_ID:0000059253,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"02"},{"PAN\_SS":"접수중","RNUM":"4","PAN\_NT\_ST\_DT":"2020.04.27","AIS\_TP\_CD":"05","SPL\_INF\_TP\_CD":"050","CNP\_CD\_NM":"경기도","PAN\_ID":"0000059254","UPP\_AIS\_TP\_NM":"분양주택","AIS\_TP\_CD\_NM":"분양주택","CLSG\_DT":"2020.04.27","PAN\_DT":"20200427","UPP\_AIS\_TP\_CD":"05","PAN\_NM":"지역우선100호이상오피스텔테스트2","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_0050.xfdl&gv\_menuId=1010202&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:02,PAN\_ID:0000059254,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"02"},{"PAN\_SS":"접수마감","RNUM":"5","PAN\_NT\_ST\_DT":"2020.04.24","AIS\_TP\_CD":"05","SPL\_INF\_TP\_CD":"050","CNP\_CD\_NM":"경기도","PAN\_ID":"0000059247","UPP\_AIS\_TP\_NM":"분양주택","AIS\_TP\_CD\_NM":"분양주택","CLSG\_DT":"2020.04.24","PAN\_DT":"20200424","UPP\_AIS\_TP\_CD":"05","PAN\_NM":"지역우선 오피스텔 테스트100호 이상","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_0050.xfdl&gv\_menuId=1010202&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:02,PAN\_ID:0000059247,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"02"},{"PAN\_SS":"접수마감","RNUM":"6","PAN\_NT\_ST\_DT":"2020.04.24","AIS\_TP\_CD":"05","SPL\_INF\_TP\_CD":"050","CNP\_CD\_NM":"경기도","PAN\_ID":"0000059248","UPP\_AIS\_TP\_NM":"분양주택","AIS\_TP\_CD\_NM":"분양주택","CLSG\_DT":"2020.04.24","PAN\_DT":"20200424","UPP\_AIS\_TP\_CD":"05","PAN\_NM":"지역우선 오피스텔 테스트 100호미만","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_0050.xfdl&gv\_menuId=1010202&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:02,PAN\_ID:0000059248,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"02"},{"PAN\_SS":"접수마감","RNUM":"7","PAN\_NT\_ST\_DT":"2020.04.24","AIS\_TP\_CD":"05","SPL\_INF\_TP\_CD":"050","CNP\_CD\_NM":"경기도","PAN\_ID":"0000059249","UPP\_AIS\_TP\_NM":"분양주택","AIS\_TP\_CD\_NM":"분양주택","CLSG\_DT":"2020.04.24","PAN\_DT":"20200424","UPP\_AIS\_TP\_CD":"05","PAN\_NM":"지역우선 미적용 오피스텔 테스트","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_0050.xfdl&gv\_menuId=1010202&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:02,PAN\_ID:0000059249,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"02"},{"PAN\_SS":"접수중","RNUM":"8","PAN\_NT\_ST\_DT":"2020.04.24","AIS\_TP\_CD":"42","SPL\_INF\_TP\_CD":"390","CNP\_CD\_NM":"전국","PAN\_ID":"2015122300004663","UPP\_AIS\_TP\_NM":"공공분양(신혼희망)","AIS\_TP\_CD\_NM":"행복주택(신혼희망)","CLSG\_DT":"2020.05.06","PAN\_DT":"20200424","UPP\_AIS\_TP\_CD":"39","PAN\_NM":"[신혼희망타운(행복최초)]광주쌍촌 행복주택 최초 입주자 및 예비자 모집","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_9910.xfdl&gv\_menuId=1010206&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:03,PAN\_ID:2015122300004663,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"03"},{"PAN\_SS":"접수중","RNUM":"9","PAN\_NT\_ST\_DT":"2020.04.24","AIS\_TP\_CD":"42","SPL\_INF\_TP\_CD":"390","CNP\_CD\_NM":"전국","PAN\_ID":"2015122300005135","UPP\_AIS\_TP\_NM":"공공분양(신혼희망)","AIS\_TP\_CD\_NM":"행복주택(신혼희망)","CLSG\_DT":"2020.05.06","PAN\_DT":"20200424","UPP\_AIS\_TP\_CD":"39","PAN\_NM":"[신혼희망타운(행복동시예비)-입주자격완화3단계]광주전남 신혼희망타운 행복주택 예비 동시추첨지구 입주자모집","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_9910.xfdl&gv\_menuId=1010206&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:03,PAN\_ID:2015122300005135,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"03"},{"PAN\_SS":"접수중","RNUM":"10","PAN\_NT\_ST\_DT":"2020.04.24","AIS\_TP\_CD":"42","SPL\_INF\_TP\_CD":"390","CNP\_CD\_NM":"전국","PAN\_ID":"2015122300005134","UPP\_AIS\_TP\_NM":"공공분양(신혼희망)","AIS\_TP\_CD\_NM":"행복주택(신혼희망)","CLSG\_DT":"2020.05.06","PAN\_DT":"20200424","UPP\_AIS\_TP\_CD":"39","PAN\_NM":"[신혼희망타운(행복예비)-입주자격완화3단계]광주쌍촌 행복주택 예비 입주자 모집","ALL\_CNT":"20","DTL\_URL":"https://apply.lh.or.kr/LH/index.html?gv\_url=SIL::CLCC\_SIL\_9910.xfdl&gv\_menuId=1010206&gv\_param=CCR\_CNNT\_SYS\_DS\_CD:03,PAN\_ID:2015122300005134,LCC:Y","CCR\_CNNT\_SYS\_DS\_CD":"03"}]}]|












<a name="_toc14797323"></a>**2. OpenAPI 에러 코드정리**

|<a name="_toc502162921"></a>**에러코드**|<a name="_toc502162922"></a>**에러메시지**|<a name="_toc502162923"></a>**설명**|
| :-: | :-: | :-: |
|0|NORMAL\_CODE|정상|
|1|APPLICATION\_ERROR|어플리케이션 에러|
|2|DB\_ERROR|데이터베이스 에러|
|3|NODATA\_ERROR|데이터없음 에러|
|4|HTTP\_ERROR|HTTP 에러|
|5|SERVICETIMEOUT\_ERROR|서비스 연결실패 에러|
|10|INVALID\_REQUEST\_PARAMETER\_ERROR|잘못된 요청 파라메터 에러|
|11|NO\_MANDATORY\_REQUEST\_PARAMETERS\_ERROR|필수요청 파라메터가 없음|
|12|NO\_OPENAPI\_SERVICE\_ERROR|<p>해당 오픈API서비스가 없거나</p><p>폐기됨</p>|
|20|SERVICE\_ACCESS\_DENIED\_ERROR|서비스 접근거부|
|21|TEMPORARILY\_DISABLE\_THE\_SERVICEKEY\_ERROR|<p>일시적으로 사용할 수 없는</p><p>서비스 키</p>|
|22|LIMITED\_NUMBER\_OF\_SERVICE\_REQUESTS\_EXCEEDS\_ERROR|서비스 요청제한횟수 초과에러|
|30|SERVICE\_KEY\_IS\_NOT\_REGISTERED\_ERROR|등록되지 않은 서비스키|
|31|DEADLINE\_HAS\_EXPIRED\_ERROR|기한만료된 서비스키|
|32|UNREGISTERED\_IP\_ERROR|등록되지 않은 IP|
|33|UNSIGNED\_CALL\_ERROR|서명되지 않은 호출|
|99|UNKNOWN\_ERROR|기타에러|

\- 9 -
