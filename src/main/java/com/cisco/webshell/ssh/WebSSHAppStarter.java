package com.cisco.webshell.ssh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

/**@version 1.0.0
 * @apiNote AppStarter
 * @author prassin3
 *
 */
@SpringBootApplication
@EnableWebSocket

public class WebSSHAppStarter implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// TODO Auto-generated method stub
		registry.addHandler(handler(), "/shell").withSockJS();
		
	}
	
    @Bean
    public WebSocketHandler handler() {
        return new PerConnectionWebSocketHandler(WebSSH.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(WebSSHAppStarter.class, args);
    }


}
