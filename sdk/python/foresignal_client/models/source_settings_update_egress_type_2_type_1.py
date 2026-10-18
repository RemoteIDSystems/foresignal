from enum import StrEnum


class SourceSettingsUpdateEgressType2Type1(StrEnum):
    LOCAL_ONLY = "LOCAL_ONLY"
    OPEN = "OPEN"

    def __str__(self) -> str:
        return str(self.value)
