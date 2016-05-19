/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package crawler;
/**
 * Created by evilisn(kasimok@163.com)) on 2016/5/15.
 */

import Mail.MailConfig;
import Mail.MailService;
import Models.Artwork;
import Models.Model;
import Reposities.ArtworkRepository;
import Reposities.ModelRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.annotation.Transformer;
import org.thymeleaf.util.StringUtils;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MessageEndpoint
@ComponentScan ({"./"})
public class AScraper {
    private final Pattern patter = Pattern.compile("\\[Beautyleg\\][^a-z0-9]*(?<year>\\d{4})\\.(?<month>\\d{2})\\.(?<day>\\d{2})\\sNo\\.(?<id>\\d{3,5})\\s+(?<model>.{2,20})");
    private final String ANCHOR_TEXT_PATTERN = "Beautyleg";
    private static final Logger LOG = LoggerFactory.getLogger(AScraper.class);

    @Splitter (inputChannel = "channel1", outputChannel = "channel2")
    public List<Element> scrape(ResponseEntity<String> payload) {
        String html = payload.getBody();
        final Document htmlDoc;
        try {
            htmlDoc = Jsoup.parse(new String(html.getBytes("ISO-8859-1"), "GBK"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported page encoding.");
            return null;
        }
        final Elements anchorNodes = htmlDoc.select("body").select("div[id^=read]").select("a");
        final List<Element> anchorList = new ArrayList<>();
        anchorNodes.traverse(new NodeVisitor() {
            @Override
            public void head(org.jsoup.nodes.Node node, int depth) {
                if (node instanceof org.jsoup.nodes.Element) {
                    Element e = (Element) node;
                    if (StringUtils.containsIgnoreCase(e.text(), ANCHOR_TEXT_PATTERN, Locale.US)) {
                        anchorList.add(e);
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
            }
        });
        return anchorList;
    }

    @Filter (inputChannel = "channel2", outputChannel = "channel3")
    public boolean filter(Element payload) {
        Matcher m = patter.matcher(payload.text());
        if (m.find()) {
            return true;
        } else {
            LOG.error(String.format("Anchor text dose not match pattern:[%s]", payload.text()));
            return false;
        }
    }

    @Transformer (inputChannel = "channel3", outputChannel = "channel4")
    public Artwork convert(Element payload) throws ParseException, MalformedURLException {
        Matcher m = patter.matcher(payload.text());
        if (m.find()) {
            String year = m.group("year");
            String month = m.group("month");
            String day = m.group("day");
            int id = Integer.parseInt(m.group("id"));
            String model = m.group("model").split("[\\s\\[\\]]")[0];
            URL link = new URL(payload.attr("href"));
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            Date date = format.parse(String.format("%s-%s-%s", year, month, day));
            String thread_title = payload.text();
            return new Artwork(thread_title,
                    id,
                    -1,
                    -1,
                    null,
                    link,
                    null,
                    model,
                    date
            );
        } else {
            LOG.error(payload.text());
            return null;
        }

    }

    @Autowired
    private ArtworkRepository artworkRepository;
    @Autowired
    private ModelRepository modelRepository;


    /**
     * Insert new art work and new model to db if not exist.
     * @param artwork
     * @return  artwork if not exist in db
     * @throws ParseException
     * @throws MalformedURLException
     */
    @Transformer (inputChannel = "channel4", outputChannel = "channel5")
    public Artwork processNew(Artwork artwork) throws ParseException, MalformedURLException {
        if (modelRepository.getModelByName(artwork.getModelNickname()) == null) {
            LOG.info(String.format("Model=%s,not exist in db,inserting", artwork.getModelNickname()));
            modelRepository.insertModel(new Model(
                    artwork.getModelNickname()
            ));
        }
        Artwork artwork_db =artworkRepository.getArtwork(artwork.getArtId());
        if (artwork_db == null) {
            ThreadPageScraper threadPageScraper = new ThreadPageScraper();
            TreeMap<String, Object> results = threadPageScraper.scrape(artwork.getThreadAddress().toString());
            artwork.setThumbnailImgList((ArrayList<URL>) results.get(ThreadPageScraper.KEY_THUMBNAILS));
            artwork.setAuthorComment((String) results.get(ThreadPageScraper.KEY_AUTHOR_DESCRIPTION));
            artwork.setResolutionX((Integer) results.get(ThreadPageScraper.KEY_RESOLUTION_X));
            artwork.setResolutionY((Integer) results.get(ThreadPageScraper.KEY_RESOLUTION_Y));
            LOG.info("Artwork={},new post, Notify user...", artwork);
            ApplicationContext ctx =
                    new AnnotationConfigApplicationContext(MailConfig.class);
            MailService mailService = ctx.getBean(MailService.class);
            try {
                mailService.sendHtmlMailNotification("kasimok@163.com".split(";"), String.valueOf(mailService.genNotifyForNewArtworkPost(artwork)));
            } catch (MessagingException e) {
                return artwork;
            }
            LOG.info("Artwork={},not exist in db,inserting", artwork);
            artworkRepository.insertArtwork(artwork);
            return artwork;
        }else{
            return artwork_db;
        }
    }
}
