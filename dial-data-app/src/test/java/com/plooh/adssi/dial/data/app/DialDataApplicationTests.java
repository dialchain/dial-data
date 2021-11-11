package com.plooh.adssi.dial.data.app;

import com.plooh.adssi.dial.data.DialDataApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = DialDataApplication.class)
@ActiveProfiles("test")
public class DialDataApplicationTests {

	@Test
	void contextLoads() {}
}
