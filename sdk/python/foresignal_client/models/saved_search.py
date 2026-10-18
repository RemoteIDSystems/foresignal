from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary
    from ..models.saved_search_query import SavedSearchQuery


T = TypeVar("T", bound="SavedSearch")


@_attrs_define
class SavedSearch:
    """A named, shareable search whose new matches can notify the owner at most hourly

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        owner (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        query (SavedSearchQuery): A saved query over object envelopes: 027 hybrid search plus structured filters
        visibility (Visibility):
        tlp (Tlp): Traffic Light Protocol 2.0 label
        notify (bool):
        created_at (datetime.datetime):
        last_run_at (datetime.datetime | None | Unset):
        last_notified_at (datetime.datetime | None | Unset):
        last_match_count (int | None | Unset):
        updated_at (datetime.datetime | Unset):
    """

    id: str
    name: str
    owner: PrincipalSummary
    query: SavedSearchQuery
    visibility: Visibility
    tlp: Tlp
    notify: bool
    created_at: datetime.datetime
    last_run_at: datetime.datetime | None | Unset = UNSET
    last_notified_at: datetime.datetime | None | Unset = UNSET
    last_match_count: int | None | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        owner = self.owner.to_dict()

        query = self.query.to_dict()

        visibility = self.visibility.value

        tlp = self.tlp.value

        notify = self.notify

        created_at = self.created_at.isoformat()

        last_run_at: None | str | Unset
        if isinstance(self.last_run_at, Unset):
            last_run_at = UNSET
        elif isinstance(self.last_run_at, datetime.datetime):
            last_run_at = self.last_run_at.isoformat()
        else:
            last_run_at = self.last_run_at

        last_notified_at: None | str | Unset
        if isinstance(self.last_notified_at, Unset):
            last_notified_at = UNSET
        elif isinstance(self.last_notified_at, datetime.datetime):
            last_notified_at = self.last_notified_at.isoformat()
        else:
            last_notified_at = self.last_notified_at

        last_match_count: int | None | Unset
        if isinstance(self.last_match_count, Unset):
            last_match_count = UNSET
        else:
            last_match_count = self.last_match_count

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "owner": owner,
                "query": query,
                "visibility": visibility,
                "tlp": tlp,
                "notify": notify,
                "createdAt": created_at,
            }
        )
        if last_run_at is not UNSET:
            field_dict["lastRunAt"] = last_run_at
        if last_notified_at is not UNSET:
            field_dict["lastNotifiedAt"] = last_notified_at
        if last_match_count is not UNSET:
            field_dict["lastMatchCount"] = last_match_count
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary
        from ..models.saved_search_query import SavedSearchQuery

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        owner = PrincipalSummary.from_dict(d.pop("owner"))

        query = SavedSearchQuery.from_dict(d.pop("query"))

        visibility = Visibility(d.pop("visibility"))

        tlp = Tlp(d.pop("tlp"))

        notify = d.pop("notify")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_last_run_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_run_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_run_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_run_at = _parse_last_run_at(d.pop("lastRunAt", UNSET))

        def _parse_last_notified_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_notified_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_notified_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_notified_at = _parse_last_notified_at(d.pop("lastNotifiedAt", UNSET))

        def _parse_last_match_count(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        last_match_count = _parse_last_match_count(d.pop("lastMatchCount", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        saved_search = cls(
            id=id,
            name=name,
            owner=owner,
            query=query,
            visibility=visibility,
            tlp=tlp,
            notify=notify,
            created_at=created_at,
            last_run_at=last_run_at,
            last_notified_at=last_notified_at,
            last_match_count=last_match_count,
            updated_at=updated_at,
        )

        saved_search.additional_properties = d
        return saved_search

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
