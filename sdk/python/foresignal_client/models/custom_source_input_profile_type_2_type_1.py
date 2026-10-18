from enum import StrEnum


class CustomSourceInputProfileType2Type1(StrEnum):
    PERSONAL_LOCATION = "personal-location"

    def __str__(self) -> str:
        return str(self.value)
