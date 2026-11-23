#include <WiFi101.h>
#include <WiFiUdp.h>
#include <RTCZero.h>

// Wi-Fi
char ssid[] = "OnePlus15";
char password[] = "anas.anaa";

// Adresse IP du Raspberry Pi
IPAddress raspberryIP(10, 171, 244, 247);
const unsigned int port = 5000;

// Client Udp
WiFiUDP udp;

// Timestamp
RTCZero rtc;

// Temperature sensor pin
const int DHTPIN = 2;
const int TEMPALARM = 40;

//buzzer sensor pin
const int BUZZERPIN = 5;

void connectWifi() {
  Serial.println("Connecting to Wi-Fi...");
  while (WiFi.begin(ssid, password) != WL_CONNECTED) {
    delay(2000);
  }
  Serial.print("Wi-Fi connected IP : ");
  Serial.println(WiFi.localIP());
  udp.begin(5001);

  rtc.begin();
}

void sendUdp(String message) {
  udp.beginPacket(raspberryIP, port);
  udp.print(message);
  udp.endPacket();
  Serial.println("Data sent : " + message);
}

void initTime() {
  unsigned long t = 0;
  int tries = 0;
  while (t == 0 && tries < 20) {
    t = WiFi.getTime();
    delay(1000);
    tries++;
  }
  // +3600 = Belgian winter time (UTC+1)
  rtc.setEpoch(t + 3600);
}

String timeString() {
  char buf[9];
  sprintf(buf, "%02d:%02d:%02d", rtc.getHours(), rtc.getMinutes(), rtc.getSeconds());
  return String(buf);
}

bool particleSensor(uint16_t particleSensorValues[3]) {

  while (Serial1.available() > 0 && Serial1.peek() != 0x42) {
    Serial1.read();
  }

  uint8_t buf[32];


  if (Serial1.available() >= 32) {
    Serial1.readBytes(buf, 32);
  } else {
    return false;
  }

  uint16_t sum = 0;

  for (int i = 0; i < 30; i++) {
    sum += buf[i];
  }

  uint16_t checksum = (buf[30] << 8) | buf[31];

  if (sum == checksum) {
      particleSensorValues[0] = (buf[10] << 8) | buf[11];
      particleSensorValues[1] = (buf[12] << 8) | buf[13];
      particleSensorValues[2] = (buf[14] << 8) | buf[15];

      Serial.print("PM1.0 = ");  Serial.print(particleSensorValues[0]);
      Serial.print(" PM2.5 = "); Serial.print(particleSensorValues[1]);
      Serial.print(" PM10 = ");  Serial.println(particleSensorValues[2]);

      return true;
  } else {
      return false;
  }

}

bool waitFor(int level, unsigned int timeoutUs) {
  unsigned long start = micros();
  while (digitalRead(DHTPIN) != level) {
    if (micros() - start > timeoutUs) return false;
  }
  return true;
}

bool TempSensor(uint16_t TempSensorValues[2]) {
  uint8_t data[5] = {0, 0, 0, 0, 0};

  //wake the sensor
  pinMode(DHTPIN, OUTPUT);
  digitalWrite(DHTPIN, LOW);
  delay(20);
  digitalWrite(DHTPIN, HIGH);
  pinMode(DHTPIN, INPUT_PULLUP);

  if (!waitFor(LOW, 200) || !waitFor(HIGH, 200) || !waitFor(LOW, 200)) {
    return false;
  }

  //read the 40 bits
  for (int i = 0; i < 40; i++) {
    if (!waitFor(HIGH, 200)) {
      return false;
    }
    unsigned long t = micros();
    if (!waitFor(LOW, 200)) {
      return false;
    }
    unsigned long width = micros() - t;

    data[i / 8] <<= 1;
    if (width > 40) {
      data[i / 8] |= 1;
    }
  }

  //checksum check
  uint8_t sum = data[0] + data[1] + data[2] + data[3];
  uint8_t checksum = data[4];

  if (sum == checksum) {
    // temperature
    TempSensorValues[0] = data[2];
    // humidity
    TempSensorValues[1] = data[0];
    return true;
  } else {
    return false;
  }
}

void setup() {
  pinMode(BUZZERPIN, OUTPUT);
  digitalWrite(BUZZERPIN, LOW);

  Serial.begin(115200);
  Serial1.begin(9600);
  connectWifi();
  initTime();
}


void loop() {
  uint16_t particleSensorValues[3];
  uint16_t TempSensorValues[2];

  if (particleSensor(particleSensorValues)) {
    String message = "pms5003;" + String(particleSensorValues[0]) + "," + String(particleSensorValues[1]) + "," + String(particleSensorValues[2]) + ";" + timeString();
    sendUdp(message);
  }

  if (TempSensor(TempSensorValues)) {
    String message = "dht11;" + String(TempSensorValues[0]) + "," + String(TempSensorValues[1]) + ";" + timeString();
    sendUdp(message);

      if (TempSensorValues[0] > TEMPALARM) {
        tone(BUZZERPIN, 3000);
        delay(100);
        noTone(BUZZERPIN);
        delay(100);
      }

  }
}