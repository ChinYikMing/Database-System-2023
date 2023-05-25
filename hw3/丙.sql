create table Branch(
	Bcode varchar(255) not null,
    Librarian varchar(255) not null,
    Address varchar(255) not null,
    primary key(Bcode)
);
create table Titles(
	Title varchar(255) not null,
    Author varchar(255) not null,
    Publisher varchar(255) not null,
    primary key(Title)
);
create table Holdings(
	Branch varchar(255) not null,
    Title varchar(255) not null,
    `#copies` int not null,
    primary key(Branch, Title),
    foreign key(Branch) references Branch(Bcode),
    foreign key(Title) references Titles(Title)
);

insert into Branch
(Bcode, Librarian, Address)
values
("B1", "John Smith", "2 Anglesea Rd"),
("B2", "Mary Jones", "34 Pearse St"),
("B3", "Francis Owens", "Grange X");

insert into Titles
(Title, Author, Publisher)
values
("Susannah", "Ann Brown", "Macmillan"),
("How to Fish", "Amy Fly", "Stop Press"),
("A History of Dublin", "David Little", "Wiley"),
("Computers", "Blaise Pascal", "Applewoods"),
("The Wife", "Ann Brown", "Macmillan");

insert into Holdings
(Branch, Title, `#copies`)
values
("B1", "Susannah", 3),
("B1", "How to Fish", 2),
("B1", "A History of Dublin", 1),
("B2", "How to Fish", 4),
("B2", "Computers", 2),
("B2", "The Wife", 3),
("B3", "A History of Dublin", 1),
("B3", "Computers", 4),
("B3", "Susannah", 3),
("B3", "The Wife", 1);

/* 1 */
select Title from Titles T where T.Publisher = "Macmillan";

/* 2 */
select distinct Branch from Holdings H where H.Title 
in
	(select Title from Titles where Author = "Ann Brown");

/* 3 */
select distinct H.branch, SUM(`#copies`) AS "total number of books" from Holdings H 
group by H.Branch;