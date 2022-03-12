cd bin/obfuscated
pbpaste > stack.txt
java -jar ../../tools/proguard5.2.1/lib/retrace.jar -verbose mapping stack.txt

