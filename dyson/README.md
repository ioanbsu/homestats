Java-based library for establishing connection to dyson devices that are available in local network(Only Dyson Pure Cool TP04 currently supported).

## IMPORTANT: Add dyson's cert to your local JVM cacerts
In order to be able to make requests against https://api.cp.dyson.com/ cert from dyson to be added to local cacerts.
Run following cmds:
1. `openssl s_client -connect  api.cp.dyson.com:443 </dev/null| sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > dyson_gen.cert`
2. `sudo keytool -import -alias dyson -keystore /path/to/cacerts -file dyson_gen.cert`. Replace `/path/to/cacerts` with real path, most likely `JAVA_HOME/jre/lib/security/cacerts`
3. if password is asked try using "changeit".

## Quick start
Run `./gradlew dyson:clean dyson:run --args="your_email@blabla.com password"`

## Troubleshooting
If you encounter `java.security.InvalidKeyException: Illegal key size` when Dyson `localCredentials` key is parsed, please follow instructions below(tested in Raspbian GNU/Linux 7, but should work in most unix based envs):

1. Go to Oracle’s website and search for ‘Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files’.

2. Depending upon the Java version installed on your machine, download the zip file and extract it on your drive.

3. `unzip jce_policy-8.zip` ( or jce_policy-{YOUR_VERSION}.zip}

4. `cd UnlimitedJCEPolicyJDK8` (or UnlimitedJCEPolicyJDK{YOUR_VERSION})

5. `cp ./*policy.jar your_java_installation_directory/jre/lib/security`

6. Restart your project that uses Dyson lib. Should work now.

## Using as a library:
The `com.artigile.homestats.sensor.dyson.Dyson` contains example how to subscribe for devices updates with consumer.
Happy coding :) 

## Disclaimer
Please feel free to use this code any way you'd like. Keep in mind though that author is not responsible for any damage or malfunction or your devices.
Code at this point supports only Dyson Pure Cool TP04, but can be easily extended to support more devices, re-factoring probably will be needed in order to add new type of devices.

### Thanks
Big thanks to owners of https://github.com/shadowwa/Dyson-MQTT2RRD and https://libpurecoollink.readthedocs.io/en/stable/#installation who made this library implementation much easier!.
 