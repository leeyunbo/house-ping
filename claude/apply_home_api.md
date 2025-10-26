**Open API 활용가이드**


![](Aspose.Words.d237757a-8d45-44d2-af65-2d9ba99a0216.001.png)![](Aspose.Words.d237757a-8d45-44d2-af65-2d9ba99a0216.002.png)


![](Aspose.Words.d237757a-8d45-44d2-af65-2d9ba99a0216.003.png) **청약홈 분양정보 조회 서비스**

**Open API 활용가이드**














![](Aspose.Words.d237757a-8d45-44d2-af65-2d9ba99a0216.004.png "logo_new")

` `- 56 - 

목 차

![](Aspose.Words.d237757a-8d45-44d2-af65-2d9ba99a0216.005.png)<a name="_toc277943240"></a><a name="_toc336008741"></a><a name="_toc366165668"></a><a name="_toc208129623"></a>

[**1. 서비스 명세**	](#_toc502763083)**1**

[**1.1 청약홈 분양정보 조회 서비스**	](#_toc502763084)1

[가. API 서비스 개요	](#_toc502763085)1

[나. 코드명세	](#_toc502763086)2

[다. 상세기능 목록	](#_toc502763086)5

[라. 상세기능내역	](#_toc502763087)5

\1) APT 분양정보 상세조회 상세기능명세 [	](#_toc502763087)6

[2) 오피스텔/도시형/민간임대/생활숙박시설 분양정보 상세조회 상세기능명세\[	\](#_toc502763087)](#_toc502763089)14

[3) APT 잔여세대 분양정보 상세조회 상세기능명세	](#_toc502763089)19

[4) APT 분양정보 주택형별 상세조회 상세기능명세	](#_toc502763089)24

[5) 오피스텔/도시형/민간임대/생활숙박시설 분양정보 주택형별 상세조회 상세기능명세	](#_toc502763089)31

[6) APT 잔여세대 분양정보 주택형별 상세조회 상세기능명세	](#_toc502763089)36

[7) 공공지원 민간임대 분양정보 상세조회 상세기능명세	](#_toc502763089)39

[8) 공공지원 민간임대 분양정보 주택형별 상세조회 상세기능명세	](#_toc502763089)44

[9) 임의공급 분양정보 상세조회 상세기능명세	](#_toc502763089)47

[10) 임의공급 분양정보 주택형별 상세조회 상세기능명세	](#_toc502763089)52

[**2. OpenAPI 에러 코드정리**	](#_toc502763090)**56**





<a name="_toc502763083"></a><a name="_toc342666055"></a><a name="_toc372638639"></a>**1. 서비스 명세**

<a name="_toc502763084"></a>**1.1 청약홈 분양정보 조회 서비스**
1  ### <a name="_toc502763085"></a> API 서비스 개요

|**API 서비스 정보**|**API명(영문)**|ApplyhomeInfoDetailSvc|||
| :-: | :-: | :- | :- | :- |
||**API명(국문)**|청약홈 분양정보 조회 서비스|||
||**API 설명**|청약홈 분양정보를 조회할 수 있는 서비스로 APT(민간사전청약 및 신혼희망타운 포함), 오피스텔/도시형/ 민간임대/생활숙박시설, APT 잔여세대, 공공지원 민간임대, 임의공급 별로 분양정보 및 주택형별 분양정보를 확인할 수 있다. |||
|<p>**API 서비스**</p><p>**보안적용**</p><p>**기술 수준**</p>|**서비스 인증/권한**|<p>[O] ServiceKey    [ ] 인증서 (GPKI/NPKI)</p><p>[ ] Basic (ID/PW)  [ ] 없음</p>|||
||<p>**메시지 레벨**</p><p>**암호화**</p>|[ ] 전자서명   [ ] 암호화   [O] 없음|||
||**전송 레벨 암호화**|[ ] SSL   [O] 없음|||
||**인터페이스 표준**|<p>[ ] SOAP 1.2(RPC-Encoded, Document Literal, Document Literal Wrapped)</p><p>[O] REST (GET)</p><p>[ ] RSS 1.0   [ ] RSS 2.0   [ ] Atom 1.0   [ ] 기타</p>|||
||<p>**교환 데이터 표준**</p><p>**(중복선택가능)**</p>|[O] XML   [O] JSON   [ ] MIME   [ ] MTOM|||
|<p>**API 서비스**</p><p>**배포정보**</p>|**서비스 URL**|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/상세서비스명?page=1&perPage=10&serviceKey=서비스키|||
||<p>**서비스 명세 URL**</p><p>**(WSDL 또는WADL)**</p>|https://infuser.odcloud.kr/api/stages/37000/api-docs?165458057581|||
||**서비스 버전**|1\.0|||
||**서비스 시작일**|2022-01-24|**서비스 배포일**|2022-01-24|
||**서비스 이력**|2022-01 서비스 오픈|||
||**메시지 교환유형**|<p>[O] Request-Response   [ ] Publish-Subscribe</p><p>[ ] Fire-and-Forgot   [ ] Notification</p>|||
||**서비스 제공자**|한국부동산원 ICT센터 ICT전략부 / 053-663-8466|||
||**데이터 갱신주기**|매일|||
1  ### 코드명세
   1  #### 공급지역 코드

|**순번**|**지역구분**|**코드(SUBSCRPT\_AREA\_CODE)**|
| :-: | :-: | :-: |
|1|서울|100|
|2|강원|200|
|3|대전|300|
|4|충남|312|
|5|세종|338|
|6|충북|360|
|7|인천|400|
|8|경기|410|
|9|광주|500|
|10|전남|513|
|11|전북|560|
|12|부산|600|
|13|경남|621|
|14|울산|680|
|15|제주|690|
|16|대구|700|
|17|경북|712|

※ 전국 또는 광역권(ex. (서울, 경기, 인천), (대구, 경북) 등) 인 경우 지역코드 값이 존재하지 않습니다.
1  #### 주택구분 코드(APT)

|**순번**|**주택구분**|**코드(HOUSE\_SECD)**|
| :-: | :-: | :-: |
|1|APT|01|
|2|민간사전청약|09|
|3|신혼희망타운|10|

1  #### 주택상세구분 코드(APT)

|**순번**|**주택상세구분**|**코드(HOUSE\_DTL\_SECD)**|
| :-: | :-: | :-: |
|1|민영|01|
|2|국민|03|

1  #### 분양구분 코드 (APT)

|**순번**|**층별구분**|**코드(RENT\_SECD)**|
| :-: | :-: | :-: |
|1|분양주택|0|
|2|분양전환 가능임대|1|

1  #### 조정대상지역 코드 (APT)

|**순번**|**층별구분**|**코드(MDAT\_TRGET\_AREA\_SECD)**|
| :-: | :-: | :-: |
|1|` `과열지역|Y|
|2|미대상주택|N|


1  #### 주택상세구분 코드(오피스텔/도시형/생활숙박시설/민간임대, 검색조건에 사용)

|**순번**|**주택구분**|**코드**|
| :-: | :-: | :-: |
|1|도시형생활주택|0201|
|2|오피스텔|0202|
|3|민간임대|0203|
|4|생활형숙박시설|0204|

1  #### 주택구분 코드(잔여세대)

|**순번**|**층별구분**|**코드**|
| :-: | :-: | :-: |
|1|무순위|04|
|2|불법행위 재공급|06|











1  ### 상세기능 목록

|**번호**|**API명(국문)**|**상세기능명(영문)**|**상세기능명(국문)**|
| :-: | :-: | :-: | :-: |
|1|청약홈 분양정보 조회 서비스|getAPTLttotPblancDetail|APT 분양정보 상세조회|
|2|청약홈 분양정보 조회 서비스|getUrbtyOfctlLttotPblancDetail|오피스텔/도시형/민간임대/생활숙박시설 분양정보 상세조회|
|3|청약홈 분양정보 조회 서비스|getRemndrLttotPblancDetail|APT 잔여세대 분양정보 상세조회|
|4|청약홈 분양정보 조회 서비스|getAPTLttotPblancMdl|APT 분양정보 주택형별 상세조회|
|5|청약홈 분양정보 조회 서비스|getUrbtyOfctlLttotPblancMdl|오피스텔/도시형/민간임대/생활숙박시설 분양정보 주택형별 상세조회|
|6|청약홈 분양정보 조회 서비스|getRemndrLttotPblancMdl|APT 잔여세대 주택형별 상세조회|
|7|청약홈 분양정보 조회 서비스|getPblPvtRentLttotPblancDetail|공공지원 민간임대 분양정보 상세조회|
|8|청약홈 분양정보 조회 서비스|getPblPvtRentLttotPblancMdl|공공지원 민간임대 분양정보 주택형별 상세조회|
|9|청약홈 분양정보 조회 서비스|getOPTLttotPblancDetail|임의공급 분양정보 상세조회|
|10|청약홈 분양정보 조회 서비스|getOPTLttotPblancMdl|임의공급 분양정보 주택형별 상세조회|


1  ### 상세기능내역
   1  #### APT 분양정보 상세조회 상세기능 명세
      1  ##### 상세기능 정보

|**상세기능 번호**|1|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|APT 분양정보 상세조회|||
|**상세기능 설명**|주택관리번호, 공고번호, 공급지역명, 모집공고일 값을 이용하여 APT분양정보의 상세정보를 제공|||

1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2022000248|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2022000248|(Equal) 공고번호|
|house\_secd|주택구분코드|2|0|(EQ) 01|(Equal) 주택구분코드|
|house\_dtl\_secd|주택상세구분코드|2|0|(EQ) 01|(Equal) 주택상세구분코드|
|subscrpt\_area\_code|공급지역코드|3|0|(EQ) 100|(Equal) 공급지역코드|
|subscrpt\_area\_code\_nm|공급지역명|500|0|(EQ) 서울|(Equal) 공급지역명|
|rcrit\_pblanc\_de|모집공고일|10|0|(LT)|(Little)                  < ‘YYYY-MM-DD’|
|||||<p>(LTE) </p><p>2022-05-31</p>|(Little or Equal)         <= ‘YYYY-MM-DD’|
|||||(GT)|(Greater)                  > ‘YYYY-MM-DD’|
|||||<p>(GTE) </p><p>2022-01-01</p>|(Greater or Equal)         >= ‘YYYY-MM-DD’|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	
1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|1|2022000248|주택관리번호|
|pblanc\_no|공고번호|40|1|2022000248|공고번호|
|house\_nm|주택명|200|0|창동 다우아트리체 주상복합 아파트|주택명|
|house\_secd|주택구분코드|2|0|01|주택구분코드|
|house\_secd\_nm|주택구분코드명|4000|0|APT|주택구분코드명|
|house\_dtl\_secd|주택상세구분코드|2|0|01|주택상세구분코드|
|house\_dtl\_secd\_nm|주택상세구분코드명|4000|0|민영|주택상세구분코드명|
|rent\_secd|분양구분코드|1|0|0|분양구분코드|
|rent\_secd\_nm|분양구분코드명|500|0|분양주택|분양구분코드명|
|subscrpt\_area\_code|공급지역코드|3|0|100|공급지역코드|
|subscrpt\_area\_code\_nm|공급지역명|500|0|서울|공급지역명|
|hssply\_zip|공급위치 우편번호|6|0|01400|공급위치 우편번호|
|hssply\_adres|공급위치|256|0|서울특별시 도봉구 창동 662-7번지 외 12필지|공급위치|
|tot\_suply\_hshldco|공급규모|10|0|89|공급규모|
|rcrit\_pblanc\_de|모집공고일|10|0|2022-05-12|모집공고일|
|nsprc\_nm|신문사|200|0|e대한경제|신문사|
|rcept\_bgnde|청약접수시작일|10|0|2022-05-23|청약접수시작일|
|rcept\_endde|청약접수종료일|10|0|2022-05-26|청약접수종료일|
|spsply\_rcept\_bgnde|특별공급 접수시작일|10|0|2022-05-23|특별공급 접수시작일|
|spsply\_rcept\_endde|특별공급 접수종료일|10|0|2022-05-23|특별공급 접수종료일|
|gnrl\_rnk1\_crsparea\_rcptde|1순위 해당지역 접수시작일|21|0|2022-05-24|1순위 해당지역 접수시작일|
|gnrl\_rnk1\_crsparea\_endde|1순위 해당지역 접수종료일|21|0|2022-05-24|1순위 해당지역 접수종료일|
|gnrl\_rnk1\_etc\_gg\_rcptde|1순위 경기지역 접수시작일|21|0|-|1순위 경기지역 접수시작일|
|gnrl\_rnk1\_etc\_gg\_endde|1순위 경기지역 접수종료일|21|0|-|1순위 경기지역 접수종료일|
|gnrl\_rnk1\_etc\_area\_rcptde|1순위 기타지역 접수시작일|21|0|2022-05-25|1순위 기타지역 접수시작일|
|gnrl\_rnk1\_etc\_area\_endde|1순위 기타지역 접수종료일|21|0|2022-05-25|1순위 기타지역 접수종료일|
|gnrl\_rnk2\_crsparea\_rcptde|2순위 해당지역 접수시작일|21|0|2022-05-26|2순위 해당지역 접수시작일|
|gnrl\_rnk2\_crsparea\_endde|2순위 해당지역 접수종료일|21|0|2022-05-26|2순위 해당지역 접수종료일|
|gnrl\_rnk2\_etc\_gg\_rcptde|2순위 경기지역 접수시작일|21|0|-|2순위 경기지역 접수시작일|
|gnrl\_rnk2\_etc\_gg\_endde|2순위 경기지역 접수종료일|21|0|-|2순위 경기지역 접수종료일|
|gnrl\_rnk2\_etc\_area\_rcptde|2순위 기타지역 접수시작일|21|0|2022-05-26|2순위 기타지역 접수시작일|
|gnrl\_rnk2\_etc\_area\_endde|2순위 기타지역 접수종료일|21|0|2022-05-26|2순위 기타지역 접수종료일|
|przwner\_presnatn\_de|당첨자발표일|10|0|2022-06-02|당첨자발표일|
|cntrct\_cncls\_bgnde|계약시작일|10|0|2022-06-13|계약시작일|
|cntrct\_cncls\_endde|계약종료일|10|0|2022-06-15|계약종료일|
|hmpg\_adres|홈페이지주소|256|0|http://www.dawartriche.com/|홈페이지주소|
|cnstrct\_entrps\_nm|건설업체명 (시공사)|200|0|강산건설㈜|건설업체명 (시공사)|
|mdhs\_telno|문의처|30|0|16684888|문의처|
|bsns\_mby\_nm|사업주체명 (시행사)|200|0|주식회사 하나자산신탁|사업주체명 (시행사)|
|mvn\_prearnge\_ym|입주예정월|6|0|202502|입주예정월|
|speclt\_rdn\_earth\_at|투기과열지구|1|0|Y|투기과열지구|
|mdat\_trget\_area\_secd|조정대상지역|1|0|Y|조정대상지역|
|parcprc\_uls\_at|분양가상한제|1|0|N|분양가상한제|
|imprmn\_bsns\_at|정비사업|1|0|N|정비사업|
|public\_house\_earth\_at|공공주택지구|1|0|N|공공주택지구|
|lrscl\_bldlnd\_at|대규모 택지개발지구|1|0|N|대규모 택지개발지구|
|npln\_prvopr\_public\_house\_at|수도권 내 민영 공공주택지구|1|0|N|수도권 내 민영 공공주택지구|
|public\_house\_spclm\_applc\_apt|공공주택 특별법 적용 여부|1|0|N|공공주택 특별법 적용 여부|
|pblanc\_url|모집공고 상세 URL|300|0|https://www.applyhome.co.kr/ai/aia/selectAPTLttotPblancDetail.do?houseManageNo=2022000248&pblancNo=2022000248|청약홈 분양정보 페이지 연결 URL|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)
1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getAPTLttotPblancDetail?page=1&perPage=10&cond%5BHOUSE\_MANAGE\_NO%3A%3AEQ%5D=2022000248&cond%5BPBLANC\_NO%3A%3AEQ%5D=2022000248&cond%5BHOUSE\_SECD%3A%3AEQ%5D=01&cond%5BSUBSCRPT\_AREA\_CODE\_NM%3A%3AEQ%5D=%EC%84%9C%EC%9A%B8&cond%5BRCRIT\_PBLANC\_DE%3A%3ALTE%5D=2022-05-31&cond%5BRCRIT\_PBLANC\_DE%3A%3AGTE%5D=2022-01-01&serviceKey= 서비스키|
|**응답 메시지**|
|<p>{</p><p>`  `"currentCount": 1,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"BSNS\_MBY\_NM": "주식회사 하나자산신탁",</p><p>`      `"CNSTRCT\_ENTRPS\_NM": "강산건설(주)",</p><p>`      `"CNTRCT\_CNCLS\_BGNDE": "2022-06-13",</p><p>`      `"CNTRCT\_CNCLS\_ENDDE": "2022-06-15",</p><p>`      `"GNRL\_RNK1\_CRSPAREA\_ENDDE": "2022-05-24",</p><p>`      `"GNRL\_RNK1\_CRSPAREA\_RCPTDE": "2022-05-24",</p><p>`      `"GNRL\_RNK1\_ETC\_AREA\_ENDDE": "2022-05-25",</p><p>`      `"GNRL\_RNK1\_ETC\_AREA\_RCPTDE": "2022-05-25",</p><p>`      `"GNRL\_RNK1\_ETC\_GG\_ENDDE": null,</p><p>`      `"GNRL\_RNK1\_ETC\_GG\_RCPTDE": null,</p><p>`      `"GNRL\_RNK2\_CRSPAREA\_ENDDE": "2022-05-26",</p><p>`      `"GNRL\_RNK2\_CRSPAREA\_RCPTDE": "2022-05-26",</p><p>`      `"GNRL\_RNK2\_ETC\_AREA\_ENDDE": "2022-05-26",</p><p>`      `"GNRL\_RNK2\_ETC\_AREA\_RCPTDE": "2022-05-26",</p><p>`      `"GNRL\_RNK2\_ETC\_GG\_ENDDE": null,</p><p>`      `"GNRL\_RNK2\_ETC\_GG\_RCPTDE": null,</p><p>`      `"HMPG\_ADRES": "http://www.dawartriche.com/",</p><p>`      `"HOUSE\_DTL\_SECD": "01",</p><p>`      `"HOUSE\_DTL\_SECD\_NM": "민영",</p><p>`      `"HOUSE\_MANAGE\_NO": "2022000248",</p><p>`      `"HOUSE\_NM": "창동 다우아트리체 주상복합 아파트",</p><p>`      `"HOUSE\_SECD": "01",</p><p>`      `"HOUSE\_SECD\_NM": "APT",</p><p>`      `"HSSPLY\_ADRES": "서울특별시 도봉구 창동 662-7번지 외 12필지",</p><p>`      `"HSSPLY\_ZIP": "01400",</p><p>`      `"IMPRMN\_BSNS\_AT": "N",</p><p>`      `"LRSCL\_BLDLND\_AT": "N",</p><p>`      `"MDAT\_TRGET\_AREA\_SECD": "Y",</p><p>`      `"MDHS\_TELNO": "16684888",</p><p>`      `"MVN\_PREARNGE\_YM": "202502",</p><p>`      `"NPLN\_PRVOPR\_PUBLIC\_HOUSE\_AT": "N",</p><p>"NSPRC\_NM": "e대한경제",</p><p>`      `"PARCPRC\_ULS\_AT": "N",</p><p>`      `"PBLANC\_NO": "2022000248",</p><p>`      `"PBLANC\_URL": "https://www.applyhome.co.kr/ai/aia/selectAPTLttotPblancDetail.do?houseManageNo=2022000248&pblancNo=2022000248",</p><p>`      `"PRZWNER\_PRESNATN\_DE": "2022-06-02",</p><p>`      `"PUBLIC\_HOUSE\_EARTH\_AT": "N",</p><p>`      `"RCEPT\_BGNDE": "2022-05-23",</p><p>`      `"RCEPT\_ENDDE": "2022-05-26",</p><p>`      `"RCRIT\_PBLANC\_DE": "2022-05-12",</p><p>`      `"RENT\_SECD": "0",</p><p>`      `"RENT\_SECD\_NM": "분양주택",</p><p>`      `"SPECLT\_RDN\_EARTH\_AT": "Y",</p><p>`      `"SPSPLY\_RCEPT\_BGNDE": "2022-05-23",</p><p>`      `"SPSPLY\_RCEPT\_ENDDE": "2022-05-23",</p><p>`      `"SUBSCRPT\_AREA\_CODE": "100",</p><p>`      `"SUBSCRPT\_AREA\_CODE\_NM": "서울",</p><p>`      `"TOT\_SUPLY\_HSHLDCO": 89</p><p>`    `}</p><p>`  `],</p><p>`  `"matchCount": 1,</p><p>`  `"page": 1,</p><p>`  `"perPage": 10,</p><p>`  `"totalCount": 1863</p><p>}</p>|

**
1  #### 오피스텔/도시형/민간임대 분양정보 상세조회 상세기능 명세
   1  ##### 상세기능 정보

|**상세기능 번호**|2|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|오피스텔/도시형/민간임대 분양정보 상세조회|||
|**상세기능 설명**|주택관리번호, 공고번호, 주택구분, 모집공고일 값을 이용하여 오피스텔/도시형/민간임대 분양정보의 상세정보를 제공|||
1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2022950004|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2022950004|(Equal) 공고번호|
|search\_house\_secd|주택구분|4|0|(EQ) 0201|(Equal) 주택구분 |
|subscrpt\_area\_code|공급지역코드|3|0|(EQ) 100|(Equal) 공급지역코드|
|rcrit\_pblanc\_de|모집공고일|10|0|(LT)|(Little)                  < ‘YYYY-MM-DD’|
|||||<p>(LTE) </p><p>2022-05-31</p>|(Little or Equal)         <= ‘YYYY-MM-DD’|
|||||(GT)|(Greater)                  > ‘YYYY-MM-DD’|
|||||<p>(GTE) </p><p>2022-01-01</p>|(Greater or Equal)         >= ‘YYYY-MM-DD’|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	


1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|1|2022950004|주택관리번호|
|pblanc\_no|공고번호|40|1|2022950004|공고번호|
|house\_nm|주택명|200|0|힐스테이트 청량리 메트로블 (도시형생활주택)|주택명|
|house\_secd|주택구분코드|2|0|02|주택구분코드 |
|house\_secd\_nm|주택구분코드명|4000|0|도시형/오피스텔/민간임대|주택구분코드명|
|house\_dtl\_secd|주택상세구분코드|2|0|01|주택상세구분코드|
|house\_dtl\_secd\_nm|주택상세구분코드명|4000|0|도시형생활주택|주택상세구분코드명|
|search\_house\_secd|주택구분|4|0|0201|주택구분|
|subscrpt\_area\_code|공급지역코드|3|0|100|공급지역코드|
|subscrpt\_area\_code\_nm|공급지역명|500|0|서울|공급지역명|
|hssply\_zip|공급위치 우편번호|6|0|02560|공급위치 우편번호|
|hssply\_hshldco|공급위치|256|0|서울특별시 동대문구 용두동 26-14번지 일원|공급위치|
|tot\_suply\_hshldco|공급규모|10|1|213|공급규모|
|rcrit\_pblanc\_de|모집공고일|10|0|2022-02-10|모집공고일|
|nsprc\_nm|신문사|200|0|문화일보|신문사|
|subscrpt\_rcept\_bgnde|청약접수시작일|10|0|2022-02-21|청약접수시작일|
|subscrpt\_rcept\_endde|청약접수종료일|10|0|2022-02-22|청약접수종료일|
|przwner\_presnatn\_de|당첨자발표일|10|0|2022-03-02|당첨자발표일|
|cntrct\_cncls\_bgnde|계약시작일|10|0|2022-03-03|계약시작일|
|cntrct\_cncls\_endde|계약종료일|10|0|2022-03-04|계약종료일|
|hmpg\_adres|홈페이지주소|256|0|https://www.hillstate.co.kr/s/#cheongnyangri\_metro|홈페이지주소|
|bsns\_mby\_nm|사업주체명 (시행사)|200|0|㈜하나자산신탁|사업주체명 (시행사)|
|mdhs\_telno|문의처|30|0|0234520100|문의처|
|mvn\_prearnge\_ym|입주예정월|6|0|202507|입주예정월|
|pblanc\_url|모집공고 상세 URL|300|0|https://www.applyhome.co.kr/ai/aia/selectPRMOLttotPblancDetailView.do?houseManageNo=2022950004&pblancNo=2022950004&houseSecd=02|청약홈 분양정보 페이지 연결 URL|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)


1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getUrbtyOfctlLttotPblancDetail?page=1&perPage=10&cond%5BHOUSE\_MANAGE\_NO%3A%3AEQ%5D=2022950004&cond%5BPBLANC\_NO%3A%3AEQ%5D=2022950004&cond%5BSEARCH\_HOUSE\_SECD%3A%3AEQ%5D=0201&cond%5BRCRIT\_PBLANC\_DE%3A%3ALTE%5D=2022-05-31&cond%5BRCRIT\_PBLANC\_DE%3A%3AGTE%5D=2022-01-01&serviceKey=서비스키|
|**응답 메시지**|
|<p>{</p><p>`  `"currentCount": 1,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"BSNS\_MBY\_NM": "(주)하나자산신탁",</p><p>`      `"CNTRCT\_CNCLS\_BGNDE": "2022-03-03",</p><p>`      `"CNTRCT\_CNCLS\_ENDDE": "2022-03-04",</p><p>`      `"HMPG\_ADRES": "https://www.hillstate.co.kr/s/#cheongnyangri\_metro",</p><p>`      `"HOUSE\_DTL\_SECD": "01",</p><p>`      `"HOUSE\_DTL\_SECD\_NM": "도시형생활주택",</p><p>`      `"HOUSE\_MANAGE\_NO": 2022950004,</p><p>`      `"HOUSE\_NM": "힐스테이트 청량리 메트로블(도시형생활주택)",</p><p>`      `"HOUSE\_SECD": "02",</p><p>`      `"HOUSE\_SECD\_NM": "도시형/오피스텔/민간임대",</p><p>`      `"HSSPLY\_ADRES": "서울특별시 동대문구 용두동 26-14번지 일원",</p><p>`      `"HSSPLY\_ZIP": "02560",</p><p>`      `"MDHS\_TELNO": "0234520100",</p><p>`      `"MVN\_PREARNGE\_YM": "202507",</p><p>`      `"NSPRC\_NM": "문화일보",</p><p>`      `"PBLANC\_NO": 2022950004,</p><p>"PBLANC\_URL": “https://www.applyhome.co.kr/ai/aia/selectPRMOLttotPblancDetailView.do?houseManageNo=2022950004&pblancNo=2022950004&houseSecd=02”,</p><p>`      `"PRZWNER\_PRESNATN\_DE": "2022-03-02",</p><p>`      `"RCRIT\_PBLANC\_DE": "2022-02-10",</p><p>`      `"SEARCH\_HOUSE\_SECD": "0201",</p><p>`      `"SUBSCRPT\_RCEPT\_BGNDE": "2022-02-21",</p><p>`      `"SUBSCRPT\_RCEPT\_ENDDE": "2022-02-22",</p><p>`      `"TOT\_SUPLY\_HSHLDCO": 213</p><p>`    `}</p><p>`  `],</p><p>`  `"matchCount": 1,</p><p>`  `"page": 1,</p><p>`  `"perPage": 10,</p><p>`  `"totalCount": 242</p><p>}</p>|




1  #### APT 잔여세대 분양정보 상세조회 명세
   1  ##### 상세기능 정보

|**상세기능 번호**|3|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|APT 잔여세대 분양정보 상세조회|||
|**상세기능 설명**|주택관리번호, 공고번호, 주택구분코드, 모집공고일 값을 이용하여 APT 잔여세대 분양정보의 상세 정보를 제공|||

1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2022910110|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2022910110|(Equal) 공고번호|
|house\_secd|주택구분코드|2|0|(EQ) 04|(Equal) 주택구분코드 |
|subscrpt\_area\_code|공급지역코드|3|0|(EQ) 410|(Equal) 공급지역코드|
|rcrit\_pblanc\_de|모집공고일|10|0|(LT)|(Little)                  < ‘YYYY-MM-DD’|
|||||<p>(LTE) </p><p>2022-05-31</p>|(Little or Equal)         <= ‘YYYY-MM-DD’|
|||||(GT)|(Greater)                  > ‘YYYY-MM-DD’|
|||||<p>(GTE) </p><p>2022-01-01</p>|(Greater or Equal)         >= ‘YYYY-MM-DD’|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	


1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|1|2022910110|주택관리번호|
|pblanc\_no|공고번호|40|1|2022910110|공고번호|
|house\_nm|주택명|200|0|평택화양 휴먼빌 퍼스트시티|주택명|
|house\_secd|주택구분코드|2|0|04|주택구분코드|
|house\_secd\_nm|주택구분코드명|4000|0|무순위|주택구분코드명|
|subscrpt\_area\_code|공급지역코드|3|0|410|공급지역코드|
|subscrpt\_area\_code\_nm|공급지역명|500|0|경기|공급지역명|
|hssply\_zip|공급위치 우편번호|6|0|17945|공급위치 우편번호|
|hssply\_adres|공급위치|256|0|경기도 평택시 현덕면 화양길 94-39(평택화양지구 7-1BL|공급위치|
|tot\_suply\_hshldco|공급규모|10|1|16|공급규모|
|rcrit\_pblanc\_de|모집공고일|10|0|2022-04-25|모집공고일|
|nsprc\_nm|신문사|200|0|-|신문사|
|subscrpt\_rcept\_bgnde|청약접수시작일|10|0|2022-05-02|청약접수시작일|
|subscrpt\_rcept\_endde|청약접수종료일|10|0|2022-05-02|청약접수종료일|
|spsply\_rcept\_bgnde|특별공급접수시작일|10|0|-|특별공급접수시작일|
|spsply\_rcept\_endde|특별공급접수종료일|10|0|-|특별공급접수종료일|
|gnrl\_rcept\_rcptde|일반공급접수시작일|10|0|-|일반공급접수시작일|
|gnrl\_rcept\_endde|일반공급접수종료일|10|0|-|일반공급접수종료일|
|przwner\_presnatn\_de|당첨자발표일|10|0|2022-05-06|당첨자발표일|
|cntrct\_cncls\_bgnde|계약시작일|10|0|2022-05-13|계약시작일|
|cntrct\_cncls\_endde|계약종료일|10|0|2022-05-13|계약종료일|
|hmpg\_adres|홈페이지주소|256|0|http://pt-humanvill.com|홈페이지주소|
|bsns\_mby\_nm|사업주체명 (시행사)|200|0|일신건영㈜|사업주체명 (시행사)|
|mdhs\_telno|문의처|30|0|15779333|문의처|
|mvn\_prearnge\_ym|입주예정월|6|0|202508|입주예정월|
|pblanc\_url|모집공고 상세 URL|300|0|https://www.applyhome.co.kr/ai/aia/selectAPTRemndrLttotPblancDetailView.do?houseManageNo=2022910110&pblancNo=2022910110|청약홈 분양정보 페이지 연결 URL|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)

1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getRemndrLttotPblancDetail?page=1&perPage=10&cond%5BHOUSE\_MANAGE\_NO%3A%3AEQ%5D=2022910110&cond%5BPBLANC\_NO%3A%3AEQ%5D=2022910110&cond%5BHOUSE\_SECD%3A%3AEQ%5D=04&cond%5BRCRIT\_PBLANC\_DE%3A%3ALTE%5D=2022-05-31&cond%5BRCRIT\_PBLANC\_DE%3A%3AGTE%5D=2022-01-01&serviceKey=서비스키|
|**응답 메시지**|
|<p>{</p><p>`  `"currentCount": 1,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"BSNS\_MBY\_NM": "일신건영(주)",</p><p>`      `"CNTRCT\_CNCLS\_BGNDE": "2022-05-13",</p><p>`      `"CNTRCT\_CNCLS\_ENDDE": "2022-05-13",</p><p>`      `"GNRL\_RCEPT\_BGNDE": null,</p><p>`      `"GNRL\_RCEPT\_ENDDE": null,</p><p>`      `"HMPG\_ADRES": "http://pt-humanvill.com",</p><p>`      `"HOUSE\_MANAGE\_NO": 2022910110,</p><p>`      `"HOUSE\_NM": "평택화양 휴먼빌 퍼스트시티",</p><p>`      `"HOUSE\_SECD": "04",</p><p>`      `"HOUSE\_SECD\_NM": "무순위",</p><p>`      `"HSSPLY\_ADRES": "경기도 평택시 현덕면 화양길 94-39(평택화양지구 7-1BL)",</p><p>`      `"HSSPLY\_ZIP": "17945",</p><p>`      `"MDHS\_TELNO": "15779333",</p><p>`      `"MVN\_PREARNGE\_YM": "202508",</p><p>"NSPRC\_NM": "-",</p><p>`      `"PBLANC\_NO": 2022910110,</p><p>"PBLANC\_URL": “https://www.applyhome.co.kr/ai/aia/selectAPTRemndrLttotPblancDetailView.do?houseManageNo=2022910110&pblancNo=2022910110”,</p><p>`      `"PRZWNER\_PRESNATN\_DE": "2022-05-06",</p><p>`      `"RCRIT\_PBLANC\_DE": "2022-04-25",</p><p>`      `"SPSPLY\_RCEPT\_BGNDE": null,</p><p>`      `"SPSPLY\_RCEPT\_ENDDE": null,</p><p>`      `"SUBSCRPT\_RCEPT\_BGNDE": "2022-05-02",</p><p>`      `"SUBSCRPT\_RCEPT\_ENDDE": "2022-05-02",</p><p>`      `"TOT\_SUPLY\_HSHLDCO": 16</p><p>`    `}</p><p>`  `],</p><p>`  `"matchCount": 1,</p><p>`  `"page": 1,</p><p>`  `"perPage": 10,</p><p>`  `"totalCount": 408</p><p>}</p>|

1  #### APT 분양정보 주택형별 상세조회 상세기능 명세
   1  ##### 상세기능 정보

|**상세기능 번호**|4|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|APT 분양정보 주택형별 상세조회|||
|**상세기능 설명**|<p>주택관리번호, 공고번호 값을 이용하여 APT 분양정보 주택형별 상세정보를 제공</p><p>특별공급-청년, 신생아 세대수는 공공주택일 경우만 해당</p><p>(APT 분양정보 상세조회 서비스 내 HOUSE\_DETAIL\_SECD = '03' & PUBLIC\_HOUSE\_SPCLW\_APPLC\_AT ='Y')인 경우 공공주택에 해당</p>|||

1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2022000248|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2022000248|(Equal) 공고번호|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	

1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|1|2022000248|주택관리번호|
|pblanc\_no|공고번호|40|1|2022000248|공고번호|
|model\_no|모델번호|2|0|01|모델번호|
|house\_ty|주택형|17|0|058\.8500A|주택형|
|suply\_ar|공급면적|17|0|80\.3800|공급면적|
|suply\_hshldco|일반공급세대수|10|0|8|일반공급세대수|
|spsply\_hshldco|특별공급세대수|10|0|11|특별공급세대수|
|mnych\_hshldco|특별공급-다자녀가구 세대수|10|0|2|특별공급-다자녀가구 세대수|
|nwwds\_hshldco|특별공급-신혼부부 세대수|10|0|4|특별공급-신혼부부 세대수|
|lfe\_frst\_hshldco|특별공급-생애최초 세대수|10|0|2|특별공급-생애최초 세대수|
|old\_parnts\_suport\_hshldco|특별공급-노부모부양 세대수|10|0|1|특별공급-노부모부양 세대수|
|instt\_recomend\_hshldco|특별공급-기관추천 세대수|10|0|2|특별공급-기관추천 세대수|
|etc\_hshldco|특별공급-기타 세대수|10|0|0|특별공급-기타 세대수|
|transr\_instt\_enfsn\_hshldco|특별공급-이전기관 세대수|10|0|0|특별공급-이전기관 세대수|
|ygmn\_hshldco|특별공급-청년 세대수|10|0|0|특별공급-청년 세대수|
|nwbb\_hshldco|특별공급-신생아 세대수|10|0|0|특별공급-신생아 세대수|
|lttot\_top\_amount|공급금액 (분양최고금액)|20|0|80,720|공급금액 (분양최고금액) (단위:만원)|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)
1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getAPTLttotPblancMdl?page=1&perPage=10&cond%5BHOUSE\_MANAGE\_NO%3A%3AEQ%5D=2022000248&cond%5BPBLANC\_NO%3A%3AEQ%5D=2022000248&serviceKey=서비스키|
|**응답 메시지**|
|<p>{</p><p>`  `"currentCount": 5,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"ETC\_HSHLDCO": 0,</p><p>`      `"HOUSE\_MANAGE\_NO": "2022000248",</p><p>`      `"HOUSE\_TY": "058.8500A",</p><p>`      `"INSTT\_RECOMEND\_HSHLDCO": 2,</p><p>`      `"LFE\_FRST\_HSHLDCO": 2,</p><p>`      `"LTTOT\_TOP\_AMOUNT": "80720",</p><p>`      `"MNYCH\_HSHLDCO": 2,</p><p>`      `"MODEL\_NO": "01",</p><p>`      `"NWBB\_HSHLDCO": 0,</p><p>`      `"NWWDS\_HSHLDCO": 4,</p><p>`      `"OLD\_PARNTS\_SUPORT\_HSHLDCO": 1,</p><p>`      `"PBLANC\_NO": "2022000248",</p><p>`      `"SPSPLY\_HSHLDCO": 11,</p><p>`      `"SUPLY\_AR": "80.3800",</p><p>`      `"SUPLY\_HSHLDCO": 8,</p><p>`      `"TRANSR\_INSTT\_ENFSN\_HSHLDCO": 0,</p><p>`      `"YGMN\_HSHLDCO": 0</p><p>`    `},</p><p>`    `{</p><p>`      `"ETC\_HSHLDCO": 0,</p><p>`      `"HOUSE\_MANAGE\_NO": "2022000248",</p><p>`      `"HOUSE\_TY": "058.1300B",</p><p>`      `"INSTT\_RECOMEND\_HSHLDCO": 1,</p><p>`      `"LFE\_FRST\_HSHLDCO": 1,</p><p>`      `"LTTOT\_TOP\_AMOUNT": "79380",</p><p>`      `"MNYCH\_HSHLDCO": 1,</p><p>`      `"MODEL\_NO": "02",</p><p>`      `"NWBB\_HSHLDCO": 0,</p><p>`      `"NWWDS\_HSHLDCO": 2,</p><p>`      `"OLD\_PARNTS\_SUPORT\_HSHLDCO": 0,</p><p>`      `"PBLANC\_NO": "2022000248",</p><p>`      `"SPSPLY\_HSHLDCO": 5,</p><p>`      `"SUPLY\_AR": "79.0500",</p><p>`      `"SUPLY\_HSHLDCO": 6,</p><p>`      `"TRANSR\_INSTT\_ENFSN\_HSHLDCO": 0,</p><p>`      `"YGMN\_HSHLDCO": 0</p><p>`    `},</p><p>`    `{</p><p>`      `"ETC\_HSHLDCO": 0,</p><p>`      `"HOUSE\_MANAGE\_NO": "2022000248",</p><p>`      `"HOUSE\_TY": "058.0900C",</p><p>`      `"INSTT\_RECOMEND\_HSHLDCO": 4,</p><p>`      `"LFE\_FRST\_HSHLDCO": 4,</p><p>`      `"LTTOT\_TOP\_AMOUNT": "79410",</p><p>`      `"MNYCH\_HSHLDCO": 4,</p><p>`      `"MODEL\_NO": "03",</p><p>`      `"NWBB\_HSHLDCO": 0,</p><p>`      `"NWWDS\_HSHLDCO": 8,</p><p>`      `"OLD\_PARNTS\_SUPORT\_HSHLDCO": 1,</p><p>`      `"PBLANC\_NO": "2022000248",</p><p>`      `"SPSPLY\_HSHLDCO": 21,</p><p>`      `"SUPLY\_AR": "79.0700",</p><p>`      `"SUPLY\_HSHLDCO": 17,</p><p>`      `"TRANSR\_INSTT\_ENFSN\_HSHLDCO": 0,</p><p>`      `"YGMN\_HSHLDCO": 0</p><p>`    `},</p><p>`    `{</p><p>`      `"ETC\_HSHLDCO": 0,</p><p>`      `"HOUSE\_MANAGE\_NO": "2022000248",</p><p>`      `"HOUSE\_TY": "059.8000 ",</p><p>`      `"INSTT\_RECOMEND\_HSHLDCO": 2,</p><p>`      `"LFE\_FRST\_HSHLDCO": 2,</p><p>`      `"LTTOT\_TOP\_AMOUNT": "81180",</p><p>`      `"MNYCH\_HSHLDCO": 2,</p><p>`      `"MODEL\_NO": "04",</p><p>`      `"NWBB\_HSHLDCO": 0,</p><p>`      `"NWWDS\_HSHLDCO": 4,</p><p>`      `"OLD\_PARNTS\_SUPORT\_HSHLDCO": 1,</p><p>`      `"PBLANC\_NO": "2022000248",</p><p>`      `"SPSPLY\_HSHLDCO": 11,</p><p>`      `"SUPLY\_AR": "80.8400",</p><p>`      `"SUPLY\_HSHLDCO": 8,</p><p>`      `"TRANSR\_INSTT\_ENFSN\_HSHLDCO": 0,</p><p>`      `"YGMN\_HSHLDCO": 0</p><p>`    `},</p><p>`    `{</p><p>`      `"ETC\_HSHLDCO": 0,</p><p>`      `"HOUSE\_MANAGE\_NO": "2022000248",</p><p>`      `"HOUSE\_TY": "122.5100F",</p><p>`      `"INSTT\_RECOMEND\_HSHLDCO": 0,</p><p>`      `"LFE\_FRST\_HSHLDCO": 0,</p><p>`      `"LTTOT\_TOP\_AMOUNT": "173596",</p><p>`      `"MNYCH\_HSHLDCO": 0,</p><p>`      `"MODEL\_NO": "05",</p><p>`      `"NWBB\_HSHLDCO": 0,</p><p>`      `"NWWDS\_HSHLDCO": 0,</p><p>`      `"OLD\_PARNTS\_SUPORT\_HSHLDCO": 0,</p><p>`      `"PBLANC\_NO": "2022000248",</p><p>`      `"SPSPLY\_HSHLDCO": 0,</p><p>`      `"SUPLY\_AR": "165.3450",</p><p>`      `"SUPLY\_HSHLDCO": 2,</p><p>`      `"TRANSR\_INSTT\_ENFSN\_HSHLDCO": 0,</p><p>`      `"YGMN\_HSHLDCO": 0</p><p>`    `}</p><p>`  `],</p><p>`  `"matchCount": 5,</p><p>`  `"page": 1,</p><p>`  `"perPage": 10,</p><p>`  `"totalCount": 9846</p><p>}</p>|

**
1  #### 오피스텔/도시형/민간임대 분양정보 주택형별 상세조회 상세기능 명세
   1  ##### 상세기능 정보

|**상세기능 번호**|5|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|오피스텔/도시형/민간임대 분양정보 주택형별 상세조회|||
|**상세기능 설명**|주택관리번호, 공고번호 값을 이용하여 오피스텔/도시형/민간임대 분양정보 주택형별 상세 정보를 제공|||

1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2022950004|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2022950004|(Equal) 공고번호|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	

1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|pblanc\_no|공고번호|40|1|2022950004|공고번호|
|house\_manage\_no|주택관리번호|40|1|2022950004|주택관리번호|
|model\_no|모델번호|4|0|01|모델번호|
|gp|군|30|0|1|군|
|tp|타입|10|0|26|타입|
|excluse\_ar|전용면적|17|0|26|전용면적|
|suply\_hshldco|공급세대수|10|1|25|공급세대수|
|suply\_amount|공급금액 (분양최고금액)|20|0|53,740|공급금액 (분양최고금액) (단위:만원)|
|subscrpt\_reqst\_amount|청약신청금|20|0|100|청약신청금 (단위:만원)|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)

1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getUrbtyOfctlLttotPblancMdl?page=1&perPage=10&cond%5BPBLANC\_NO%3A%3AEQ%5D=2022950004&cond%5BHOUSE\_MANAGE\_NO%3A%3AEQ%5D=2022950004&serviceKey=서비스키|
|**응답 메시지**|
|<p>{</p><p>`  `"currentCount": 7,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"EXCLUSE\_AR": 26,</p><p>`      `"GP": "1",</p><p>`      `"HOUSE\_MANAGE\_NO": 2022950004,</p><p>`      `"MODEL\_NO": "01",</p><p>`      `"PBLANC\_NO": 2022950004,</p><p>`      `"SUBSCRPT\_REQST\_AMOUNT": "100",</p><p>`      `"SUPLY\_AMOUNT": "53,740",</p><p>`      `"SUPLY\_HSHLDCO": 25,</p><p>`      `"TP": "26"</p><p>`    `},</p><p>`    `{</p><p>`      `"EXCLUSE\_AR": 41,</p><p>`      `"GP": "2",</p><p>`      `"HOUSE\_MANAGE\_NO": 2022950004,</p><p>`      `"MODEL\_NO": "02",</p><p>`      `"PBLANC\_NO": 2022950004,</p><p>`      `"SUBSCRPT\_REQST\_AMOUNT": "100",</p><p>`      `"SUPLY\_AMOUNT": "79,090",</p><p>`      `"SUPLY\_HSHLDCO": 24,</p><p>`      `"TP": "41"</p><p>`    `},</p><p>`    `{</p><p>`      `"EXCLUSE\_AR": 44,</p><p>`      `"GP": "2",</p><p>`      `"HOUSE\_MANAGE\_NO": 2022950004,</p><p>`      `"MODEL\_NO": "02",</p><p>`      `"PBLANC\_NO": 2022950004,</p><p>`      `"SUBSCRPT\_REQST\_AMOUNT": "100",</p><p>`      `"SUPLY\_AMOUNT": "85,520",</p><p>`      `"SUPLY\_HSHLDCO": 24,</p><p>`      `"TP": "44"</p><p>`    `},</p><p>`    `{</p><p>`      `"EXCLUSE\_AR": 49,</p><p>`      `"GP": "3",</p><p>`      `"HOUSE\_MANAGE\_NO": 2022950004,</p><p>`      `"MODEL\_NO": "03",</p><p>`      `"PBLANC\_NO": 2022950004,</p><p>`      `"SUBSCRPT\_REQST\_AMOUNT": "100",</p><p>`      `"SUPLY\_AMOUNT": "89,970",</p><p>`      `"SUPLY\_HSHLDCO": 72,</p><p>`      `"TP": "48A"</p><p>`    `},</p><p>`    `{</p><p>`      `"EXCLUSE\_AR": 49,</p><p>`      `"GP": "3",</p><p>`      `"HOUSE\_MANAGE\_NO": 2022950004,</p><p>`      `"MODEL\_NO": "03",</p><p>`      `"PBLANC\_NO": 2022950004,</p><p>`      `"SUBSCRPT\_REQST\_AMOUNT": "100",</p><p>`      `"SUPLY\_AMOUNT": "89,210",</p><p>`      `"SUPLY\_HSHLDCO": 24,</p><p>`      `"TP": "48A1"</p><p>`    `},</p><p>`    `{</p><p>`      `"EXCLUSE\_AR": 49,</p><p>`      `"GP": "3",</p><p>`      `"HOUSE\_MANAGE\_NO": 2022950004,</p><p>`      `"MODEL\_NO": "03",</p><p>`      `"PBLANC\_NO": 2022950004,</p><p>`      `"SUBSCRPT\_REQST\_AMOUNT": "100",</p><p>`      `"SUPLY\_AMOUNT": "89,430",</p><p>`      `"SUPLY\_HSHLDCO": 22,</p><p>`      `"TP": "48B"</p><p>`    `},</p><p>`    `{</p><p>`      `"EXCLUSE\_AR": 49,</p><p>`      `"GP": "3",</p><p>`      `"HOUSE\_MANAGE\_NO": 2022950004,</p><p>`      `"MODEL\_NO": "03",</p><p>`      `"PBLANC\_NO": 2022950004,</p><p>`      `"SUBSCRPT\_REQST\_AMOUNT": "100",</p><p>`      `"SUPLY\_AMOUNT": "89,550",</p><p>`      `"SUPLY\_HSHLDCO": 22,</p><p>`      `"TP": "48C"</p><p>`    `}</p><p>`  `],</p><p>`  `"matchCount": 7,</p><p>`  `"page": 1,</p><p>`  `"perPage": 10,</p><p>`  `"totalCount": 1950</p><p>}</p>|

1  #### APT 잔여세대 분양정보 주택형별 상세조회 상세기능 명세
   1  ##### 상세기능 정보

|**상세기능 번호**|6|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|APT 잔여세대 분양정보 주택형별 상세조회|||
|**상세기능 설명**|주택관리번호, 공고번호 값을 이용하여 APT 잔여세대 분양정보 주택형별 상세 정보를 제공|||

1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2022910110|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2022910110|(Equal) 공고번호|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	

1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|2022910110|주택관리번호|
|pblanc\_no|공고번호|40|0|2022910110|공고번호|
|model\_no|모델번호|2|0|01|모델번호|
|house\_ty|모델타입|17|0|084\.9840|모델타입|
|suply\_ar|공급면적|17|0|-|공급면적|
|suply\_hshldco|일반공급세대수|10|0|16|일반공급세대수|
|spsply\_hshldco|특별공급세대수|10|0|-|특별공급세대수|
|lttot\_top\_amount|공급금액 (분양최고금액)|20|0|46,357|공급금액 (분양최고금액) (단위:만원)|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)

1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getRemndrLttotPblancMdl?page=1&perPage=10&cond%5BHOUSE\_MANAGE\_NO%3A%3AEQ%5D=2022910110&cond%5BPBLANC\_NO%3A%3AEQ%5D=2022910110&serviceKey=서비스키|
|**응답 메시지**|
|<p>{</p><p>`  `"currentCount": 1,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"HOUSE\_MANAGE\_NO": 2022910110,</p><p>`      `"HOUSE\_TY": "084.9840 ",</p><p>`      `"LTTOT\_TOP\_AMOUNT": "46,357",</p><p>`      `"MODEL\_NO": "01",</p><p>`      `"PBLANC\_NO": 2022910110,</p><p>`      `"SPSPLY\_HSHLDCO": null,</p><p>`      `"SUPLY\_AR": null,</p><p>`      `"SUPLY\_HSHLDCO": 16</p><p>`    `}</p><p>`  `],</p><p>`  `"matchCount": 1,</p><p>`  `"page": 1,</p><p>`  `"perPage": 10,</p><p>`  `"totalCount": 1330</p><p>}</p>|

