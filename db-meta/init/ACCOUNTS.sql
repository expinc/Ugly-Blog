alter table ACCOUNTS add unique(AccountName);
alter table ACCOUNTS add check(Type in ('Admin', 'Normal'));

insert into ACCOUNTS (AccountName, Passcode, "Type", Nickname) values ('admin', 'pass', 'Admin', 'admin');