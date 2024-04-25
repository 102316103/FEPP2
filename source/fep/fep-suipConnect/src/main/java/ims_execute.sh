#!/bin/sh

echo "Start compiler"
echo "execute javac -cp commons-lang3-3.12.0.jar;fep-enclib.jar com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.java"
javac -cp C:\\Syscom\\TCBFEP\\source\\fep\\fep-suipConnect\\src\\main\\java\\fep-enclib.jar;C:\\Syscom\\TCBFEP\\source\\fep\\fep-suipConnect\\src\\main\\java\\commons-lang3-3.12.0.jar
com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.java
#javac -cp fep-enclib.jar com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.java
#javac -cp spring-boot-2.6.3.jar com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.java
#javac -cp .:commons-lang3-3.12.0.jar;fep-enclib.jar com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.java
#echo "execute javac -cp .:com/system/test/ims/adapterTest/ImsConnect.class com/system/test/ims/adapterTest/imsConnectionTest.java"
#javac -cp .:com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.class
echo "compiler execute successful"
echo "Packaged into a jar file Start..."
echo "execute jar -cvfm makeMac.jar MANIFEST.MF com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.class"
jar -cvfm makeMac.jar MANIFEST.MF com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.class
echo "Packaged into a jar file successful"
read null