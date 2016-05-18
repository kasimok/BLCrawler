package crawler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by evilisn_jiang(evilisn_jiang@trendmicro.com.cn)) on 2016/5/17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringApplicationConfiguration(classes = {CrawlerConfig.class,RestTemplate.class})
public class DownloaderTest {
    @Autowired
    private CrawlerConfig config;
    @Autowired
    private RestTemplate template;
    @Test
    public void downloadIndex() throws Exception {
        Downloader downloader=new Downloader();
        downloader.setConfig(config);
        downloader.setTemplate(template);
        assertThat(downloader.download().getBody(),containsString("[Beautyleg]"));
    }

    @Test
    public void downloadEachThread() throws Exception {
        Downloader downloader=new Downloader();
        config.setUrl("http://www.itokoo.com/read.php?tid=29155");
        downloader.setConfig(config);
        downloader.setTemplate(template);

        ResponseEntity<String> payload= downloader.download();
        assertThat(payload.getBody(),containsString("[Beautyleg]"));
//        ThreadPageScraper threadPageScraper = new ThreadPageScraper();
//        System.out.println(threadPageScraper.scrape(payload).toString());
    }

    @Test
    public void name() throws Exception {
        System.out.println(String.format("No.%03d", 1));
        System.out.println(String.format("No.%03d", 1234));
    }
}