**
1  #### 공공지원 민간임대 분양정보 상세조회 상세기능 명세
   1  ##### 상세기능 정보

|**상세기능 번호**|7|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|공공지원 민간임대 분양정보 상세조회|||
|**상세기능 설명**|주택관리번호, 공고번호, 모집공고일 값을 이용하여 공공지원 민간임대 분양정보의 상세정보를 제공|||

1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2024850001|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2024850001|(Equal) 공고번호|
|subscrpt\_area\_code|공급지역코드|3|0|(EQ) 621|(Equal) 공급지역코드|
|rcrit\_pblanc\_de|모집공고일|10|0|(LT)|(Little)                  < ‘YYYYMMDD’|
|||||<p>(LTE) </p><p>20240131</p>|(Little or Equal)         <= ‘YYYYMMDD’|
|||||(GT)|(Greater)                  > ‘YYYYMMDD’|
|||||<p>(GTE) </p><p>20240101</p>|(Greater or Equal)         >= ‘YYYYMMDD’|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	


1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|2024850001|주택관리번호|
|pblanc\_no|공고번호|40|0|2024850001|공고번호|
|house\_nm|주택명|200|0|성산 삼정그린코아 웰레스트|주택명|
|house\_secd|주택구분코드|2|0|03|주택구분코드|
|house\_secd\_nm|주택구분코드명|4000|0|공공지원민간임대|주택구분코드명|
|house\_detail\_secd|주택상세구분코드|2|0|03|주택상세구분코드|
|house\_detail\_secd\_nm|주택상세구분코드명|4000|0|공공지원민간임대|주택상세구분코드명|
|subscrpt\_area\_code|공급지역코드|3|0|621|공급지역코드|
|subscrpt\_area\_code\_nm|공급지역명|500|0|경남|공급지역명|
|rcrit\_pblanc\_de|모집공고일|10|0|20240124|모집공고일|
|nsprc\_nm|신문사|200|0|아시아경제|신문사|
|subscrpt\_rcept\_bgnde|청약접수시작일|10|0|20240129|청약접수시작일|
|subscrpt\_rcept\_endde|청약접수종료일|10|0|20240129|청약접수종료일|
|przwner\_presnatn\_de|당첨자발표일|10|0|20240201|당첨자발표일|
|hssply\_zip|공급위치 우편번호|6||51551|공급위치 우편번호|
|hssply\_adres|공급위치|256||경상남도 창원시 성산구 안민동 614-2번지 일원[창원안민 공공지원 민간임대주택 공급촉진지구]|공급위치|
|tot\_suply\_hshldco|공급규모|10||163|공급규모|
|cntrct\_cncls\_bgnde|계약시작일|10|0|20240205|계약시작일|
|cntrct\_cncls\_endde|계약종료일|10|0|20240206|계약종료일|
|hmpg\_adres|홈페이지주소|256|0|http://ssgreencore2.com|홈페이지주소|
|bsns\_mby\_nm|사업주체명 (시행사)|200|0|창원뉴스테이 주식회사|사업주체명 (시행사)|
|mdhs\_telno|문의처|30|0|15668728|문의처|
|mvn\_prearnge\_ym|입주예정월|6|0|202502|입주예정월|
|pblanc\_url|모집공고 상세 URL|300|0|https://www.applyhome.co.kr/ai/aia/selectPRMOLttotPblancDetailView.do?houseManageNo=2024850001&pblancNo=2024850001&houseSecd=03|청약홈 분양정보 페이지 연결 URL|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)


