from enum import StrEnum


class ChronicleAccessEventStatus(StrEnum):
    DECLINED = "declined"
    GRANTED = "granted"
    PENDING = "pending"

    def __str__(self) -> str:
        return str(self.value)
