from enum import StrEnum


class CustomSourceInputProfileType3Type1(StrEnum):
    PERSONAL_LOCATION = "personal-location"

    def __str__(self) -> str:
        return str(self.value)
