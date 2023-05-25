create table Students(
	PID varchar(255) not null,
	Name varchar(255) not null,
	Address varchar(255) not null,
	primary key(PID)
);

insert into Students(PID, Name, Address)
values 
('Zadeh', 'Lofti', 'Seattle, WA'),
('Patterson', 'David', 'Los Angeles, CA'),
('Smith', 'Alan', 'San Francisco, CA'),
('Feiner', 'Steven', 'Boston, MA'),
('Kuck', 'David', 'Bloomington, IN'),
('Kender', 'John', 'Los Angeles, CA'),
('Huang', 'Thomas', 'Atlanta, GA'),
('Fischer', 'Michael', 'Madison, WI'),
('Appel', 'Andrew', 'Miami, FL'),
('Dobkin', 'David', 'Salt Lake City, UT'),
('Li', 'Kai', 'Las Vegas, NV'),
('Peterson', 'Larry', 'Chicago, IL');

create table Departments(
	Name varchar(255) not null,
	ChairmanPID varchar(255),
	primary key(Name)
);

delimiter $$
create trigger before_departments_insert 
before insert on Departments
for each row
begin
	if NEW.ChairmanPID in (select D.ChairmanPID from Departments D) then
		signal sqlstate '45000' 
		set message_text = 'Each chairperson can be the head of at most one department';
	end if;
end$$
delimiter ;

delimiter $$
create trigger before_departments_update
before update on Departments
for each row
begin
	if NEW.ChairmanPID in (select D.ChairmanPID from Departments D) then
		signal sqlstate '45000' 
		set message_text = 'Each chairperson can be the head of at most one department';
	end if;
end$$
delimiter ;

insert into Departments(Name, ChairmanPID)
values
('CS', 'Ullman'),
('EE', 'Knuth'),
('ME', 'Lam'),
('BIO', null),
('PHY', 'Reiss'),
('MATH', 'Wegner');

create table Professors(
	PID varchar(255) not null,
	Name varchar(255) not null,
	Age varchar(255) not null,
	DepartmentName varchar(50) not null,
	primary key(PID),
    constraint Check_age check (Age = "old" or Age = "very old" or Age = "still alive")
);

insert into Professors(PID, Name, Age, DepartmentName)
values
('Widom', 'Jennifer', 'old', 'BIO'),
('Canny', 'John', 'very old', 'EE'),
('Ullman', 'Jeff', 'still alive', 'CS'),
('Reiss', 'Steve', 'very old', 'PHY'),
('Karp', 'Richard', 'still alive', 'MATH'),
('Lam', 'Monica', 'old', 'ME'),
('Chien', 'Andrew', 'old', 'PHY'),
('Wegner', 'Peter', 'still alive', 'MATH'),
('Hart', 'John', 'very old', 'BIO'),
('Katz', 'Randy', 'very old', 'CS'),
('Knuth', 'Don', 'still alive', 'EE'),
('Barsky', 'Brian', 'old', 'EE');

alter table Departments add constraint fk_chairman 
foreign key(ChairmanPID) references Professors(PID);

alter table Professors add constraint fk_department 
foreign key(DepartmentName) references Departments(Name);

create table Courses(
	`Number` integer not null,
	DeptName varchar(255) not null,
	CourseName varchar(255) not null,
	MaxEnrollment integer not null,
	ActualEnrollment integer not null,
	primary key(`Number`),
	foreign key(DeptName) references Departments(Name)
);

insert into Courses(`Number`, DeptName, CourseName, MaxEnrollment, ActualEnrollment)
values
(132, 'ME', 'Dynamic Systems', 120, 118),
(61, 'CS', 'Data Structure', 100, 90),
(1, 'MATH', 'Calculus', 150, 132),
(123, 'EE', 'Digital Signal Proc', 80, 72),
(111, 'PHY', 'Modern Physics', 40, 39),
(109, 'ME', 'Heat Transfer', 10, 8),
(54, 'MATH', 'Linear Algebra', 50, 50),
(162, 'CS', 'Operating Systems', 50, 32),
(137, 'PHY', 'Quantum Mech', 10, 3),
(145, 'BIO', 'Genomics', 5, 2),
(186, 'CS', 'Database Systems', 50, 48),
(224, 'EE', 'Digital Comm', 30, 22);

