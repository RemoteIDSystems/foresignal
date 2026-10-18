from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ingest_policy_latest_mode import IngestPolicyLatestMode
from ..types import UNSET, Unset

T = TypeVar("T", bound="IngestPolicyLatest")


@_attrs_define
class IngestPolicyLatest:
    """LATEST policy: SAMPLED without deviation/threshold samples (significant changes, edges, keyframes)

    Attributes:
        mode (IngestPolicyLatestMode):
        min_interval (None | str | Unset): Minimum interval (2 s ≤ minInterval ≤ keyframe). ISO-8601 duration (P7D,
            PT15M) or shorthand (7d, 15m, 12h) Example: 60s.
        keyframe (str | Unset): Keyframe interval on the TTL-anchor axis: a duration or `none` (only without a TTL).
            ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 240s.
        edge_min_interval (None | str | Unset): Minimum Δt between TRIGGER_EDGE versions. ISO-8601 duration (P7D, PT15M)
            or shorthand (7d, 15m, 12h) Example: 15s.
    """

    mode: IngestPolicyLatestMode
    min_interval: None | str | Unset = UNSET
    keyframe: str | Unset = UNSET
    edge_min_interval: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mode = self.mode.value

        min_interval: None | str | Unset
        if isinstance(self.min_interval, Unset):
            min_interval = UNSET
        else:
            min_interval = self.min_interval

        keyframe = self.keyframe

        edge_min_interval: None | str | Unset
        if isinstance(self.edge_min_interval, Unset):
            edge_min_interval = UNSET
        else:
            edge_min_interval = self.edge_min_interval

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "mode": mode,
            }
        )
        if min_interval is not UNSET:
            field_dict["minInterval"] = min_interval
        if keyframe is not UNSET:
            field_dict["keyframe"] = keyframe
        if edge_min_interval is not UNSET:
            field_dict["edgeMinInterval"] = edge_min_interval

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        mode = IngestPolicyLatestMode(d.pop("mode"))

        def _parse_min_interval(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        min_interval = _parse_min_interval(d.pop("minInterval", UNSET))

        keyframe = d.pop("keyframe", UNSET)

        def _parse_edge_min_interval(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        edge_min_interval = _parse_edge_min_interval(d.pop("edgeMinInterval", UNSET))

        ingest_policy_latest = cls(
            mode=mode,
            min_interval=min_interval,
            keyframe=keyframe,
            edge_min_interval=edge_min_interval,
        )

        ingest_policy_latest.additional_properties = d
        return ingest_policy_latest

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
