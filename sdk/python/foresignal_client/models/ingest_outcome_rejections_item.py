from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IngestOutcomeRejectionsItem")


@_attrs_define
class IngestOutcomeRejectionsItem:
    """
    Attributes:
        index (int | Unset):
        external_id (None | str | Unset):
        reason (str | Unset): Rejection reason code (normalizer, prompt 008): EXTERNAL_ID_INVALID, UNKNOWN_KIND,
            MISSING_OBSERVED_AT, OBSERVED_AT_TOO_OLD, PAYLOAD_TOO_LARGE, PAYLOAD_TOO_DEEP, PAYLOAD_TOO_MANY_KEYS,
            PAYLOAD_STRING_TOO_LONG, PAYLOAD_NON_FINITE_NUMBER, PAYLOAD_UNSUPPORTED_TYPE, TOO_MANY_OBSERVERS,
            GEOMETRY_TOO_MANY_VERTICES, GEOMETRY_WRONG_SRID, COORDINATES_OUT_OF_RANGE, QUALITY_INVALID,
            VALID_RANGE_INVERTED. Format rejections (024, 039): INVALID_ITEM, LOW_ACCURACY (accuracy worse than
            maxAccuracyMeters), INVALID_FIX (valid=false, missing or out-of-range coordinates), NULL_ISLAND (exact 0,0),
            ENCRYPTED_UNSUPPORTED (OwnTracks payload encryption). Later prompts may add codes.
    """

    index: int | Unset = UNSET
    external_id: None | str | Unset = UNSET
    reason: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        index = self.index

        external_id: None | str | Unset
        if isinstance(self.external_id, Unset):
            external_id = UNSET
        else:
            external_id = self.external_id

        reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if index is not UNSET:
            field_dict["index"] = index
        if external_id is not UNSET:
            field_dict["externalId"] = external_id
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        index = d.pop("index", UNSET)

        def _parse_external_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        external_id = _parse_external_id(d.pop("externalId", UNSET))

        reason = d.pop("reason", UNSET)

        ingest_outcome_rejections_item = cls(
            index=index,
            external_id=external_id,
            reason=reason,
        )

        ingest_outcome_rejections_item.additional_properties = d
        return ingest_outcome_rejections_item

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
