from enum import StrEnum


class EmergencySquawkPayloadMeaning(StrEnum):
    EMERGENCY = "emergency"
    RADIO_FAILURE = "radio failure"
    UNLAWFUL_INTERFERENCE = "unlawful interference"

    def __str__(self) -> str:
        return str(self.value)
