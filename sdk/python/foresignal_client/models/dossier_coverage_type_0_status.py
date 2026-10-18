from enum import StrEnum


class DossierCoverageType0Status(StrEnum):
    OK = "ok"
    PARTIAL = "partial"
    UNAVAILABLE = "unavailable"

    def __str__(self) -> str:
        return str(self.value)