1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getPblPvtRentLttotPblancDetail?page=1&perPage=10&cond%5BPBLANC\_NO%3A%3AEQ%5D=2024850001&cond%5BRCRIT\_PBLANC\_DE%3A%3ALT%5D=2024850001&cond%5BRCRIT\_PBLANC\_DE%3A%3ALTE%5D=20240131&cond%5BRCRIT\_PBLANC\_DE%3A%3AGTE%5D=20240101&serviceKey=서비스키|
|**응답 메시지**|
|<p>{</p><p>`  `"currentCount": 1,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"BSNS\_MBY\_NM": "창원뉴스테이 주식회사",</p><p>`      `"CNTRCT\_CNCLS\_BGNDE": "20240205",</p><p>`      `"CNTRCT\_CNCLS\_ENDDE": "20240206",</p><p>`      `"HMPG\_ADRES": "http://ssgreencore2.com",</p><p>`      `"HOUSE\_DETAIL\_SECD": "03",</p><p>`      `"HOUSE\_DETAIL\_SECD\_NM": "공공지원민간임대",</p><p>`      `"HOUSE\_MANAGE\_NO": "2024850001",</p><p>`      `"HOUSE\_NM": "성산 삼정그린코아 웰레스트",</p><p>`      `"HOUSE\_SECD": "03",</p><p>`      `"HOUSE\_SECD\_NM": "공공지원민간임대",</p><p>`      `"HSSPLY\_ADRES": "경상남도 창원시 성산구 안민동 614-2번지 일원[창원안민 공공지원 민간임대주택 공급촉진지구]",</p><p>`      `"HSSPLY\_ZIP": "51551",</p><p>`      `"MDHS\_TELNO": "15668728",</p><p>`      `"MVN\_PREARNGE\_YM": "202502",</p><p>`      `"NSPRC\_NM": "아시아경제",</p><p>`      `"PBLANC\_NO": "2024850001",</p><p>`      `"PBLANC\_URL": "https://www.applyhome.co.kr/ai/aia/selectPRMOLttotPblancDetailView.do?houseManageNo=2024850001&pblancNo=2024850001&houseSecd=03",</p><p>`      `"PRZWNER\_PRESNATN\_DE": "20240201",</p><p>`      `"RCRIT\_PBLANC\_DE": "20240124",</p><p>`      `"SEARCH\_HOUSE\_SECD": "0303",</p><p>"SUBSCRPT\_AREA\_CODE": "621",</p><p>"SUBSCRPT\_AREA\_CODE\_NM": "경남",</p><p>`      `"SUBSCRPT\_RCEPT\_BGNDE": "20240129",</p><p>`      `"SUBSCRPT\_RCEPT\_ENDDE": "20240129",</p><p>`      `"TOT\_SUPLY\_HSHLDCO": 163</p><p>`    `}</p><p>`  `],</p><p>`  `"matchCount": 1,</p><p>`  `"page": 1,</p><p>`  `"perPage": 10,</p><p>`  `"totalCount": 96</p><p>}</p>|

