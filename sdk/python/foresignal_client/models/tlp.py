from enum import StrEnum


class Tlp(StrEnum):
    AMBER = "AMBER"
    AMBERSTRICT = "AMBER+STRICT"
    CLEAR = "CLEAR"
    GREEN = "GREEN"
    RED = "RED"

    def __str__(self) -> str:
        return str(self.value)
