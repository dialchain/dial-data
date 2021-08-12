package com.plooh.adssi.dial.data;

import com.plooh.adssi.dial.data.config.MapStoreConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = MessagingApplication.class)
@ContextConfiguration(classes = {MapStoreConfig.class})
public class MessagingApplicationTests {
}
