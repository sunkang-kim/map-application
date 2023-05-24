-- tripSchedule 테이블 생성
CREATE TABLE tripSchedule (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              title VARCHAR(255) NOT NULL,
                              startDate DATE NOT NULL,
                              endDate DATE NOT NULL,
                              userId INT NOT NULL
);

-- tripDailySchedule 테이블 생성
CREATE TABLE tripDailySchedule (
                                   id INT PRIMARY KEY AUTO_INCREMENT,
                                   parentId INT NOT NULL,
                                   title VARCHAR(255) NOT NULL,
                                   tripDate DATE NOT NULL,
                                   FOREIGN KEY (parentId) REFERENCES tripSchedule(id)
);


--- 트리거
DROP TRIGGER IF EXISTS `mapapp`.`tripSchedule_BEFORE_INSERT`;

CREATE TRIGGER `mapapp`.`tripSchedule_BEFORE_INSERT` BEFORE INSERT ON `tripSchedule` FOR EACH ROW
BEGIN
  DECLARE currentDate DATE;
  DECLARE sortOrder INT DEFAULT 1;
  SET currentDate = NEW.startDate;

  WHILE currentDate <= NEW.endDate DO
    INSERT INTO tripDailySchedule (parentId, title, date, sortOrder)
    VALUES (NEW.id, 'Daily Schedule', currentDate, sortOrder);
    SET currentDate = currentDate + INTERVAL 1 DAY;
    SET sortOrder = sortOrder + 1;
END WHILE;
END;
