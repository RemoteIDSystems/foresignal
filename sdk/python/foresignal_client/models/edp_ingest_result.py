from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.edp_item_rejection import EdpItemRejection
    from ..models.ingest_outcome import IngestOutcome


T = TypeVar("T", bound="EdpIngestResult")


@_attrs_define
class EdpIngestResult:
    """Result of a push

    Attributes:
        accepted (int):
        rejected (list[EdpItemRejection]):
        outcome (IngestOutcome | Unset):
    """

    accepted: int
    rejected: list[EdpItemRejection]
    outcome: IngestOutcome | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        accepted = self.accepted

        rejected = []
        for rejected_item_data in self.rejected:
            rejected_item = rejected_item_data.to_dict()
            rejected.append(rejected_item)

        outcome: dict[str, Any] | Unset = UNSET
        if not isinstance(self.outcome, Unset):
            outcome = self.outcome.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "accepted": accepted,
                "rejected": rejected,
            }
        )
        if outcome is not UNSET:
            field_dict["outcome"] = outcome

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_item_rejection import EdpItemRejection
        from ..models.ingest_outcome import IngestOutcome

        d = dict(src_dict)
        accepted = d.pop("accepted")

        rejected = []
        _rejected = d.pop("rejected")
        for rejected_item_data in _rejected:
            rejected_item = EdpItemRejection.from_dict(rejected_item_data)

            rejected.append(rejected_item)

        _outcome = d.pop("outcome", UNSET)
        outcome: IngestOutcome | Unset
        if isinstance(_outcome, Unset):
            outcome = UNSET
        else:
            outcome = IngestOutcome.from_dict(_outcome)

        edp_ingest_result = cls(
            accepted=accepted,
            rejected=rejected,
            outcome=outcome,
        )

        edp_ingest_result.additional_properties = d
        return edp_ingest_result

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
