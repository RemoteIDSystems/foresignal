from enum import StrEnum


class ChronicleSketchBatchAccessDecisionsItemStatus(StrEnum):
    DECLINED = "declined"
    GRANTED = "granted"

    def __str__(self) -> str:
        return str(self.value)