**
1  #### 공공지원 민간임대 분양정보 주택형별 상세조회 상세기능 명세
   1  ##### 상세기능 정보

|**상세기능 번호**|8|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|공공지원 민간임대 분양정보 주택형별 상세조회|||
|**상세기능 설명**|주택관리번호, 공고번호 값을 이용하여 공공지원 민간임대 분양정보 주택형별 상세 정보를 제공|||

1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2024850001|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2024850001|(Equal) 공고번호|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	

1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|2024850001|주택관리번호|
|pblanc\_no|공고번호|40|0|2024850001|공고번호|
|model\_no|모델번호|4|0|01|모델번호|
|gp|군|30|0|-|군|
|tp|타입|10|0|59A|타입|
|excluse\_ar|전용면적|17|0|59\.9536|전용면적|
|supply\_ar|공급면적|17|0|83\.6704|공급면적|
|cntrct\_ar|계약면적|17|0|121\.9784|계약면적|
|suply\_hshldco|공급세대수|10|0|43|공급세대수|
|gnsply\_hshldco|일반공급 세대수|10|0|0|일반공급 세대수|
|spsply\_ygmn\_hshldco|특별공급 청년 세대수|10|0|2|특별공급 청년 세대수|
|spsply\_new\_mrrg\_hshldco|특별공급 신혼 세대수|10|0|31|특별공급 신혼 세대수|
|spsply\_aged\_hshldco|특별공급 고령자 세대수|10|0|10|특별공급 고령자 세대수|
|suply\_amount|공급금액(분양최고급액) (단위:만원)|20|0|17900|공급금액(분양최고급액) (단위:만원)|
|subscrpt\_reqst\_amount|청약신청금 (단위:만원)|20|0|10|청약신청금 (단위:만원)|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)

