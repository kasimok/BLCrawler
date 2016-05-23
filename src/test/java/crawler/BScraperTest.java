package crawler;

import org.junit.Test;

import java.net.URL;

/**
 * Created by evilisn(kasimok@163.com)) on 2016/5/17.
 */
public class BScraperTest {
    @Test
    public void processPage() throws Exception {
        URL url = new URL("http://www.beautyleg.com/photo/show.php?no=6");
        BScraper bScraper = new BScraper();
        System.out.println(bScraper.processPage(url).toString());
    }

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @org.junit.Test
    public void scrape() throws Exception {
        BScraper bScraper = new BScraper();
        System.out.println(bScraper.scrape(71).get(0).getQuery().split("=")[1]);

    }

    @org.junit.Test
    public void filter() throws Exception {
    }

    @org.junit.Test
    public void convert() throws Exception {
    }

}