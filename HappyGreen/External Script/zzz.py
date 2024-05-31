import serial.tools.list_ports
import time
import firebase_admin

from firebase_admin import credentials
from firebase_admin import db

cred = credentials.Certificate("config.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://happygreen-71102-default-rtdb.firebaseio.com'    # connecting to realtime database
})

ref = db.reference()                                                         # make a reference to head of database
ref = ref.child('id')


ser = serial.Serial('COM3', 9600)

id = '1'

pump_ref = ref.child(id)  #make ref to system 1

while True:
    ct = time.ctime()
    if 1:
             info = int(ser.readline().strip())
             pump_info = pump_ref.get().get('Btn')
             if (info > 50):
                info = info - 35
             else:
                info = info + 35
             print(info)
                
             if (info > 40  and pump_info == "OFF"):
                ref.update({
                    id: {
                    'Humidity': info,
                    'Time': ct,
                    'Pump': 'OFF',
                    'Btn':'OFF'}
                        })
             else:
                 ref.update({
                    id: {
                    'Humidity': info,
                    'Time': ct,
                    'Pump': 'ON',
                    'Btn':'OFF'}
                        })
             
             if (info > 40 and pump_info == "ON"):
                 ser.write(b'1')
                 time.sleep(5)
                 
