from enum import StrEnum


class NwsAlertPayloadMessageType(StrEnum):
    ALERT = "Alert"
    CANCEL = "Cancel"
    UPDATE = "Update"

    def __str__(self) -> str:
        return str(self.value)
