from enum import StrEnum


class ActionResultAction(StrEnum):
    ACK = "ack"
    FEEDBACKNOISE = "feedback:noise"
    FEEDBACKUSEFUL = "feedback:useful"
    MUTE = "mute"
    SNOOZE = "snooze"

    def __str__(self) -> str:
        return str(self.value)
