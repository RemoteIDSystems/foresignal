from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.external_source import ExternalSource


T = TypeVar("T", bound="ExternalSourceCreated")


@_attrs_define
class ExternalSourceCreated:
    """A registered source and its ingest key (shown once)

    Attributes:
        source (ExternalSource): An EDP provider registered on this instance (024)
        ingest_key (str): eik_… (shown once)
    """

    source: ExternalSource
    ingest_key: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source = self.source.to_dict()

        ingest_key = self.ingest_key

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "source": source,
                "ingestKey": ingest_key,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.external_source import ExternalSource

        d = dict(src_dict)
        source = ExternalSource.from_dict(d.pop("source"))

        ingest_key = d.pop("ingestKey")

        external_source_created = cls(
            source=source,
            ingest_key=ingest_key,
        )

        external_source_created.additional_properties = d
        return external_source_created

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
