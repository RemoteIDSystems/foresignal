from enum import StrEnum


class SinkTypeModesItem(StrEnum):
    PUSH = "PUSH"
    SERVE = "SERVE"

    def __str__(self) -> str:
        return str(self.value)
