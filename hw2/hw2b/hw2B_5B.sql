use hw2b;

/* 5B */

drop table Company;
drop table SeatArrangement;
drop table Aircraft;

create table Company(
	companyID int not null auto_increment,
    primary key(companyID)
);
explain Company;

alter table SeatArrangement Modify seatMap blob not null;
create table SeatArrangement(
	seatArrangementID int not null auto_increment,
    seatMap blob not null,
    primary key(seatArrangementID)
);
explain SeatArrangement;

alter table Aircraft Modify companyID int not null;
alter table Aircraft Modify seatArrangementID int not null;
alter table Aircraft Modify make varchar(50) not null;
alter table Aircraft Modify model varchar(50) not null;
alter table Aircraft Modify maxCapacity int not null;
create table Aircraft(
	registrationNo int not null auto_increment,
    companyID int not null,
    seatArrangementID int not null,
    make varchar(50) not null,
    model varchar(50) not null,
    maxCapacity int not null,
    primary key(registrationNo),
    foreign key(companyID) references Company(companyID),
    foreign key(seatArrangementID) references SeatArrangement(seatArrangementID)
);
explain Aircraft;

drop table IntermediateStop;
create table IntermediateStop(
	stopCode int not null auto_increment,
    primary key(stopCode)
);
explain IntermediateStop;

drop table Customer;
alter table Customer Modify age int not null;
alter table Customer Modify passport varchar(50) not null;
alter table Customer Modify `name` varchar(50) not null;
alter table Customer Modify email varchar(50) not null;
create table Customer(
	customerID int not null auto_increment,
    age int not null,
    passport varchar(50) not null,
    `name` varchar(50) not null,
    email varchar(50) not null,
	primary key(customerID)
);
explain Customer;

drop table Reservation;
alter table Reservation Modify reservedDateTime datetime not null;
alter table Reservation Modify creditCardNo varchar(50) not null;
create table Reservation(
	reservationID int not null auto_increment,
    customerID int not null,
    paidFlag boolean,
    reservedDateTime datetime not null,
    creditCardNo varchar(50) not null,
    primary key(reservationID),
    foreign key(customerID) references Customer(customerID)
);
explain Reservation;

alter table Ticket Modify validness varchar(50) not null;
create table Ticket(
	reservationID int not null,
    ticketNo varchar(50) not null,
    validness varchar(50) not null,
    primary key(reservationID, ticketNo),
    foreign key(reservationID) references Reservation(reservationID)
);
explain Ticket;

alter table CancellableReservation Modify reservationID int not null;
alter table CancellableReservation Modify customerID int not null;
create table CancellableReservation(
	cancellableReservationID int not null auto_increment,
    reservationID int not null,
    customerID int not null,
    primary key(cancellableReservationID),
    foreign key(reservationID) references Reservation(reservationID),
    foreign key(customerID) references Customer(customerID)
);
explain CancellableReservation;

alter table Airport Modify country varchar(50) not null;
alter table Airport Modify city varchar(50) not null;
alter table Airport Modify `description` varchar(50) not null;
alter table Airport Modify `name` varchar(50) not null;
create table Airport(
	airportCode int not null auto_increment,
    country varchar(50) not null,
    city varchar(50) not null,
    `description` varchar(50) not null,
    `name` varchar(50) not null,
    primary key(airportCode)
);
explain Airport;

alter table Class Modify classDescription varchar(50) not null;
create table Class(
	classID int not null,
    classDescription varchar(50) not null,
    primary key(classID)
);
explain Class;

alter table `Time` Modify timeSlot time not null;
drop table `Time`;
create table `Time`(
	timeID int not null,
    timeSlot time not null,
    primary key(timeID)
);
explain `Time`;

alter table TicketPrice Modify price int not null;
create table TicketPrice(
	priceID int not null,
    price int not null,
    primary key(priceID)
);
explain TicketPrice;

use hw2b;
drop table Seat;
drop table Reservation;
drop table Ticket;
drop table CancellableReservation;
drop table reserve;
drop table Flight;

alter table Flight drop foreign key reservationID;

drop table Flight;
alter table Flight Modify flightNo int not null;
alter table Flight Modify departureDateTime datetime not null;
alter table Flight Modify arrivalDateTime datetime not null;
alter table Flight Modify companyID int not null;
alter table Flight Modify departureAirportCode int not null;
alter table Flight Modify registrationNo int not null;
alter table Flight Modify arrivalAirportCode int not null;
create table Flight(
	flightID int not null auto_increment,
    flightNo int not null,
    departureDateTime datetime not null,
    arrivalDateTime datetime not null,
    companyID int not null,
    departureAirportCode int not null,
    arrivalAirportCode int not null,
    registrationNo int not null,
    primary key(flightID),
    foreign key(companyID) references Company(companyID),
    foreign key(departureAirportCode) references Airport(airportCode),
    foreign key(arrivalAirportCode) references Airport(airportCode),
    foreign key(registrationNo) references Aircraft(registrationNo)
);
explain Flight;

create table Seat(
	flightID int not null,
    seatNo int not null,
    customerID int,
    primary key(flightID, seatNo),
    foreign key(flightID) references Flight(flightID)
);
explain Seat;

create table `stop`(
	flightID int not null,
    stopCode int not null,
    primary key(flightID, stopCode),
	foreign key(flightID) references Flight(flightID),
    foreign key(stopCode) references IntermediateStop(stopCode)
);
explain `stop`;

create table reserve(
	flightID int not null,
    reservationID int not null,
    primary key(flightID, reservationID),
    foreign key(flightID) references Flight(flightID),
    foreign key(reservationID) references Reservation(reservationID)
);
explain reserve;

create table register(
	flightID int not null,
    airportCode int not null,
    primary key(flightID, airportCode),
    foreign key(flightID) references Flight(flightID),
    foreign key(airportCode) references Airport(airportCode)
);
explain register;

create table associate(
	classID int not null,
    timeID int not null,
    priceID int not null,
    primary key(classID, timeID, priceID),
    foreign key(classID) references Class(classID),
    foreign key(timeID) references `Time`(timeID),
    foreign key(priceID) references TicketPrice(priceID)
);
explain associate;

create table transfer_flight(
	customerID int not null,
    stopCode int not null,
    primary key(customerID, stopCode),
    foreign key(customerID) references Customer(customerID),
    foreign key(stopCode) references IntermediateStop(stopCode)
);
explain transfer_flight;

drop table associate;
drop table register;
drop table reserve;
drop table `stop`;

explain Company;