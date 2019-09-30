package com.cisco.webshell.ssh;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class ServletInit extends org.springframework.boot.web.servlet.support.SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WebSSHAppStarter.class);
	}


}
