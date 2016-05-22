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

import Models.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by evilisn(kasimok@163.com)) on 2016/5/15.
 */
@Repository
@ComponentScan ({"./"})
public class ImageReposity {
    private static final Logger LOG = LoggerFactory.getLogger(ImageReposity.class);
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    private static final String TABLE_NAME = "img";


    private static final RowMapper<Image> ImageMapper = (rs, rowNum) -> {
        Image image = new Image();
        image.setArtworkId(rs.getInt("artwork_id"));
        image.setDateCreated(rs.getDate("data_created"));
        image.setImageId(rs.getInt("image_id"));
        image.setRelativePath(rs.getString("relative_path"));
        image.setSha1(rs.getString("sha1"));
        return image;
    };

    @Transactional (readOnly = true)
    public Image getImage(int artwork_id,int imageId) {
        final String SQL = "select * from " + TABLE_NAME + " where image_id=? and artwork_id=?";
        Image entry = null;
        try {
            entry = jdbcTemplate.queryForObject(SQL,
                    new Object[]{imageId,artwork_id},
                    this.ImageMapper);
        } catch (DataAccessException e) {
            return null;
        }
        return entry;
    }

    /**
     * Insert a new image, if art work exists, returns false;
     *
     * @param image
     *
     * @return success if added.
     */
    public void insertImage(Image image) {
        String sql = "INSERT INTO `" + TABLE_NAME + "`" +
                "(image_id," +
                "artwork_id," +
                "sha1," +
                "relative_path," +
                "data_created)"+
                "VALUES " +
                "(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, new Object[]{
                image.getImageId(),
                image.getArtworkId(),
                image.getSha1(),
                image.getRelativePath(),
                image.getDateCreated()
        });
    }





}
