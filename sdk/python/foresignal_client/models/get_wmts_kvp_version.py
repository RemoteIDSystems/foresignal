from enum import StrEnum


class GetWmtsKvpVERSION(StrEnum):
    VALUE_0 = "1.0.0"

    def __str__(self) -> str:
        return str(self.value)
