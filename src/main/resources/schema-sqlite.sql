DROP TABLE IF EXISTS models;
DROP TABLE IF EXISTS models_artwork;
DROP TABLE IF EXISTS users;

-- USERS
CREATE TABLE IF NOT EXISTS models (
  model_fullname VARCHAR(45),
  nickname       VARCHAR(45) NOT NULL,
  date_of_birth  DATE,
  update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (nickname)
);
-- Trigger
CREATE TRIGGER [UpdateLastTime]
AFTER UPDATE
ON models
FOR EACH ROW
  WHEN NEW.update_time < OLD.update_time --- this avoid infinite loop
BEGIN
  UPDATE models
  SET update_time = CURRENT_TIMESTAMP
  WHERE ActionId = OLD.ActionId;
END;
-- ART_WORK
CREATE TABLE IF NOT EXISTS models_artwork (
  artwork_id         INT PRIMARY KEY NOT NULL,
  title              VARCHAR(255),
  resolution_x       INT(6),
  resolution_y       INT(6),
  author_comment     TEXT,
  thread_address     VARCHAR(255),
  thumbnail_img_list TEXT,
  model_nickname     VARCHAR(45)     NOT NULL,
  data_created       DATETIME,
  update_time        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `FK_MODEL` FOREIGN KEY (`model_nickname`
  ) REFERENCES `models` (`nickname`
  )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);
-- Trigger2
CREATE TRIGGER [UpdateLastTime2]
AFTER UPDATE
ON models_artwork
FOR EACH ROW
  WHEN NEW.update_time < OLD.update_time --- this avoid infinite loop
BEGIN
  UPDATE models_artwork
  SET update_time = CURRENT_TIMESTAMP
  WHERE ActionId = OLD.ActionId;
END;

-- FreeArtwork
CREATE TABLE IF NOT EXISTS free_artwork (
  artwork_id     INT PRIMARY KEY NOT NULL,
  thread_address VARCHAR(255),
  img_list       TEXT,
  data_created   DATETIME
);
-- Trigger3
CREATE TRIGGER [UpdateLastTime3]
AFTER UPDATE
ON free_artwork
FOR EACH ROW
  WHEN NEW.update_time < OLD.update_time --- this avoid infinite loop
BEGIN
  UPDATE free_artwork
  SET update_time = CURRENT_TIMESTAMP
  WHERE ActionId = OLD.ActionId;
END;

-- image_file
-- FreeArtwork
CREATE TABLE IF NOT EXISTS img (
  image_id      INT(6),
  artwork_id    INT(6),
  sha1          VARCHAR(255) PRIMARY KEY,
  relative_path TEXT,
  data_created  DATETIME,
  update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `FK_ART_ID` FOREIGN KEY (`artwork_id`
  ) REFERENCES `free_artwork` (`artwork_id`
  )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE UNIQUE INDEX IF NOT EXISTS UNIQ_IMG ON img (image_id, artwork_id);