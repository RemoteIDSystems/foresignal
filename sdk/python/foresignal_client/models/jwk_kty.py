from enum import StrEnum


class JwkKty(StrEnum):
    OKP = "OKP"

    def __str__(self) -> str:
        return str(self.value)
