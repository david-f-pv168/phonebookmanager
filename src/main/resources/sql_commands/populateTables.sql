INSERT INTO CONTACT (FIRST_NAME, SURNAME, PRIMARY_EMAIL, BIRTHDAY)
VALUES ('Gregory', 'House', 'gregory.house@fi.muni.cz', '1958-06-11');

INSERT INTO PHONENUMBER (NUMBER, COUNTRY_CODE, PHONE_TYPE, CONTACT_ID)
VALUES ('777777777', '00421', 'FAMILY', (SELECT ID FROM CONTACT));