from enum import StrEnum


class MeshChannelCommonConfigAllowedSendersAdditionalProperty(StrEnum):
    ACK = "ack"
    REPORT = "report"

    def __str__(self) -> str:
        return str(self.value)
