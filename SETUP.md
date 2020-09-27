For ios Certificate p12 file to crt and key file
--------------------------------------------------------
openssl pkcs12 -in PUSH_KDCHAT2_PROD.p12 -nocerts -nodes | openssl rsa > PUSH_KDCHAT2_PROD.key
openssl pkcs12 -in PUSH_KDCHAT2_PROD.p12 -nokeys -out PUSH_KDCHAT2_PROD.crt

For jks file from cert and and key file
--------------------------------------------------------
cd /home/ubuntu/kdchat/build/config/
cp /etc/letsencrypt/live/chatv2.fruvation.com/fullchain.pem .
cp /etc/letsencrypt/live/chatv2.fruvation.com/privkey.pem .
 
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -name "chatv2.fruvation.com" -out chatv2.p12.cert

keytool -importkeystore -deststorepass 123456 -destkeypass 123456 -destkeystore chatv2.jks -srckeystore chatv2.p12.cert -srcstoretype PKCS12 -srcstorepass 123456 -alias chatv2.fruvation.com

keytool -import -v -trustcacerts -file fullchain.pem -alias chatv2.fruvation.com -keystore chatv2.fruvation.com.truststore.jks

Not required below
-------------------------
keytool -import -v -trustcacerts -alias chatv2.fruvation.com -file chatv2.p12.cert -keystore chatv2.truststore.jks
keytool -import -trustcacerts -alias chatv2.fruvation.com -file fullchain.pem -keypass 123456 -keystore chatv2.jks -storepass 123456

Convert Server certificate to iOS compatible Certificate 
--------------------------------------------------------
openssl x509 -in chatv2.fruvation.com.cer -out textsecure.cer -outform DER

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
D:/SOFTWARES/JAVA/jdk-11/bin/java -jar target/TextSecureServer-2.92.jar accountdb migrate config/local.yml
D:/SOFTWARES/JAVA/jdk-11/bin/java -jar target/TextSecureServer-2.92.jar messagedb migrate config/local.yml
D:/SOFTWARES/JAVA/jdk-11/bin/java -jar target/TextSecureServer-2.92.jar abusedb migrate config/local.yml
D:/SOFTWARES/JAVA/jdk-11/bin/java -jar target/TextSecureServer-2.92.jar keysdb migrate config/local.yml

Database Migration (Prod)
------------------------------------------------------
java -jar /home/ubuntu/kdchat/build/TextSecureServer-2.92.jar accountdb migrate /home/ubuntu/kdchat/build/config/prod.yml
java -jar /home/ubuntu/kdchat/build/TextSecureServer-2.92.jar messagedb migrate /home/ubuntu/kdchat/build/config/prod.yml
java -jar /home/ubuntu/kdchat/build/TextSecureServer-2.92.jar abusedb migrate /home/ubuntu/kdchat/build/config/prod.yml
java -jar /home/ubuntu/kdchat/build/TextSecureServer-2.92.jar keysdb migrate /home/ubuntu/kdchat/build/config/prod.yml

Run Jar Command (Local)
------------------------------------------------------
D:/SOFTWARES/JAVA/jdk-11/bin/java -jar target/TextSecureServer-2.92.jar server config/local.yml

Run Jar Command (Prod)
------------------------------------------------------
java -jar /home/ubuntu/kdchat/build/TextSecureServer-2.92.jar server /home/ubuntu/kdchat/build/config/prod.yml