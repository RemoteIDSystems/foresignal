from enum import StrEnum


class GetAlertsAtomStateItem(StrEnum):
    ACKNOWLEDGED = "acknowledged"
    FIRING = "firing"
    RESOLVED = "resolved"

    def __str__(self) -> str:
        return str(self.value)
