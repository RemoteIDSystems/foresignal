from enum import StrEnum


class SinkCloudEventType(StrEnum):
    AI_FORESIGNAL_RECORD_CREATED = "ai.foresignal.record.created"
    AI_FORESIGNAL_RECORD_SNAPSHOT = "ai.foresignal.record.snapshot"
    AI_FORESIGNAL_RECORD_TOMBSTONED = "ai.foresignal.record.tombstoned"
    AI_FORESIGNAL_RECORD_UPDATED = "ai.foresignal.record.updated"

    def __str__(self) -> str:
        return str(self.value)
