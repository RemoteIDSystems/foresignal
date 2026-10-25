from enum import StrEnum


class ReferenceAreaKind(StrEnum):
    ADMIN1 = "admin1"
    COUNTRY = "country"
    PLACE = "place"

    def __str__(self) -> str:
        return str(self.value)
