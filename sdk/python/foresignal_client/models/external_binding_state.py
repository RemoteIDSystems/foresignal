from enum import StrEnum


class ExternalBindingState(StrEnum):
    ACTIVE = "active"
    BACKFILLING = "backfilling"
    DISABLED = "disabled"
    ERROR = "error"

    def __str__(self) -> str:
        return str(self.value)
