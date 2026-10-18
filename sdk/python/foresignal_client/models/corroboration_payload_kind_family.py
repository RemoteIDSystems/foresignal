from enum import StrEnum


class CorroborationPayloadKindFamily(StrEnum):
    CONFLICT = "conflict"
    EARTHQUAKE = "earthquake"
    FIRE = "fire"
    OUTAGE = "outage"

    def __str__(self) -> str:
        return str(self.value)
