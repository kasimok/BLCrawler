-- Create Database
CREATE DATABASE IF NOT EXISTS beautyleg
  CHARACTER SET utf8;

USE beautyleg;

-- USERS
CREATE TABLE IF NOT EXISTS models (
  model_fullname VARCHAR(45) NULL,
  nickname       VARCHAR(45) NOT NULL,
  date_of_birth  DATE        NULL,
  update_time    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (nickname)
);

-- ART_WORK
CREATE TABLE IF NOT EXISTS models_artwork (
  artwork_id         SERIAL,
  title              VARCHAR(255) NULL,
  resolution_x       INT(6),
  resolution_y       INT(6),
  author_comment     TEXT         NULL,
  thread_address     VARCHAR(255) NULL,
  thumbnail_img_list TEXT         NULL,
  model_nickname     VARCHAR(45)  NOT NULL,
  date_created       DATE         NULL,
  update_time        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (artwork_id),
  KEY `FK_MODEL` (`model_nickname`),
  CONSTRAINT `FK_MODEL` FOREIGN KEY (`model_nickname`) REFERENCES `models` (`nickname`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

