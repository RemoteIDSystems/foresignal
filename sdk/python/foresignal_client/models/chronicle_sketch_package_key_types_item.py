from enum import StrEnum


class ChronicleSketchPackageKeyTypesItem(StrEnum):
    AREA_TIME = "area-time"
    ENTITIES = "entities"
    EVIDENCE = "evidence"
    TOPICS = "topics"

    def __str__(self) -> str:
        return str(self.value)
