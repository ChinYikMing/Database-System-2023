create table Emp (
  eid INTEGER not null,
  ename VARCHAR(255) not null,
  age INTEGER not null,
  salary REAL not null,
  PRIMARY KEY(eid)
);
INSERT INTO Emp (eid, ename, age, salary) 
VALUES
  (1, 'Alice', 28, 55000.0),
  (2, 'Bob', 32, 62000.0),
  (3, 'Charlie', 45, 75000.0),
  (4, 'David', 22, 100000.0),
  (5, 'Emily', 36, 80000.0),
  (6, 'Frank', 50, 95000.0),
  (7, 'George', 29, 56000.0),
  (8, 'Henry', 41, 68000.0),
  (9, 'Isabelle', 27, 50000.0),
  (10, 'Jack', 31, 61000.0);
 
 
 
 CREATE TABLE Dept (
  did INTEGER not null,
  dname VARCHAR(255) not null,
  budget REAL not null,
  managerid INTEGER not null,
  PRIMARY KEY(did),
  FOREIGN KEY (managerid) REFERENCES Emp(eid)
);

INSERT INTO Dept (did, dname, budget, managerid) 
VALUES
  (1, 'Software', 3000000.0, 3),
  (2, 'Marketing', 75000.0, 5),
  (3, 'Hardware', 4000001.0, 6),
  (4, 'Finance', 1000000.0, 7),
  (5, 'HR', 90000.0, 9),
  (6, 'Sales', 1000000.0, 6);
 
 CREATE TABLE Works (
  eid INTEGER not null,
  did INTEGER not null,
  pct_time INTEGER not null,
  PRIMARY KEY (eid, did),
  FOREIGN KEY (eid) REFERENCES Emp(eid),
  FOREIGN KEY (did) REFERENCES Dept(did)
);

delimiter $$
create trigger before_works_insert 
before insert on Works
for each row
begin
	if NEW.pct_time <= 0 or 
		exists
		(
			select t.pct_time_sum from 
				(select sum(pct_time) + NEW.pct_time as 'pct_time_sum' from Works 
					where eid = NEW.eid) as t 
			where t.pct_time_sum > 100
		)
	then
		signal sqlstate '45000' 
		set message_text = 'Cannot insert less than equal 0% or exceed 100% of time';
	end if;
end$$
delimiter ;

delimiter $$
create trigger before_works_update
before update on Works
for each row
begin
	if NEW.pct_time <= 0 or 
		exists
		(
			select t.pct_time_sum from 
				(select sum(pct_time) + NEW.pct_time - OLD.pct_time as 'pct_time_sum' from Works 
					where eid = NEW.eid) as t 
			where t.pct_time_sum > 100
		)
	then
		signal sqlstate '45000' 
		set message_text = 'Cannot update less than equal 0% or exceed 100% of time';
	end if;
end$$
delimiter ;

INSERT INTO Works (eid, did, pct_time) 
VALUES
  (1, 1, 100),
  (2, 1, 100),
  (3, 1, 33),
  (3, 3, 33),
  (3, 4, 34),
  (4, 1, 30),
  (4, 2, 70),
  (5, 1, 30),
  (5, 2, 70),
  (6, 3, 70),
  (6, 6, 30),
  (7, 1, 70),
  (7, 4, 30),
  (8, 1, 70),
  (8, 4, 30),
  (9, 1, 70),
  (9, 5, 30),
  (10, 1, 70),
  (10, 5, 30);

/* 1 */
select E.ename, E.age from Emp E, Dept D, Works W 
where E.eid = W.eid and W.did = D.did and D.dname = "Hardware"
intersect
select E.ename, E.age from Emp E, Dept D, Works W 
where E.eid = W.eid and W.did = D.did and D.dname = "Software"; 

/* 2 */
select W.did, count(W.eid) as 'Number of Employees' from Works W
group by W.did
having 2000 < (select sum(W2.pct_time) from Works W2 where W2.did = W.did);

/* 3 */
select E.ename from Emp E where 
not exists
	(select * from Dept D, Works W 
	where D.did = W.did and E.eid = W.eid and E.salary < D.budget);

/* 4 */
select distinct D.managerid from Dept D where
not exists
	(select * from Dept D2
	where D2.managerid = D.managerid and D2.budget <= 1000000);

/* 5 */
select E.ename from Emp E where E.eid = 
    (select D.managerid from Dept D 
        where D.budget = (select max(D2.budget) from Dept D2));

/* 6 */
select distinct D.managerid from Dept D
where exists
		(select t.`sum` from
			(select sum(D2.budget) as "sum" from Dept D2
			where D2.managerid = D.managerid) as t
		where t.`sum` > 5000000);

/* 7 */
select distinct D.managerid from Dept D
where exists
		(select t.`sum` from
			(select sum(D2.budget) as "sum" from Dept D2
			where D2.managerid = D.managerid) as t
		where t.`sum` = (select max(t2.`sumBudget`) from
						(select D.managerid, sum(D.budget) as 'sumBudget' from Dept D
						group by D.managerid) as t2));

/* 8 */
select E.ename from Emp E where
not exists
	(select D2.budget from Dept D2 where D2.managerid = E.eid and D2.budget <= 1000000)
and
exists
	(select D3.budget from Dept D3 where D3.managerid = E.eid
	and D3.budget > 1000000 and D3.budget < 5000000);