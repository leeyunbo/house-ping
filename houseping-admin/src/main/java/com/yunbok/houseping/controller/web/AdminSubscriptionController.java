package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.service.dto.AdminSubscriptionDto;
import com.yunbok.houseping.service.dto.AdminSubscriptionSearchCriteria;
import com.yunbok.houseping.support.dto.HouseTypeComparison;
import com.yunbok.houseping.core.domain.RealTransaction;
import com.yunbok.houseping.support.dto.CalendarEventDto;
import com.yunbok.houseping.support.dto.SyncResult;
import com.yunbok.houseping.service.AdminSubscriptionService;
import com.yunbok.houseping.core.service.subscription.SubscriptionAnalysisService;
import com.yunbok.houseping.core.service.subscription.SubscriptionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

import com.yunbok.houseping.entity.SubscriptionPriceEntity;
import com.yunbok.houseping.repository.SubscriptionPriceRepository;

@Controller
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final AdminSubscriptionService queryService;
    private final SubscriptionManagementService managementUseCase;
    private final SubscriptionPriceRepository priceRepository;
    private final SubscriptionAnalysisService analysisUseCase;

    @Value("${kakao.map.app-key:}")
    private String kakaoMapAppKey;

    @GetMapping
    public String list(AdminSubscriptionSearchCriteria criteria, Model model) {
        model.addAttribute("resultPage", queryService.search(criteria));
        model.addAttribute("search", criteria);
        model.addAttribute("areas", queryService.availableAreas());
        model.addAttribute("houseTypes", queryService.availableHouseTypes());
        model.addAttribute("sources", queryService.availableSources());
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);
        return "admin/subscriptions/list";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey);
        return "admin/subscriptions/calendar";
    }

    @GetMapping("/calendar/events")
    @ResponseBody
    public ResponseEntity<List<CalendarEventDto>> calendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(queryService.getCalendarEvents(start, end));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<AdminSubscriptionDto> getById(@PathVariable Long id) {
        return queryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sync")
    public String sync(RedirectAttributes redirectAttributes) {
        try {
            SyncResult result = managementUseCase.sync();
            redirectAttributes.addFlashAttribute("message",
                    String.format("동기화 완료: 신규 %d건, 업데이트 %d건",
                            result.inserted(), result.updated()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "동기화 실패: " + e.getMessage());
        }
        return "redirect:/admin/subscriptions";
    }

    /**
     * 알림 구독 토글
     */
    @PostMapping("/{id}/notification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleNotification(@PathVariable Long id) {
        boolean enabled = queryService.toggleNotification(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "enabled", enabled,
                "message", enabled ? "알림이 설정되었습니다." : "알림이 해제되었습니다."
        ));
    }

    /**
     * 알림 구독 해제
     */
    @DeleteMapping("/{id}/notification")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeNotification(@PathVariable Long id) {
        queryService.removeNotification(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "알림이 해제되었습니다."
        ));
    }

    /**
     * 일괄 알림 설정
     */
    @PostMapping("/notifications/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkEnableNotifications(@RequestBody BulkNotificationRequest request) {
        int count = queryService.enableNotifications(request.ids());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", count,
                "message", count + "개 청약에 알림이 설정되었습니다."
        ));
    }

    public record BulkNotificationRequest(List<Long> ids) {}

    /**
     * 분양가 상세 조회
     */
    @GetMapping("/{id}/prices")
    @ResponseBody
    public ResponseEntity<List<PriceDto>> getPrices(@PathVariable Long id) {
        return queryService.findById(id)
                .map(sub -> {
                    if (sub.houseManageNo() == null || sub.houseManageNo().isEmpty()) {
                        return ResponseEntity.ok(List.<PriceDto>of());
                    }
                    List<PriceDto> prices = priceRepository.findByHouseManageNo(sub.houseManageNo())
                            .stream()
                            .map(PriceDto::from)
                            .toList();
                    return ResponseEntity.ok(prices);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public record PriceDto(
            String houseType,
            Double supplyArea,
            Integer supplyCount,
            Integer specialSupplyCount,
            Long topAmount,
            Long pricePerPyeong
    ) {
        public static PriceDto from(SubscriptionPriceEntity entity) {
            return new PriceDto(
                    entity.getHouseType(),
                    entity.getSupplyArea() != null ? entity.getSupplyArea().doubleValue() : null,
                    entity.getSupplyCount(),
                    entity.getSpecialSupplyCount(),
                    entity.getTopAmount(),
                    entity.getPricePerPyeong()
            );
        }
    }

    /**
     * 실거래가 시세 조회 (주택형별 비교 포함)
     */
    @GetMapping("/{id}/market-analysis")
    @ResponseBody
    public ResponseEntity<MarketAnalysisDto> getMarketAnalysis(@PathVariable Long id) {
        try {
            var analysis = analysisUseCase.analyze(id);
            var market = analysis.getMarketAnalysis();

            // 주택형별 비교 정보
            List<HouseTypeComparisonDto> comparisons = analysis.getHouseTypeComparisons().stream()
                    .map(HouseTypeComparisonDto::from)
                    .toList();

            if (market == null) {
                return ResponseEntity.ok(new MarketAnalysisDto(
                        null, null, null, null, 0,
                        analysis.getDongName(),
                        List.of(),
                        comparisons
                ));
            }

            List<TransactionDto> transactions = analysis.getRecentTransactions().stream()
                    .limit(5)
                    .map(TransactionDto::from)
                    .toList();

            return ResponseEntity.ok(new MarketAnalysisDto(
                    market.getAverageAmountFormatted(),
                    formatPricePerPyeong(market.getAveragePricePerPyeong()),
                    market.getMaxAmountFormatted(),
                    market.getMinAmountFormatted(),
                    market.getTransactionCount(),
                    analysis.getDongName(),
                    transactions,
                    comparisons
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(new MarketAnalysisDto(null, null, null, null, 0, null, List.of(), List.of()));
        }
    }

    private String formatPricePerPyeong(long price) {
        if (price == 0) return "-";
        return String.format("%,d만/평", price);
    }

    public record MarketAnalysisDto(
            String averageAmount,
            String pricePerPyeong,
            String maxAmount,
            String minAmount,
            int transactionCount,
            String dongName,
            List<TransactionDto> recentTransactions,
            List<HouseTypeComparisonDto> houseTypeComparisons
    ) {}

    public record HouseTypeComparisonDto(
            String houseType,
            String supplyArea,
            String supplyPrice,
            String marketPrice,
            String estimatedProfit,
            String transactionInfo,
            int transactionCount,
            boolean hasProfit
    ) {
        public static HouseTypeComparisonDto from(HouseTypeComparison c) {
            String areaStr = c.getSupplyArea() != null
                    ? c.getSupplyArea().setScale(0, java.math.RoundingMode.HALF_UP) + "㎡"
                    : "-";

            int txCount = c.getSimilarTransactions() != null ? c.getSimilarTransactions().size() : 0;

            return new HouseTypeComparisonDto(
                    c.getHouseType(),
                    areaStr,
                    c.getSupplyPriceFormatted(),
                    c.getMarketPriceFormatted(),
                    c.getEstimatedProfitFormatted(),
                    c.getTransactionInfo(),
                    txCount,
                    c.hasProfit()
            );
        }
    }

    public record TransactionDto(
            String aptName,
            String area,
            Integer floor,
            String amount,
            String dealDate
    ) {
        public static TransactionDto from(RealTransaction tx) {
            String amountStr = tx.getDealAmount() >= 10000
                    ? String.format("%.1f억", tx.getDealAmount() / 10000.0)
                    : String.format("%,d만", tx.getDealAmount());

            String dateStr = tx.getDealDate() != null
                    ? tx.getDealDate().toString()
                    : "-";

            String areaStr = tx.getExclusiveArea() != null
                    ? tx.getExclusiveArea().setScale(0, java.math.RoundingMode.HALF_UP) + "㎡"
                    : "-";

            return new TransactionDto(
                    tx.getAptName(),
                    areaStr,
                    tx.getFloor(),
                    amountStr,
                    dateStr
            );
        }
    }
}
