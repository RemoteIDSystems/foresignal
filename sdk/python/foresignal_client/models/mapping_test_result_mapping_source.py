from enum import StrEnum


class MappingTestResultMappingSource(StrEnum):
    PROPOSED = "proposed"
    STORED = "stored"

    def __str__(self) -> str:
        return str(self.value)
