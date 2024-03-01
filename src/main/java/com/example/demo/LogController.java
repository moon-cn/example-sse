package com.example.demo;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {



    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void streamLogs(HttpServletResponse response) throws IOException {
        // 设置日志文件路径
        Path logFilePath = Paths.get("D:\\tmp\\log.txt");

        // 创建 WatchService 监视日志文件的修改
        WatchService watchService = FileSystems.getDefault().newWatchService();

        // 注册日志文件所在目录到 WatchService
        Path logFileDir = logFilePath.getParent();
        logFileDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        // 初始化缓存的日志行
        List<String> cachedLogLines = Files.readAllLines(logFilePath);

        // 设置 SSE 专用的响应头
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Content-Type", "text/event-stream");
        response.setHeader("Connection", "keep-alive");
        response.setCharacterEncoding("UTF-8");

        // 将缓存的日志行发送给前端作为初始数据
        for (String logLine : cachedLogLines) {
            sendSseMessage(response, logLine);
        }

        // 在连接保持活跃状态期间，持续发送新的日志行
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 接收 WatchService 的事件
                WatchKey watchKey = watchService.take();

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        // 日志文件有修改，读取新增的日志行并发送给前端
                        List<String> newLogLines = Files.readAllLines(logFilePath);
                        for (String logLine : newLogLines) {
                            sendSseMessage(response, logLine);
                        }
                    }
                }

                // 重置 WatchKey，以便可以接收后续的事件
                watchKey.reset();
            } catch (InterruptedException e) {
                break;
            }
        }
    }



    private void sendSseMessage(HttpServletResponse response, String message) throws IOException {
        response.getWriter().write("data: " + message + "\n\n");
        response.flushBuffer();
    }
}