#!/bin/bash

server="SERVER PATH"
warmupTime=1.00

TakePicture(){
	#TAKE PICTURE
	./imagesnap -w $warmupTime ./snapshot.jpeg

	openssl base64 -in ./snapshot.png -out ./snapshot.b64

}

SendPhotoToServer(){

curl --request POST \
 --url https://api.sendgrid.com/api/mail.send.json \
 --form api_user=****** \
 --form 'api_key=******' \
 --form to=try@eden-battlehack.bymail.in \
 --form subject=mood-6 \
 --form from='******' \
 --form text=.snapshot.b64 \
 # --data .snapshot.png;base64

}

TakePicture
SendPhotoToServer


