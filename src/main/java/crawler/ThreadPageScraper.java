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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to scrap the thread pages.
 */
public class ThreadPageScraper {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadPageScraper.class);
    private static final Pattern PATTER_RESOLUTION = Pattern.compile("\\[(?<resX>\\d+)×(?<resY>\\d+)\\]");
    private static final Pattern PATTER_ART_SIZE = Pattern.compile("\\[(?<numPic>\\d+)P/(?<diskSize>\\d+)M\\]");

    public static final String KEY_RESOLUTION_X = "RES_X";
    public static final String KEY_RESOLUTION_Y = "RES_Y";
    public static final String KEY_NUM_OF_ARTWORK = "NUM_OF_ART";
    public static final String KEY_ART_SIZE = "ART_SIZE";
    public static final String KEY_THUMBNAILS = "THUMBNAILS";
    public static final String KEY_AUTHOR_DESCRIPTION = "DESCRIPTION";

    public TreeMap<String, Object> scrape(String url) {
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> entity = template.getForEntity(url, String.class);
        TreeMap<String, Object> results = new TreeMap<>();
        String html = entity.getBody();
        //The site is not using utf-8 encoding
        Document htmlDoc;
        try {
            htmlDoc = Jsoup.parse(new String(html.getBytes("ISO-8859-1"), "GBK"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported page encoding.");
            return null;
        }
        final Elements TOP_POST_DIV = htmlDoc.select("div#read_tpc");
        final Elements THUMBNAILS = TOP_POST_DIV.select("img");
        //Now traverse the ThumbNails to get a list
        final ArrayList<URL> THUMBNAIL_URLS = new ArrayList<>();
        THUMBNAILS.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int i) {
                if (node instanceof Element) {
                    Element e = (Element) node;
                    try {
                        THUMBNAIL_URLS.add(new URL(e.attr("src").indexOf('?') > 0 ? e.attr("src").substring(0, e.attr("src").indexOf("?")) : e.attr("src")));
                    } catch (MalformedURLException e1) {
                        LOG.error(String.format("Bad url format[%s]", e.attr("src")));
                    }
                }
            }

            @Override
            public void tail(Node node, int i) {
            }
        });
        results.put(KEY_THUMBNAILS, THUMBNAIL_URLS);
        final Element FIRST_FONT_TAG = TOP_POST_DIV.select("font:first-child").get(0);
        Matcher m_patter_resolution = PATTER_RESOLUTION.matcher(FIRST_FONT_TAG.html());
        Matcher m_patter_art_size = PATTER_ART_SIZE.matcher(FIRST_FONT_TAG.html());
        if (m_patter_art_size.find()) {
            results.put(KEY_NUM_OF_ARTWORK, Integer.valueOf(m_patter_art_size.group("numPic")));
            results.put(KEY_ART_SIZE, Integer.valueOf(m_patter_art_size.group("diskSize")));
        } else {
            LOG.error(FIRST_FONT_TAG.html());
            results.put(KEY_NUM_OF_ARTWORK, -1);
            results.put(KEY_ART_SIZE, -1);

        }
        if (m_patter_resolution.find()) {
            results.put(KEY_RESOLUTION_X, Integer.valueOf(m_patter_resolution.group("resX")));
            results.put(KEY_RESOLUTION_Y, Integer.valueOf(m_patter_resolution.group("resY")));

        } else {
            LOG.error(FIRST_FONT_TAG.html());
            results.put(KEY_RESOLUTION_X, -1);
            results.put(KEY_RESOLUTION_Y, -1);

        }
        final Elements authorComments = htmlDoc.select("body div#read_tpc>span[style*='color']");
        if (authorComments.size() > 0) {
            results.put(KEY_AUTHOR_DESCRIPTION, authorComments.get(0).text());
        } else {
            results.put(KEY_AUTHOR_DESCRIPTION, "");
        }
        return results;
    }


}
