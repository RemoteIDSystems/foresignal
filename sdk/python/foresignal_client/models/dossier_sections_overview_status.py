from enum import StrEnum


class DossierSectionsOverviewStatus(StrEnum):
    EMPTY = "empty"
    NOT_INSTALLED = "not-installed"
    OK = "ok"
    UNAVAILABLE = "unavailable"

    def __str__(self) -> str:
        return str(self.value)
