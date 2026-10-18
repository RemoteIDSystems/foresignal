from enum import StrEnum


class SocialVolumeScopeType(StrEnum):
    ENTITY = "entity"
    PLACE = "place"
    TOPIC = "topic"

    def __str__(self) -> str:
        return str(self.value)
