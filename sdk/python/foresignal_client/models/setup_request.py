from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.resource_thresholds import ResourceThresholds
    from ..models.setup_request_admin import SetupRequestAdmin
    from ..models.setup_request_retention_type_0 import SetupRequestRetentionType0
    from ..models.smtp_settings_input import SmtpSettingsInput


T = TypeVar("T", bound="SetupRequest")


@_attrs_define
class SetupRequest:
    """First-run bootstrap: create the first admin and optionally seed SMTP, sources and thresholds (C4/190)

    Attributes:
        admin (SetupRequestAdmin): The first admin to create
        smtp (None | SmtpSettingsInput | Unset):
        sources (list[str] | Unset): Source ids to enable in the collection set; each must be a registered source
        thresholds (None | ResourceThresholds | Unset):
        retention (None | SetupRequestRetentionType0 | Unset): Optional retention policy to seed
    """

    admin: SetupRequestAdmin
    smtp: None | SmtpSettingsInput | Unset = UNSET
    sources: list[str] | Unset = UNSET
    thresholds: None | ResourceThresholds | Unset = UNSET
    retention: None | SetupRequestRetentionType0 | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        from ..models.resource_thresholds import ResourceThresholds
        from ..models.setup_request_retention_type_0 import SetupRequestRetentionType0
        from ..models.smtp_settings_input import SmtpSettingsInput

        admin = self.admin.to_dict()

        smtp: dict[str, Any] | None | Unset
        if isinstance(self.smtp, Unset):
            smtp = UNSET
        elif isinstance(self.smtp, SmtpSettingsInput):
            smtp = self.smtp.to_dict()
        else:
            smtp = self.smtp

        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        thresholds: dict[str, Any] | None | Unset
        if isinstance(self.thresholds, Unset):
            thresholds = UNSET
        elif isinstance(self.thresholds, ResourceThresholds):
            thresholds = self.thresholds.to_dict()
        else:
            thresholds = self.thresholds

        retention: dict[str, Any] | None | Unset
        if isinstance(self.retention, Unset):
            retention = UNSET
        elif isinstance(self.retention, SetupRequestRetentionType0):
            retention = self.retention.to_dict()
        else:
            retention = self.retention

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "admin": admin,
            }
        )
        if smtp is not UNSET:
            field_dict["smtp"] = smtp
        if sources is not UNSET:
            field_dict["sources"] = sources
        if thresholds is not UNSET:
            field_dict["thresholds"] = thresholds
        if retention is not UNSET:
            field_dict["retention"] = retention

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.resource_thresholds import ResourceThresholds
        from ..models.setup_request_admin import SetupRequestAdmin
        from ..models.setup_request_retention_type_0 import SetupRequestRetentionType0
        from ..models.smtp_settings_input import SmtpSettingsInput

        d = dict(src_dict)
        admin = SetupRequestAdmin.from_dict(d.pop("admin"))

        def _parse_smtp(data: object) -> None | SmtpSettingsInput | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                smtp_type_0 = SmtpSettingsInput.from_dict(data)

                return smtp_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SmtpSettingsInput | Unset, data)

        smtp = _parse_smtp(d.pop("smtp", UNSET))

        sources = cast(list[str], d.pop("sources", UNSET))

        def _parse_thresholds(data: object) -> None | ResourceThresholds | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                thresholds_type_0 = ResourceThresholds.from_dict(data)

                return thresholds_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ResourceThresholds | Unset, data)

        thresholds = _parse_thresholds(d.pop("thresholds", UNSET))

        def _parse_retention(data: object) -> None | SetupRequestRetentionType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                retention_type_0 = SetupRequestRetentionType0.from_dict(data)

                return retention_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SetupRequestRetentionType0 | Unset, data)

        retention = _parse_retention(d.pop("retention", UNSET))

        setup_request = cls(
            admin=admin,
            smtp=smtp,
            sources=sources,
            thresholds=thresholds,
            retention=retention,
        )

        return setup_request
