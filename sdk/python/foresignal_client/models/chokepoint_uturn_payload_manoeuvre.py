from enum import StrEnum


class ChokepointUturnPayloadManoeuvre(StrEnum):
    COURSE_CHANGE = "course-change"
    SPEED_DROP = "speed-drop"
    U_TURN = "u-turn"

    def __str__(self) -> str:
        return str(self.value)
