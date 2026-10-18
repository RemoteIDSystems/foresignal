from enum import StrEnum


class GetStatsTimeseriesMeasure(StrEnum):
    ALERTS = "alerts"
    EDGES = "edges"
    ENTITIES = "entities"
    FIRSTS = "firsts"
    TOMBSTONES = "tombstones"
    VERSIONS = "versions"

    def __str__(self) -> str:
        return str(self.value)