1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getPblPvtRentLttotPblancMdl?page=1&perPage=10&cond%5BHOUSE\_MANAGE\_NO%3A%3AEQ%5D=2024850001&cond%5BPBLANC\_NO%3A%3AEQ%5D=2024850001&serviceKey=서비스키|
|**응답 메시지**|
|<p>{</p><p>`  `"currentCount": 6,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"CNTRCT\_AR": "121.9784",</p><p>`      `"EXCLUSE\_AR": "59.9536",</p><p>`      `"GNSPLY\_HSHLDCO": 0,</p><p>`      `"GP": "-",</p><p>`      `"HOUSE\_MANAGE\_NO": "2024850001",</p><p>`      `"MODEL\_NO": "01",</p><p>`      `"PBLANC\_NO": "2024850001",</p><p>`      `"SPSPLY\_AGED\_HSHLDCO": 10,</p><p>`      `"SPSPLY\_NEW\_MRRG\_HSHLDCO": 31,</p><p>`      `"SPSPLY\_YGMN\_HSHLDCO": 2,</p><p>`      `"SUBSCRPT\_REQST\_AMOUNT": "10",</p><p>`      `"SUPLY\_AMOUNT": "17900",</p><p>`      `"SUPLY\_AR": "83.6704",</p><p>`      `"SUPLY\_HSHLDCO": 43,</p><p>`      `"TP": "59A"</p><p>`    `}</p><p>],</p><p>`  `"matchCount": 6,</p><p>`  `"page": 1,</p><p>`  `"perPage": 1,</p><p>`  `"totalCount": 747</p><p>}</p>|

