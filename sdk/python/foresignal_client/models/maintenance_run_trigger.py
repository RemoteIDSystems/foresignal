from enum import StrEnum


class MaintenanceRunTrigger(StrEnum):
    MANUAL = "manual"
    MONTHLY_CYCLE = "monthly-cycle"
    SCHEDULE = "schedule"

    def __str__(self) -> str:
        return str(self.value)
