-- create table

CREATE TABLE IF NOT EXISTS `item` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `url` VARCHAR(200) NOT NULL,
    `comment` VARCHAR(200) NOT NULL,
    PRIMARY KEY (`id`))
ENGINE = InnoDB;