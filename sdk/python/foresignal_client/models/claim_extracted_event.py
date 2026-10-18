from __future__ import annotations

import datetime
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
    from ..models.claim import Claim


T = TypeVar("T", bound="ClaimExtractedEvent")


@_attrs_define
class ClaimExtractedEvent:
    """SSE `claim.extracted` (topic `social`) (136)

    Attributes:
        event (Literal['claim.extracted']):
        claim (Claim): A structured claim extracted from a narrative: what, where, when, who. A report, not a fact;
            usable as a graded 125 evidence item (`source = social-claim`) (136)
        at (datetime.datetime):
    """

    event: Literal["claim.extracted"]
    claim: Claim
    at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event

        claim = self.claim.to_dict()

        at = self.at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "claim": claim,
                "at": at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.claim import Claim

        d = dict(src_dict)
        event = cast(Literal["claim.extracted"], d.pop("event"))
        if event != "claim.extracted":
            raise ValueError(f"event must match const 'claim.extracted', got '{event}'")

        claim = Claim.from_dict(d.pop("claim"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        claim_extracted_event = cls(
            event=event,
            claim=claim,
            at=at,
        )

        claim_extracted_event.additional_properties = d
        return claim_extracted_event

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
