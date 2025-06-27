package com.fffattiger.wechatbot.infrastructure.config;


import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;
import com.fffattiger.wechatbot.shared.properties.DatabaseInitProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Configuration
@EnableConfigurationProperties({ChatBotProperties.class, DatabaseInitProperties.class})
@Slf4j
@RequiredArgsConstructor
public class ChatBotConfiguration {

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean
    public PluginManager pluginManager() {
        return new DefaultPluginManager();
    }

    @Bean
    public RestClient.Builder restClientBuilder(ChatBotProperties chatBotProperties) {
        RestClient.Builder builder = RestClient.builder();
        ChatBotProperties.Proxy proxyConfig = chatBotProperties.getProxy();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = chatBotProperties.getHttpTimeout();
        requestFactory.setConnectTimeout((int) timeout.toMillis());
        requestFactory.setReadTimeout((int) timeout.toMillis());

        if (proxyConfig.isEnabled() && StringUtils.hasText(proxyConfig.getHost())) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort()));
            requestFactory.setProxy(proxy);
        }
        builder.requestFactory(requestFactory);

        return builder;
    }

    @Bean
    public WebClient.Builder webClientBuilder(ChatBotProperties chatBotProperties) {
        WebClient.Builder builder = WebClient.builder();
        ChatBotProperties.Proxy proxyConfig = chatBotProperties.getProxy();
        HttpClient httpClient = HttpClient.create().responseTimeout(chatBotProperties.getHttpTimeout());
        if (proxyConfig.isEnabled() && StringUtils.hasText(proxyConfig.getHost())) {
            httpClient = httpClient
                    .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                            .host(proxyConfig.getHost())
                            .port(proxyConfig.getPort())
                    );
        }
        builder.clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient));
        return builder;
    }


}
