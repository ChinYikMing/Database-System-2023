CREATE TABLE Books (
    bid INT not null,
    btitle VARCHAR(255) not null,
    author VARCHAR(255) not null,
    year INT not null,
    price int not null,
    PRIMARY KEY(bid)
);
INSERT INTO Books (bid, btitle, author, year, price)
VALUES 
    (1, 'The Girl on the Train', 'Paula Hawkins', 2000, 20),
    (2, 'The Nightingale', 'Kristin Hannah', 2015, 18),
    (3, 'The Hunger Games', 'Suzanne Collins', 2008, 40),
    (4, 'The Immortalists', 'Codson Whitehead', 2018, 22),
    (5, 'The Underground Railroad', 'Colson Whitehead', 1990, 120),
    (6, 'The Midnight Library', 'Matt Haig', 1990, 22);

CREATE TABLE Customers (
    cid INT not null,
    cname VARCHAR(255) not null,
    zipcode VARCHAR(255) not null,
    PRIMARY KEY(cid)
);
INSERT INTO Customers (cid, cname, zipcode)
VALUES 
    (1, 'John Smith', '10001'),
    (2, 'Jane Doe', '12345'),
    (3, 'Bob Johnson', '66666'),
    (4, 'Mary Davis', '02125'),
    (5, 'David Lee', '02125'),
    (6, 'Laura Hill', '60606'),
    (7, 'Chris Brown', '10001'),
    (8, 'Emily Wilson', '10001');
   
CREATE TABLE Orders (
    cid INT not null,
    bid INT not null,
    quantity INT not null,
    PRIMARY KEY (cid, bid),
    FOREIGN KEY (cid) REFERENCES Customers(cid),
    FOREIGN KEY (bid) REFERENCES Books(bid)
);
INSERT INTO Orders (cid, bid, quantity)
VALUES 
    (3, 2, 10),
    (5, 5, 9),
    (1, 1, 28),
    (5, 4, 30),
    (1, 5, 25),
    (2, 3, 222),
    (4, 4, 53),
    (3, 5, 22),
    (1, 2, 36),
    (2, 2, 108),
    (1, 3, 12),
    (1, 6, 30),
    (2, 5, 10),
    (6, 5, 13);

/* 1 錯，找出只被訂購至少100本的那些書的標題 */
select B.btitle from Books B where
exists
	(select t.`quantity` from
	(select O.quantity as "quantity" from Orders O where O.bid = B.bid) as t
	where t.`quantity` >= 100);

/* 2 */
select B.author from Books B where B.price <= 40 and
exists
	(select * from Orders O where O.bid = B.bid and O.cid
	in
		(select C.cid from Customers C where C.zipcode = "12345"));

/* 3 */
select C.cname from Customers C where
exists
	(select * from Orders O where O.cid = C.cid and O.bid
	in
		(select B.bid from Books B where B.year = 2000))
and exists 
	(select t.`q` from
	(select O2.quantity as 'q' from Orders O2, Books B2 
		where O2.cid = C.cid and O2.bid = B2.bid and B2.price > 100) as t
	where t.`q` >= 10);

/* 4 */
select B.author from Books B where
exists
	(select t.`c` from 
	(select count(*) as 'c' from Orders O where O.bid = B.bid) as t
	where t.`c` >= 2);

/* 5, 錯 */
select B.btitle from Books B where
not exists
	(select t.zipcode from
		(select C.zipcode from Orders O, Customers C 
		where O.bid = B.bid and O.cid = C.cid) as t
		where t.zipcode
		in
			(select C2.zipcode from Customers C2
			group by C2.zipcode
			having count(*) != 1)
	);

/* 6 */
select B.author from Books B where
exists
	(select * from Customers C where C.zipcode = "02125" and
		exists
			(select * from Orders O where O.cid = C.cid and O.bid = B.bid))
and not exists
	(select * from Customers C where C.zipcode != "02125" and
		exists
			(select * from Orders O where O.cid = C.cid and O.bid = B.bid));

/* 7 */
select distinct C.zipcode from Customers C where
exists
	(select * from Orders O, Books B where 
	O.cid = C.cid and O.bid = B.bid and O.quantity >= 10 and B.author like 'Cod%');

/* 8 */
select t2.cname, max(B2.price) as "most expensive price of book published in 1990" 
from Orders O2, Books B2,
	(select t.cid, t.cname from
		(select distinct C.cid, C.cname, B.bid from Customers C, Orders O, Books B
		where C.cid = O.cid and O.bid = B.bid) as t
	group by t.cid, t.cname
	having count(distinct t.bid) >= 5) as t2
where O2.cid = t2.cid and O2.bid = B2.bid and B2.year = 1990
group by t2.cname;

/* 9 */
select B.btitle from Books B where
exists
	(select t2.`c` from
	(select count(distinct t.zipcode) as 'c' from
		(select distinct C.zipcode from Orders O, Customers C where O.bid = B.bid and O.cid = C.cid) as t) as t2
	where t2.`c` = (select count(distinct C2.zipcode) from Customers C2));

/* 10 */
select distinct C.cname, sum(O.quantity * B.price) as "total dollar amount of purchases" 
from Customers C, Books B, Orders O 
where O.cid = C.cid and O.bid = B.bid and C.zipcode = "02125"
group by C.cid;

/* 11 */
select t2.zipcode from 
	(select t.zipcode, sum(t.`revenue`) as 'revenue' from
		(select distinct C.zipcode, sum(O.quantity * B.price) as 'revenue' 
		from Customers C, Books B, Orders O 
		where O.cid = C.cid and O.bid = B.bid 
		group by C.cid) as t
	group by t.zipcode) as t2 
where t2.`revenue` >= all
	(select t4.`revenue` from 
		(select t3.zipcode, sum(t3.`revenue`) as 'revenue' from
			(select distinct C.zipcode, sum(O.quantity * B.price) as 'revenue' 
			from Customers C, Books B, Orders O 
			where O.cid = C.cid and O.bid = B.bid 
		group by C.cid) as t3
	group by t3.zipcode) as t4);