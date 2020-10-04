For ios Certificate p12 file to crt and key file
--------------------------------------------------------
openssl pkcs12 -in PUSH_KDCHAT2_PROD.p12 -nocerts -nodes | openssl rsa > PUSH_KDCHAT2_PROD.key

openssl pkcs12 -in PUSH_KDCHAT2_PROD.p12 -nokeys -out PUSH_KDCHAT2_PROD.crt

Else

Use KeyStore Tool
------------------
Open iOS developer shared apns_cert.p12 file in KeyStore Tool.
Enter Password if required(Asked)
Right click on the opened(imported) certificate and click on Export Certificate (full chain)
Export Cert and save with apns_chat_stage.cer
Again Right click on the opened(imported) certificate and click on Export Private Key
Export Key and save with apns_chat_stage.key


For jks file from cert and and key file
--------------------------------------------------------
cp /etc/letsencrypt/live/chat.ezifytech.com/fullchain.pem /home/ubuntu/chat/build/config/

cp /etc/letsencrypt/live/chat.ezifytech.com/privkey.pem /home/ubuntu/chat/build/config/
 
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -name "chat.ezifytech.com" -out chatv2.p12.cert

keytool -importkeystore -deststorepass 123456 -destkeypass 123456 -destkeystore chatv2.jks -srckeystore chatv2.p12.cert -srcstoretype PKCS12 -srcstorepass 123456 -alias chat.ezifytech.com

keytool -import -v -trustcacerts -file fullchain.pem -alias chat.ezifytech.com -keystore chatv2.truststore.jks

Not required below
-------------------------
keytool -import -v -trustcacerts -alias chat.ezifytech.com -file chatv2.p12.cert -keystore chatv2.truststore.jks

keytool -import -trustcacerts -alias chat.ezifytech.com -file fullchain.pem -keypass 123456 -keystore chatv2.jks -storepass 123456

Convert Server certificate to iOS compatible Certificate 
--------------------------------------------------------
openssl x509 -in chat.ezifytech.com -out textsecure.cer -outform DER

Android whisper.store file changes
--------------------------------------------------------
Open whisper.store file

Enter password 'whisper'

Delete existing trusted certificate

Import above textsecure.der (certificate file generated for ios)

save whisper.store file (ctrl + s)

Share with Andorid developer

To convert/generated certificates online: https://www.sslshopper.com/ssl-converter.html


Local Self Signed Certificate Creation Commands
--------------------------------------------------------
keytool -export -alias localhost -file localhost.crt -keystore localhost.jks

keytool -import -v -trustcacerts -alias localhost -file localhost.crt -keystore truststore.jks

Postgress Change Password Command
------------------------------------------------
sudo -u postgres psql postgres

postgres=# \password

Postgress Useful Commands
------------------------------------------------
psql -V

sudo vi /etc/postgresql/12/main/pg_hba.conf

sudo vi /etc/postgresql/12/main/postgresql.conf

sudo /etc/init.d/postgresql restart

Database Migration (Local)
------------------------------------------------------
D:/SOFTWARES/JAVA/jdk-11/bin/java -jar target/TextSecureServer-3.21.jar accountdb migrate config/local.yml

D:/SOFTWARES/JAVA/jdk-11/bin/java -jar target/TextSecureServer-3.21.jar messagedb migrate config/local.yml

D:/SOFTWARES/JAVA/jdk-11/bin/java -jar target/TextSecureServer-3.21.jar abusedb migrate config/local.yml

Database Migration (Prod)
------------------------------------------------------
java -jar /home/ubuntu/chat/build/TextSecureServer-3.21.jar accountdb migrate /home/ubuntu/chat/build/config/prod.yml

java -jar /home/ubuntu/chat/build/TextSecureServer-3.21.jar messagedb migrate /home/ubuntu/chat/build/config/prod.yml

java -jar /home/ubuntu/chat/build/TextSecureServer-3.21.jar abusedb migrate /home/ubuntu/chat/build/config/prod.yml

Run Jar Command (Local)
------------------------------------------------------
D:/SOFTWARES/JAVA/jdk-11/bin/java -jar target/TextSecureServer-3.21.jar server config/local.yml

Run Jar Command (Prod)
------------------------------------------------------
java -jar /home/ubuntu/kdchat/build/TextSecureServer-3.21.jar server /home/ubuntu/chat/build/config/prod.yml
