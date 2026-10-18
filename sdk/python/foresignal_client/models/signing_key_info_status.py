from enum import StrEnum


class SigningKeyInfoStatus(StrEnum):
    ACTIVE = "active"
    RETIRED = "retired"

    def __str__(self) -> str:
        return str(self.value)
