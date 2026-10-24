from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialCueExternalResultsItem")


@_attrs_define
class SocialCueExternalResultsItem:
    """What one EDP provider's `transport.search` returned for the cue (`edpProviderSearch`)

    Attributes:
        source_id (str): The provider's source
        queries (int): Queries sent (at most 10)
        items (int): Records stored as cue results
        error (None | str | Unset): The first failure, e.g. BUDGET_EXCEEDED
    """

    source_id: str
    queries: int
    items: int
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        queries = self.queries

        items = self.items

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "queries": queries,
                "items": items,
            }
        )
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        queries = d.pop("queries")

        items = d.pop("items")

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        social_cue_external_results_item = cls(
            source_id=source_id,
            queries=queries,
            items=items,
            error=error,
        )

        social_cue_external_results_item.additional_properties = d
        return social_cue_external_results_item

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
