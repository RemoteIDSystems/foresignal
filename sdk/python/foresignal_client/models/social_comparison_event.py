from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_comparison_event_previous_status_type_1 import (
    SocialComparisonEventPreviousStatusType1,
)
from ..models.social_comparison_event_previous_status_type_2_type_1 import (
    SocialComparisonEventPreviousStatusType2Type1,
)
from ..models.social_comparison_event_previous_status_type_3_type_1 import (
    SocialComparisonEventPreviousStatusType3Type1,
)
from ..models.social_comparison_event_status import SocialComparisonEventStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialComparisonEvent")


@_attrs_define
class SocialComparisonEvent:
    """SSE `comparison.updated` (topic `social`): a comparison was created or revised (137)

    Attributes:
        event (Literal['comparison.updated']):
        comparison_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        claim_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (SocialComparisonEventStatus):
        revision (int):
        evaluated_at (datetime.datetime):
        previous_status (None | SocialComparisonEventPreviousStatusType1 | SocialComparisonEventPreviousStatusType2Type1
            | SocialComparisonEventPreviousStatusType3Type1 | Unset):
    """

    event: Literal["comparison.updated"]
    comparison_id: str
    claim_id: str
    narrative_id: str
    status: SocialComparisonEventStatus
    revision: int
    evaluated_at: datetime.datetime
    previous_status: (
        None
        | SocialComparisonEventPreviousStatusType1
        | SocialComparisonEventPreviousStatusType2Type1
        | SocialComparisonEventPreviousStatusType3Type1
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event

        comparison_id = self.comparison_id

        claim_id = self.claim_id

        narrative_id = self.narrative_id

        status = self.status.value

        revision = self.revision

        evaluated_at = self.evaluated_at.isoformat()

        previous_status: None | str | Unset
        if isinstance(self.previous_status, Unset):
            previous_status = UNSET
        elif (
            isinstance(self.previous_status, SocialComparisonEventPreviousStatusType1)
            or isinstance(
                self.previous_status, SocialComparisonEventPreviousStatusType2Type1
            )
            or isinstance(
                self.previous_status, SocialComparisonEventPreviousStatusType3Type1
            )
        ):
            previous_status = self.previous_status.value
        else:
            previous_status = self.previous_status

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "comparisonId": comparison_id,
                "claimId": claim_id,
                "narrativeId": narrative_id,
                "status": status,
                "revision": revision,
                "evaluatedAt": evaluated_at,
            }
        )
        if previous_status is not UNSET:
            field_dict["previousStatus"] = previous_status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        event = cast(Literal["comparison.updated"], d.pop("event"))
        if event != "comparison.updated":
            raise ValueError(
                f"event must match const 'comparison.updated', got '{event}'"
            )

        comparison_id = d.pop("comparisonId")

        claim_id = d.pop("claimId")

        narrative_id = d.pop("narrativeId")

        status = SocialComparisonEventStatus(d.pop("status"))

        revision = d.pop("revision")

        evaluated_at = datetime.datetime.fromisoformat(d.pop("evaluatedAt"))

        def _parse_previous_status(
            data: object,
        ) -> (
            None
            | SocialComparisonEventPreviousStatusType1
            | SocialComparisonEventPreviousStatusType2Type1
            | SocialComparisonEventPreviousStatusType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_status_type_1 = SocialComparisonEventPreviousStatusType1(data)

                return previous_status_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_status_type_2_type_1 = (
                    SocialComparisonEventPreviousStatusType2Type1(data)
                )

                return previous_status_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                previous_status_type_3_type_1 = (
                    SocialComparisonEventPreviousStatusType3Type1(data)
                )

                return previous_status_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | SocialComparisonEventPreviousStatusType1
                | SocialComparisonEventPreviousStatusType2Type1
                | SocialComparisonEventPreviousStatusType3Type1
                | Unset,
                data,
            )

        previous_status = _parse_previous_status(d.pop("previousStatus", UNSET))

        social_comparison_event = cls(
            event=event,
            comparison_id=comparison_id,
            claim_id=claim_id,
            narrative_id=narrative_id,
            status=status,
            revision=revision,
            evaluated_at=evaluated_at,
            previous_status=previous_status,
        )

        social_comparison_event.additional_properties = d
        return social_comparison_event

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
