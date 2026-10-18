from enum import IntEnum


class DeclarativePushPayloadWebPush(IntEnum):
    VALUE_8030 = 8030

    def __str__(self) -> str:
        return str(self.value)
