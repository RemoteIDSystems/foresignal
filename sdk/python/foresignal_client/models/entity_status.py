from enum import StrEnum


class EntityStatus(StrEnum):
    ACTIVE = "ACTIVE"
    MERGED = "MERGED"

    def __str__(self) -> str:
        return str(self.value)
