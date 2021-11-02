package com.plooh.adssi.dial.data.app;

import com.plooh.adssi.dial.data.DialDataConfig;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.utils.BriefLogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication(scanBasePackageClasses = { DialDataConfig.class })
public class DialDataApplication {

	private static final Logger log = LoggerFactory.getLogger(DialDataApplication.class);

	public static void main(String[] args) throws UnknownHostException {
		SpringApplication app = new SpringApplication(DialDataApplication.class);
		Environment env = app.run(args).getEnvironment();
		printAppInfo(env);
	}

	@Autowired
	private void startNode(PeerGroup peerGroup) {
		BriefLogFormatter.init();
		// Starts the PeerGroup and begins network activity.
		peerGroup.start();
	}

	private static void printAppInfo(Environment env) throws UnknownHostException {
		String protocol = "http";

		//@formatter:off
		log.info("\n----------------------------------------------------------\n\t" +
				"Application '{}' is running! Access URLs:\n\t" +
				"Local: \t\t{}://localhost:{}{}\n\t" +
				"External: \t{}://{}:{}{}\n\t" +
				"Profile(s): \t{}" +
				"\n----------------------------------------------------------",
			env.getProperty("spring.application.name"),
			protocol,
			env.getProperty("server.port"),
			StringUtils.trimToEmpty(env.getProperty("server.servlet.context-path")),
			protocol,
			InetAddress.getLocalHost().getHostAddress(),
			env.getProperty("server.port"),
			StringUtils.trimToEmpty(env.getProperty("server.servlet.context-path")),
			env.getActiveProfiles());
	}

}
