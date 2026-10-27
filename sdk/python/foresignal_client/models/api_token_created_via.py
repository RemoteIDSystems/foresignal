from enum import StrEnum


class ApiTokenCreatedVia(StrEnum):
    API = "api"
    BOOTSTRAP = "bootstrap"
    PAIRING = "pairing"
    SELF = "self"

    def __str__(self) -> str:
        return str(self.value)
