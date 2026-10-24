from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.report_block_base_text_origin import ReportBlockBaseTextOrigin
from ..models.report_block_base_type import ReportBlockBaseType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="ReportBlockBase")


@_attrs_define
class ReportBlockBase:
    """Fields common to every report block

    Attributes:
        type_ (ReportBlockBaseType):
        id (str | Unset): Server-assigned; comment on a block with `report:{id}#{blockId}`
        title (None | str | Unset):
        text_origin (ReportBlockBaseTextOrigin | Unset): `llm` blocks are labeled narrative from the optional 028 tool
            loop
        updated_at (datetime.datetime | Unset):
        updated_by (None | PrincipalSummary | Unset):
    """

    type_: ReportBlockBaseType
    id: str | Unset = UNSET
    title: None | str | Unset = UNSET
    text_origin: ReportBlockBaseTextOrigin | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    updated_by: None | PrincipalSummary | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.principal_summary import PrincipalSummary

        type_ = self.type_.value

        id = self.id

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        text_origin: str | Unset = UNSET
        if not isinstance(self.text_origin, Unset):
            text_origin = self.text_origin.value

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        updated_by: dict[str, Any] | None | Unset
        if isinstance(self.updated_by, Unset):
            updated_by = UNSET
        elif isinstance(self.updated_by, PrincipalSummary):
            updated_by = self.updated_by.to_dict()
        else:
            updated_by = self.updated_by

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if id is not UNSET:
            field_dict["id"] = id
        if title is not UNSET:
            field_dict["title"] = title
        if text_origin is not UNSET:
            field_dict["textOrigin"] = text_origin
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if updated_by is not UNSET:
            field_dict["updatedBy"] = updated_by

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        type_ = ReportBlockBaseType(d.pop("type"))

        id = d.pop("id", UNSET)

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        _text_origin = d.pop("textOrigin", UNSET)
        text_origin: ReportBlockBaseTextOrigin | Unset
        if isinstance(_text_origin, Unset):
            text_origin = UNSET
        else:
            text_origin = ReportBlockBaseTextOrigin(_text_origin)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        def _parse_updated_by(data: object) -> None | PrincipalSummary | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                updated_by_type_0 = PrincipalSummary.from_dict(data)

                return updated_by_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrincipalSummary | Unset, data)

        updated_by = _parse_updated_by(d.pop("updatedBy", UNSET))

        report_block_base = cls(
            type_=type_,
            id=id,
            title=title,
            text_origin=text_origin,
            updated_at=updated_at,
            updated_by=updated_by,
        )

        report_block_base.additional_properties = d
        return report_block_base

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
