from enum import StrEnum


class SinkTestResultPhase(StrEnum):
    BIND = "bind"
    CONNECT = "connect"
    SEND = "send"
    VALIDATE = "validate"

    def __str__(self) -> str:
        return str(self.value)
