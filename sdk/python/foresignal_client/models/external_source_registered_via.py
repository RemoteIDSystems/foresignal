from enum import StrEnum


class ExternalSourceRegisteredVia(StrEnum):
    OPERATOR = "operator"
    REGISTRATION_TOKEN = "registration-token"

    def __str__(self) -> str:
        return str(self.value)
