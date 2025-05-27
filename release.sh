mkdir out
javac -d out src/*.java
jar cfm Tetris.jar MANIFEST.MF -C out .
jpackage --name Tetris --input . --main-jar Tetris.jar --main-class Frame --type exe --runtime-image "C:\Program Files\Eclipse Adoptium\jdk-17.0.8.101-hotspot" --dest release --win-shortcut
rm Tetris.jar
rm -rf out
read -p "Press enter to continue"