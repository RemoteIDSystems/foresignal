from __future__ import annotations

from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.social_cue import SocialCue


T = TypeVar("T", bound="SocialCueEvent")


@_attrs_define
class SocialCueEvent:
    """SSE `cue.completed` (topic `social`): a cue finished, failed or was refused by budget (137)

    Attributes:
        event (Literal['cue.completed']):
        cue (SocialCue): A cue-driven social search; results are a targeted sample that never feeds baselines and never
            corroborates its own trigger (`SocialCueService`, 137)
    """

    event: Literal["cue.completed"]
    cue: SocialCue
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event

        cue = self.cue.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "cue": cue,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_cue import SocialCue

        d = dict(src_dict)
        event = cast(Literal["cue.completed"], d.pop("event"))
        if event != "cue.completed":
            raise ValueError(f"event must match const 'cue.completed', got '{event}'")

        cue = SocialCue.from_dict(d.pop("cue"))

        social_cue_event = cls(
            event=event,
            cue=cue,
        )

        social_cue_event.additional_properties = d
        return social_cue_event

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
