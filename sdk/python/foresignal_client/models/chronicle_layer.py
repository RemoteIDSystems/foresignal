from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.capture_mode import CaptureMode
    from ..models.chronicle_area import ChronicleArea
    from ..models.chronicle_external_stream_selector import (
        ChronicleExternalStreamSelector,
    )
    from ..models.chronicle_layer_estimate_type_0 import ChronicleLayerEstimateType0
    from ..models.chronicle_layer_selector_type_0 import ChronicleLayerSelectorType0
    from ..models.chronicle_layer_selector_type_1 import ChronicleLayerSelectorType1
    from ..models.chronicle_layer_selector_type_2 import ChronicleLayerSelectorType2
    from ..models.chronicle_layer_selector_type_3 import ChronicleLayerSelectorType3
    from ..models.chronicle_layer_selector_type_4 import ChronicleLayerSelectorType4
    from ..models.chronicle_layer_selector_type_5 import ChronicleLayerSelectorType5
    from ..models.chronicle_layer_selector_type_6 import ChronicleLayerSelectorType6
    from ..models.chronicle_layer_selector_type_7 import ChronicleLayerSelectorType7
    from ..models.chronicle_layer_selector_type_8 import ChronicleLayerSelectorType8
    from ..models.chronicle_layer_stats import ChronicleLayerStats
    from ..models.chronicle_social_narratives_selector import (
        ChronicleSocialNarrativesSelector,
    )
    from ..models.chronicle_social_volume_selector import ChronicleSocialVolumeSelector


T = TypeVar("T", bound="ChronicleLayer")


