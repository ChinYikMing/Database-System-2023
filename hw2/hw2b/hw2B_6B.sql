use hw2b6B;

alter table Critic Modify `name` varchar(50) not null;
alter table Critic Modify phoneNo varchar(50) not null;
create table Critic(
	criticID int not null auto_increment,
	name varchar(50) not null,
    phoneNo varchar(50) not null,
    primary key(criticID)
);
explain Critic;

alter table ForeignMovie Modify `language` varchar(50) not null;
alter table ForeignMovie Modify country varchar(50) not null;
create table ForeignMovie(
	title varchar(50) not null,
    `language` varchar(50) not null,
    country varchar(50) not null,
    primary key(title)
);
explain ForeignMovie;

alter table Subtitle Modify title varchar(50) not null;
alter table Subtitle Modify subtitleLanguage varchar(50) not null;
alter table Subtitle Modify subtitle varchar(50) not null;
create table Subtitle(
	subtitleID int not null auto_increment,
    title varchar(50) not null,
    subtitleLanguage varchar(50) not null,
    subtitle longtext not null,
    primary key(subtitleID),
    foreign key(title) references ForeignMovie(title)
);
explain Subtitle;

alter table DomesticMovie Modify zone varchar(50) not null;
create table DomesticMovie(
	title varchar(50) not null,
    zone varchar(50) not null,
    primary key(title)
);
explain DomesticMovie;

alter table ComedyMovie Modify funniness int not null;
create table ComedyMovie(
	title varchar(50) not null,
    funniness int not null,
    primary key(title)
);
explain ComedyMovie;

alter table DramaMovie Modify shortDesc varchar(50) not null;
create table DramaMovie(
	title varchar(50) not null,
    shortDesc varchar(50) not null,
    primary key(title)
);
explain DramaMovie;

alter table ActionMovie Modify ageRestrict int not null;
create table ActionMovie(
	title varchar(50) not null,
    ageRestrict int not null,
    primary key(title)
);
explain ActionMovie;

alter table HorrorMovie Modify horrorLevel int not null;
create table HorrorMovie(
	title varchar(50) not null,
    horrorLevel int not null,
    primary key(title)
);
explain HorrorMovie;

alter table CreditCard Modify SSN varchar(50) not null;
alter table CreditCard Modify expiredDate date not null;
alter table CreditCard Modify `type` varchar(50) not null;
create table CreditCard(
	cardNo varchar(50) not null,
    SSN varchar(50) not null,
    expiredDate date not null,
    `type` varchar(50) not null,
    primary key(cardNo),
    foreign key(SSN) references Customer(SSN)
);
explain CreditCard;

alter table Customer Modify address varchar(50) not null;
create table Customer(
	SSN varchar(50) not null,
    address varchar(50) not null,
    primary key(SSN)
);
explain Customer;

alter table Location Modify address varchar(50) not null;
create table Location(
	locationID int not null auto_increment,
    address varchar(50) not null,
    primary key(locationID)
);
explain Location;

alter table Employee Modify locationID int not null;
alter table Employee Modify dateOfBirth date not null;
alter table Employee Modify supervisor_SSN varchar(50) not null;
create table Employee(
	SSN varchar(50) not null,
    locationID int not null,
    dateOfBirth date not null,
    supervisor_SSN varchar(50) not null,
    primary key(SSN),
    foreign key(locationID) references Location(locationID)
);
explain Employee;

alter table PermanentEmployee Modify isSupervisor Boolean not null;
alter table PermanentEmployee Modify permanentEmpSalary int not null;
create table PermanentEmployee(
	SSN varchar(50) not null,
    isSupervisor Boolean not null,
    permanentEmpSalary int not null,
    primary key(SSN)
);
explain PermanentEmployee;

alter table TemporaryEmployee Modify temporaryEmpSalary int not null;
create table TemporaryEmployee(
	SSN varchar(50) not null,
    temporaryEmpSalary int,
    primary key(SSN)
);
explain TemporaryEmployee;

alter table BillStatement Modify SSN varchar(50) not null;
alter table BillStatement Modify billingDate date not null;
alter table BillStatement Modify totalCharge int not null;
create table BillStatement(
	billingNo int not null auto_increment,
    SSN varchar(50) not null,
    billingDate date not null,
    totalCharge int not null,
    primary key(billingNo),
    foreign key(SSN) references Customer(SSN)
);
explain BillStatement;

alter table Actor Modify age int not null;
alter table Actor Modify name varchar(50) not null;
alter table Actor Modify specialty varchar(50) not null;
alter table Actor Modify email varchar(50) not null;
create table Actor(
	actorID int not null auto_increment,
    age int not null,
    name varchar(50) not null,
    specialty varchar(50) not null,
    email varchar(50) not null,
    primary key(actorID)
);
explain Actor;

create table MoDProvider(
	MoDProviderID int not null auto_increment,
    primary key(MoDProviderID)
);
explain MoDProvider;

