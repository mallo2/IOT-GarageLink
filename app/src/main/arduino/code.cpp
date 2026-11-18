#include <WiFi101.h>
#include <WiFiUdp.h>
#include <RTCZero.h>

// Wi-Fi
char ssid[] = "OnePlus15";
char password[] = "anas.anaa";

// Adresse IP du Raspberry Pi
IPAddress raspberryIP(10, 171, 244, 112);
const unsigned int port = 5000;

// Client Udp
WiFiUDP udp;

// Timestamp
RTCZero rtc;

void connectWifi() {
  Serial.println("Connexion au Wi-Fi...");
  while (WiFi.begin(ssid, password) != WL_CONNECTED) {
    delay(2000);
  }
  Serial.print("Wi-Fi connecte ! IP : ");
  Serial.println(WiFi.localIP());
  udp.begin(5001);

  rtc.begin();
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
      particleSensorValues[0]  = (buf[10] << 8) | buf[11];
      particleSensorValues[1] = (buf[12] << 8) | buf[13];
      particleSensorValues[2] = (buf[14] << 8) | buf[15];

      Serial.print("PM1.0=");  Serial.print(particleSensorValues[0]);
      Serial.print(" PM2.5="); Serial.print(particleSensorValues[1]);
      Serial.print(" PM10=");  Serial.println(particleSensorValues[2]);

      return true;
  } else {
      return false;
  }

}


void setup() {
  Serial.begin(115200);
  Serial1.begin(9600);
  connectWifi();
  initTime();
}


void loop() {
  uint16_t particleSensorValues[3];

  if (particleSensor(particleSensorValues)) {
    String message = "pms5003;" + String(particleSensorValues[0]) + "," + String(particleSensorValues[1]) + "," + String(particleSensorValues[2]) + ";" + timeString();

    udp.beginPacket(raspberryIP, port);
    udp.print(message);
    udp.endPacket();

    Serial.println("Donnee envoyee : " + message);
  }
}