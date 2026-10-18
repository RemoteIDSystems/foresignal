from enum import StrEnum


class ClaimPlacePrecision(StrEnum):
    ADMIN1 = "admin1"
    COUNTRY = "country"
    LOCALITY = "locality"
    POINT = "point"

    def __str__(self) -> str:
        return str(self.value)
