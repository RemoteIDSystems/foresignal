from enum import StrEnum


class EdpOriginClassType3Type1(StrEnum):
    AGENCY = "agency"
    AGGREGATOR = "aggregator"
    CROWD = "crowd"
    DERIVED = "derived"
    MEDIA = "media"
    OPERATOR = "operator"
    SENSOR_NETWORK = "sensor-network"
    SOCIAL = "social"

    def __str__(self) -> str:
        return str(self.value)
