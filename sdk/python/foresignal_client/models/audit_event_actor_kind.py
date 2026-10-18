from enum import StrEnum


class AuditEventActorKind(StrEnum):
    ACTION = "action"
    CHAT = "chat"
    INGEST_KEY = "ingest_key"
    MESH = "mesh"
    PAIRING = "pairing"
    SYSTEM = "system"
    TOKEN = "token"

    def __str__(self) -> str:
        return str(self.value)