1  #### 임의공급 분양정보 상세조회 상세기능 명세
   1  ##### 상세기능 정보

|**상세기능 번호**|9|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|임의공급 분양정보 상세조회|||
|**상세기능 설명**|주택관리번호, 공고번호, 모집공고일 값을 이용하여 임의공급 분양정보의 상세정보를 제공|||

1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2024940001|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2024940001|(Equal) 공고번호|
|subscrpt\_area\_code|공급지역코드|3|0|(EQ) 100|(Equal) 공급지역코드|
|rcrit\_pblanc\_de|모집공고일|8|0|(LT)|(Little)                  < ‘YYYYMMDD’|
|||||<p>(LTE) </p><p>20240131</p>|(Little or Equal)         <= ‘YYYYMMDD’|
|||||(GT)|(Greater)                  > ‘YYYYMMDD’|
|||||<p>(GTE) </p><p>20240101</p>|(Greater or Equal)         >= ‘YYYYMMDD’|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	

1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|2024940001|주택관리번호|
|pblanc\_no|공고번호|40|0|2024940001|공고번호|
|house\_nm|주택명|200|0|신독산 솔리힐 뉴포레(2차)|주택명|
|house\_secd|주택구분코드|2|0|11|주택구분코드|
|house\_secd\_nm|주택구분코드명|4000|0|임의공급|주택구분코드명|
|subscrpt\_area\_code|공급지역코드|3|0|100|공급지역코드|
|subscrpt\_area\_code\_nm|공급지역명|500|0|서울|공급지역명|
|hssply\_zip|공급위치 우편번호|6|0|08552|공급위치 우편번호|
|hssply\_adres|공급위치|256|0|서울특별시 금천구 독산동 234-72 일원|공급위치|
|tot\_suply\_hshldco|공급규모|10|0|9|공급규모|
|rcrit\_pblanc\_de|모집공고일|8|0|20240118|모집공고일|
|subscrpt\_rcept\_bgnde|청약접수시작일|8|0|20240122|청약접수시작일|
|subscrpt\_rcept\_endde|청약접수종료일|8|0|20240122|청약접수종료일|
|spsply\_rcept\_bgnde|특별공급접수시작일|8|0|-|특별공급접수시작일|
|spsply\_rcept\_endde|특별공급접수종료일|8|0|-|특별공급접수종료일|
|gnrl\_rcept\_gbnde|일반공급접수시작일|8|0|-|일반공급접수시작일|
|gnrl\_rcept\_endde|일반공급접수종료일|8|0|-|일반공급접수종료일|
|przwner\_presnatn\_de|당첨자발표일|8|0|20240125|당첨자발표일|
|cntrct\_cncls\_bgnde|계약시작일|8|0|20240201|계약시작일|
|cntrct\_cncls\_endde|계약종료일|8|0|20240201|계약종료일|
|hmpg\_adres|홈페이지주소|256|0|https://solihil.imweb.me|홈페이지주소|
|bsns\_mby\_nm|사업주체명 (시행사)|200|0|동진빌라가로주택정비사업조합|사업주체명 (시행사)|
|mdhs\_telno|문의처|30|0|028302468|문의처|
|mvn\_prearnge\_ym|입주예정월|6|0|202402|입주예정월|
|pblanc\_url|모집공고 상세 URL|300|0|https://www.applyhome.co.kr/ai/aia/selectAPTRemndrLttotPblancDetailView.do?houseManageNo=2024940001&pblancNo=2024940001|청약홈 분양정보 페이지 연결 URL|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)


