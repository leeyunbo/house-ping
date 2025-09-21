import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class Test {

    public static void main(String[] args) {
        // 방법 2: WebClient 사용 (비동기)

        WebClient webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .defaultHeader("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
                .defaultHeader("Accept-Encoding", "gzip, deflate, br")
                .defaultHeader("Connection", "keep-alive")
                .defaultHeader("Referer", "https://apply.lh.or.kr/lhapply/apply/main.do")
                .build();

        String html = webClient.get()
                .uri("https://apply.lh.or.kr/lhapply/apply/sc/list.do")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        String cleanedHtml = html.replaceAll("\\s+", " ").trim();

        System.out.println("html = " + cleanedHtml);

    }
}
