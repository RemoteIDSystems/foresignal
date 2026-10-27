from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="ResourceThresholdsCollectorBacklogType0")


@_attrs_define
class ResourceThresholdsCollectorBacklogType0:
    """Raises `system.collector_backlog` when the trigger lag or the fetches waiting for a pull permit stay above their
    bound for `sustainedSeconds` (R2-014)

        Attributes:
            trigger_lag_seq (int | None | Unset): Trigger lag bound in sequence numbers; null is off
            waiting_pulls (int | None | Unset): Waiting-fetch bound; null is off
            sustained_seconds (int | Unset): How long a bound must be exceeded (default 300)
    """

    trigger_lag_seq: int | None | Unset = UNSET
    waiting_pulls: int | None | Unset = UNSET
    sustained_seconds: int | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        trigger_lag_seq: int | None | Unset
        if isinstance(self.trigger_lag_seq, Unset):
            trigger_lag_seq = UNSET
        else:
            trigger_lag_seq = self.trigger_lag_seq

        waiting_pulls: int | None | Unset
        if isinstance(self.waiting_pulls, Unset):
            waiting_pulls = UNSET
        else:
            waiting_pulls = self.waiting_pulls

        sustained_seconds = self.sustained_seconds

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if trigger_lag_seq is not UNSET:
            field_dict["triggerLagSeq"] = trigger_lag_seq
        if waiting_pulls is not UNSET:
            field_dict["waitingPulls"] = waiting_pulls
        if sustained_seconds is not UNSET:
            field_dict["sustainedSeconds"] = sustained_seconds

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_trigger_lag_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        trigger_lag_seq = _parse_trigger_lag_seq(d.pop("triggerLagSeq", UNSET))

        def _parse_waiting_pulls(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        waiting_pulls = _parse_waiting_pulls(d.pop("waitingPulls", UNSET))

        sustained_seconds = d.pop("sustainedSeconds", UNSET)

        resource_thresholds_collector_backlog_type_0 = cls(
            trigger_lag_seq=trigger_lag_seq,
            waiting_pulls=waiting_pulls,
            sustained_seconds=sustained_seconds,
        )

        return resource_thresholds_collector_backlog_type_0
