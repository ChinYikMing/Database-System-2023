 CREATE TABLE Students (
  sid INTEGER not null,
  sname VARCHAR(255) not null,
  age INTEGER not null,
  primary key(sid)
);
INSERT INTO Students (sid, sname, age)
VALUES 
  (1, 'Alice', 18),
  (2, 'Bob', 25),
  (3, 'Charlie', 20),
  (4, 'David', 19),
  (5, 'Ella', 23),
  (6, 'Frank', 24),
  (7, 'Grace', 18),
  (8, 'Henry', 22),
  (9, 'Ivy', 25),
  (10, 'John', 21);
 
 CREATE TABLE Courses (
  cid INTEGER not null,
  cname VARCHAR(255) not null,
  credits INTEGER not null,
  primary key(cid)
);

INSERT INTO Courses (cid, cname, credits) 
VALUES
  (1, 'Math', 4),
  (2, 'Science', 3),
  (3, 'English', 4),
  (4, 'Databases', 3),
  (5, 'Art', 2),
  (6, 'Music', 2),
  (7, 'Computer Sci', 4);
 
 
CREATE TABLE Enrolled (
  sid INTEGER not null,
  cid INTEGER not null,
  grade INTEGER not null,
  PRIMARY KEY (sid, cid),
  FOREIGN KEY (sid) REFERENCES Students(sid),
  FOREIGN KEY (cid) REFERENCES Courses(cid)
);
INSERT INTO Enrolled (sid, cid, grade) 
VALUES
(1, 5, 6),
  (1, 7, 10),
  (2, 7, 7),
  (3, 1, 7),
  (3, 3, 7),
  (3, 7, 9),
  (4, 1, 9),
  (4, 7, 8),
  (5, 3, 7),
  (5, 7, 7),
  (6, 4, 10),
  (6, 6, 8),
  (7, 5, 8),
  (7, 7, 9),
  (8, 1, 6),
  (8, 7, 8),
  (9, 2, 7),
  (9, 4, 7),
  (10, 7, 8);

/* 1 */
CREATE TABLE Enrolled (
  sid INTEGER,
  cid INTEGER,
  grade INTEGER,
  FOREIGN KEY (sid) REFERENCES Students(sid),
  FOREIGN KEY (cid) REFERENCES Courses(cid),
  PRIMARY KEY (sid, cid)
);

/* 2 */
select S.sname from Students S where
S.age = (select min(S2.age) from Students S2);

/* 3, 錯, 缺少cid=9的學生，此題應是選修"學分小於四的課程"的學生 */
select distinct S.sid, S.age, S.sname from Students S where
exists
	(select * from Enrolled E, Courses C
	where E.sid = S.sid and E.cid = C.cid)
and not exists
	(select * from Enrolled E, Courses C
	where E.sid = S.sid and E.cid = C.cid and C.credits != 4);

/* 4 */
select S.age from Students S where
exists
	(select distinct * from Enrolled E, Courses C 
	where E.sid = S.sid and E.cid = C.cid and C.cname = "Databases" and E.grade = 10);

/* 5 */
select t.cid, avg(S.age) as 'average age' from 
	(select C2.cid from Courses C2, Enrolled E2
	where C2.cid = E2.cid
	group by C2.cid
	having count(*) >= 50) as t, Enrolled E, Students S
where t.cid = E.cid and E.sid = S.sid and S.age >= 20
group by t.cid;

/* 6 */
select S.sname from Students S where
not exists
	((select C.cid, C.cname, C.credits from Courses C where C.credits = 4)
	  except
	  (select C2.cid, C2.cname, C2.credits from Courses C2, Enrolled E
		where E.sid = S.sid and E.cid = C2.cid and E.grade >= 7)
	);

/* 7 */
select t2.sname from 
(select t.sname, avg(t.grade) as 'avgGPA' from
	(select S.sname, E.grade  from Courses C, Enrolled E, Students S
	where E.sid = S.sid and E.cid = C.cid) as t
	group by t.sname) as t2 where t2.`avgGPA` >= all
(select avg(t.grade) as 'avgGPA' from
	(select S.sname, E.grade  from Courses C, Enrolled E, Students S
	where E.sid = S.sid and E.cid = C.cid) as t
	group by t.sname);

/* 8, 錯, 缺少部分結果，缺少部分為助教的額外測資 */
select distinct S.age from Students S where
exists
	(select * from Enrolled E, Courses C 
	where E.sid = S.sid and E.cid = C.cid and
	C.cid in (select C2.cid from Courses C2 where C2.credits = 3));

/* 9 */
select S.sname from Students S where
exists
	(select * from Enrolled E, Courses C
	where E.sid = S.sid and E.cid = C.cid and C.credits < 4 and E.grade >= 8);

/* 10 */
select S.sname from Students S where
exists
	(select * from Enrolled E, Courses C
	where E.sid = S.sid and E.cid = C.cid)
and not exists
	(select * from Enrolled E, Courses C
	where E.sid = S.sid and E.cid = C.cid and E.grade != 10);

/* 11 */
select S.sname from Students S, Enrolled E, Courses C 
	where E.sid = S.sid and E.cid = C.cid and C.credits = 3 and
	exists 
		(select t.sname from
				(select S2.sname, E2.cid from Students S2, Enrolled E2, Courses C2
					where E2.sid = S.sid and E2.cid = C2.cid) as t
			group by t.sname
			having count(distinct t.cid) = 1
		)
union
select S2.sname from Students S2 where
exists
	(select * from Enrolled E2, Courses C2
	where E2.sid = S2.sid and E2.cid = C2.cid and E2.grade = 10);

/* 12 */
select t.sname from
(select S.sname, E.cid from Students S, Enrolled E, Courses C 
where E.sid = S.sid and E.cid = C.cid) as t
group by t.sname
having count(distinct t.cid) = 1;
