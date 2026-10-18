from __future__ import annotations

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

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.field_encodings import FieldEncodings
    from ..models.field_manifest_components_item import FieldManifestComponentsItem
    from ..models.field_manifest_cycle import FieldManifestCycle
    from ..models.field_manifest_grid import FieldManifestGrid


T = TypeVar("T", bound="FieldManifest")


@_attrs_define
class FieldManifest:
    """GEV `schemaVersion: 1` wind-field manifest plus Foresignal additions

    Attributes:
        schema_version (Literal[1]):
        model (str):  Example: gfs.
        cycle (FieldManifestCycle): Model cycle
        level (str):  Example: 10 m above ground.
        units (Literal['m/s']):
        grid (FieldManifestGrid): Regular lat/lon grid; rows north→south, longitudes 0..359
        stale (bool):
        unavailable (bool):
        components (list[FieldManifestComponentsItem]):
        byte_length (int): Raw byte length (1°: 521,280)
        sha256 (str):
        data_url (str): Content-addressed `.f32` URL (run pinned, bbox snapped)
        reason (None | str | Unset):
        attribution (Attribution | Unset): Structured attribution clients must render for a product or derived output
            (Weather-Imagery-Design §6.10)
        encodings (FieldEncodings | Unset): Alternative encodings of a wind field listed in the field manifest
    """

    schema_version: Literal[1]
    model: str
    cycle: FieldManifestCycle
    level: str
    units: Literal["m/s"]
    grid: FieldManifestGrid
    stale: bool
    unavailable: bool
    components: list[FieldManifestComponentsItem]
    byte_length: int
    sha256: str
    data_url: str
    reason: None | str | Unset = UNSET
    attribution: Attribution | Unset = UNSET
    encodings: FieldEncodings | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        schema_version = self.schema_version

        model = self.model

        cycle = self.cycle.to_dict()

        level = self.level

        units = self.units

        grid = self.grid.to_dict()

        stale = self.stale

        unavailable = self.unavailable

        components = []
        for components_item_data in self.components:
            components_item = components_item_data.to_dict()
            components.append(components_item)

        byte_length = self.byte_length

        sha256 = self.sha256

        data_url = self.data_url

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        attribution: dict[str, Any] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = self.attribution.to_dict()

        encodings: dict[str, Any] | Unset = UNSET
        if not isinstance(self.encodings, Unset):
            encodings = self.encodings.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "schemaVersion": schema_version,
                "model": model,
                "cycle": cycle,
                "level": level,
                "units": units,
                "grid": grid,
                "stale": stale,
                "unavailable": unavailable,
                "components": components,
                "byteLength": byte_length,
                "sha256": sha256,
                "dataUrl": data_url,
            }
        )
        if reason is not UNSET:
            field_dict["reason"] = reason
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if encodings is not UNSET:
            field_dict["encodings"] = encodings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.field_encodings import FieldEncodings
        from ..models.field_manifest_components_item import FieldManifestComponentsItem
        from ..models.field_manifest_cycle import FieldManifestCycle
        from ..models.field_manifest_grid import FieldManifestGrid

        d = dict(src_dict)
        schema_version = cast(Literal[1], d.pop("schemaVersion"))
        if schema_version != 1:
            raise ValueError(
                f"schemaVersion must match const 1, got '{schema_version}'"
            )

        model = d.pop("model")

        cycle = FieldManifestCycle.from_dict(d.pop("cycle"))

        level = d.pop("level")

        units = cast(Literal["m/s"], d.pop("units"))
        if units != "m/s":
            raise ValueError(f"units must match const 'm/s', got '{units}'")

        grid = FieldManifestGrid.from_dict(d.pop("grid"))

        stale = d.pop("stale")

        unavailable = d.pop("unavailable")

        components = []
        _components = d.pop("components")
        for components_item_data in _components:
            components_item = FieldManifestComponentsItem.from_dict(
                components_item_data
            )

            components.append(components_item)

        byte_length = d.pop("byteLength")

        sha256 = d.pop("sha256")

        data_url = d.pop("dataUrl")

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        _attribution = d.pop("attribution", UNSET)
        attribution: Attribution | Unset
        if isinstance(_attribution, Unset):
            attribution = UNSET
        else:
            attribution = Attribution.from_dict(_attribution)

        _encodings = d.pop("encodings", UNSET)
        encodings: FieldEncodings | Unset
        if isinstance(_encodings, Unset):
            encodings = UNSET
        else:
            encodings = FieldEncodings.from_dict(_encodings)

        field_manifest = cls(
            schema_version=schema_version,
            model=model,
            cycle=cycle,
            level=level,
            units=units,
            grid=grid,
            stale=stale,
            unavailable=unavailable,
            components=components,
            byte_length=byte_length,
            sha256=sha256,
            data_url=data_url,
            reason=reason,
            attribution=attribution,
            encodings=encodings,
        )

        field_manifest.additional_properties = d
        return field_manifest

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
