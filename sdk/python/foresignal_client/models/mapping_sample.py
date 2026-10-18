from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mapping_sample_query_type_1 import MappingSampleQueryType1


T = TypeVar("T", bound="MappingSample")


@_attrs_define
class MappingSample:
    """A raw inbound sample for preview or suggestion; form and query inputs are converted to a flat JSON object of strings
    before mapping (059)

        Attributes:
            content_type (str):  Example: application/x-www-form-urlencoded.
            body (Any | Unset): Raw body as a string, or a JSON value for JSON samples
            query (MappingSampleQueryType1 | None | str | Unset): Raw query string or name→value map (GET protocols such as
                WU)
    """

    content_type: str
    body: Any | Unset = UNSET
    query: MappingSampleQueryType1 | None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.mapping_sample_query_type_1 import MappingSampleQueryType1

        content_type = self.content_type

        body = self.body

        query: dict[str, Any] | None | str | Unset
        if isinstance(self.query, Unset):
            query = UNSET
        elif isinstance(self.query, MappingSampleQueryType1):
            query = self.query.to_dict()
        else:
            query = self.query

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "contentType": content_type,
            }
        )
        if body is not UNSET:
            field_dict["body"] = body
        if query is not UNSET:
            field_dict["query"] = query

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_sample_query_type_1 import MappingSampleQueryType1

        d = dict(src_dict)
        content_type = d.pop("contentType")

        body = d.pop("body", UNSET)

        def _parse_query(data: object) -> MappingSampleQueryType1 | None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                query_type_1 = MappingSampleQueryType1.from_dict(data)

                return query_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(MappingSampleQueryType1 | None | str | Unset, data)

        query = _parse_query(d.pop("query", UNSET))

        mapping_sample = cls(
            content_type=content_type,
            body=body,
            query=query,
        )

        mapping_sample.additional_properties = d
        return mapping_sample

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
