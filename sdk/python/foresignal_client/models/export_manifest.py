from __future__ import annotations

import datetime
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

from ..models.export_manifest_format import ExportManifestFormat
from ..models.export_manifest_kind import ExportManifestKind
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.export_licence_summary import ExportLicenceSummary
    from ..models.export_manifest_files_item import ExportManifestFilesItem
    from ..models.export_manifest_signature import ExportManifestSignature
    from ..models.export_manifest_spec import ExportManifestSpec


T = TypeVar("T", bound="ExportManifest")


@_attrs_define
class ExportManifest:
    """SHA-256 manifest of an export, snapshot or pack; signed as manifest.json.jws with the instance key (111)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        kind (ExportManifestKind):
        format_ (ExportManifestFormat):
        instance_id (str):
        created_at (datetime.datetime):
        files (list[ExportManifestFilesItem]):
        licence (ExportLicenceSummary): Licence sidecar summary of an export, snapshot, pack or archive file (111)
        manifest_version (Literal[1] | Unset):
        watermark_seq (int | None | Unset):
        at_seq (int | None | Unset):
        spec (ExportManifestSpec | Unset):
        row_count (int | Unset):
        personal_use (bool | Unset):
        signature (ExportManifestSignature | Unset): Detached JWS over manifest.json
    """

    id: str
    kind: ExportManifestKind
    format_: ExportManifestFormat
    instance_id: str
    created_at: datetime.datetime
    files: list[ExportManifestFilesItem]
    licence: ExportLicenceSummary
    manifest_version: Literal[1] | Unset = UNSET
    watermark_seq: int | None | Unset = UNSET
    at_seq: int | None | Unset = UNSET
    spec: ExportManifestSpec | Unset = UNSET
    row_count: int | Unset = UNSET
    personal_use: bool | Unset = UNSET
    signature: ExportManifestSignature | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        kind = self.kind.value

        format_ = self.format_.value

        instance_id = self.instance_id

        created_at = self.created_at.isoformat()

        files = []
        for files_item_data in self.files:
            files_item = files_item_data.to_dict()
            files.append(files_item)

        licence = self.licence.to_dict()

        manifest_version = self.manifest_version

        watermark_seq: int | None | Unset
        if isinstance(self.watermark_seq, Unset):
            watermark_seq = UNSET
        else:
            watermark_seq = self.watermark_seq

        at_seq: int | None | Unset
        if isinstance(self.at_seq, Unset):
            at_seq = UNSET
        else:
            at_seq = self.at_seq

        spec: dict[str, Any] | Unset = UNSET
        if not isinstance(self.spec, Unset):
            spec = self.spec.to_dict()

        row_count = self.row_count

        personal_use = self.personal_use

        signature: dict[str, Any] | Unset = UNSET
        if not isinstance(self.signature, Unset):
            signature = self.signature.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "kind": kind,
                "format": format_,
                "instanceId": instance_id,
                "createdAt": created_at,
                "files": files,
                "licence": licence,
            }
        )
        if manifest_version is not UNSET:
            field_dict["manifestVersion"] = manifest_version
        if watermark_seq is not UNSET:
            field_dict["watermarkSeq"] = watermark_seq
        if at_seq is not UNSET:
            field_dict["atSeq"] = at_seq
        if spec is not UNSET:
            field_dict["spec"] = spec
        if row_count is not UNSET:
            field_dict["rowCount"] = row_count
        if personal_use is not UNSET:
            field_dict["personalUse"] = personal_use
        if signature is not UNSET:
            field_dict["signature"] = signature

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.export_licence_summary import ExportLicenceSummary
        from ..models.export_manifest_files_item import ExportManifestFilesItem
        from ..models.export_manifest_signature import ExportManifestSignature
        from ..models.export_manifest_spec import ExportManifestSpec

        d = dict(src_dict)
        id = d.pop("id")

        kind = ExportManifestKind(d.pop("kind"))

        format_ = ExportManifestFormat(d.pop("format"))

        instance_id = d.pop("instanceId")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        files = []
        _files = d.pop("files")
        for files_item_data in _files:
            files_item = ExportManifestFilesItem.from_dict(files_item_data)

            files.append(files_item)

        licence = ExportLicenceSummary.from_dict(d.pop("licence"))

        manifest_version = cast(Literal[1] | Unset, d.pop("manifestVersion", UNSET))
        if manifest_version != 1 and not isinstance(manifest_version, Unset):
            raise ValueError(
                f"manifestVersion must match const 1, got '{manifest_version}'"
            )

        def _parse_watermark_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        watermark_seq = _parse_watermark_seq(d.pop("watermarkSeq", UNSET))

        def _parse_at_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        at_seq = _parse_at_seq(d.pop("atSeq", UNSET))

        _spec = d.pop("spec", UNSET)
        spec: ExportManifestSpec | Unset
        if isinstance(_spec, Unset):
            spec = UNSET
        else:
            spec = ExportManifestSpec.from_dict(_spec)

        row_count = d.pop("rowCount", UNSET)

        personal_use = d.pop("personalUse", UNSET)

        _signature = d.pop("signature", UNSET)
        signature: ExportManifestSignature | Unset
        if isinstance(_signature, Unset):
            signature = UNSET
        else:
            signature = ExportManifestSignature.from_dict(_signature)

        export_manifest = cls(
            id=id,
            kind=kind,
            format_=format_,
            instance_id=instance_id,
            created_at=created_at,
            files=files,
            licence=licence,
            manifest_version=manifest_version,
            watermark_seq=watermark_seq,
            at_seq=at_seq,
            spec=spec,
            row_count=row_count,
            personal_use=personal_use,
            signature=signature,
        )

        export_manifest.additional_properties = d
        return export_manifest

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
