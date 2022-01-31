#!/bin/sh

if test -z "$(command -v apt-get)"; then
	echo "该脚步太烂，无法在此电脑运行。"
	exit 1
else
	echo "准备安装软件"
	sudo apt-get update 
	sudo apt-get install -y tesseract-ocr libtesseract-dev tesseract-ocr-chi-sim openjdk-17-jdk
fi

if test -z "$(command -v crontab)"; then
	echo "安装 crontab 失败"
	exit 1
fi

if test -z "$(command -v tesseract)"; then
	echo "安装 tesseract 失败"
	exit 1
fi

if test -z "$(tesseract --list-langs | grep chi_sim)"; then
	echo "安装 tesseract-ocr-chi-sim 失败"
	exit 1
fi

if test -z "$(java --version)"; then
	echo "安装 openjdk-17-jdk 失败"
	exit 1
fi

javac ./Main.java 
if [ ! -d "./backup_crontab" ]; then
  mkdir ./backup_crontab
fi
sudo cp /var/spool/cron/crontabs/$(whoami) ./backup_crontab/$(whoami)_$(ls -al|grep "^-"| wc -l)
sudo echo "15 * * * * cd $(pwd) && java Main auto" >> /var/spool/cron/crontabs/$(whoami)
