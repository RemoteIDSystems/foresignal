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

from ..models.social_platform_id import SocialPlatformId
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_spend import SocialSpend


T = TypeVar("T", bound="SocialPlatformStateEvent")


@_attrs_define
class SocialPlatformStateEvent:
    """SSE `social.platform.state` (topic `social`): an adapter became enabled, disabled, degraded, budget-exhausted,
    credentials-required or unavailable (135)

        Attributes:
            event (Literal['social.platform.state']):
            platform_id (SocialPlatformId): Social platform adapter (a 004 source with descriptor, budget, health and
                credentials). Only `bluesky-jetstream` is on by default (135)
            state (str):
            at (datetime.datetime):
            previous_state (None | str | Unset):
            reason (None | str | Unset):
            spend (None | SocialSpend | Unset):
    """

    event: Literal["social.platform.state"]
    platform_id: SocialPlatformId
    state: str
    at: datetime.datetime
    previous_state: None | str | Unset = UNSET
    reason: None | str | Unset = UNSET
    spend: None | SocialSpend | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.social_spend import SocialSpend

        event = self.event

        platform_id = self.platform_id.value

        state = self.state

        at = self.at.isoformat()

        previous_state: None | str | Unset
        if isinstance(self.previous_state, Unset):
            previous_state = UNSET
        else:
            previous_state = self.previous_state

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        spend: dict[str, Any] | None | Unset
        if isinstance(self.spend, Unset):
            spend = UNSET
        elif isinstance(self.spend, SocialSpend):
            spend = self.spend.to_dict()
        else:
            spend = self.spend

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "platformId": platform_id,
                "state": state,
                "at": at,
            }
        )
        if previous_state is not UNSET:
            field_dict["previousState"] = previous_state
        if reason is not UNSET:
            field_dict["reason"] = reason
        if spend is not UNSET:
            field_dict["spend"] = spend

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_spend import SocialSpend

        d = dict(src_dict)
        event = cast(Literal["social.platform.state"], d.pop("event"))
        if event != "social.platform.state":
            raise ValueError(
                f"event must match const 'social.platform.state', got '{event}'"
            )

        platform_id = SocialPlatformId(d.pop("platformId"))

        state = d.pop("state")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_previous_state(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        previous_state = _parse_previous_state(d.pop("previousState", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        def _parse_spend(data: object) -> None | SocialSpend | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                spend_type_0 = SocialSpend.from_dict(data)

                return spend_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialSpend | Unset, data)

        spend = _parse_spend(d.pop("spend", UNSET))

        social_platform_state_event = cls(
            event=event,
            platform_id=platform_id,
            state=state,
            at=at,
            previous_state=previous_state,
            reason=reason,
            spend=spend,
        )

        social_platform_state_event.additional_properties = d
        return social_platform_state_event

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
