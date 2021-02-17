# Tugas Besar 1 - Strategi Algoritma
> Pemanfaatan Algoritma *Greedy* dalam Aplikasi Permainan *Worms*

### Kelompok 8 - Anang Hijau
| Anggota | NIM |
| --- | --- |
|Dionisius Darryl H. | 13519058 |	
|Josep Marcello| 13519164 |	
|Wilson Tandya | 13519209 |

## Table of contents
* [General info](#general-info)
* [Technologies and Requirements](#technologies)
* [Setup](#setup)

## General info
Sebuah bot permainan _Worms_ yang mengimplementasikan algoritma _Greedy_.
Pada bot ini, terdapat 3 strategi _Greedy_ utama yang diimplementasikan yaitu:
1. Strategi pergerakan (_move_) berdasarkan prioritas _health_ dan jarak ke musuh.
2. Strategi pemilihan senjata berdasarkan _utilities_ yang ada.
3. Strategi pengambilan _health pack_

## Technologies and Requirements
**Environment requirements**
* Install the **Java SE Development Kit 8** for your environment here: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
* Make sure **JAVA_HOME system variable** is set, Windows 10 tutorial here: https://www.mkyong.com/java/how-to-set-java_home-on-windows-10/
* Install **IntelliJ IDEA** here: https://www.jetbrains.com/idea/download/

## Setup
**Configuring the game and runner**

Simply edit the game-config.json and game-config-runner.json in the main folder.

**Modifying the bot**
1. Make your modifications to the starter bot using IntelliJ. 
2. Once you are happy with your changes, package your bot by opening up the "Maven Projects" tab on the right side of the screen. 
3. From here go to the "java-sample-bot" > "Lifecycle" group and double-click "Install" This will create a .jar file in the folder called "target". 
4. The file will be called "java-sample-bot-jar-with-dependencies.jar".

**Running**

Simply run (double-click) the "run.bat" file, or open up Command Prompt in the starter pack's root directory and run the "run.bat" command.

## Thank you
