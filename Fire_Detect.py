from firebase_admin import credentials, initialize_app, storage

import cv2
import imutils
from datetime import datetime
import pyrebase

firebaseConfig = {
    "apiKey": "AIzaSyB3lqLMl_FA7wBB78UQi9PHAwOrV7Nm3_E",
    "authDomain": "it-ad2f8.firebaseapp.com",
    "databaseURL": "https://it-ad2f8-default-rtdb.firebaseio.com",
    "projectId": "it-ad2f8",
    "storageBucket": "it-ad2f8.appspot.com",
    "messagingSenderId": "344609205528",
    "appId": "1:344609205528:web:654d890c398ac71907c26d",
    "measurementId": "G-S0C9SG8R0Y"
}

firebase = pyrebase.initialize_app(firebaseConfig)
db = firebase.database()
db.child("Secureit").update({"fire": 0})


fire_cascade = cv2.CascadeClassifier('Fire.xml')

cap = cv2.VideoCapture(0)

cred = credentials.Certificate("it-ad2f8-b5c158d2ca40.json")
initialize_app(cred, {'storageBucket': 'it-ad2f8.appspot.com'})
bucket = storage.bucket()

count=0

while(True):
    ret, frame = cap.read()
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    fire = fire_cascade.detectMultiScale(frame, 1.2, 5)#scalefactor,no. of neighbours

    for (x,y,w,h) in fire:
        cv2.rectangle(frame,(x-20,y-20),(x+w+20,y+h+20),(255,0,0),2)
        roi_gray = gray[y:y+h, x:x+w]
        roi_color = frame[y:y+h, x:x+w]
        print("fire is detected")
        db.child("Secureit").update({"fire": 1})
        del fire
        ct = datetime.now()
        ts = ct.timestamp()
        ts = datetime.fromtimestamp(ts)
        ts = ts.strftime("%Hhrs%Mmins%Ssecs%d,%B,%Y")
        filename = 'Fire' + ts + ".jpg"
        cv2.imwrite(filename, frame)
        blob = bucket.blob(filename)
        blob.upload_from_filename(filename)
        blob.make_public()
        data = {"imageUrl": blob.public_url, "name": filename}
        db.child("Uploads").push(data)
        count += 1

    cv2.imshow('frame', frame)
    if cv2.waitKey(27) & 0xFF == ord('q'):
        break
