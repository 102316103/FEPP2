#!/bin/bash

echo "Start compiler"
echo "execute javac -cp .:IMSConnectAPI-3.2.0.1.jar com/system/test/ims/adapterTest/ImsConnect.java"
javac -cp .:/lib/fep-enclib.jar com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.java
echo "execute javac -cp .:com/system/test/ims/adapterTest/ImsConnect.class com/system/test/ims/adapterTest/imsConnectionTest.java"
#javac -cp .:com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.class
echo "compiler execute successful"
echo "Packaged into a jar file Start..."
echo "execute jar -cvfm sendToIms.jar MANIFEST.MF com/system/test/ims/adapterTest/*"
jar -cvfm makeMac.jar MANIFEST.MF com/syscom/fep/suipConnect/ConnectSuipChangeDateAndMakeMac.class
echo "Packaged into a jar file successful"