1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getOPTLttotPblancDetail?page=1&perPage=10&cond%5BPBLANC\_NO%3A%3AEQ%5D=2024940001&cond%5BRCRIT\_PBLANC\_DE%3A%3ALT%5D=2024940001&cond%5BRCRIT\_PBLANC\_DE%3A%3ALTE%5D=20240131&cond%5BRCRIT\_PBLANC\_DE%3A%3AGTE%5D=20240101&serviceKey= 서비스키|
|**응답 메시지**|
|<p></p><p>{</p><p>`  `"currentCount": 1,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"BSNS\_MBY\_NM": "동진빌라가로주택정비사업조합",</p><p>`      `"CNTRCT\_CNCLS\_BGNDE": "20240201",</p><p>`      `"CNTRCT\_CNCLS\_ENDDE": "20240201",</p><p>`      `"GNRL\_RCEPT\_BGNDE": null,</p><p>`      `"GNRL\_RCEPT\_ENDDE": null,</p><p>`      `"HMPG\_ADRES": "https://solihil.imweb.me",</p><p>`      `"HOUSE\_MANAGE\_NO": "2024940001",</p><p>`      `"HOUSE\_NM": "신독산 솔리힐 뉴포레(2차)",</p><p>`      `"HOUSE\_SECD": "11",</p><p>`      `"HOUSE\_SECD\_NM": "임의공급",</p><p>`      `"HSSPLY\_ADRES": "서울특별시 금천구 독산동 234-72 일원",</p><p>`      `"HSSPLY\_ZIP": "08552",</p><p>`      `"MDHS\_TELNO": "028302468",</p><p>`      `"MVN\_PREARNGE\_YM": "202402",</p><p>`      `"PBLANC\_NO": "2024940001",</p><p>`      `"PBLANC\_URL": "https://www.applyhome.co.kr/ai/aia/selectAPTRemndrLttotPblancDetailView.do?houseManageNo=2024940001&pblancNo=2024940001",</p><p>`      `"PRZWNER\_PRESNATN\_DE": "20240125",</p><p>`      `"RCRIT\_PBLANC\_DE": "20240118",</p><p>`      `"SPSPLY\_RCEPT\_BGNDE": null,</p><p>`      `"SPSPLY\_RCEPT\_ENDDE": null,</p><p>`      `"SUBSCRPT\_RCEPT\_BGNDE": "20240122",</p><p>`      `"SUBSCRPT\_RCEPT\_ENDDE": "20240122",</p><p>`      `"TOT\_SUPLY\_HSHLDCO": 9</p><p>`    `}</p><p>`  `],</p><p>`  `"matchCount": 1,</p><p>`  `"page": 1,</p><p>`  `"perPage": 10,</p><p>`  `"totalCount": 93</p><p>}</p>|

