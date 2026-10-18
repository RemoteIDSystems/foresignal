from enum import IntEnum


class StormTrackPayloadNowcastItemLeadMinutes(IntEnum):
    VALUE_15 = 15
    VALUE_30 = 30
    VALUE_60 = 60

    def __str__(self) -> str:
        return str(self.value)
