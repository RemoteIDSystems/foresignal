from enum import StrEnum


class OpaqueChronicleLinkAccessRequestStatusType3Type1(StrEnum):
    DECLINED = "declined"
    GRANTED = "granted"
    PENDING = "pending"

    def __str__(self) -> str:
        return str(self.value)
