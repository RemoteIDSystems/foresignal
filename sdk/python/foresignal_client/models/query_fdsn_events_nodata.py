from enum import IntEnum


class QueryFdsnEventsNodata(IntEnum):
    VALUE_204 = 204
    VALUE_404 = 404

    def __str__(self) -> str:
        return str(self.value)
