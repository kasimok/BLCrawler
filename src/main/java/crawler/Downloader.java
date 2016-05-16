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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.web.client.RestTemplate;

/**
 * Created by evilisn_jiang(evilisn_jiang@trendmicro.com.cn)) on 2016/5/15.
 */
@MessageEndpoint
public class Downloader {
    @Autowired
    private CrawlerConfig config;

    @Autowired
    private RestTemplate template;

    @InboundChannelAdapter(value = "channel1", poller = @Poller("downloadIndexTrigger"))
    public ResponseEntity<String> download() {
        String url = config.getUrl();
        ResponseEntity<String> entity = template.getForEntity(url, String.class);
        return entity;
    }

}