create table PreReq(
	`Number` integer not null,
	DeptName varchar(255) not null,
	PreReqNumber integer not null,
	PreReqDeptName varchar(255) not null,
	primary key(`Number`, PreReqNumber),
	foreign key(DeptName) references Departments(Name),
	foreign key(PreReqNumber) references Courses(`Number`),
	foreign key(PreReqDeptName) references Departments(Name)
);

delimiter $$
create trigger before_prereq_insert 
before insert on PreReq
for each row
begin
	if NEW.`Number` = NEW.PreReqNumber
	then
		signal sqlstate '45000' 
		set message_text = 'A course cannot be a pre-requisites for itself';
	end if;
end$$
delimiter ;

delimiter $$
create trigger before_prereq_update 
before update on PreReq
for each row
begin
	if NEW.`Number` = NEW.PreReqNumber
	then
		signal sqlstate '45000' 
		set message_text = 'A course cannot be a pre-requisites for itself';
	end if;
end$$
delimiter ;

insert into PreReq(`Number`, DeptName, PreReqNumber, PreReqDeptName)
values
(137, 'PHY', 1, 'MATH'),
(186, 'CS', 61, 'CS'),
(186, 'CS', 54, 'MATH'),
(145, 'BIO', 132, 'ME'),
(224, 'EE', 54, 'MATH'),
(162, 'CS', 186, 'CS'),
(111, 'PHY', 132, 'ME'),
(109, 'ME', 224, 'EE'),
(224, 'EE', 61, 'CS'),
(111, 'PHY', 1, 'MATH'),
(132, 'ME', 145, 'BIO'),
(54, 'MATH', 162, 'CS'),
(123, 'EE', 54, 'MATH');

create table Teach(
	ProfessorPID varchar(255) not null,
	`Number` integer not null,
	DeptName varchar(255) not null,
	primary key(ProfessorPID, `Number`),
	foreign key(ProfessorPID) references Professors(PID),
	foreign key(`Number`) references Courses(`Number`),
	foreign key(DeptName) references Departments(Name)
);

delimiter $$
create trigger before_teach_insert 
before insert on Teach
for each row
begin
	if NEW.`Number` in (select T.`Number` from Teach T) then
		signal sqlstate '45000' 
		set message_text = 'At most one professor teaches each course';
	end if;
end$$
delimiter ;

delimiter $$
create trigger before_teach_update
before update on Teach
for each row
begin
	if NEW.`Number` in (select T.`Number` from Teach T) then
		signal sqlstate '45000' 
		set message_text = 'At most one professor teaches each course';
	end if;
end$$
delimiter ;

insert into Teach(ProfessorPID, `Number`, DeptName)
values
('Knuth', 123, 'EE'),
('Reiss', 54, 'MATH'),
('Widom', 145, 'BIO'),
('Ullman', 61, 'CS'),
('Karp', 224, 'EE'),
('Lam', 132, 'ME'),
('Reiss', 111, 'PHY'),
('Wegner', 1, 'MATH'),
('Ullman', 186, 'CS'),
('Reiss', 137, 'PHY'),
('Chien', 109, 'ME'),
('Barsky', 162, 'CS');

create table Take(
	StudentPID varchar(255) not null,
	`Number` integer not null,
	DeptName varchar(255) not null,
	Grade varchar(255) not null,
	ProfessorEvaluation integer not null,
	primary key(StudentPID, `Number`),
	foreign key(StudentPID) references Students(PID),
	foreign key(`Number`) references Courses(`Number`),
	foreign key(DeptName) references Departments(Name)
);

#如果真正批改資料並非完全符合題意的話需要把 before_take_insert trigger 刪除
delimiter $$
create trigger before_take_insert 
before insert on Take
for each row
begin
	if NEW.StudentPID in (select S.PID from Students S) and
	   exists 
	   		(
	   			select P.PreReqNumber from PreReq P where NEW.`Number` = P.`Number`
				except
				select T.`Number` from Take T where T.StudentPID = NEW.StudentPID
	   		)
	then
		signal sqlstate '45000' 
		set message_text = 'A student enrolled in a course must have enrolled in all its pre-requisites';
	end if;
end$$
delimiter ;

