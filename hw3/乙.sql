create table department (
  deptname varchar(255) not null,
  building varchar(255) not null,
  primary key(deptname, building)
);

create table professor (
  profname varchar(255) not null,
  deptname varchar(255) not null,
  primary key(profname, deptname),
  foreign key(deptname) references department(deptname)
);

create table committee (
  commname varchar(255) not null,
  profname varchar(255) not null,
  primary key(commname, profname),
  foreign key(profname) references professor(profname)
);

insert into department
(deptname, building) values
("Computer Science", "ICICS/CS"),
("Electrical Engineering", "KAIS"),
("Mechanical Engineering", "CEME");

insert into professor
(profname, deptname) values
("Piper", "Computer Science"),
("James", "Computer Science"),
("George", "Computer Science"),
("William", "Electrical Engineering"),
("Matthew", "Electrical Engineering"),
("Oliver", "Mechanical Engineering"),
("Lewis", "Mechanical Engineering");

insert into committee
(commname, profname) values
("Operation", "James"),
("Operation", "William"),
("Communication", "James"),
("Communication", "Piper"),
("Communication", "Oliver"),
("Communication", "Lewis"),
("Teaching", "James"),
("Teaching", "Piper"),
("Teaching", "Matthew"),
("Teaching", "Lewis"),
("Graduate Admissions", "William"),
("Graduate Admissions", "George"),
("Computing", "Matthew");

/* 1 */
select distinct profname from committee C where C.commname 
in (select commname from committee C2 where C2.profname = "Piper");

/* 2 */
select distinct c1.profname from committee c1 where 
not exists 
  (select c2.commname from committee c2 where c2.profname = 'Piper' and 
         c2.commname not in 
         	(select c3.commname from committee c3 where c3.profname = c1.profname));

/* 3 */
select P.profname from professor P, Department D 
where P.deptname = D.deptname and D.building not in 
	(select D2.building from professor P2, Department D2
	where P2.profname = "Piper" and P2.deptname = D2.deptname);