from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.window_state_state import WindowStateState
from ..types import UNSET, Unset

T = TypeVar("T", bound="WindowState")


@_attrs_define
class WindowState:
    """Per-group state of a windowed rule

    Attributes:
        group_key (str): Group key; `*` when the rule has no groupBy
        state (WindowStateState):
        since (datetime.datetime):
        last_value (float | None | Unset): Distinct count (count/absence) or bucket count (anomaly)
        threshold (float | None | Unset): Count threshold, or the expected value for anomaly rules
        last_evaluated_at (datetime.datetime | None | Unset):
        last_watermark_seq (int | None | Unset):
        reason (None | str | Unset): Why the state is unknown or learning (coverage gate reason, warm-up)
        last_alert_id (int | None | Unset):
    """

    group_key: str
    state: WindowStateState
    since: datetime.datetime
    last_value: float | None | Unset = UNSET
    threshold: float | None | Unset = UNSET
    last_evaluated_at: datetime.datetime | None | Unset = UNSET
    last_watermark_seq: int | None | Unset = UNSET
    reason: None | str | Unset = UNSET
    last_alert_id: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        group_key = self.group_key

        state = self.state.value

        since = self.since.isoformat()

        last_value: float | None | Unset
        if isinstance(self.last_value, Unset):
            last_value = UNSET
        else:
            last_value = self.last_value

        threshold: float | None | Unset
        if isinstance(self.threshold, Unset):
            threshold = UNSET
        else:
            threshold = self.threshold

        last_evaluated_at: None | str | Unset
        if isinstance(self.last_evaluated_at, Unset):
            last_evaluated_at = UNSET
        elif isinstance(self.last_evaluated_at, datetime.datetime):
            last_evaluated_at = self.last_evaluated_at.isoformat()
        else:
            last_evaluated_at = self.last_evaluated_at

        last_watermark_seq: int | None | Unset
        if isinstance(self.last_watermark_seq, Unset):
            last_watermark_seq = UNSET
        else:
            last_watermark_seq = self.last_watermark_seq

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        last_alert_id: int | None | Unset
        if isinstance(self.last_alert_id, Unset):
            last_alert_id = UNSET
        else:
            last_alert_id = self.last_alert_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "groupKey": group_key,
                "state": state,
                "since": since,
            }
        )
        if last_value is not UNSET:
            field_dict["lastValue"] = last_value
        if threshold is not UNSET:
            field_dict["threshold"] = threshold
        if last_evaluated_at is not UNSET:
            field_dict["lastEvaluatedAt"] = last_evaluated_at
        if last_watermark_seq is not UNSET:
            field_dict["lastWatermarkSeq"] = last_watermark_seq
        if reason is not UNSET:
            field_dict["reason"] = reason
        if last_alert_id is not UNSET:
            field_dict["lastAlertId"] = last_alert_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        group_key = d.pop("groupKey")

        state = WindowStateState(d.pop("state"))

        since = datetime.datetime.fromisoformat(d.pop("since"))

        def _parse_last_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        last_value = _parse_last_value(d.pop("lastValue", UNSET))

        def _parse_threshold(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        threshold = _parse_threshold(d.pop("threshold", UNSET))

        def _parse_last_evaluated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_evaluated_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_evaluated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_evaluated_at = _parse_last_evaluated_at(d.pop("lastEvaluatedAt", UNSET))

        def _parse_last_watermark_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_watermark_seq = _parse_last_watermark_seq(d.pop("lastWatermarkSeq", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        def _parse_last_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_alert_id = _parse_last_alert_id(d.pop("lastAlertId", UNSET))

        window_state = cls(
            group_key=group_key,
            state=state,
            since=since,
            last_value=last_value,
            threshold=threshold,
            last_evaluated_at=last_evaluated_at,
            last_watermark_seq=last_watermark_seq,
            reason=reason,
            last_alert_id=last_alert_id,
        )

        window_state.additional_properties = d
        return window_state

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
