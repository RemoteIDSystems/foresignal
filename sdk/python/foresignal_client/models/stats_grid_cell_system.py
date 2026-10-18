from enum import StrEnum


class StatsGridCellSystem(StrEnum):
    H3 = "h3"
    HEX3857 = "hex3857"

    def __str__(self) -> str:
        return str(self.value)
