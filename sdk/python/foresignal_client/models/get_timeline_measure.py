from enum import StrEnum


class GetTimelineMeasure(StrEnum):
    ENTITIES = "entities"
    VERSIONS = "versions"

    def __str__(self) -> str:
        return str(self.value)
