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
 * Created by evilisn_jiang(evilisn_jiang@trendmicro.com.cn)) on 2016/5/15.
 */
import Models.Artwork;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.annotation.Transformer;
import org.thymeleaf.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MessageEndpoint
public class Scraper {
    private final Pattern patter = Pattern.compile("\\[Beautyleg\\][^a-z0-9]*(?<year>\\d{4})\\.(?<month>\\d{2})\\.(?<day>\\d{2})\\sNo\\.(?<id>\\d{3,5})\\s+(?<model>\\w{3,20})");
    private final String ANCHOR_TEXT_PATTERN = "Beautyleg";
    private static final Logger log = LoggerFactory.getLogger(ArtworkRepository.class);
    @Splitter(inputChannel = "channel1", outputChannel = "channel2")
    public List<Element> scrape(ResponseEntity<String> payload) {
        String html = payload.getBody();
        final Document htmlDoc = Jsoup.parse(html);
        final Elements anchorNodes = htmlDoc.select("body").select("div[id^=read]").select("a");

        final List<Element> anchorList = new ArrayList<>();
        anchorNodes.traverse(new NodeVisitor() {
            @Override
            public void head(org.jsoup.nodes.Node node, int depth) {
                if (node instanceof org.jsoup.nodes.Element) {
                    Element e = (Element)node;
                    if (StringUtils.containsIgnoreCase(e.text(),ANCHOR_TEXT_PATTERN, Locale.US)) {
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

    @Filter(inputChannel = "channel2", outputChannel = "channel3")
    public boolean filter(Element payload) {
        Matcher m = patter.matcher(payload.text());
        if (m.find()) {
            return true;
        }else{
            log.error(String.format("Anchor text dose not match pattern:[%s]", payload.text()));
            return false;
        }
    }

    @Transformer(inputChannel = "channel3", outputChannel = "channel4")
    public Artwork convert(Element payload) throws ParseException, MalformedURLException {
        Matcher m = patter.matcher(payload.text());
        if (m.find()) {
            String year = m.group("year");
            String month =m.group("month");
            String day = m.group("day");
            int id = Integer.parseInt(m.group("id"));
            String model = m.group("model");
            URL link = new URL(payload.attr("href"));
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            Date date = format.parse(String.format("%s-%s-%s", year,month,day));
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
        }else{
            log.error(payload.text());
            return null;
        }

    }
}
