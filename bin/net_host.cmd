@echo off
java -Xmx1024M -cp "%~d0%~p0../framework/dist/ttfms-framework.jar" org.w3c.exi.ttf.NetworkHost %*
