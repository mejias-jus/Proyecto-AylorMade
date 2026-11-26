@echo off
rem Compila y ejecuta AylorMade sin necesidad de NetBeans. Requiere el JDK (javac) instalado.
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
echo Compilando...
javac -encoding UTF-8 -cp "lib\*" -d out src\modelo\*.java src\datos\*.java src\logica\*.java src\presentacion\*.java src\principal\*.java
if errorlevel 1 (
    echo.
    echo No se pudo compilar. Verifique que el JDK este instalado y que la carpeta lib tenga los .jar.
    pause
    exit /b 1
)
xcopy /E /I /Y src\imagenes out\imagenes >nul
echo Iniciando AylorMade...
java -cp "out;lib\*" principal.Main
