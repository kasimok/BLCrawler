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
package Mail;
/**
 * Created by evilisn_jiang(evilisn_jiang@trendmicro.com.cn)) on 2016/5/18.
 */

import Models.Artwork;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.StringWriter;

public class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);
    private JavaMailSenderImpl mailSender;
    private SimpleMailMessage templateMessage;

    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    public void setTemplateMessage(SimpleMailMessage templateMessage) {
        this.templateMessage = templateMessage;
    }

    public boolean sendTextMailNotification(String to, String text) {
        // Create a thread safe "copy" of the template message and customize it
        SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
        msg.setTo(to);
        msg.setText(text);
        try {
            this.mailSender.send(msg);
            return true;
        } catch (MailException ex) {
            LOG.error("Notification send failed," + ex.getMessage());
            return false;
        }
    }

    public boolean sendHtmlMailNotification(String to, String text) throws MessagingException {
        MimeMessage mimeMessage=this.mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,true,"UTF-8");
        messageHelper.setTo(to);
        messageHelper.setFrom(this.templateMessage.getFrom());
        messageHelper.setSubject(this.templateMessage.getSubject());
        messageHelper.setText(text, true);
        try {
            this.mailSender.send(messageHelper.getMimeMessage());
        } catch (MailException e) {
            LOG.error("Notification send failed," + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Generate notification user about new post.
     *
     * @return
     */

    public StringWriter notifyNewArtworkPost(Artwork artwork) {
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        VelocityContext context = new VelocityContext();
        context.put("thumbnailList", artwork.getThumbnailImgList());
        context.put("model",artwork.getModelNickname());
        context.put("comment",artwork.getAuthorComment());
        context.put("title",artwork.getTitle());
        Template t = ve.getTemplate("src/main/resources/email_templates/email_html.vm");
        StringWriter writer = new StringWriter();
        t.merge(context, writer);
        return writer;
    }

}