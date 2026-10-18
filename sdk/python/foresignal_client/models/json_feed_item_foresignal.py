from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.json_feed_item_foresignal_severity import JsonFeedItemForesignalSeverity
from ..models.json_feed_item_foresignal_state import JsonFeedItemForesignalState
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="JsonFeedItemForesignal")


@_attrs_define
class JsonFeedItemForesignal:
    """Foresignal extension (alert items)

    Attributes:
        severity (JsonFeedItemForesignalSeverity | Unset):
        state (JsonFeedItemForesignalState | Unset):
        source_id (str | Unset):
        external_id (str | Unset):
        geometry (Geometry | None | Unset):
    """

    severity: JsonFeedItemForesignalSeverity | Unset = UNSET
    state: JsonFeedItemForesignalState | Unset = UNSET
    source_id: str | Unset = UNSET
    external_id: str | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        severity: str | Unset = UNSET
        if not isinstance(self.severity, Unset):
            severity = self.severity.value

        state: str | Unset = UNSET
        if not isinstance(self.state, Unset):
            state = self.state.value

        source_id = self.source_id

        external_id = self.external_id

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if severity is not UNSET:
            field_dict["severity"] = severity
        if state is not UNSET:
            field_dict["state"] = state
        if source_id is not UNSET:
            field_dict["sourceId"] = source_id
        if external_id is not UNSET:
            field_dict["externalId"] = external_id
        if geometry is not UNSET:
            field_dict["geometry"] = geometry

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        _severity = d.pop("severity", UNSET)
        severity: JsonFeedItemForesignalSeverity | Unset
        if isinstance(_severity, Unset):
            severity = UNSET
        else:
            severity = JsonFeedItemForesignalSeverity(_severity)

        _state = d.pop("state", UNSET)
        state: JsonFeedItemForesignalState | Unset
        if isinstance(_state, Unset):
            state = UNSET
        else:
            state = JsonFeedItemForesignalState(_state)

        source_id = d.pop("sourceId", UNSET)

        external_id = d.pop("externalId", UNSET)

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        json_feed_item_foresignal = cls(
            severity=severity,
            state=state,
            source_id=source_id,
            external_id=external_id,
            geometry=geometry,
        )

        json_feed_item_foresignal.additional_properties = d
        return json_feed_item_foresignal

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
