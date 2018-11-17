In order to be able to make requests against https://api.cp.dyson.com/ cert from dyson to be added to local cacerts.
## IMPORTANT: Add dyson's cert to your local JVM cacerts
Run following cmds:
1. `openssl s_client -connect  api.cp.dyson.com:443 </dev/null| sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > dyson_gen.cert`
2. `sudo keytool -import -alias dyson -keystore /path/to/cacerts -file ~/Workspace/git/homestats/dyson_gen.cert`. Replace `/path/to/cacerts` with real path, most likely `JAVA_HOME/jre/lib/security/cacerts`

## Quick start
Run `./gradlew dyson:clean dyson:run --args="your_email@blabla.com password`

## Using as a library:
The `com.artigile.homestats.sensor.dyson.Dyson` contains example how this 

## Disclaimer
Please feel free to use this code any way you'd like. Keep in mind though that author is not responsible for any damage or malfunction or your devices.
Code at this point supports only Dyson Pure Cool TP04, but can be easily extended to support more devices, re-factoring probably will be needed in order to add new type of devices.

### Thanks
Big thanks to owners of https://github.com/shadowwa/Dyson-MQTT2RRD and https://libpurecoollink.readthedocs.io/en/stable/#installation who made this library implementation much easier!.
 