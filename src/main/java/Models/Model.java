package Models;/*
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is for models only.
 * Created by evilisn_jiang(evilisn_jiang@trendmicro.com.cn)) on 2016/5/15.
 */
public class Model {
    private String modelFullname;
    private String nickname;
    private Date dateOfBirth;

    public Model(String modelFullname, String nickname, Date dateOfBirth) {
        this.modelFullname = modelFullname;
        this.nickname = nickname;
        this.dateOfBirth = dateOfBirth;
    }

    public Model(String nickname) {
        this.nickname = nickname;
    }

    public Model() {
    }

    public String getModelFullname() {
        return modelFullname;
    }

    public void setModelFullname(String modelFullname) {
        this.modelFullname = modelFullname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public String toString() {
        return "Model{" +
                "modelFullname='" + modelFullname + '\'' +
                ", nickname='" + nickname + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
