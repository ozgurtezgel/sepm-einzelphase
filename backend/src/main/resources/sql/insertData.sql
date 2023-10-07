-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE FROM owner where id < 0;
DELETE FROM horse where id < 0;

INSERT INTO owner (id, first_name, last_name, email)
VALUES (-1, 'Uncle', 'Bob', 'uncle.bob@gmail.com'),
       (-2, 'Rob', 'Pelinka', 'rob.en@pelinka.com'),
       (-3, 'Third', 'Owner', 'thirdowner-123@horse.com'),
       (-4, 'Owner', 'Test', 'owner@test.com'),
       (-5, 'Baris', 'Unsal', 'baris@unsal.com'),
       (-6, 'Onur', 'Tezgel', 'tezg.el@test.com'),
       (-7, 'Ben', 'Mate', 'bb_bb@mate.com'),
       (-8, 'Hakim', 'II', 'hakim2@gmail.com'),
       (-9, 'Sarri', 'Host', 'hostsari11@m.com'),
       (-10, 'Pep', 'Si', 'pep@si.com')
;

INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id, mother_id, father_id)
VALUES (-1, 'Wendy', 'Grand Mother', '2012-12-12', 'FEMALE', -4, null, null),
       (-2, 'Baba', 'Grand Father', '2010-10-10', 'MALE', -9, null, null),
       (-3, 'Mendy', 'Father', '2014-12-12', 'MALE', -1, -1, -2),
       (-4, 'Bella', 'Mother', '2015-01-01', 'FEMALE', -1, null, null),
       (-5, 'JJJ', 'Child', '2016-12-12', 'MALE', -1, -4, -3),
       (-6, 'George', 'vet', '1990-01-01', 'MALE', -10, null, null),
       (-7, 'Bullseye', 'Too Strong', '2013-10-11', 'MALE', -6, null, -6),
       (-8, 'Plane', 'flies', '2011-11-11', 'FEMALE', -2, null, null),
       (-9, 'Pegasus', 'Daughter', '2018-12-12', 'FEMALE', -7, null, -3),
       (-10, 'THY', 'Former Champ', '2015-03-12', 'MALE', -5, -8, -7)
;
