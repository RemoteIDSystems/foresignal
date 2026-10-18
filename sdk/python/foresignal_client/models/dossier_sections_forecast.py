from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.dossier_sections_forecast_status import DossierSectionsForecastStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.dossier_sections_forecast_facets_type_0 import (
        DossierSectionsForecastFacetsType0,
    )
    from ..models.dossier_sections_forecast_links_type_0 import (
        DossierSectionsForecastLinksType0,
    )
    from ..models.dossier_sections_forecast_official_item import (
        DossierSectionsForecastOfficialItem,
    )
    from ..models.ledger_question_brief import LedgerQuestionBrief


T = TypeVar("T", bound="DossierSectionsForecast")


@_attrs_define
class DossierSectionsForecast:
    """Official forecasts and ledger questions about the object

    Attributes:
        status (DossierSectionsForecastStatus):
        message (None | str | Unset):
        links (DossierSectionsForecastLinksType0 | None | Unset):
        facets (DossierSectionsForecastFacetsType0 | None | Unset):
        truncated (bool | Unset):
        official (list[DossierSectionsForecastOfficialItem] | Unset):
        questions (list[LedgerQuestionBrief] | Unset):
    """

    status: DossierSectionsForecastStatus
    message: None | str | Unset = UNSET
    links: DossierSectionsForecastLinksType0 | None | Unset = UNSET
    facets: DossierSectionsForecastFacetsType0 | None | Unset = UNSET
    truncated: bool | Unset = UNSET
    official: list[DossierSectionsForecastOfficialItem] | Unset = UNSET
    questions: list[LedgerQuestionBrief] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.dossier_sections_forecast_facets_type_0 import (
            DossierSectionsForecastFacetsType0,
        )
        from ..models.dossier_sections_forecast_links_type_0 import (
            DossierSectionsForecastLinksType0,
        )

        status = self.status.value

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        links: dict[str, Any] | None | Unset
        if isinstance(self.links, Unset):
            links = UNSET
        elif isinstance(self.links, DossierSectionsForecastLinksType0):
            links = self.links.to_dict()
        else:
            links = self.links

        facets: dict[str, Any] | None | Unset
        if isinstance(self.facets, Unset):
            facets = UNSET
        elif isinstance(self.facets, DossierSectionsForecastFacetsType0):
            facets = self.facets.to_dict()
        else:
            facets = self.facets

        truncated = self.truncated

        official: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.official, Unset):
            official = []
            for official_item_data in self.official:
                official_item = official_item_data.to_dict()
                official.append(official_item)

        questions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.questions, Unset):
            questions = []
            for questions_item_data in self.questions:
                questions_item = questions_item_data.to_dict()
                questions.append(questions_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if message is not UNSET:
            field_dict["message"] = message
        if links is not UNSET:
            field_dict["links"] = links
        if facets is not UNSET:
            field_dict["facets"] = facets
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if official is not UNSET:
            field_dict["official"] = official
        if questions is not UNSET:
            field_dict["questions"] = questions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.dossier_sections_forecast_facets_type_0 import (
            DossierSectionsForecastFacetsType0,
        )
        from ..models.dossier_sections_forecast_links_type_0 import (
            DossierSectionsForecastLinksType0,
        )
        from ..models.dossier_sections_forecast_official_item import (
            DossierSectionsForecastOfficialItem,
        )
        from ..models.ledger_question_brief import LedgerQuestionBrief

        d = dict(src_dict)
        status = DossierSectionsForecastStatus(d.pop("status"))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_links(
            data: object,
        ) -> DossierSectionsForecastLinksType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                links_type_0 = DossierSectionsForecastLinksType0.from_dict(data)

                return links_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsForecastLinksType0 | None | Unset, data)

        links = _parse_links(d.pop("links", UNSET))

        def _parse_facets(
            data: object,
        ) -> DossierSectionsForecastFacetsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                facets_type_0 = DossierSectionsForecastFacetsType0.from_dict(data)

                return facets_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DossierSectionsForecastFacetsType0 | None | Unset, data)

        facets = _parse_facets(d.pop("facets", UNSET))

        truncated = d.pop("truncated", UNSET)

        _official = d.pop("official", UNSET)
        official: list[DossierSectionsForecastOfficialItem] | Unset = UNSET
        if _official is not UNSET:
            official = []
            for official_item_data in _official:
                official_item = DossierSectionsForecastOfficialItem.from_dict(
                    official_item_data
                )

                official.append(official_item)

        _questions = d.pop("questions", UNSET)
        questions: list[LedgerQuestionBrief] | Unset = UNSET
        if _questions is not UNSET:
            questions = []
            for questions_item_data in _questions:
                questions_item = LedgerQuestionBrief.from_dict(questions_item_data)

                questions.append(questions_item)

        dossier_sections_forecast = cls(
            status=status,
            message=message,
            links=links,
            facets=facets,
            truncated=truncated,
            official=official,
            questions=questions,
        )

        dossier_sections_forecast.additional_properties = d
        return dossier_sections_forecast

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