@_attrs_define
class ChronicleLayer:
    """One layer of a chronicle with its selector and capture mode

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        chronicle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        selector (ChronicleExternalStreamSelector | ChronicleLayerSelectorType0 | ChronicleLayerSelectorType1 |
            ChronicleLayerSelectorType2 | ChronicleLayerSelectorType3 | ChronicleLayerSelectorType4 |
            ChronicleLayerSelectorType5 | ChronicleLayerSelectorType6 | ChronicleLayerSelectorType7 |
            ChronicleLayerSelectorType8 | ChronicleSocialNarrativesSelector | ChronicleSocialVolumeSelector): What a
            chronicle layer selects (one variant by `type`)
        capture (CaptureMode): How a chronicle layer captures matching data
        enabled (bool):
        added_at (datetime.datetime):
        area_override (ChronicleArea | None | Unset):
        effective_capture (CaptureMode | Unset): How a chronicle layer captures matching data
        added_at_seq (int | None | Unset): Edits apply from this seq onward
        removed_at (datetime.datetime | None | Unset):
        estimate (ChronicleLayerEstimateType0 | None | Unset):
        stats (ChronicleLayerStats | Unset): Captured volume
    """

    id: str
    chronicle_id: str
    name: str
    selector: (
        ChronicleExternalStreamSelector
        | ChronicleLayerSelectorType0
        | ChronicleLayerSelectorType1
        | ChronicleLayerSelectorType2
        | ChronicleLayerSelectorType3
        | ChronicleLayerSelectorType4
        | ChronicleLayerSelectorType5
        | ChronicleLayerSelectorType6
        | ChronicleLayerSelectorType7
        | ChronicleLayerSelectorType8
        | ChronicleSocialNarrativesSelector
        | ChronicleSocialVolumeSelector
    )
    capture: CaptureMode
    enabled: bool
    added_at: datetime.datetime
    area_override: ChronicleArea | None | Unset = UNSET
    effective_capture: CaptureMode | Unset = UNSET
    added_at_seq: int | None | Unset = UNSET
    removed_at: datetime.datetime | None | Unset = UNSET
    estimate: ChronicleLayerEstimateType0 | None | Unset = UNSET
    stats: ChronicleLayerStats | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_area import ChronicleArea
        from ..models.chronicle_layer_estimate_type_0 import ChronicleLayerEstimateType0
        from ..models.chronicle_layer_selector_type_0 import ChronicleLayerSelectorType0
        from ..models.chronicle_layer_selector_type_1 import ChronicleLayerSelectorType1
        from ..models.chronicle_layer_selector_type_2 import ChronicleLayerSelectorType2
        from ..models.chronicle_layer_selector_type_3 import ChronicleLayerSelectorType3
        from ..models.chronicle_layer_selector_type_4 import ChronicleLayerSelectorType4
        from ..models.chronicle_layer_selector_type_5 import ChronicleLayerSelectorType5
        from ..models.chronicle_layer_selector_type_6 import ChronicleLayerSelectorType6
        from ..models.chronicle_layer_selector_type_7 import ChronicleLayerSelectorType7
        from ..models.chronicle_layer_selector_type_8 import ChronicleLayerSelectorType8
        from ..models.chronicle_social_narratives_selector import (
            ChronicleSocialNarrativesSelector,
        )
        from ..models.chronicle_social_volume_selector import (
            ChronicleSocialVolumeSelector,
        )

        id = self.id

        chronicle_id = self.chronicle_id

        name = self.name

        selector: dict[str, Any]
        if (
            isinstance(self.selector, ChronicleLayerSelectorType0)
            or isinstance(self.selector, ChronicleLayerSelectorType1)
            or isinstance(self.selector, ChronicleLayerSelectorType2)
            or isinstance(self.selector, ChronicleLayerSelectorType3)
            or isinstance(self.selector, ChronicleLayerSelectorType4)
            or isinstance(self.selector, ChronicleLayerSelectorType5)
            or isinstance(self.selector, ChronicleLayerSelectorType6)
            or isinstance(self.selector, ChronicleLayerSelectorType7)
            or isinstance(self.selector, ChronicleLayerSelectorType8)
            or isinstance(self.selector, ChronicleSocialNarrativesSelector)
            or isinstance(self.selector, ChronicleSocialVolumeSelector)
        ):
            selector = self.selector.to_dict()
        else:
            selector = self.selector.to_dict()

        capture = self.capture.to_dict()

        enabled = self.enabled

        added_at = self.added_at.isoformat()

        area_override: dict[str, Any] | None | Unset
        if isinstance(self.area_override, Unset):
            area_override = UNSET
        elif isinstance(self.area_override, ChronicleArea):
            area_override = self.area_override.to_dict()
        else:
            area_override = self.area_override

        effective_capture: dict[str, Any] | Unset = UNSET
        if not isinstance(self.effective_capture, Unset):
            effective_capture = self.effective_capture.to_dict()

        added_at_seq: int | None | Unset
        if isinstance(self.added_at_seq, Unset):
            added_at_seq = UNSET
        else:
            added_at_seq = self.added_at_seq

        removed_at: None | str | Unset
        if isinstance(self.removed_at, Unset):
            removed_at = UNSET
        elif isinstance(self.removed_at, datetime.datetime):
            removed_at = self.removed_at.isoformat()
        else:
            removed_at = self.removed_at

        estimate: dict[str, Any] | None | Unset
        if isinstance(self.estimate, Unset):
            estimate = UNSET
        elif isinstance(self.estimate, ChronicleLayerEstimateType0):
            estimate = self.estimate.to_dict()
        else:
            estimate = self.estimate

        stats: dict[str, Any] | Unset = UNSET
        if not isinstance(self.stats, Unset):
            stats = self.stats.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "chronicleId": chronicle_id,
                "name": name,
                "selector": selector,
                "capture": capture,
                "enabled": enabled,
                "addedAt": added_at,
            }
        )
        if area_override is not UNSET:
            field_dict["areaOverride"] = area_override
        if effective_capture is not UNSET:
            field_dict["effectiveCapture"] = effective_capture
        if added_at_seq is not UNSET:
            field_dict["addedAtSeq"] = added_at_seq
        if removed_at is not UNSET:
            field_dict["removedAt"] = removed_at
        if estimate is not UNSET:
            field_dict["estimate"] = estimate
        if stats is not UNSET:
            field_dict["stats"] = stats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.capture_mode import CaptureMode
        from ..models.chronicle_area import ChronicleArea
        from ..models.chronicle_external_stream_selector import (
            ChronicleExternalStreamSelector,
        )
        from ..models.chronicle_layer_estimate_type_0 import ChronicleLayerEstimateType0
        from ..models.chronicle_layer_selector_type_0 import ChronicleLayerSelectorType0
        from ..models.chronicle_layer_selector_type_1 import ChronicleLayerSelectorType1
        from ..models.chronicle_layer_selector_type_2 import ChronicleLayerSelectorType2
        from ..models.chronicle_layer_selector_type_3 import ChronicleLayerSelectorType3
        from ..models.chronicle_layer_selector_type_4 import ChronicleLayerSelectorType4
        from ..models.chronicle_layer_selector_type_5 import ChronicleLayerSelectorType5
        from ..models.chronicle_layer_selector_type_6 import ChronicleLayerSelectorType6
        from ..models.chronicle_layer_selector_type_7 import ChronicleLayerSelectorType7
        from ..models.chronicle_layer_selector_type_8 import ChronicleLayerSelectorType8
        from ..models.chronicle_layer_stats import ChronicleLayerStats
        from ..models.chronicle_social_narratives_selector import (
            ChronicleSocialNarrativesSelector,
        )
        from ..models.chronicle_social_volume_selector import (
            ChronicleSocialVolumeSelector,
        )

        d = dict(src_dict)
        id = d.pop("id")

        chronicle_id = d.pop("chronicleId")

        name = d.pop("name")

        def _parse_selector(
            data: object,
        ) -> (
            ChronicleExternalStreamSelector
            | ChronicleLayerSelectorType0
            | ChronicleLayerSelectorType1
            | ChronicleLayerSelectorType2
            | ChronicleLayerSelectorType3
            | ChronicleLayerSelectorType4
            | ChronicleLayerSelectorType5
            | ChronicleLayerSelectorType6
            | ChronicleLayerSelectorType7
            | ChronicleLayerSelectorType8
            | ChronicleSocialNarrativesSelector
            | ChronicleSocialVolumeSelector
        ):
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_0 = (
                    ChronicleLayerSelectorType0.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_1 = (
                    ChronicleLayerSelectorType1.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_2 = (
                    ChronicleLayerSelectorType2.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_3 = (
                    ChronicleLayerSelectorType3.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_3
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_4 = (
                    ChronicleLayerSelectorType4.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_4
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_5 = (
                    ChronicleLayerSelectorType5.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_5
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_6 = (
                    ChronicleLayerSelectorType6.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_6
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_7 = (
                    ChronicleLayerSelectorType7.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_7
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_8 = (
                    ChronicleLayerSelectorType8.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_8
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_9 = (
                    ChronicleSocialNarrativesSelector.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_9
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_chronicle_layer_selector_type_10 = (
                    ChronicleSocialVolumeSelector.from_dict(data)
                )

                return componentsschemas_chronicle_layer_selector_type_10
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            componentsschemas_chronicle_layer_selector_type_11 = (
                ChronicleExternalStreamSelector.from_dict(data)
            )

            return componentsschemas_chronicle_layer_selector_type_11

        selector = _parse_selector(d.pop("selector"))

        capture = CaptureMode.from_dict(d.pop("capture"))

        enabled = d.pop("enabled")

        added_at = datetime.datetime.fromisoformat(d.pop("addedAt"))

        def _parse_area_override(data: object) -> ChronicleArea | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_override_type_0 = ChronicleArea.from_dict(data)

                return area_override_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleArea | None | Unset, data)

        area_override = _parse_area_override(d.pop("areaOverride", UNSET))

        _effective_capture = d.pop("effectiveCapture", UNSET)
        effective_capture: CaptureMode | Unset
        if isinstance(_effective_capture, Unset):
            effective_capture = UNSET
        else:
            effective_capture = CaptureMode.from_dict(_effective_capture)

        def _parse_added_at_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        added_at_seq = _parse_added_at_seq(d.pop("addedAtSeq", UNSET))

        def _parse_removed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                removed_at_type_0 = datetime.datetime.fromisoformat(data)

                return removed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        removed_at = _parse_removed_at(d.pop("removedAt", UNSET))

        def _parse_estimate(data: object) -> ChronicleLayerEstimateType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                estimate_type_0 = ChronicleLayerEstimateType0.from_dict(data)

                return estimate_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleLayerEstimateType0 | None | Unset, data)

        estimate = _parse_estimate(d.pop("estimate", UNSET))

        _stats = d.pop("stats", UNSET)
        stats: ChronicleLayerStats | Unset
        if isinstance(_stats, Unset):
            stats = UNSET
        else:
            stats = ChronicleLayerStats.from_dict(_stats)

        chronicle_layer = cls(
            id=id,
            chronicle_id=chronicle_id,
            name=name,
            selector=selector,
            capture=capture,
            enabled=enabled,
            added_at=added_at,
            area_override=area_override,
            effective_capture=effective_capture,
            added_at_seq=added_at_seq,
            removed_at=removed_at,
            estimate=estimate,
            stats=stats,
        )

        chronicle_layer.additional_properties = d
        return chronicle_layer

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
