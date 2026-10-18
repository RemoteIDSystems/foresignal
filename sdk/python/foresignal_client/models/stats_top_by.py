from enum import StrEnum


class StatsTopBy(StrEnum):
    CELL = "cell"
    ENTITY = "entity"
    KIND = "kind"
    SOURCE = "source"

    def __str__(self) -> str:
        return str(self.value)
