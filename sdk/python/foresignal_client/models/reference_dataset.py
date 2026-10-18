from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.reference_dataset_state import ReferenceDatasetState
from ..types import UNSET, Unset

T = TypeVar("T", bound="ReferenceDataset")


@_attrs_define
class ReferenceDataset:
    """One reference dataset (040)

    Attributes:
        name (str):  Example: ne-admin1.
        state (ReferenceDatasetState):
        edition (None | str | Unset):
        url (str | Unset):
        sha256 (str | Unset):
        licence (str | Unset):
        attribution (None | str | Unset):
        rows (int | None | Unset):
        loaded_at (datetime.datetime | None | Unset):
        optional (bool | Unset):
        error (None | str | Unset):  Example: REFERENCE_CHECKSUM_MISMATCH.
    """

    name: str
    state: ReferenceDatasetState
    edition: None | str | Unset = UNSET
    url: str | Unset = UNSET
    sha256: str | Unset = UNSET
    licence: str | Unset = UNSET
    attribution: None | str | Unset = UNSET
    rows: int | None | Unset = UNSET
    loaded_at: datetime.datetime | None | Unset = UNSET
    optional: bool | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        state = self.state.value

        edition: None | str | Unset
        if isinstance(self.edition, Unset):
            edition = UNSET
        else:
            edition = self.edition

        url = self.url

        sha256 = self.sha256

        licence = self.licence

        attribution: None | str | Unset
        if isinstance(self.attribution, Unset):
            attribution = UNSET
        else:
            attribution = self.attribution

        rows: int | None | Unset
        if isinstance(self.rows, Unset):
            rows = UNSET
        else:
            rows = self.rows

        loaded_at: None | str | Unset
        if isinstance(self.loaded_at, Unset):
            loaded_at = UNSET
        elif isinstance(self.loaded_at, datetime.datetime):
            loaded_at = self.loaded_at.isoformat()
        else:
            loaded_at = self.loaded_at

        optional = self.optional

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "state": state,
            }
        )
        if edition is not UNSET:
            field_dict["edition"] = edition
        if url is not UNSET:
            field_dict["url"] = url
        if sha256 is not UNSET:
            field_dict["sha256"] = sha256
        if licence is not UNSET:
            field_dict["licence"] = licence
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if rows is not UNSET:
            field_dict["rows"] = rows
        if loaded_at is not UNSET:
            field_dict["loadedAt"] = loaded_at
        if optional is not UNSET:
            field_dict["optional"] = optional
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        state = ReferenceDatasetState(d.pop("state"))

        def _parse_edition(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        edition = _parse_edition(d.pop("edition", UNSET))

        url = d.pop("url", UNSET)

        sha256 = d.pop("sha256", UNSET)

        licence = d.pop("licence", UNSET)

        def _parse_attribution(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution = _parse_attribution(d.pop("attribution", UNSET))

        def _parse_rows(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        rows = _parse_rows(d.pop("rows", UNSET))

        def _parse_loaded_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                loaded_at_type_0 = datetime.datetime.fromisoformat(data)

                return loaded_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        loaded_at = _parse_loaded_at(d.pop("loadedAt", UNSET))

        optional = d.pop("optional", UNSET)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        reference_dataset = cls(
            name=name,
            state=state,
            edition=edition,
            url=url,
            sha256=sha256,
            licence=licence,
            attribution=attribution,
            rows=rows,
            loaded_at=loaded_at,
            optional=optional,
            error=error,
        )

        reference_dataset.additional_properties = d
        return reference_dataset

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
