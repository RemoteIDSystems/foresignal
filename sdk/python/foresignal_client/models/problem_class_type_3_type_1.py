from enum import StrEnum


class ProblemClassType3Type1(StrEnum):
    ANONYMOUS = "anonymous"
    EXPENSIVE = "expensive"
    METRICS = "metrics"
    READ = "read"
    STREAM = "stream"
    TILES = "tiles"
    WRITE = "write"

    def __str__(self) -> str:
        return str(self.value)
