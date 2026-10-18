from enum import StrEnum


class JwkUse(StrEnum):
    SIG = "sig"

    def __str__(self) -> str:
        return str(self.value)
