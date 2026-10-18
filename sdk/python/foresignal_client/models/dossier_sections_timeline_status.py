from enum import StrEnum


class DossierSectionsTimelineStatus(StrEnum):
    EMPTY = "empty"
    NOT_INSTALLED = "not-installed"
    OK = "ok"
    UNAVAILABLE = "unavailable"

    def __str__(self) -> str:
        return str(self.value)
