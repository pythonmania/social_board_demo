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
    PRIMARY KEY (userid),
    FOREIGN KEY (followerid) REFERENCES User (userid)
);

CREATE TABLE User2Tweet (
    userid varchar(255) NOT NULL,
    tweetid varchar(255) NOT NULL,
    PRIMARY KEY (userid),
    FOREIGN KEY (tweetid) REFERENCES Tweet (tweetid)
);

# --- !Downs
 
DROP TABLE User;

DROP TABLE Tweet;

DROP TABLE User2User;

DROP TABLE User2Tweet;