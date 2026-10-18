from enum import StrEnum


class GetRecordIncludeItem(StrEnum):
    ENTITY = "entity"
    OBSERVER = "observer"
    REGION = "region"

    def __str__(self) -> str:
        return str(self.value)
