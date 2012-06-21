# Users schema
 
# --- !Ups
 
CREATE TABLE User (
    userid varchar(255) NOT NULL,
    PRIMARY KEY (userid)
);

CREATE TABLE Tweet (
    tweetid varchar(255) NOT NULL,
    text varchar(255) NOT NULL,
    link varchar(255) NOT NULL,
    date int,
    PRIMARY KEY (tweetid)
);

CREATE TABLE User2User (
    userid varchar(255) NOT NULL,
    followerid varchar(255) NOT NULL,
);

CREATE TABLE User2Tweet (
    userid varchar(255) NOT NULL,
    tweetid varchar(255) NOT NULL,
);

CREATE INDEX idx_user2user 
  ON user2user(userid, followerid); 

CREATE INDEX idx_user2tweet 
  ON user2tweet(userid, tweetid); 

# --- !Downs
 
DROP TABLE User;

DROP TABLE Tweet;

DROP TABLE User2User;

DROP TABLE User2Tweet;

DROP INDEX idx_user2user ;

DROP INDEX idx_user2tweet ;