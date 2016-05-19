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
package Reposities;

import Models.Artwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by evilisn(kasimok@163.com)) on 2016/5/15.
 */
@Repository
@ComponentScan ({"./"})
public class ArtworkRepository {
    private static final Logger LOG = LoggerFactory.getLogger(ArtworkRepository.class);
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    private static final String TABLE_NAME = "models_artwork";


    private static final RowMapper<Artwork> ArtworkMapper = (rs, rowNum) -> {
        Artwork artwork = new Artwork();
        artwork.setArtId(rs.getInt("artwork_id"));
        artwork.setTitle(rs.getString("title"));
        artwork.setResolutionX(rs.getInt("resolution_x"));
        artwork.setResolutionY(rs.getInt("resolution_y"));
        artwork.setAuthorComment(rs.getString("author_comment"));
        try {
            artwork.setThreadAddress(new URL(rs.getString("thread_address")));
        } catch (MalformedURLException e) {
            LOG.error("Bad URL");
            artwork.setThreadAddress(null);
        }
        ArrayList<URL> arr = new ArrayList<>();
        if (!StringUtils.isEmpty(rs.getString("thumbnail_img_list"))) {
            for (String thumbnail_img_list_entry : StringUtils.split(rs.getString("thumbnail_img_list"), ",")) {
                try {
                    arr.add(new URL(thumbnail_img_list_entry));
                } catch (MalformedURLException e) {
                    LOG.error(String.format("Fail to cast [%s] to url", thumbnail_img_list_entry));
                }
            }
        }
        artwork.setThumbnailImgList(arr);
        artwork.setModelNickname(rs.getString("model_nickname"));
        artwork.setDataCreated(rs.getDate("data_created"));
        return artwork;
    };

    @Transactional (readOnly = true)
    public Artwork getArtwork(int artId) {
        final String SQL = "select * from " + TABLE_NAME + " where artwork_id=?";
        Artwork entry = null;
        try {
            entry = jdbcTemplate.queryForObject(SQL,
                    new Object[]{artId},
                    this.ArtworkMapper);
        } catch (DataAccessException e) {
//            LOG.error(e.getMessage());
            return null;
        }
        return entry;
    }

    /**
     * Insert a new arkwork, if art work exists, returns false;
     *
     * @param artwork
     *
     * @return success if added.
     */
    public void insertArtwork(Artwork artwork) {
        String sql = "INSERT INTO `" + TABLE_NAME + "`" +
                "(artwork_id," +
                "title," +
                "resolution_x," +
                "resolution_y," +
                "author_comment," +
                "thread_address," +
                "thumbnail_img_list," +
                "model_nickname," +
                "data_created)"+
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new Object[]{
                artwork.getArtId(),
                artwork.getTitle(),
                artwork.getResolutionX(),
                artwork.getResolutionY(),
                artwork.getAuthorComment(),
                artwork.getThreadAddress().toString(),
                artwork.getThumbnailImgList()==null?null:StringUtils.join(artwork.getThumbnailImgList(),","),
                artwork.getModelNickname(),
                artwork.getDataCreated()
        });
    }

    public List<Artwork> getAllPostOfModel(String modelNickname){
        String SQL = "SELECT * FROM "+TABLE_NAME+ " WHERE `model_nickname`=? ORDER BY `artwork_id` desc COLLATE NOCASE";
        List<Artwork> data = this.jdbcTemplate.query(SQL, new Object[]{modelNickname}, (rs, i) -> {
            Artwork artwork=new Artwork();
            artwork.setArtId(rs.getInt("artwork_id"));
            artwork.setTitle(rs.getString("title"));
            artwork.setResolutionX(rs.getInt("resolution_x"));
            artwork.setResolutionY(rs.getInt("resolution_y"));
            artwork.setAuthorComment(rs.getString("author_comment"));
            try {
                artwork.setThreadAddress(new URL(rs.getString("thread_address")));
            } catch (MalformedURLException e) {
                LOG.error("Bad URL");
                artwork.setThreadAddress(null);
            }
            ArrayList<URL> arr = new ArrayList<>();
            if (!StringUtils.isEmpty(rs.getString("thumbnail_img_list"))) {
                for (String thumbnail_img_list_entry : StringUtils.split(rs.getString("thumbnail_img_list"), ",")) {
                    try {
                        arr.add(new URL(thumbnail_img_list_entry));
                    } catch (MalformedURLException e) {
                        LOG.error(String.format("Fail to cast [%s] to url", thumbnail_img_list_entry));
                    }
                }
            }
            artwork.setThumbnailImgList(arr);
            artwork.setModelNickname(rs.getString("model_nickname"));
            artwork.setDataCreated(rs.getDate("data_created"));
            return artwork;
        });
        return data;
    }

}
