from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_sketch_package_key_types_item import (
    ChronicleSketchPackageKeyTypesItem,
)
from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_sketch_package_capture_span import (
        ChronicleSketchPackageCaptureSpan,
    )
    from ..models.chronicle_sketch_package_minhash import ChronicleSketchPackageMinhash
    from ..models.chronicle_sketch_package_size_band import (
        ChronicleSketchPackageSizeBand,
    )


T = TypeVar("T", bound="ChronicleSketchPackage")


@_attrs_define
class ChronicleSketchPackage:
    """A privacy-preserving fingerprint of a `network`-discoverable chronicle exchanged between peers (134)

    Attributes:
        sketch_id (str): Random per chronicle per salt
        instance_id (str):
        network_id (str):
        key_types (list[ChronicleSketchPackageKeyTypesItem]):
        minhash (ChronicleSketchPackageMinhash): Key type → 128 MinHash values (base64 of 128 big-endian 64-bit
            integers)
        size_band (ChronicleSketchPackageSizeBand): Key type → log2 bucket of the set size
        capture_span (ChronicleSketchPackageCaptureSpan): Capture span at ISO-week granularity
        tlp (Tlp): Traffic Light Protocol 2.0 label
        issued_at (datetime.datetime):
        title (None | str | Unset): Only if revealed
        owner_handle (None | str | Unset): Only if revealed
        expires_at (datetime.datetime | Unset):
    """

    sketch_id: str
    instance_id: str
    network_id: str
    key_types: list[ChronicleSketchPackageKeyTypesItem]
    minhash: ChronicleSketchPackageMinhash
    size_band: ChronicleSketchPackageSizeBand
    capture_span: ChronicleSketchPackageCaptureSpan
    tlp: Tlp
    issued_at: datetime.datetime
    title: None | str | Unset = UNSET
    owner_handle: None | str | Unset = UNSET
    expires_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        sketch_id = self.sketch_id

        instance_id = self.instance_id

        network_id = self.network_id

        key_types = []
        for key_types_item_data in self.key_types:
            key_types_item = key_types_item_data.value
            key_types.append(key_types_item)

        minhash = self.minhash.to_dict()

        size_band = self.size_band.to_dict()

        capture_span = self.capture_span.to_dict()

        tlp = self.tlp.value

        issued_at = self.issued_at.isoformat()

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        owner_handle: None | str | Unset
        if isinstance(self.owner_handle, Unset):
            owner_handle = UNSET
        else:
            owner_handle = self.owner_handle

        expires_at: str | Unset = UNSET
        if not isinstance(self.expires_at, Unset):
            expires_at = self.expires_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sketchId": sketch_id,
                "instanceId": instance_id,
                "networkId": network_id,
                "keyTypes": key_types,
                "minhash": minhash,
                "sizeBand": size_band,
                "captureSpan": capture_span,
                "tlp": tlp,
                "issuedAt": issued_at,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if owner_handle is not UNSET:
            field_dict["ownerHandle"] = owner_handle
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_sketch_package_capture_span import (
            ChronicleSketchPackageCaptureSpan,
        )
        from ..models.chronicle_sketch_package_minhash import (
            ChronicleSketchPackageMinhash,
        )
        from ..models.chronicle_sketch_package_size_band import (
            ChronicleSketchPackageSizeBand,
        )

        d = dict(src_dict)
        sketch_id = d.pop("sketchId")

        instance_id = d.pop("instanceId")

        network_id = d.pop("networkId")

        key_types = []
        _key_types = d.pop("keyTypes")
        for key_types_item_data in _key_types:
            key_types_item = ChronicleSketchPackageKeyTypesItem(key_types_item_data)

            key_types.append(key_types_item)

        minhash = ChronicleSketchPackageMinhash.from_dict(d.pop("minhash"))

        size_band = ChronicleSketchPackageSizeBand.from_dict(d.pop("sizeBand"))

        capture_span = ChronicleSketchPackageCaptureSpan.from_dict(d.pop("captureSpan"))

        tlp = Tlp(d.pop("tlp"))

        issued_at = datetime.datetime.fromisoformat(d.pop("issuedAt"))

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        def _parse_owner_handle(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        owner_handle = _parse_owner_handle(d.pop("ownerHandle", UNSET))

        _expires_at = d.pop("expiresAt", UNSET)
        expires_at: datetime.datetime | Unset
        if isinstance(_expires_at, Unset):
            expires_at = UNSET
        else:
            expires_at = datetime.datetime.fromisoformat(_expires_at)

        chronicle_sketch_package = cls(
            sketch_id=sketch_id,
            instance_id=instance_id,
            network_id=network_id,
            key_types=key_types,
            minhash=minhash,
            size_band=size_band,
            capture_span=capture_span,
            tlp=tlp,
            issued_at=issued_at,
            title=title,
            owner_handle=owner_handle,
            expires_at=expires_at,
        )

        chronicle_sketch_package.additional_properties = d
        return chronicle_sketch_package

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
