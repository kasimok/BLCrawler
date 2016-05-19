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
 * This is for album art work only.
 * Created by evilisn(kasimok@163.com)) on 2016/5/15.
 */
public class Artwork {
    private int artId;
    private String title;
    private int resolutionX;
    private int resolutionY;
    private String authorComment;
    private URL threadAddress;
    private ArrayList<URL> thumbnailImgList;
    private String modelNickname;
    private Date dataCreated;

    public Date getDataCreated() {
        return dataCreated;
    }

    public void setDataCreated(Date dataCreated) {
        this.dataCreated = dataCreated;
    }


    public Artwork() {
    }

    public Artwork(String title, int artId, int resolutionX, int resolutionY, String authorComment, URL threadAddress, ArrayList<URL> thumbnailImgList, String modelNickname, Date dataCreated) {
        this.title = title;
        this.artId = artId;
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        this.authorComment = authorComment;
        this.threadAddress = threadAddress;
        this.thumbnailImgList = thumbnailImgList;
        this.modelNickname = modelNickname;
        this.dataCreated = dataCreated;
    }

    @Override
    public String toString() {
        return "Artwork{" +
                "artId=" + artId +
                ", resolution=[" + resolutionX + "*" + resolutionY + "]" +
                ", threadAddress=" + threadAddress +
                ", modelNickname='" + modelNickname + '\'' +
                '}';
    }

    public int getArtId() {
        return artId;
    }

    public void setArtId(int artId) {
        this.artId = artId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getResolutionX() {
        return resolutionX;
    }

    public void setResolutionX(int resolutionX) {
        this.resolutionX = resolutionX;
    }

    public int getResolutionY() {
        return resolutionY;
    }

    public void setResolutionY(int resolutionY) {
        this.resolutionY = resolutionY;
    }

    public String getAuthorComment() {
        return authorComment;
    }

    public void setAuthorComment(String authorComment) {
        this.authorComment = authorComment;
    }

    public URL getThreadAddress() {
        return threadAddress;
    }

    public void setThreadAddress(URL threadAddress) {
        this.threadAddress = threadAddress;
    }

    public ArrayList<URL> getThumbnailImgList() {
        return thumbnailImgList;
    }

    public void setThumbnailImgList(ArrayList<URL> thumbnailImgList) {
        this.thumbnailImgList = thumbnailImgList;
    }

    public String getModelNickname() {
        return modelNickname;
    }

    public void setModelNickname(String modelNickname) {
        this.modelNickname = modelNickname;
    }

}
