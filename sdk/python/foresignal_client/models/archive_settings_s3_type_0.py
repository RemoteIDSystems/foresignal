from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.archive_settings_s3_type_0_storage_class_type_1 import (
    ArchiveSettingsS3Type0StorageClassType1,
)
from ..models.archive_settings_s3_type_0_storage_class_type_2_type_1 import (
    ArchiveSettingsS3Type0StorageClassType2Type1,
)
from ..models.archive_settings_s3_type_0_storage_class_type_3_type_1 import (
    ArchiveSettingsS3Type0StorageClassType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ArchiveSettingsS3Type0")


@_attrs_define
class ArchiveSettingsS3Type0:
    """S3-compatible target (SigV4; endpoint checked by the outbound URL policy)

    Attributes:
        endpoint (str):
        bucket (str):
        prefix (str | Unset):
        region (str | Unset):
        path_style (bool | Unset):
        storage_class (ArchiveSettingsS3Type0StorageClassType1 | ArchiveSettingsS3Type0StorageClassType2Type1 |
            ArchiveSettingsS3Type0StorageClassType3Type1 | None | Unset): `x-amz-storage-class` of uploads (R2-016); null
            uses the bucket default. With target `glacier` it must be GLACIER (the default) or DEEP_ARCHIVE; with target
            `s3` it may not be either, since those objects cannot be read until thawed.
    """

    endpoint: str
    bucket: str
    prefix: str | Unset = UNSET
    region: str | Unset = UNSET
    path_style: bool | Unset = UNSET
    storage_class: (
        ArchiveSettingsS3Type0StorageClassType1
        | ArchiveSettingsS3Type0StorageClassType2Type1
        | ArchiveSettingsS3Type0StorageClassType3Type1
        | None
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        endpoint = self.endpoint

        bucket = self.bucket

        prefix = self.prefix

        region = self.region

        path_style = self.path_style

        storage_class: None | str | Unset
        if isinstance(self.storage_class, Unset):
            storage_class = UNSET
        elif (
            isinstance(self.storage_class, ArchiveSettingsS3Type0StorageClassType1)
            or isinstance(
                self.storage_class, ArchiveSettingsS3Type0StorageClassType2Type1
            )
            or isinstance(
                self.storage_class, ArchiveSettingsS3Type0StorageClassType3Type1
            )
        ):
            storage_class = self.storage_class.value
        else:
            storage_class = self.storage_class

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "endpoint": endpoint,
                "bucket": bucket,
            }
        )
        if prefix is not UNSET:
            field_dict["prefix"] = prefix
        if region is not UNSET:
            field_dict["region"] = region
        if path_style is not UNSET:
            field_dict["pathStyle"] = path_style
        if storage_class is not UNSET:
            field_dict["storageClass"] = storage_class

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        endpoint = d.pop("endpoint")

        bucket = d.pop("bucket")

        prefix = d.pop("prefix", UNSET)

        region = d.pop("region", UNSET)

        path_style = d.pop("pathStyle", UNSET)

        def _parse_storage_class(
            data: object,
        ) -> (
            ArchiveSettingsS3Type0StorageClassType1
            | ArchiveSettingsS3Type0StorageClassType2Type1
            | ArchiveSettingsS3Type0StorageClassType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                storage_class_type_1 = ArchiveSettingsS3Type0StorageClassType1(data)

                return storage_class_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                storage_class_type_2_type_1 = (
                    ArchiveSettingsS3Type0StorageClassType2Type1(data)
                )

                return storage_class_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                storage_class_type_3_type_1 = (
                    ArchiveSettingsS3Type0StorageClassType3Type1(data)
                )

                return storage_class_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                ArchiveSettingsS3Type0StorageClassType1
                | ArchiveSettingsS3Type0StorageClassType2Type1
                | ArchiveSettingsS3Type0StorageClassType3Type1
                | None
                | Unset,
                data,
            )

        storage_class = _parse_storage_class(d.pop("storageClass", UNSET))

        archive_settings_s3_type_0 = cls(
            endpoint=endpoint,
            bucket=bucket,
            prefix=prefix,
            region=region,
            path_style=path_style,
            storage_class=storage_class,
        )

        archive_settings_s3_type_0.additional_properties = d
        return archive_settings_s3_type_0

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
