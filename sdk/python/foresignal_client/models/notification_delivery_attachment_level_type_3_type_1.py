from enum import StrEnum


class NotificationDeliveryAttachmentLevelType3Type1(StrEnum):
    MAP_600 = "map-600"
    MAP_800 = "map-800"
    TEXT_ONLY = "text-only"
    THUMBNAIL = "thumbnail"

    def __str__(self) -> str:
        return str(self.value)
