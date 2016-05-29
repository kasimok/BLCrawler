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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by evilisn(kasimok@163.com)) on 2016/5/15.
 */
@MessageEndpoint
public class Downloader {
    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);

    @Autowired
    private CrawlerConfig config;

    @Autowired
    private RestTemplate template;

    @InboundChannelAdapter(value = "channel1", poller = @Poller("downloadIndexTrigger"))
    public ResponseEntity<String> download() {
        String url = config.getUrl();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        LOG.info(">>> Wake up @"+dateFormat.format(cal.getTime()));

        ResponseEntity<String> entity = this.template.getForEntity(url, String.class);
        return entity;
    }
    public void setConfig(CrawlerConfig config) {
        this.config = config;
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }
}
