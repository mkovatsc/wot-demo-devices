# Samsung WoT Demo

Web of Things Demonstrator

## Prerequisites

1. Get the resource directory (RD) `cf-rd` from https://github.com/eclipse/californium.tools and create the JARs through `mvn clean install`
2. Get the iot-semantics projects `reasoning-server` and `semantic-ide` from https://github.com/mkovatsc/iot-semantics and create the JARs through `mvn clean install`

## Getting Started

1. Add the IPv6 address 2001:0470:cafe::38b2:cf50 to the ethernet interface of the machine running this demo 
2. Start the RD (opens port 5683)
3. Start the reasoning server (opens port 5681)
4. Start the Semantic IDE (opens HTTP port on 9090)
5. Start devices (open random port and register with RD)

## Android Devices and LIFX Light Bulbs

For Wi-Fi devices, connect the machine running the demo to a wireless router that supports IPv6 and is configured with the prefix `2001:0470:cafe::/64`. While Android devices are straight forward to connect, the LIFX bulbs require a smartphone app to bootstrap the Wi-Fi SSID and password.

1. Use Android Studio to compile and run the Android app `android.AudioDock` on an actual phone.
2. Start the `services.LIFX` gateway that provides a CoAP frontend for the proprietary LIFX API.

## Bluetooth Low Energy Devices

Flash the Nordic nRF51 development board with the `client_server_observe` example from the `nrf51` directory. A standard Bluetooth 4.0 Dongle (e.g., connected to Raspberry Pi) can serve as border router.
