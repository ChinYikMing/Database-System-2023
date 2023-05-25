CREATE TABLE Dishes (
    did INTEGER not null,
    dname VARCHAR(255) not null,
    origin VARCHAR(255) not null,
    popularity INTEGER not null,
    primary key(did)
);

INSERT INTO Dishes (did, dname, origin, popularity)
VALUES (1, 'Spaghetti', 'Italy', 8),
       (2, 'Sushi', 'Japan', 9),
       (3, 'Tacos', 'Mexico', 7),
       (4, 'Paella', 'Spain', 6),
       (5, 'Pad Thai', 'Thailand', 8),
       (6, 'Burger', 'USA', 10001);
       
      
CREATE TABLE Ingredients (
    iid INTEGER not null,
    iname VARCHAR(255) not null,
    unitprice INTEGER  not null,
    primary key(iid)
);
INSERT INTO Ingredients (iid, iname, unitprice)
VALUES 
    (1, 'Pasta', 2),
    (2, 'Tomatoes', 3),
    (3, 'saffron', 50),
    (4, 'Rice', 1),
    (5, 'sugar', 4),
    (6, 'butter', 6),
    (7, 'Beef', 55),
    (8, 'Chicken', 7),
    (9, 'starch', 4),
    (10, 'Potatoes', 2);
    
CREATE TABLE Recipes (
    did INTEGER not null,
    iid INTEGER not null,
    quantity INTEGER not null,
    PRIMARY KEY (did, iid),
    FOREIGN KEY (did) REFERENCES Dishes(did),
    FOREIGN KEY (iid) REFERENCES Ingredients(iid)
);
INSERT INTO Recipes (did, iid, quantity)
VALUES (1, 1, 200),
       (1, 2, 50),
       (1, 3, 100),
       (2, 5, 10),
       (2, 6, 20),
       (3, 2, 30),
       (3, 8, 50),
       (4, 4, 100),
       (5, 1, 150),
       (5, 2, 30),
       (5, 9, 50),
       (6, 7, 50);

/* 1 */
select D.dname from Dishes D where
not exists
	(select * from Ingredients I, Recipes R
	where R.did = D.did and R.iid = I.iid and
	I.iid in 
			(select I2.iid from Ingredients I2 
			where I2.iname = 'sugar' or I2.iname = 'butter' or I2.iname = 'starch'));

/* 2 */
select I.iname from Ingredients I where I.unitprice >= 10 and
exists
	(select * from Dishes D, Recipes R
	where R.did = D.did and R.iid = I.iid and D.popularity > 10000);

/* 3 */
select D.origin from Dishes D, Ingredients I, Recipes R
	where R.iid = I.iid and R.did = D.did and I.iname = "saffron" and R.quantity >= 1;

/* 4 */
select D.popularity from Dishes D where
not exists
	(select * from Ingredients I, Recipes R
	where R.did = D.did and R.iid = I.iid and I.unitprice < 50);

/* 5 */
select t.iname, t.unitprice from
(select D.did, I.iid, I.iname, I.unitprice from Ingredients I, Dishes D, Recipes R
where R.did = D.did and R.iid = I.iid) as t
group by t.iid
having count(*) = 1;