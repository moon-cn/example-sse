package com.example.demo;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {



    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void streamLogs(HttpServletResponse response) throws IOException, InterruptedException {
        // 设置 SSE 专用的响应头
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Type", "text/event-stream");
        response.setHeader("Connection", "keep-alive");
        response.setCharacterEncoding("UTF-8");

        // 在连接保持活跃状态期间，持续发送新的日志
        while (!Thread.currentThread().isInterrupted()) {
            sendSseMessage(response, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            Thread.sleep(1000);
        }
    }



    private void sendSseMessage(HttpServletResponse response, String message) throws IOException {
        response.getWriter().write("data: " + message + "\n\n");
        response.flushBuffer();
    }
}