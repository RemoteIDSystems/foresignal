from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.storm_track_payload_status import StormTrackPayloadStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.detector_ref import DetectorRef
    from ..models.storm_track_payload_cell import StormTrackPayloadCell
    from ..models.storm_track_payload_history_item import StormTrackPayloadHistoryItem
    from ..models.storm_track_payload_lightning_jump_type_0 import (
        StormTrackPayloadLightningJumpType0,
    )
    from ..models.storm_track_payload_motion import StormTrackPayloadMotion
    from ..models.storm_track_payload_nowcast_item import StormTrackPayloadNowcastItem
    from ..models.storm_track_payload_trend import StormTrackPayloadTrend


T = TypeVar("T", bound="StormTrackPayload")


@_attrs_define
class StormTrackPayload:
    """Kind `storm-track` (`wx-storm-tracks`, TITAN-lite): a tracked convective cell with motion, trend, nowcast and
    lightning jump

        Attributes:
            cell (StormTrackPayloadCell): Current cell summary
            motion (StormTrackPayloadMotion): Storm motion
            status (StormTrackPayloadStatus):
            history (list[StormTrackPayloadHistoryItem] | Unset):
            trend (StormTrackPayloadTrend | Unset): Trend
            lightning_jump (None | StormTrackPayloadLightningJumpType0 | Unset):
            nowcast (list[StormTrackPayloadNowcastItem] | Unset):
            merged_into (None | str | Unset):
            parent_track_id (None | str | Unset):
            detector (DetectorRef | Unset):
    """

    cell: StormTrackPayloadCell
    motion: StormTrackPayloadMotion
    status: StormTrackPayloadStatus
    history: list[StormTrackPayloadHistoryItem] | Unset = UNSET
    trend: StormTrackPayloadTrend | Unset = UNSET
    lightning_jump: None | StormTrackPayloadLightningJumpType0 | Unset = UNSET
    nowcast: list[StormTrackPayloadNowcastItem] | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    parent_track_id: None | str | Unset = UNSET
    detector: DetectorRef | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.storm_track_payload_lightning_jump_type_0 import (
            StormTrackPayloadLightningJumpType0,
        )

        cell = self.cell.to_dict()

        motion = self.motion.to_dict()

        status = self.status.value

        history: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.history, Unset):
            history = []
            for history_item_data in self.history:
                history_item = history_item_data.to_dict()
                history.append(history_item)

        trend: dict[str, Any] | Unset = UNSET
        if not isinstance(self.trend, Unset):
            trend = self.trend.to_dict()

        lightning_jump: dict[str, Any] | None | Unset
        if isinstance(self.lightning_jump, Unset):
            lightning_jump = UNSET
        elif isinstance(self.lightning_jump, StormTrackPayloadLightningJumpType0):
            lightning_jump = self.lightning_jump.to_dict()
        else:
            lightning_jump = self.lightning_jump

        nowcast: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.nowcast, Unset):
            nowcast = []
            for nowcast_item_data in self.nowcast:
                nowcast_item = nowcast_item_data.to_dict()
                nowcast.append(nowcast_item)

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        parent_track_id: None | str | Unset
        if isinstance(self.parent_track_id, Unset):
            parent_track_id = UNSET
        else:
            parent_track_id = self.parent_track_id

        detector: dict[str, Any] | Unset = UNSET
        if not isinstance(self.detector, Unset):
            detector = self.detector.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "cell": cell,
                "motion": motion,
                "status": status,
            }
        )
        if history is not UNSET:
            field_dict["history"] = history
        if trend is not UNSET:
            field_dict["trend"] = trend
        if lightning_jump is not UNSET:
            field_dict["lightningJump"] = lightning_jump
        if nowcast is not UNSET:
            field_dict["nowcast"] = nowcast
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into
        if parent_track_id is not UNSET:
            field_dict["parentTrackId"] = parent_track_id
        if detector is not UNSET:
            field_dict["detector"] = detector

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.detector_ref import DetectorRef
        from ..models.storm_track_payload_cell import StormTrackPayloadCell
        from ..models.storm_track_payload_history_item import (
            StormTrackPayloadHistoryItem,
        )
        from ..models.storm_track_payload_lightning_jump_type_0 import (
            StormTrackPayloadLightningJumpType0,
        )
        from ..models.storm_track_payload_motion import StormTrackPayloadMotion
        from ..models.storm_track_payload_nowcast_item import (
            StormTrackPayloadNowcastItem,
        )
        from ..models.storm_track_payload_trend import StormTrackPayloadTrend

        d = dict(src_dict)
        cell = StormTrackPayloadCell.from_dict(d.pop("cell"))

        motion = StormTrackPayloadMotion.from_dict(d.pop("motion"))

        status = StormTrackPayloadStatus(d.pop("status"))

        _history = d.pop("history", UNSET)
        history: list[StormTrackPayloadHistoryItem] | Unset = UNSET
        if _history is not UNSET:
            history = []
            for history_item_data in _history:
                history_item = StormTrackPayloadHistoryItem.from_dict(history_item_data)

                history.append(history_item)

        _trend = d.pop("trend", UNSET)
        trend: StormTrackPayloadTrend | Unset
        if isinstance(_trend, Unset):
            trend = UNSET
        else:
            trend = StormTrackPayloadTrend.from_dict(_trend)

        def _parse_lightning_jump(
            data: object,
        ) -> None | StormTrackPayloadLightningJumpType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                lightning_jump_type_0 = StormTrackPayloadLightningJumpType0.from_dict(
                    data
                )

                return lightning_jump_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | StormTrackPayloadLightningJumpType0 | Unset, data)

        lightning_jump = _parse_lightning_jump(d.pop("lightningJump", UNSET))

        _nowcast = d.pop("nowcast", UNSET)
        nowcast: list[StormTrackPayloadNowcastItem] | Unset = UNSET
        if _nowcast is not UNSET:
            nowcast = []
            for nowcast_item_data in _nowcast:
                nowcast_item = StormTrackPayloadNowcastItem.from_dict(nowcast_item_data)

                nowcast.append(nowcast_item)

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        def _parse_parent_track_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        parent_track_id = _parse_parent_track_id(d.pop("parentTrackId", UNSET))

        _detector = d.pop("detector", UNSET)
        detector: DetectorRef | Unset
        if isinstance(_detector, Unset):
            detector = UNSET
        else:
            detector = DetectorRef.from_dict(_detector)

        storm_track_payload = cls(
            cell=cell,
            motion=motion,
            status=status,
            history=history,
            trend=trend,
            lightning_jump=lightning_jump,
            nowcast=nowcast,
            merged_into=merged_into,
            parent_track_id=parent_track_id,
            detector=detector,
        )

        storm_track_payload.additional_properties = d
        return storm_track_payload

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
