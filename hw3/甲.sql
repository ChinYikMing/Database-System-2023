create table politician (
  name varchar(255) not null,
  party varchar(255) not null,
  gender varchar(255) not null,
  primary key(name)
);

create table government (
  name varchar(255) not null,
  `rank` varchar(255) not null,
  salary int,
  primary key(name)
);

create table donation (
  name varchar(255) not null,
  organization varchar(255) not null,
  amount int not null,
  primary key(name, organization)
);

insert into politician
(name, party, gender) values
("Albert", "Republic", "male"),
("Charlie", "Democrat", "male");;

insert into government
(name, `rank`, salary) values
("Albert", "minister", 150000),
("Bobbie", "clerk", 50000),
("Don", "clerk", null);

insert into donation
(name, organization, amount) values
("Charlie", "American Red Cross", 150000),
("Charlie", "National AIDS Fund", 80000),
("Charlie", "UNICEF", 80000),
("Don", "NineMillion", 50000),
("Don", "American Red Cross", 60000),
("Campbell", "American Red Cross", 70000),
("Campbell", "National AIDS Fund", 60000),
("Mike", "NineMillion", 90000);

