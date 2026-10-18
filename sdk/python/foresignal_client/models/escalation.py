from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.escalation_state import EscalationState
from ..types import UNSET, Unset

T = TypeVar("T", bound="Escalation")


@_attrs_define
class Escalation:
    """Escalation state of one alert (033)

    Attributes:
        alert_id (int):
        policy_id (UUID):
        state (EscalationState):
        next_step (int | Unset):
        next_at (datetime.datetime | None | Unset):
        repeats_done (int | Unset):
        stopped_reason (None | str | Unset):  Example: acknowledged.
        updated_at (datetime.datetime | Unset):
    """

    alert_id: int
    policy_id: UUID
    state: EscalationState
    next_step: int | Unset = UNSET
    next_at: datetime.datetime | None | Unset = UNSET
    repeats_done: int | Unset = UNSET
    stopped_reason: None | str | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alert_id = self.alert_id

        policy_id = str(self.policy_id)

        state = self.state.value

        next_step = self.next_step

        next_at: None | str | Unset
        if isinstance(self.next_at, Unset):
            next_at = UNSET
        elif isinstance(self.next_at, datetime.datetime):
            next_at = self.next_at.isoformat()
        else:
            next_at = self.next_at

        repeats_done = self.repeats_done

        stopped_reason: None | str | Unset
        if isinstance(self.stopped_reason, Unset):
            stopped_reason = UNSET
        else:
            stopped_reason = self.stopped_reason

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "alertId": alert_id,
                "policyId": policy_id,
                "state": state,
            }
        )
        if next_step is not UNSET:
            field_dict["nextStep"] = next_step
        if next_at is not UNSET:
            field_dict["nextAt"] = next_at
        if repeats_done is not UNSET:
            field_dict["repeatsDone"] = repeats_done
        if stopped_reason is not UNSET:
            field_dict["stoppedReason"] = stopped_reason
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        alert_id = d.pop("alertId")

        policy_id = UUID(d.pop("policyId"))

        state = EscalationState(d.pop("state"))

        next_step = d.pop("nextStep", UNSET)

        def _parse_next_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                next_at_type_0 = datetime.datetime.fromisoformat(data)

                return next_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        next_at = _parse_next_at(d.pop("nextAt", UNSET))

        repeats_done = d.pop("repeatsDone", UNSET)

        def _parse_stopped_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        stopped_reason = _parse_stopped_reason(d.pop("stoppedReason", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        escalation = cls(
            alert_id=alert_id,
            policy_id=policy_id,
            state=state,
            next_step=next_step,
            next_at=next_at,
            repeats_done=repeats_done,
            stopped_reason=stopped_reason,
            updated_at=updated_at,
        )

        escalation.additional_properties = d
        return escalation

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
