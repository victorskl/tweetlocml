@echo off

echo _
echo ***************************************************************************************************
echo * It takes quite sometime to generate with this classifier. Using dry-run for demo purpose only...*
echo ***************************************************************************************************
echo _
echo _
pause

java -jar tweetlocml-1.0-SNAPSHOT-jar-with-dependencies.jar -a place -o place/output -d 50 -i 123

pause