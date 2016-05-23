package Mail;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by evilisn(kasimok@163.com)) on 2016/5/18.
 */
@Component
@PropertySource ("classpath*:**/test.properties")
public class MailServiceTest {
    @Test
    public void checkMailServiceAvailable() throws Exception {
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(MailConfig.class);
        MailService ms = ctx.getBean(MailService.class);
        assertThat(ms.checkMailServiceAvailable(),is(true));
    }


    @Test
    public void placeOrder() throws Exception {
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(MailConfig.class);
        MailService ms = ctx.getBean(MailService.class);
        ms.sendTextMailNotification("kasimok@163.com", "Test MSG");
    }

    @Test
    public void testConfiguration() throws Exception {
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(MailConfig.class);
        MailService ms = ctx.getBean(MailService.class);
        assertThat(ms.getMailSender(), notNullValue());
        assertThat(ms.getTemplateMessage(), notNullValue());
    }




}