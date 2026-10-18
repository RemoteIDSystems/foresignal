from enum import StrEnum


class PushPayloadActionsItemAction(StrEnum):
    ACK = "ack"
    FEEDBACKNOISE = "feedback:noise"
    FEEDBACKUSEFUL = "feedback:useful"
    MUTE = "mute"
    SNOOZE = "snooze"

    def __str__(self) -> str:
        return str(self.value)
