from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ingest_policy_sampled_mode import IngestPolicySampledMode
from ..types import UNSET, Unset

T = TypeVar("T", bound="IngestPolicySampled")


@_attrs_define
class IngestPolicySampled:
    """SAMPLED policy: significant changes, trigger edges, keyframes and samples by deviation or threshold

    Attributes:
        mode (IngestPolicySampledMode):
        min_interval (str): Minimum Δt between SAMPLE versions (2 s ≤ minInterval ≤ keyframe). ISO-8601 duration (P7D,
            PT15M) or shorthand (7d, 15m, 12h) Example: 60s.
        keyframe (str | Unset): Keyframe interval on the TTL-anchor axis: a duration or `none` (only without a TTL).
            ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 240s.
        min_deviation_meters (float | Unset): Dead-reckoning deviation that triggers a SAMPLE
        thresholds (bool | Unset): Also sample when a descriptor threshold is exceeded Default: False.
        edge_min_interval (None | str | Unset): Minimum Δt between TRIGGER_EDGE versions (≤ minInterval; default
            minInterval/2). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 30s.
    """

    mode: IngestPolicySampledMode
    min_interval: str
    keyframe: str | Unset = UNSET
    min_deviation_meters: float | Unset = UNSET
    thresholds: bool | Unset = False
    edge_min_interval: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mode = self.mode.value

        min_interval = self.min_interval

        keyframe = self.keyframe

        min_deviation_meters = self.min_deviation_meters

        thresholds = self.thresholds

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
                "minInterval": min_interval,
            }
        )
        if keyframe is not UNSET:
            field_dict["keyframe"] = keyframe
        if min_deviation_meters is not UNSET:
            field_dict["minDeviationMeters"] = min_deviation_meters
        if thresholds is not UNSET:
            field_dict["thresholds"] = thresholds
        if edge_min_interval is not UNSET:
            field_dict["edgeMinInterval"] = edge_min_interval

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        mode = IngestPolicySampledMode(d.pop("mode"))

        min_interval = d.pop("minInterval")

        keyframe = d.pop("keyframe", UNSET)

        min_deviation_meters = d.pop("minDeviationMeters", UNSET)

        thresholds = d.pop("thresholds", UNSET)

        def _parse_edge_min_interval(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        edge_min_interval = _parse_edge_min_interval(d.pop("edgeMinInterval", UNSET))

        ingest_policy_sampled = cls(
            mode=mode,
            min_interval=min_interval,
            keyframe=keyframe,
            min_deviation_meters=min_deviation_meters,
            thresholds=thresholds,
            edge_min_interval=edge_min_interval,
        )

        ingest_policy_sampled.additional_properties = d
        return ingest_policy_sampled

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
