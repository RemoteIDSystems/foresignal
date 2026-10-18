from enum import StrEnum


class SituationEventEvent(StrEnum):
    SITUATION_ACTIVATED = "situation.activated"
    SITUATION_CLOSED = "situation.closed"
    SITUATION_CREATED = "situation.created"
    SITUATION_DORMANT = "situation.dormant"
    SITUATION_ESCALATED = "situation.escalated"
    SITUATION_MERGED = "situation.merged"
    SITUATION_UPDATED = "situation.updated"

    def __str__(self) -> str:
        return str(self.value)
