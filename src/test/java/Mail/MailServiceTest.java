package Mail;

import Models.Artwork;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.containsString;
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
    public void testNotifyNewArtworkPost() throws Exception {
        Artwork artwork = new Artwork();
        artwork.setAuthorComment("BLAH");
        ArrayList<URL> thumbnails = new ArrayList<>();
        for (String s : "http://www.itokoo.com/attachment/Mon_1603/21_3_afa086924aa0d34.jpg,http://www.itokoo.com/attachment/Mon_1603/21_3_db25b912a5c9d07.jpg,http://www.itokoo.com/attachment/Mon_1603/21_3_f50c0116a113e4c.jpg,http://www.itokoo.com/attachment/Mon_1603/21_3_d1e5e07e456e68e.jpg".split(",")) {
            thumbnails.add(new URL(s));
        }
        artwork.setThumbnailImgList(thumbnails);
        artwork.setModelNickname("Brindy");
        artwork.setTitle("[Beautyleg]美腿寫真 2016.03.30 No.1273 Brindy");
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(MailConfig.class);
        MailService ms = ctx.getBean(MailService.class);
        String mainContent = String.valueOf(ms.genNotifyForNewArtworkPost(artwork));
        assertThat(mainContent, containsString(artwork.getTitle()));
        assertThat(mainContent, containsString(artwork.getModelNickname()));
        assertThat(mainContent, containsString(artwork.getAuthorComment()));
        ms.sendHtmlMailNotification(new String[]{"kasimok@163.com"}, mainContent);
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