from enum import StrEnum


class ChronicleSocialVolumeSelectorScopesItemType(StrEnum):
    ENTITY = "entity"
    PLACE = "place"
    TOPIC = "topic"

    def __str__(self) -> str:
        return str(self.value)