insert into Take(StudentPID, `Number`, DeptName, Grade, ProfessorEvaluation)
values
('Appel', 111, 'PHY', 'B', 2),
('Patterson', 186, 'CS', 'B', 3),
('Li', 137, 'PHY', 'A', 3),
('Huang', 186, 'CS', 'A', 4),
('Smith', 109, 'ME', 'A', 3),
('Appel', 1, 'MATH', 'C', 2),
('Huang', 123, 'EE', 'A', 4),
('Fischer', 145, 'BIO', 'A', 2),
('Zadeh', 61, 'CS', 'A', 1),
('Dobkin', 123, 'EE', 'B', 4),
('Huang', 111, 'PHY', 'B', 3),
('Li', 162, 'CS', 'A', 3),
('Kender', 54, 'MATH', 'B', 4);

/* 1 */
select PID from Students where Name = "David";

/* 2 */
select S1.PID, S1.Name as "Name1", S2.PID, S2.Name as "Name2" from Students S1, Students S2 
where S1.Address = S2.Address and S1.Name != S2.Name;

/* 3 */
select distinct C.DeptName from Courses C where
exists
	(select PreReqDeptName from PreReq P where P.PreReqDeptName != C.DeptName and P.DeptName = C.DeptName);

/* 4, 錯, 執行結果有誤，輸出應是具有迴圈的組合，你的有不在迴圈內的課程出現。 */
with recursive prereq_cycle(`Number`, PreReqNumber, visited, has_cycle) as 
(
  select `Number`, PreReqNumber, json_array(`Number`), false from PreReq
  union all
  select PR.`Number`, PR.PreReqNumber, 
  			json_array_append(PRC.visited, '$', PR.PreReqNumber),
  			PR.PreReqNumber member of (PRC.visited)
  from PreReq PR
  join prereq_cycle PRC on(json_extract(PRC.visited, '$[last]') = PR.`Number`)
  where not has_cycle
)
select distinct json_extract(visited, '$[0]') as 'Number'
from prereq_cycle where has_cycle;

/* 5 */
select S.PID, S.Name, S.Address from Students S where S.PID
in
	(select T.StudentPID from Take T where T.`Number` = 186 and T.DeptName = "CS");

/* 6 */
select C.CourseName from Courses C, Teach T 
where C.`Number` = T.`Number` and T.professorPID = 
	(select ChairmanPID from Departments D where D.Name = "CS");

/* 7 */
select D.ChairmanPID from Departments D where
exists
	(select * from Teach T where T.ProfessorPID = D.ChairmanPID and T.DeptName != D.Name);

/* 8 */
select t.PID, t.Name from
	(select S.PID, S.Name from Students S, Take T, Departments D, Teach T2 where
	S.PID = T.StudentPID and T.DeptName = D.Name and 
	D.ChairmanPID = T2.ProfessorPID and T.`Number` = T2.`Number`) as t
group by t.PID, t.Name
having count(*) >= 2;

/* 9 */
select P.PID, P.Name from Professors P where P.age = "still alive" and 
exists
	(select t.ProfessorPID from 
		(select Te.ProfessorPID, T.ProfessorEvaluation from Take T, Teach Te 
			where T.`Number` = Te.`Number` and Te.ProfessorPID = P.PID) as t
	group by t.ProfessorPID
	having avg(t.ProfessorEvaluation) > 2.5);

/* 10 */
select S.PID, S.Name from Students S where S.PID
in
	(select T2.StudentPID from Take T2)
and not exists
	(select * from Take T where S.PID = T.StudentPID and T.Grade != 'A');

/* 11, 錯, 缺少全A的學生，他們也是獲得B比A多的學生 */
select t3.PID, t3.Name from
(select t.PID, t.Name, count(t.grade) as "grade A count" from 
	(select S.PID, S.Name, T.Grade from Students S, Take T 
		where S.PID = T.StudentPID and T.Grade = 'A') as t
group by t.PID, t.Name) as t3,
(select t2.PID, t2.Name, count(t2.grade) as "grade B count" from 
	(select S.PID, S.Name, T.Grade from Students S, Take T 
		where S.PID = T.StudentPID and T.Grade = 'B') as t2
group by t2.PID, t2.Name) as t5
where t3.Name = t5.Name and t3.`grade A count` > t5.`grade B count`;