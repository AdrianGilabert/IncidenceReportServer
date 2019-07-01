# IncidenceReportServer

Installation

sudo apt-get update

sudo apt-get upgrade

sudo apt-get install ffmpeg

sudo apt-get install sox

sudo apt-get install flex

sudo apt-get install bison

sudo apt-get install libfftw3

unzip iatros-matriculas on the locaation where you wanto to install with the password specified on the pdf

open the file iatros_launche-modr.sh with a text editor and raplace all tje relative paths with complete paths, for example, ./iatros/build/lib/ replace with /home/adrian/iatros_matriculas/iatros/build/lib/

cd iatros

mkdir build

cd build

../configure

make

make install

open the project with intellij

open the classes PlateList.java EditIncidenceController.java and AudioRecogniser.java and replace all the paths with your speceific paths.

Run

go to the iatros_matriculas folder and run the scipt speech_background.sh

exectute the javafx aplication with intelliJ or make an executable if you want.
