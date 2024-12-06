use rel;

CREATE TABLE Person (
	name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    SSN VARCHAR(50) NOT NULL,
    PRIMARY KEY(SSN)
);

explain Person;

CREATE TABLE Movie (
	Release_year INT NOT NULL,
    Rating INT NOT NULL,
    Title VARCHAR(50) NOT NULL,
    PRIMARY KEY(Title)
);

explain Movie;

CREATE TABLE Art (
	SSN VARCHAR(50) NOT NULL,
    Title VARCHAR(50) NOT NULL,
	PRIMARY KEY(SSN, Title),
    FOREIGN KEY(SSN) REFERENCES Person(SSN),
    FOREIGN KEY(Title) REFERENCES Movie(Title)
);

explain Art;

INSERT INTO Person (name, age, SSN) 
VALUES ("John", 58, "1234567");

SELECT * FROM Person;

DROP TABLE Art;
DROP TABLE Movie;
