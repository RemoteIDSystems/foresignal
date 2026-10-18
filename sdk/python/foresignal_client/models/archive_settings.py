from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.archive_settings_target import ArchiveSettingsTarget
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.archive_settings_s3_type_0 import ArchiveSettingsS3Type0
    from ..models.archive_settings_secrets import ArchiveSettingsSecrets


T = TypeVar("T", bound="ArchiveSettings")


@_attrs_define
class ArchiveSettings:
    """Cold archive settings (settings key `archive`); secrets are write-only (111)

    Attributes:
        enabled (bool): Default false Default: False.
        target (ArchiveSettingsTarget):  Default: ArchiveSettingsTarget.LOCAL.
        sources (list[str] | Unset): Allowlist; empty = all non-private STANDARD and TRACK sources
        root (str | Unset): Default ${FORESIGNAL_DATA_DIR}/archive; an absolute path, or a path relative to the data
            directory
        s3 (ArchiveSettingsS3Type0 | None | Unset):
        secrets (ArchiveSettingsSecrets | Unset): Write-only S3 credentials
        secrets_set (list[str] | Unset): Names of secrets that have a stored value
        keep_local (bool | Unset): Keep the local copy after a verified upload Default: True.
    """

    enabled: bool = False
    target: ArchiveSettingsTarget = ArchiveSettingsTarget.LOCAL
    sources: list[str] | Unset = UNSET
    root: str | Unset = UNSET
    s3: ArchiveSettingsS3Type0 | None | Unset = UNSET
    secrets: ArchiveSettingsSecrets | Unset = UNSET
    secrets_set: list[str] | Unset = UNSET
    keep_local: bool | Unset = True
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.archive_settings_s3_type_0 import ArchiveSettingsS3Type0

        enabled = self.enabled

        target = self.target.value

        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        root = self.root

        s3: dict[str, Any] | None | Unset
        if isinstance(self.s3, Unset):
            s3 = UNSET
        elif isinstance(self.s3, ArchiveSettingsS3Type0):
            s3 = self.s3.to_dict()
        else:
            s3 = self.s3

        secrets: dict[str, Any] | Unset = UNSET
        if not isinstance(self.secrets, Unset):
            secrets = self.secrets.to_dict()

        secrets_set: list[str] | Unset = UNSET
        if not isinstance(self.secrets_set, Unset):
            secrets_set = self.secrets_set

        keep_local = self.keep_local

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "enabled": enabled,
                "target": target,
            }
        )
        if sources is not UNSET:
            field_dict["sources"] = sources
        if root is not UNSET:
            field_dict["root"] = root
        if s3 is not UNSET:
            field_dict["s3"] = s3
        if secrets is not UNSET:
            field_dict["secrets"] = secrets
        if secrets_set is not UNSET:
            field_dict["secretsSet"] = secrets_set
        if keep_local is not UNSET:
            field_dict["keepLocal"] = keep_local

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.archive_settings_s3_type_0 import ArchiveSettingsS3Type0
        from ..models.archive_settings_secrets import ArchiveSettingsSecrets

        d = dict(src_dict)
        enabled = d.pop("enabled")

        target = ArchiveSettingsTarget(d.pop("target"))

        sources = cast(list[str], d.pop("sources", UNSET))

        root = d.pop("root", UNSET)

        def _parse_s3(data: object) -> ArchiveSettingsS3Type0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                s3_type_0 = ArchiveSettingsS3Type0.from_dict(data)

                return s3_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ArchiveSettingsS3Type0 | None | Unset, data)

        s3 = _parse_s3(d.pop("s3", UNSET))

        _secrets = d.pop("secrets", UNSET)
        secrets: ArchiveSettingsSecrets | Unset
        if isinstance(_secrets, Unset):
            secrets = UNSET
        else:
            secrets = ArchiveSettingsSecrets.from_dict(_secrets)

        secrets_set = cast(list[str], d.pop("secretsSet", UNSET))

        keep_local = d.pop("keepLocal", UNSET)

        archive_settings = cls(
            enabled=enabled,
            target=target,
            sources=sources,
            root=root,
            s3=s3,
            secrets=secrets,
            secrets_set=secrets_set,
            keep_local=keep_local,
        )

        archive_settings.additional_properties = d
        return archive_settings

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
