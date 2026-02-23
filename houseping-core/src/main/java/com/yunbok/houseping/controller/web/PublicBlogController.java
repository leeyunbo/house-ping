package com.yunbok.houseping.controller.web;

import com.yunbok.houseping.core.domain.BlogPost;
import com.yunbok.houseping.core.domain.Subscription;
import com.yunbok.houseping.core.service.blog.BlogPublishService;
import com.yunbok.houseping.core.service.subscription.SubscriptionSearchService;
import com.yunbok.houseping.entity.BlogCardImageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/home/blog")
@RequiredArgsConstructor
public class PublicBlogController {

    private final BlogPublishService blogPublishService;
    private final SubscriptionSearchService subscriptionSearchService;

    @GetMapping
    public String blogList(Model model) {
        List<BlogPost> posts = blogPublishService.findPublished();
        model.addAttribute("posts", posts);
        return "home/blog/index";
    }

    @GetMapping("/{id}")
    public String blogDetail(@PathVariable Long id, Model model) {
        BlogPost post = blogPublishService.findById(id);
        List<Subscription> subscriptions = subscriptionSearchService.findSubscriptionsForWeek(
                post.getWeekStartDate(), post.getWeekEndDate());
        model.addAttribute("post", post);
        model.addAttribute("subscriptions", subscriptions);
        return "home/blog/detail";
    }

    @GetMapping("/{postId}/card/{rank}.png")
    @ResponseBody
    public ResponseEntity<byte[]> cardImage(@PathVariable Long postId, @PathVariable int rank) {
        BlogCardImageEntity card = blogPublishService.findCardImage(postId, rank);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(card.getImageData());
    }

}
