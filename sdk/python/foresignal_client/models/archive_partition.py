from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.archive_partition_tier import ArchivePartitionTier
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.archive_partition_files_item import ArchivePartitionFilesItem
    from ..models.export_licence_summary import ExportLicenceSummary


T = TypeVar("T", bound="ArchivePartition")


@_attrs_define
class ArchivePartition:
    """One hive partition of the cold archive with its files (111)

    Attributes:
        tier (ArchivePartitionTier):
        source_id (str):
        date (datetime.date): yyyy-mm-dd of ingested_at (UTC)
        path (str): record_version/tier={tier}/source_id={id}/date={date}
        files (list[ArchivePartitionFilesItem]):
        licence (ExportLicenceSummary | None | Unset):
    """

    tier: ArchivePartitionTier
    source_id: str
    date: datetime.date
    path: str
    files: list[ArchivePartitionFilesItem]
    licence: ExportLicenceSummary | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.export_licence_summary import ExportLicenceSummary

        tier = self.tier.value

        source_id = self.source_id

        date = self.date.isoformat()

        path = self.path

        files = []
        for files_item_data in self.files:
            files_item = files_item_data.to_dict()
            files.append(files_item)

        licence: dict[str, Any] | None | Unset
        if isinstance(self.licence, Unset):
            licence = UNSET
        elif isinstance(self.licence, ExportLicenceSummary):
            licence = self.licence.to_dict()
        else:
            licence = self.licence

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "tier": tier,
                "sourceId": source_id,
                "date": date,
                "path": path,
                "files": files,
            }
        )
        if licence is not UNSET:
            field_dict["licence"] = licence

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.archive_partition_files_item import ArchivePartitionFilesItem
        from ..models.export_licence_summary import ExportLicenceSummary

        d = dict(src_dict)
        tier = ArchivePartitionTier(d.pop("tier"))

        source_id = d.pop("sourceId")

        date = datetime.date.fromisoformat(d.pop("date"))

        path = d.pop("path")

        files = []
        _files = d.pop("files")
        for files_item_data in _files:
            files_item = ArchivePartitionFilesItem.from_dict(files_item_data)

            files.append(files_item)

        def _parse_licence(data: object) -> ExportLicenceSummary | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                licence_type_0 = ExportLicenceSummary.from_dict(data)

                return licence_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ExportLicenceSummary | None | Unset, data)

        licence = _parse_licence(d.pop("licence", UNSET))

        archive_partition = cls(
            tier=tier,
            source_id=source_id,
            date=date,
            path=path,
            files=files,
            licence=licence,
        )

        archive_partition.additional_properties = d
        return archive_partition

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
