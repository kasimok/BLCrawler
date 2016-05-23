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

import Models.Image;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MessageEndpoint
public class BScraper {
    private static final Logger LOG = LoggerFactory.getLogger(BScraper.class);

    @Splitter (inputChannel = "channel2-1", outputChannel = "channel2-2")
    public ArrayList<URL> scrape(int maxThreadID) {
        final String urlPrefix = "http://www.beautyleg.com/photo/show.php?no=";
        ArrayList<URL> possibleThreadLists = new ArrayList<>();
        while (maxThreadID > 0) {
            try {
                possibleThreadLists.add(new URL(urlPrefix + maxThreadID));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            maxThreadID--;
        }
        return possibleThreadLists;
    }

    @Filter (inputChannel = "channel2-2", outputChannel = "channel2-3")
    public boolean filter(URL url) {
        if (url==null) {
            return false;
        }else{
            return true;
        }
    }

    @Autowired
    private ImageReposity imageReposity;

    @ServiceActivator (inputChannel = "channel2-3", outputChannel = "channel2-4")
    public List<String> processPage(URL url) {
        LOG.info(">>>Processing URL: "+url.toString());
        final Pattern imgPattern = Pattern.compile("/(?<id>\\d{1,4})\\.jpg");
        RestTemplate template = new RestTemplate();
        int ART_WORK_ID = -1;
        try {
            ART_WORK_ID = Integer.parseInt(url.getQuery().split("=")[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        File artworkFolder = new File("Beautyleg" + File.separator + "Free" + File.separator + String.format("No.%03d", ART_WORK_ID));
        if (!artworkFolder.exists() || !artworkFolder.isDirectory()) {
            LOG.debug(artworkFolder.getPath());
            artworkFolder.mkdirs();
            //New post
        }
        ResponseEntity<String> entity;
        try {
            entity = template.getForEntity(url.toString(), String.class);
        } catch (RestClientException e) {
            LOG.error("Forbidden resource.");
            return new ArrayList<>();
        }
        String html = entity.getBody();

        final Document htmlDoc = Jsoup.parse(html);
        final Elements imageElements = htmlDoc.select("table.table_all a");
        if (imageElements.size() > 0) {
            final List<String> anchorList = new ArrayList<>();
            int finalART_WORK_ID = ART_WORK_ID;
            imageElements.traverse(new NodeVisitor() {
                @Override
                public void head(org.jsoup.nodes.Node node, int depth) {
                    if (node instanceof org.jsoup.nodes.Element) {
                        Element e = (Element) node;
                        Matcher m = imgPattern.matcher(e.attr("href"));
                        if (m.find()) {
//                            if (null == imageReposity.getImage(finalART_WORK_ID, Integer.parseInt(m.group("id")))) {
                                //Download New
                                anchorList.add(e.attr("href"));
//                            } else {
//                                Existed...
//                            }
                        } else {
                            LOG.info(e + " not match,SKIP!");
                        }
                    }
                }

                @Override
                public void tail(Node node, int depth) {
                }
            });
            anchorList.parallelStream().forEach(o -> {
                RestTemplate restTemplate = new RestTemplate();
                String baseName = FilenameUtils.getBaseName(o.toString());
                String extension = FilenameUtils.getExtension(o.toString());
                boolean isJpeg = extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg");
                File img = new File(artworkFolder + File.separator + baseName + "." + extension);
                if (img.isFile() && img.exists()) {
                    LOG.debug("Skipping existing file " + img.getPath());
                } else if (!isJpeg) {
                    LOG.debug("Skipping none jpg file " + baseName + "." + extension);
                } else {
                    LOG.info("Downloading new image: " + img.toPath() + ",URL " + o);
                    byte[] imageBytes = restTemplate.getForObject(o.toString(), byte[].class);
                    try {
                        Files.write(img.toPath(), imageBytes);
                        LOG.info("Downloaded new image: " + img.toPath() + ",URL " + o + " Size:" + FileUtils.byteCountToDisplaySize(imageBytes.length));
                        Image image = new Image();
                        MessageDigest md = MessageDigest.getInstance("SHA-1");
                        image.setSha1(byteArray2Hex(md.digest(imageBytes)));
                        image.setDateCreated(new Date());
                        image.setRelativePath(artworkFolder + File.separator + baseName + "." + extension);
                        image.setArtworkId(finalART_WORK_ID);
                        Matcher m = imgPattern.matcher(o.toString());
                        if (m.find()) {
                            image.setImageId(Integer.parseInt(m.group("id")));
                            imageReposity.insertImage(image);
                        } else {
                        }
                    } catch (IOException e) {
                        LOG.error("IOE");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            });
            return anchorList;
        } else {
            return null;
        }
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
