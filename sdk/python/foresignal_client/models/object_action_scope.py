from enum import StrEnum


class ObjectActionScope(StrEnum):
    ADMIN = "admin"
    AGENT = "agent"
    READ = "read"

    def __str__(self) -> str:
        return str(self.value)
