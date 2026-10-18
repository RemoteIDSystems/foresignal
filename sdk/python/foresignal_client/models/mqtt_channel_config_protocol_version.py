from enum import StrEnum


class MqttChannelConfigProtocolVersion(StrEnum):
    VALUE_0 = "5"
    VALUE_1 = "3.1.1"

    def __str__(self) -> str:
        return str(self.value)
