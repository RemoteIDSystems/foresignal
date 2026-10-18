from enum import StrEnum


class TriggerRuleGeofenceOrigin(StrEnum):
    INLINE = "inline"
    NEAR = "near"

    def __str__(self) -> str:
        return str(self.value)
