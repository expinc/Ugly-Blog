alter table ACCOUNT_BOARD_AUTHORITIES add check(Authority in ('Read', 'Reply', 'Post', 'Manage', 'Own'));

insert into ACCOUNT_BOARD_AUTHORITIES (AccountId, BoardId, Authority) values (1, 1, 'Own');