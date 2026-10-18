from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.media_rating_dimension_type_0 import MediaRatingDimensionType0
from ..models.media_rating_egress import MediaRatingEgress
from ..models.media_rating_provider import MediaRatingProvider
from ..types import UNSET, Unset

T = TypeVar("T", bound="MediaRating")


@_attrs_define
class MediaRating:
    """One rating of an outlet from a declared provider, with licence and date; never model-generated

    Attributes:
        outlet_id (str):
        provider (MediaRatingProvider):
        dimension (MediaRatingDimensionType0 | str): Rating dimension; provider axes that are not left–right are
            recorded as `{provider}:{axis}`, never forced into lean
        licence (str):
        attribution (str):
        egress (MediaRatingEgress):
        imported_at (datetime.datetime):
        value_norm (float | None | Unset): Lean −2…+2 (left … right); factuality 1…5
        value_raw (None | str | Unset):
        label (None | str | Unset):
        rated_at (datetime.datetime | None | Unset):
        url (None | str | Unset):
    """

    outlet_id: str
    provider: MediaRatingProvider
    dimension: MediaRatingDimensionType0 | str
    licence: str
    attribution: str
    egress: MediaRatingEgress
    imported_at: datetime.datetime
    value_norm: float | None | Unset = UNSET
    value_raw: None | str | Unset = UNSET
    label: None | str | Unset = UNSET
    rated_at: datetime.datetime | None | Unset = UNSET
    url: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        outlet_id = self.outlet_id

        provider = self.provider.value

        dimension: str
        if isinstance(self.dimension, MediaRatingDimensionType0):
            dimension = self.dimension.value
        else:
            dimension = self.dimension

        licence = self.licence

        attribution = self.attribution

        egress = self.egress.value

        imported_at = self.imported_at.isoformat()

        value_norm: float | None | Unset
        if isinstance(self.value_norm, Unset):
            value_norm = UNSET
        else:
            value_norm = self.value_norm

        value_raw: None | str | Unset
        if isinstance(self.value_raw, Unset):
            value_raw = UNSET
        else:
            value_raw = self.value_raw

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        rated_at: None | str | Unset
        if isinstance(self.rated_at, Unset):
            rated_at = UNSET
        elif isinstance(self.rated_at, datetime.datetime):
            rated_at = self.rated_at.isoformat()
        else:
            rated_at = self.rated_at

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "outletId": outlet_id,
                "provider": provider,
                "dimension": dimension,
                "licence": licence,
                "attribution": attribution,
                "egress": egress,
                "importedAt": imported_at,
            }
        )
        if value_norm is not UNSET:
            field_dict["valueNorm"] = value_norm
        if value_raw is not UNSET:
            field_dict["valueRaw"] = value_raw
        if label is not UNSET:
            field_dict["label"] = label
        if rated_at is not UNSET:
            field_dict["ratedAt"] = rated_at
        if url is not UNSET:
            field_dict["url"] = url

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        outlet_id = d.pop("outletId")

        provider = MediaRatingProvider(d.pop("provider"))

        def _parse_dimension(data: object) -> MediaRatingDimensionType0 | str:
            try:
                if not isinstance(data, str):
                    raise TypeError()
                dimension_type_0 = MediaRatingDimensionType0(data)

                return dimension_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(MediaRatingDimensionType0 | str, data)

        dimension = _parse_dimension(d.pop("dimension"))

        licence = d.pop("licence")

        attribution = d.pop("attribution")

        egress = MediaRatingEgress(d.pop("egress"))

        imported_at = datetime.datetime.fromisoformat(d.pop("importedAt"))

        def _parse_value_norm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        value_norm = _parse_value_norm(d.pop("valueNorm", UNSET))

        def _parse_value_raw(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        value_raw = _parse_value_raw(d.pop("valueRaw", UNSET))

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        def _parse_rated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                rated_at_type_0 = datetime.datetime.fromisoformat(data)

                return rated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        rated_at = _parse_rated_at(d.pop("ratedAt", UNSET))

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        media_rating = cls(
            outlet_id=outlet_id,
            provider=provider,
            dimension=dimension,
            licence=licence,
            attribution=attribution,
            egress=egress,
            imported_at=imported_at,
            value_norm=value_norm,
            value_raw=value_raw,
            label=label,
            rated_at=rated_at,
            url=url,
        )

        media_rating.additional_properties = d
        return media_rating

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
