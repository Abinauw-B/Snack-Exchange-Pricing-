package com.retailpos.websocket;

import com.retailpos.pricing.MarketCrashService;
import com.retailpos.pricing.PricingEngineService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;

@Slf4j
@Controller
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WebSocketGatewayController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MarketCrashService marketCrashService;
    private final PricingEngineService pricingEngineService;

    @Data
    @Builder
    public static class STOMPHeartbeatMessage {
        private String status;
        private String serverTimestamp;
        private boolean crashActive;
    }

    @MessageMapping("/ping")
    @SendTo("/topic/status")
    public STOMPHeartbeatMessage handlePing() {
        return STOMPHeartbeatMessage.builder()
                .status("ONLINE")
                .serverTimestamp(LocalDateTime.now().toString())
                .crashActive(marketCrashService.isCrashActive())
                .build();
    }

    public void broadcastMarketCrashAlert(MarketCrashService.MarketCrashStatus crashStatus) {
        log.info("📢 Broadcasting Market Crash Alert via WebSocket: {}", crashStatus);
        messagingTemplate.convertAndSend("/topic/market-crash", crashStatus);
    }

    public void broadcastLEDDisplayPayload(Object payload) {
        messagingTemplate.convertAndSend("/topic/led-display", payload);
    }
}
