from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_item_rejection_schema_errors_item import (
        EdpItemRejectionSchemaErrorsItem,
    )


T = TypeVar("T", bound="EdpItemRejection")


@_attrs_define
class EdpItemRejection:
    """Why one item was rejected

    Attributes:
        index (int):
        reason (str):  Example: SCHEMA_INVALID.
        stream (None | str | Unset):
        id (None | str | Unset):
        schema_errors (list[EdpItemRejectionSchemaErrorsItem] | Unset):
    """

    index: int
    reason: str
    stream: None | str | Unset = UNSET
    id: None | str | Unset = UNSET
    schema_errors: list[EdpItemRejectionSchemaErrorsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        index = self.index

        reason = self.reason

        stream: None | str | Unset
        if isinstance(self.stream, Unset):
            stream = UNSET
        else:
            stream = self.stream

        id: None | str | Unset
        if isinstance(self.id, Unset):
            id = UNSET
        else:
            id = self.id

        schema_errors: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.schema_errors, Unset):
            schema_errors = []
            for schema_errors_item_data in self.schema_errors:
                schema_errors_item = schema_errors_item_data.to_dict()
                schema_errors.append(schema_errors_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "index": index,
                "reason": reason,
            }
        )
        if stream is not UNSET:
            field_dict["stream"] = stream
        if id is not UNSET:
            field_dict["id"] = id
        if schema_errors is not UNSET:
            field_dict["schemaErrors"] = schema_errors

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_item_rejection_schema_errors_item import (
            EdpItemRejectionSchemaErrorsItem,
        )

        d = dict(src_dict)
        index = d.pop("index")

        reason = d.pop("reason")

        def _parse_stream(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        stream = _parse_stream(d.pop("stream", UNSET))

        def _parse_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        id = _parse_id(d.pop("id", UNSET))

        _schema_errors = d.pop("schemaErrors", UNSET)
        schema_errors: list[EdpItemRejectionSchemaErrorsItem] | Unset = UNSET
        if _schema_errors is not UNSET:
            schema_errors = []
            for schema_errors_item_data in _schema_errors:
                schema_errors_item = EdpItemRejectionSchemaErrorsItem.from_dict(
                    schema_errors_item_data
                )

                schema_errors.append(schema_errors_item)

        edp_item_rejection = cls(
            index=index,
            reason=reason,
            stream=stream,
            id=id,
            schema_errors=schema_errors,
        )

        edp_item_rejection.additional_properties = d
        return edp_item_rejection

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
