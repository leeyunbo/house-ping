package com.yunbok.houseping.support.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 청약홈 API 응답 DTO
 */
@Data
public class ApplyhomeApiResponse {

    @JsonProperty("currentCount")
    private Integer currentCount;

    @JsonProperty("data")
    private List<SubscriptionData> data;

    @JsonProperty("matchCount")
    private Integer matchCount;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("perPage")
    private Integer perPage;

    @JsonProperty("totalCount")
    private Integer totalCount;

    /**
     * 청약 정보 데이터
     */
    @Data
    public static class SubscriptionData {

        @JsonProperty("HOUSE_MANAGE_NO")
        private String houseManageNo;

        @JsonProperty("PBLANC_NO")
        private String pblancNo;

        @JsonProperty("HOUSE_NM")
        private String houseName;

        @JsonProperty("HOUSE_SECD")
        private String houseSecd;

        @JsonProperty("HOUSE_SECD_NM")
        private String houseSecdNm;

        @JsonProperty("HOUSE_DTL_SECD")
        private String houseDtlSecd;

        @JsonProperty("HOUSE_DTL_SECD_NM")
        private String houseDtlSecdNm;

        @JsonProperty("RENT_SECD")
        private String rentSecd;

        @JsonProperty("RENT_SECD_NM")
        private String rentSecdNm;

        @JsonProperty("SUBSCRPT_AREA_CODE")
        private String subscrptAreaCode;

        @JsonProperty("SUBSCRPT_AREA_CODE_NM")
        private String subscrptAreaCodeNm;

        @JsonProperty("HSSPLY_ZIP")
        private String hssplyZip;

        @JsonProperty("HSSPLY_ADRES")
        private String hssplyAdres;

        @JsonProperty("TOT_SUPLY_HSHLDCO")
        private Integer totSuplyHshldco;

        @JsonProperty("RCRIT_PBLANC_DE")
        private String rcritPblancDe;

        @JsonProperty("NSPRC_NM")
        private String nsprcNm;

        @JsonProperty("RCEPT_BGNDE")
        private String rceptBgnde;

        @JsonProperty("RCEPT_ENDDE")
        private String rceptEndde;

        @JsonProperty("SPSPLY_RCEPT_BGNDE")
        private String spsplyRceptBgnde;

        @JsonProperty("SPSPLY_RCEPT_ENDDE")
        private String spsplyRceptEndde;

        @JsonProperty("GNRL_RNK1_CRSPAREA_RCPTDE")
        private String gnrlRnk1CrspareaRcptde;

        @JsonProperty("GNRL_RNK1_CRSPAREA_ENDDE")
        private String gnrlRnk1CrspareaEndde;

        @JsonProperty("GNRL_RNK1_ETC_GG_RCPTDE")
        private String gnrlRnk1EtcGgRcptde;

        @JsonProperty("GNRL_RNK1_ETC_GG_ENDDE")
        private String gnrlRnk1EtcGgEndde;

        @JsonProperty("GNRL_RNK1_ETC_AREA_RCPTDE")
        private String gnrlRnk1EtcAreaRcptde;

        @JsonProperty("GNRL_RNK1_ETC_AREA_ENDDE")
        private String gnrlRnk1EtcAreaEndde;

        @JsonProperty("GNRL_RNK2_CRSPAREA_RCPTDE")
        private String gnrlRnk2CrspareaRcptde;

        @JsonProperty("GNRL_RNK2_CRSPAREA_ENDDE")
        private String gnrlRnk2CrspareaEndde;

        @JsonProperty("GNRL_RNK2_ETC_GG_RCPTDE")
        private String gnrlRnk2EtcGgRcptde;

        @JsonProperty("GNRL_RNK2_ETC_GG_ENDDE")
        private String gnrlRnk2EtcGgEndde;

        @JsonProperty("GNRL_RNK2_ETC_AREA_RCPTDE")
        private String gnrlRnk2EtcAreaRcptde;

        @JsonProperty("GNRL_RNK2_ETC_AREA_ENDDE")
        private String gnrlRnk2EtcAreaEndde;

        @JsonProperty("PRZWNER_PRESNATN_DE")
        private String przwnerPresentatnDe;

        @JsonProperty("CNTRCT_CNCLS_BGNDE")
        private String cntrctCnclsBgnde;

        @JsonProperty("CNTRCT_CNCLS_ENDDE")
        private String cntrctCnclsEndde;

        @JsonProperty("HMPG_ADRES")
        private String hmpgAdres;

        @JsonProperty("CNSTRCT_ENTRPS_NM")
        private String cnstrctEntrpsNm;

        @JsonProperty("MDHS_TELNO")
        private String mdhsTelno;

        @JsonProperty("BSNS_MBY_NM")
        private String bsnsMbyNm;

        @JsonProperty("MVN_PREARNGE_YM")
        private String mvnPrearngeYm;

        @JsonProperty("SPECLT_RDN_EARTH_AT")
        private String specltRdnEarthAt;

        @JsonProperty("MDAT_TRGET_AREA_SECD")
        private String mdatTrgetAreaSecd;

        @JsonProperty("PARCPRC_ULS_AT")
        private String parcprcUlsAt;

        @JsonProperty("IMPRMN_BSNS_AT")
        private String imprmnBsnsAt;

        @JsonProperty("PUBLIC_HOUSE_EARTH_AT")
        private String publicHouseEarthAt;

        @JsonProperty("LRSCL_BLDLND_AT")
        private String lrsclBldlndAt;

        @JsonProperty("NPLN_PRVOPR_PUBLIC_HOUSE_AT")
        private String nplnPrvoprPublicHouseAt;

        @JsonProperty("PUBLIC_HOUSE_SPCLM_APPLC_AT")
        private String publicHouseSpclmApplcAt;

        @JsonProperty("PBLANC_URL")
        private String pblancUrl;
    }
}
