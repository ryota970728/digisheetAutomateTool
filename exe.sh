#!/bin/bash

# ディレクトリの移動
cd /Users/hiraryouta/git/selenium-project

# javacでソースコードファイルをバイトコードにコンパイルする
javac -d /Users/hiraryouta/git/selenium-project/jar -cp "libs/selenium-java-4.22.0/*" src/main/java/com/example/*.java

# マニフェストファイルの内容を設定
echo "Main-Class: com.example.App" > /Users/hiraryouta/git/selenium-project/MANIFEST.MF

# jarファイルを作成
jar cvfm automateTool.jar /Users/hiraryouta/git/selenium-project/MANIFEST.MF -C /Users/hiraryouta/git/selenium-project/jar .

# シェルスクリプトの実行権限を付与する
chmod +x /Users/hiraryouta/git/selenium-project/exe.sh

# 環境変数にJavaのパスを設定
export JAVA_HOME=/usr/bin/java
export PATH=$JAVA_HOME/bin:$PATH

# JARファイルを実行
java -cp "libs/selenium-java-4.22.0/*:automateTool.jar" com.example.App
