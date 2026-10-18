from enum import StrEnum


class MediaOutletPatchType(StrEnum):
    AGGREGATOR = "aggregator"
    BLOG = "blog"
    LOCAL = "local"
    NATIONAL = "national"
    OFFICIAL = "official"
    OTHER = "other"
    PUBLIC_BROADCASTER = "public-broadcaster"
    REGIONAL = "regional"
    STATE_AFFILIATED = "state-affiliated"
    WIRE = "wire"

    def __str__(self) -> str:
        return str(self.value)
