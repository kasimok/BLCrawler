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