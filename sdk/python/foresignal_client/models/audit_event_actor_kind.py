from enum import StrEnum


class AuditEventActorKind(StrEnum):
    ACTION = "action"
    CHAT = "chat"
    INGEST_KEY = "ingest_key"
    MESH = "mesh"
    PAIRING = "pairing"
    SYSTEM = "system"
    TOKEN = "token"
    USER = "user"

    def __str__(self) -> str:
        return str(self.value)
