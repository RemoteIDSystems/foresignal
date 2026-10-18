from enum import StrEnum


class SinkTypeId(StrEnum):
    APRS_IS_SERVE = "aprs-is-serve"
    CLOUDEVENTS = "cloudevents"
    COT = "cot"
    FEDERATION_PUSH = "federation-push"
    MQTT_STATE = "mqtt-state"
    NMEA_AIVDM = "nmea-aivdm"
    READSB_JSON = "readsb-json"
    SBS = "sbs"

    def __str__(self) -> str:
        return str(self.value)
