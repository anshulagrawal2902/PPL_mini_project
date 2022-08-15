from firebase_admin import credentials, initialize_app, storage
import numpy as np
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
db.child("Secureit").update({"gun": 0})

gun_cascade = cv2.CascadeClassifier('gun_cascade.xml')
camera = cv2.VideoCapture(0)

firstFrame = None
gun_exist = 0

cred = credentials.Certificate("it-ad2f8-b5c158d2ca40.json")
initialize_app(cred, {'storageBucket': 'it-ad2f8.appspot.com'})
bucket = storage.bucket()

count = 0
while True:

    ret, frame = camera.read()

    frame = imutils.resize(frame, width=500)
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    gun = gun_cascade.detectMultiScale(gray,
                                       1.9, 6,
                                       minSize=(100, 100))

    if len(gun) > 0:
        gun_exist += 1

    for (x, y, w, h) in gun:
        frame = cv2.rectangle(frame,
                              (x, y),
                              (x + w, y + h),
                              (255, 0, 0), 2)
        roi_gray = gray[y:y + h, x:x + w]
        roi_color = frame[y:y + h, x:x + w]
        font = cv2.FONT_HERSHEY_DUPLEX
        cv2.putText(frame, 'Gun Detected', (x + 6, y - 6), font, 0.5, (0, 0, 255), 1)

    if firstFrame is None:
        firstFrame = gray
        continue

    cv2.imshow("Security Feed", frame)
    if (cv2.waitKey(2) == 27):
        break

    if (gun_exist > 12):
        print("guns detected")
        db.child("Secureit").update({"gun": 1})
        gun_exist = 0
        del gun
        ct=datetime.now()
        ts=ct.timestamp()
        ts = datetime.fromtimestamp(ts)
        ts = ts.strftime("%Hhrs%Mmins%Ssecs%d,%B,%Y")
        filename = 'Gun' + ts + ".jpg"
        cv2.imwrite(filename, frame)
        blob = bucket.blob(filename)
        blob.upload_from_filename(filename)
        blob.make_public()
        data={"imageUrl":blob.public_url,"name":filename}
        db.child("Uploads").push(data)
        count += 1

camera.release()
cv2.destroyAllWindows()