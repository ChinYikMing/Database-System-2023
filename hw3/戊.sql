CREATE TABLE Manufacturer (
  name varchar(255) not null,
  country varchar(255) not null,
  phone varchar(255) not null,
  primary key(name)
);

INSERT INTO Manufacturer (Name, Country, Phone)
VALUES 
  ('Apple', 'United States', '14089961010'),
  ('ASUS', 'Taiwan', '228943447'),
  ('Dell', 'United States', '18006249896'),
  ('HP', 'United States', '16508571501'),
  ('Lenovo', 'China', '287227700'),
  ('Acer', 'Taiwan', '800258222');
  
CREATE TABLE Desktop (
  model varchar(255) not null,
  speed double not null,
  RAM int not null,
  HD int not null,
  list_price int not null,
  primary key(model)
);

INSERT INTO Desktop (model, speed, RAM, HD, list_price)
VALUES 
  ('iMac', 3.4, 8192, 500, 799),
  ('ROG Strix GL12CX', 3.2, 16384, 1000, 1099),
  ('Inspiron 3671', 2.4, 4096, 250, 499),
  ('EliteDesk 800 G5', 3.3, 32768, 2000, 1999),
  ('Mac Mini', 3.2, 16384, 200, 699),
  ('OMEN 45L', 3.4, 65536, 4000, 2999),
  ('Mac Pro', 3.8, 131072, 8000, 5999);

CREATE TABLE Laptop (
  model varchar(255) not null,
  speed double not null,
  RAM int not null,
  HD int not null,
  screen int not null,
  list_price int not null,
  primary key(model)
);

INSERT INTO Laptop (model, speed, RAM, HD, screen, list_price)
VALUES 
  ('MacBook Pro', 3.2, 4096, 250, 14, 2299),
  ('VivoBook S15', 2.4, 8192, 500, 15, 899),
  ('XPS 13', 3.2, 8192, 500, 15, 1399),
  ('EliteBook 840 G7', 2.6, 16384, 1000, 17, 1299),
  ('Macbook Air', 3.1, 8192, 200, 13, 899),
  ('ThinkPad X1 Carbon', 2.4, 4096, 250, 13, 499),
  ('IdeaPad L340', 2.6, 32768, 2000, 17, 699);


 CREATE TABLE Product_name (
 	model varchar(255) not null,
 	primary key(model)
 );

INSERT INTO Product_name(model)
(select D.model from Desktop D
union
select L.model from Laptop L);

 CREATE TABLE Product (
  manu_name varchar(255) not null,
  model varchar(255) not null,
  style varchar(255) not null check (style = "Desktop" or style = "Laptop"),
  primary key(manu_name, model, style),
  foreign key(model) references Product_name(model) on update cascade on delete cascade
);

INSERT INTO Product (manu_name, model, style)
VALUES 
  ('Apple', 'iMac', 'Desktop'),
  ('Apple', 'MacBook Pro', 'Laptop'),
  ('Apple', 'Mac Pro', 'Desktop'),
  ('Apple', 'Mac Mini', 'Desktop'),
  ('Apple', 'Macbook Air', 'Laptop'),
  ('ASUS', 'ROG Strix GL12CX', 'Desktop'),
  ('ASUS', 'VivoBook S15', 'Laptop'),
  ('Dell', 'Inspiron 3671', 'Desktop'),
  ('Dell', 'XPS 13', 'Laptop'),
  ('HP', 'EliteBook 840 G7', 'Laptop'),
  ('HP', 'EliteDesk 800 G5', 'Desktop'),
  ('HP', 'OMEN 45L', 'Desktop'),
  ('Lenovo', 'IdeaPad L340', 'Laptop'),
  ('Lenovo', 'ThinkPad X1 Carbon', 'Laptop');

/* 1 */
select avg(HD) as "Average HD Size" from Desktop;

/* 2 */
select avg(list_price) as "Average Price" from Laptop where speed >= 3.0;

/* 3 */
select avg(temp.list_price) as "Average Price" from
	(select model, list_price from Desktop where model
		in (select model from Product where manu_name = "Dell")
	union
	select model, list_price from Laptop where model
		in (select model from Product where manu_name = "Dell")) as temp;

/* 4 */
select temp.list_price, avg(temp.speed) as "average speed" from
	(select speed, list_price from Desktop
	union
	select speed, list_price from Laptop) as temp
	group by list_price;

/* 5 */
select manu_name from Product where style = "Desktop"
group by manu_name 
having count(*) >= 3;

/* 6 */
select P.manu_name, max(D.speed) as "maximum speed" from Product P, Desktop D 
where P.style = "Desktop" and P.model = D.model
group by manu_name;

/* 7 */
select speed, avg(HD) as "average HD size" from Desktop where speed > 2.5
group by speed;

/* 8 */
select P.manu_name, avg(L.speed) as "Laptop speed" from Product P, Laptop L 
where style = "Laptop" and P.model = L.model
group by manu_name;

/* 9 */
select avg(t.HD) as "average HD size" from
(select P.manu_name, D.HD from Product P, Desktop D where P.manu_name
	in (select name from Manufacturer where 
			exists 
				(select manu_name from Product where style = "Laptop" and name = manu_name))
	and P.model = D.model) as t;

/* 10 */
delete from Product_name P where P.model 
in
	(select D.model from Desktop D where HD < 400);

delete from Desktop D where D.HD < 400;


/* 11 */
insert into Product_name(model)
values ("1500");

insert into Desktop(model, speed, RAM, HD, list_price)
values ("1500", 3.1, 2048, 300, 799);

insert into Product(manu_name, model, style)
values ("Acer", "1500", "Desktop");

/* 12 */
delete from Laptop L where L.model
in
	(
	select P4.model from Product_name P4 where P4.model 
	in
		(select t.model from
			(select P3.model from Product P3 where P3.manu_name
			in
				(select M.name from Manufacturer M where
				exists
					(select P1.manu_name from Product P1 where P1.manu_name = M.name and P1.style = "Laptop")
				and not exists
					(select P2.manu_name from Product P2 where P2.manu_name = M.name and P2.style = "Desktop"))
			) as t
		)
	);

delete from Product_name P4 where P4.model 
in
	(select t.model from
		(select P3.model from Product P3 where P3.manu_name
		in
			(select M.name from Manufacturer M where
			exists
				(select P1.manu_name from Product P1 where P1.manu_name = M.name and P1.style = "Laptop")
			and not exists
				(select P2.manu_name from Product P2 where P2.manu_name = M.name and P2.style = "Desktop"))
		) as t
	);

/* 13 */
update Desktop set HD = 2*HD, RAM = 2048+RAM;
update Laptop set HD = 2*HD, RAM = 2048+RAM;

/* 14 */
update Laptop set screen = 1+screen, list_price = list_price-200 where model
in 
	(select model from Product where manu_name = "Dell" and style = "Laptop");