alter table Distributor Modify MoDProviderID int not null;
alter table Distributor Modify name varchar(50) not null;
alter table Distributor Modify state varchar(50) not null;
alter table Distributor Modify city varchar(50) not null;
alter table Distributor Modify zipCode varchar(50) not null;
alter table Distributor Modify streetAddress varchar(50) not null;
alter table Distributor Modify address varchar(50) not null;
alter table Distributor Modify phoneNo varchar(50) not null;
create table Distributor(
	distributorID int not null auto_increment,
    MoDProviderID int not null,
    `name` varchar(50) not null,
    state varchar(50) not null,
    city varchar(50) not null,
    zipCode varchar(50) not null,
    streetAddress varchar(50) not null,
    address varchar(50) not null,
    phoneNo varchar(50) not null,
    primary key(distributorID),
    foreign key(MoDProviderID) references MoDProvider(MoDProviderID)
);
explain Distributor;

alter table ReleaseChargeInfo Modify releaseChargeType varchar(50) not null;
alter table ReleaseChargeInfo Modify additionalViewingFee int not null;
create table ReleaseChargeInfo(
	releaseChargeType varchar(50) not null,
    firstViewingFee int not null,
    additionalViewingFee int not null,
    primary key(releaseChargeType)
);
explain ReleaseChargeInfo;

alter table Movie Modify releaseChargeType varchar(50) not null;
alter table Movie Modify rating int not null;
alter table Movie Modify releaseDate date not null;
alter table Movie Modify runningTime int not null;
alter table Movie Modify director varchar(50) not null;
alter table Movie Modify distributorID int not null;
create table Movie(
	title varchar(50) not null,
    releaseChargeType varchar(50) not null,
    distributorID int not null,
    rating int not null,
    releaseDate date not null,
    runningTime int not null,
    director varchar(50) not null,
    primary key(title),
    foreign key(releaseChargeType) references ReleaseChargeInfo(releaseChargeType),
    foreign key(distributorID) references Distributor(distributorID)
);
explain Movie;

alter table AcademyAward Modify academyAwardDesc int not null;
create table AcademyAward(
	academyAwardID int not null auto_increment,
    title varchar(50) not null,
    academyAwardDesc varchar(50) not null,
    primary key(academyAwardID),
    foreign key(title) references Movie(title)
);
explain AcademyAward;

alter table ViewingTransaction Modify title varchar(50) not null;
alter table ViewingTransaction Modify billingNo int not null;
alter table ViewingTransaction Modify viewingDateTime datetime not null;
create table ViewingTransaction(
	viewingTransactionID int not null auto_increment,
    billingNo int not null,
    viewingDateTime datetime not null,
    primary key(viewingTransactionID),
    foreign key(billingNo) references BillStatement(billingNo)
);
explain ViewingTransaction;

alter table VideoServer Modify locationID int not null;
alter table VideoServer Modify distributorID int not null;
create Table VideoServer(
	videoServerID int not null auto_increment,
    locationID int not null,
    distributorID int not null,
    primary key(videoServerID),
    foreign key(locationID) references Location(locationID),
    foreign key(distributorID) references Distributor(distributorID)
);
explain VideoServer;

alter table HomeSettopBox Modify SSN varchar(50) not null;
create Table HomeSettopBox(
	settopBoxID int not null auto_increment,
    SSN varchar(50) not null,
    primary key(settopBoxID),
    foreign key(SSN) references Customer(SSN)
);
explain HomeSettopBox;

alter table viewed Modify criticID int not null;
create table `view`(
	title varchar(50) not null,
    criticID int not null,
    primary key(title, criticID),
    foreign key(title) references Movie(title),
    foreign key(criticID) references Critic(criticID)
);
explain `view`;
drop table viewed;

create table `stream`(
	settopBoxID int not null,
    videoServerID int not null,
    primary key(settopBoxID, videoServerID),
    foreign key(settopBoxID) references HomeSettopBox(settopBoxID),
    foreign key(videoServerID) references VideoServer(videoServerID)
);
explain `stream`;

create table has_actor(
	actorID int not null,
    title varchar(50) not null,
    primary key(actorID, title),
    foreign key(actorID) references Actor(actorID),
    foreign key(title) references Movie(title)
);
explain has_actor;

create table request(
	SSN varchar(50) not null,
    title varchar(50) not null,
    primary key(SSN, title),
    foreign key(SSN) references Customer(SSN),
    foreign key(title) references Movie(title)
);
explain request;

create table store(
	videoServerID int not null,
    title varchar(50) not null,
    primary key(videoServerID, title),
    foreign key(videoServerID) references VideoServer(videoServerID),
    foreign key(title) references Movie(title)
);
explain store;

create table has_movieTitle(
	billingNo int not null,
    title varchar(50) not null,
    primary key(billingNo, title),
    foreign key(billingNo) references BillStatement(billingNo),
    foreign key(title) references Movie(title)
);
explain has_movieTitle;

drop table has_movie;


drop table store;
drop table request;
drop table has_actor;
drop table `stream`;
drop table viewed;