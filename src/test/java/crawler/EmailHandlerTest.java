package crawler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

/**
 * Created by evilisn_jiang on 17/5/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringApplicationConfiguration(classes = {CrawlerConfig.class, RestTemplate.class})
public class EmailHandlerTest {
    @Autowired
    ApplicationContext context;
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSendNewThreadNotification() throws Exception {

        //Get the mailer instance
        EmailHandler mailer = (EmailHandler) context.getBean("mailService");

        //Send a composed mail
        mailer.sendMail("somebody@gmail.com", "Test Subject", "Testing body");

        //Send a pre-configured mail
        mailer.sendPreConfiguredMail("Exception occurred everywhere.. where are you ????");
    }
}