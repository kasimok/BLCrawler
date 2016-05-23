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
import org.apache.commons.lang.StringUtils;
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
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by evilisn(kasimok@163.com)) on 2016/5/15.
 */
@MessageEndpoint
public class Downloader {
    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);
    @Autowired
    private ItokooCrawlerConfig config;

    @Autowired
    private FreeCrawlerConfig freeConfig;
    @Autowired
    private RestTemplate template;

//    @InboundChannelAdapter(value = "channel1", poller = @Poller("downloadIndexTrigger"))
    public ResponseEntity<String> download() {
        String url = config.getUrl();
        ResponseEntity<String> entity = template.getForEntity(url, String.class);
        return entity;
    }
    @InboundChannelAdapter(value = "channel2-1", poller = @Poller("downloadFreeTrigger"))
    public int downloadFree() {
        String url = freeConfig.getUrl();
        final Pattern patterFree = Pattern.compile("Free\\s+download\\sVol.(?<id>\\d{1,4})");
        ResponseEntity<String> entity = template.getForEntity(url, String.class);
        String html = entity.getBody();
        final Document htmlDoc;
        try {
            htmlDoc = Jsoup.parse(new String(html.getBytes("ISO-8859-1"), "GBK"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported page encoding.");
            return -1;
        }
        final Elements anchorNodes = htmlDoc.select(".table_all div.table02 li a");
        final ArrayList<Integer> ids = new ArrayList<>();
        final List<Element> anchorList = new ArrayList<>();
        anchorNodes.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof Element) {
                    Element e = (Element) node;
                    Matcher m = patterFree.matcher(e.text());
                    if (m.find()) {
                        LOG.debug("Matching Free:"+m.group("id"));
                        ids.add(new Integer(Integer.valueOf(m.group("id"))));
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
            }
        });
        return Collections.max(ids);
    }
    public void setConfig(ItokooCrawlerConfig config) {
        this.config = config;
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}
