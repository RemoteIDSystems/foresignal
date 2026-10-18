from enum import StrEnum


class PresenceEventClientsItemClientKind(StrEnum):
    AGENT = "agent"
    ANALYST = "analyst"
    GLOBE = "globe"
    MAP = "map"
    MOBILE = "mobile"
    OTHER = "other"
    RF = "rf"
    SATELLITE = "satellite"

    def __str__(self) -> str:
        return str(self.value)