**
1  #### 임의공급 분양정보 주택형별 상세조회 상세기능 명세
   1  ##### 상세기능 정보

|**상세기능 번호**|10|**상세기능 유형**|조회|
| :-: | :- | :-: | :- |
|**상세기능명(국문)**|임의공급 분양정보 주택형별 상세조회|||
|**상세기능 설명**|주택관리번호, 공고번호 값을 이용하여 임의공급 분양정보 주택형별 상세 정보를 제공|||

1  ##### 요청 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|(EQ) 2024940001|(Equal) 주택관리번호|
|pblanc\_no|공고번호|40|0|(EQ) 2024940001|(Equal) 공고번호|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)	

1  ##### 응답 메시지 명세

|**항목명(영문)**|**항목명(국문)**|**항목크기**|**항목구분**|**샘플데이터**|**항목설명**|
| :-: | :-: | :-: | :-: | :-: | :-: |
|house\_manage\_no|주택관리번호|40|0|2024940001|주택관리번호|
|pblanc\_no|공고번호|40|0|2024940001|공고번호|
|model\_no|모델번호|2|0|01|모델번호|
|house\_ty|주택형|17|0|059\.1669A|주택형|
|suply\_hshldco|일반공급세대수|10|0|1|일반공급세대수|
|lttot\_top\_amount|공급금액(분양최고금액) (단위:만원)|20|0|77,600|공급금액(분양최고금액) (단위:만원)|

※ 항목구분 : 필수(1), 옵션(0), 1건 이상 복수건(1..n), 0건 또는 복수건(0..n)

1  ##### 요청 / 응답 메시지 예제

|**요청메시지**|
| :-: |
|https://api.odcloud.kr/api/ApplyhomeInfoDetailSvc/v1/getOPTLttotPblancMdl?page=1&perPage=10&cond%5BHOUSE\_MANAGE\_NO%3A%3AEQ%5D=2024940001&cond%5BPBLANC\_NO%3A%3AEQ%5D=2024940001&serviceKey=서비스키|
|**응답 메시지**|
|<p>{</p><p>`  `"currentCount": 5,</p><p>`  `"data": [</p><p>`    `{</p><p>`      `"HOUSE\_MANAGE\_NO": "2024940001",</p><p>`      `"HOUSE\_TY": "059.1669A",</p><p>`      `"LTTOT\_TOP\_AMOUNT": "77,600",</p><p>`      `"MODEL\_NO": "01",</p><p>`      `"PBLANC\_NO": "2024940001",</p><p>`      `"SUPLY\_HSHLDCO": 1</p><p>`    `},</p><p>`    `{</p><p>`      `"HOUSE\_MANAGE\_NO": "2024940001",</p><p>`      `"HOUSE\_TY": "059.5063C",</p><p>`      `"LTTOT\_TOP\_AMOUNT": "78,000",</p><p>`      `"MODEL\_NO": "02",</p><p>`      `"PBLANC\_NO": "2024940001",</p><p>`      `"SUPLY\_HSHLDCO": 1</p><p>`    `},</p><p>`    `{</p><p>`      `"HOUSE\_MANAGE\_NO": "2024940001",</p><p>`      `"HOUSE\_TY": "068.5501B",</p><p>`      `"LTTOT\_TOP\_AMOUNT": "69,700",</p><p>`      `"MODEL\_NO": "03",</p><p>`      `"PBLANC\_NO": "2024940001",</p><p>`      `"SUPLY\_HSHLDCO": 2</p><p>`    `},</p><p>`    `{</p><p>`      `"HOUSE\_MANAGE\_NO": "2024940001",</p><p>`      `"HOUSE\_TY": "068.7601C",</p><p>`      `"LTTOT\_TOP\_AMOUNT": "68,800",</p><p>`      `"MODEL\_NO": "04",</p><p>`      `"PBLANC\_NO": "2024940001",</p><p>`      `"SUPLY\_HSHLDCO": 3</p><p>`    `},</p><p>`    `{</p><p>`      `"HOUSE\_MANAGE\_NO": "2024940001",</p><p>`      `"HOUSE\_TY": "068.7509D",</p><p>`      `"LTTOT\_TOP\_AMOUNT": "67,400",</p><p>`      `"MODEL\_NO": "05",</p><p>`      `"PBLANC\_NO": "2024940001",</p><p>`      `"SUPLY\_HSHLDCO": 2</p><p>`    `}</p><p>`  `],</p><p>`  `"matchCount": 5,</p><p>`  `"page": 1,</p><p>`  `"perPage": 10,</p><p>`  `"totalCount": 47</p><p>}</p>|

**

**2. OpenAPI 에러 코드정리**

|**에러코드**|**설명**|
| :-: | :-: |
|200|성공적으로 수행됨|
|401|인증 정보가 정확하지 않음|
|500|API서버에 문제가 발생하였음|

