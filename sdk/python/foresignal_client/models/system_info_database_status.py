from enum import StrEnum


class SystemInfoDatabaseStatus(StrEnum):
    DOWN = "down"
    UP = "up"

    def __str__(self) -> str:
        return str(self.value)
