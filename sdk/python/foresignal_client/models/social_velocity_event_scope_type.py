from enum import StrEnum


class SocialVelocityEventScopeType(StrEnum):
    ENTITY = "entity"
    PLACE = "place"
    TOPIC = "topic"

    def __str__(self) -> str:
        return str(self.value)
