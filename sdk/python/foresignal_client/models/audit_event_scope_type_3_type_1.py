from enum import StrEnum


class AuditEventScopeType3Type1(StrEnum):
    ADMIN = "admin"
    AGENT = "agent"
    READ = "read"

    def __str__(self) -> str:
        return str(self.value)
