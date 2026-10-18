from enum import StrEnum


class TripHazardSection(StrEnum):
    NEAR_ROUTE = "near-route"
    ON_ROUTE = "on-route"
    TIMING = "timing"

    def __str__(self) -> str:
        return str(self.value)
