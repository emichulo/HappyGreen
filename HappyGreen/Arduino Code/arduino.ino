int water; //random variable

void setup() {
  pinMode(3, OUTPUT); //output pin for relay board, this will send a signal to the relay
  pinMode(A0, INPUT); //input pin coming from soil sensor
  Serial.begin(9600);
}

void loop() {
  water = analogRead(A0);  // reading the signal from the soil sensor
  int prt = map(water, 0, 1023, 0, 100);
  Serial.println(prt);


    char x = Serial.read();
    if (x == '1') {
      digitalWrite(3, HIGH); // HIGH to activate the relay
      delay(5000);
      digitalWrite(3, LOW); // LOW to deactivate the relay
    }
  

  if (water > 400) {
    digitalWrite(3, HIGH); // LOW to deactivate the relay
  } else {
    digitalWrite(3, LOW); // HIGH to activate the relay
  }

  delay(2000);
}


