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

import Models.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by evilisn(kasimok@163.com)) on 2016/5/15.
 */
@Repository
public class ModelRepository {
    private static final Logger log = LoggerFactory.getLogger(ModelRepository.class);
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    private static final String TABLE_NAME = "models";


    private static final RowMapper<Model> ModelMapper = (rs, rowNum) -> {
        Model model = new Model();
        model.setDateOfBirth(rs.getDate("date_of_birth"));
        model.setNickname(rs.getString("nickname"));
        model.setModelFullname(rs.getString("model_fullname"));
        return model;
    };

    @Transactional (readOnly = true)
    public Model getModelByName(String nickname) {
        final String SQL = "select * from " + TABLE_NAME + " where nickname=?";
        Model entry = null;
        try {
            entry = jdbcTemplate.queryForObject(SQL,
                    new Object[]{nickname},
                    this.ModelMapper);
        } catch (DataAccessException e) {
            return null;
        }
        return entry;
    }

    /**
     * Insert a new model, if art work exists, returns false;
     *
     * @param model
     *
     * @return success if added.
     */
    public void insertModel(Model model) {
        String SQL = "INSERT INTO `" + TABLE_NAME + "`" +
                "(model_fullname," +
                "nickname," +
                "date_of_birth)" +
                "VALUES " +
                "(?, ?, ?)";
        jdbcTemplate.update(SQL, new Object[]{
                model.getModelFullname(),
                model.getNickname(),
                model.getDateOfBirth()
        });
    }

    public List<String> getExistedModels(){
        String SQL = "SELECT `nickname` FROM "+TABLE_NAME;
        List<String> data = this.jdbcTemplate.queryForList(SQL,String.class);
        return data;
    }


}
