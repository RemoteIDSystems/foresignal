from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CelestrakGpElement")


@_attrs_define
class CelestrakGpElement:
    """One CCSDS OMM element set in CelesTrak `FORMAT=JSON` shape (064). Keys keep CelesTrak's upper-case OMM names for
    client compatibility.

        Attributes:
            object_name (str):  Example: ISS (ZARYA).
            object_id (str):  Example: 1998-067A.
            epoch (str): UTC, no zone suffix Example: 2026-10-10T11:52:31.123456.
            mean_motion (float):
            eccentricity (float):
            inclination (float):
            ra_of_asc_node (float):
            arg_of_pericenter (float):
            mean_anomaly (float):
            norad_cat_id (int): Up to 9 digits; never assume 5
            ephemeris_type (int | Unset):
            classification_type (str | Unset):  Example: U.
            element_set_no (int | Unset):
            rev_at_epoch (int | Unset):
            bstar (float | Unset):
            mean_motion_dot (float | Unset):
            mean_motion_ddot (float | Unset):
    """

    object_name: str
    object_id: str
    epoch: str
    mean_motion: float
    eccentricity: float
    inclination: float
    ra_of_asc_node: float
    arg_of_pericenter: float
    mean_anomaly: float
    norad_cat_id: int
    ephemeris_type: int | Unset = UNSET
    classification_type: str | Unset = UNSET
    element_set_no: int | Unset = UNSET
    rev_at_epoch: int | Unset = UNSET
    bstar: float | Unset = UNSET
    mean_motion_dot: float | Unset = UNSET
    mean_motion_ddot: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        object_name = self.object_name

        object_id = self.object_id

        epoch = self.epoch

        mean_motion = self.mean_motion

        eccentricity = self.eccentricity

        inclination = self.inclination

        ra_of_asc_node = self.ra_of_asc_node

        arg_of_pericenter = self.arg_of_pericenter

        mean_anomaly = self.mean_anomaly

        norad_cat_id = self.norad_cat_id

        ephemeris_type = self.ephemeris_type

        classification_type = self.classification_type

        element_set_no = self.element_set_no

        rev_at_epoch = self.rev_at_epoch

        bstar = self.bstar

        mean_motion_dot = self.mean_motion_dot

        mean_motion_ddot = self.mean_motion_ddot

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "OBJECT_NAME": object_name,
                "OBJECT_ID": object_id,
                "EPOCH": epoch,
                "MEAN_MOTION": mean_motion,
                "ECCENTRICITY": eccentricity,
                "INCLINATION": inclination,
                "RA_OF_ASC_NODE": ra_of_asc_node,
                "ARG_OF_PERICENTER": arg_of_pericenter,
                "MEAN_ANOMALY": mean_anomaly,
                "NORAD_CAT_ID": norad_cat_id,
            }
        )
        if ephemeris_type is not UNSET:
            field_dict["EPHEMERIS_TYPE"] = ephemeris_type
        if classification_type is not UNSET:
            field_dict["CLASSIFICATION_TYPE"] = classification_type
        if element_set_no is not UNSET:
            field_dict["ELEMENT_SET_NO"] = element_set_no
        if rev_at_epoch is not UNSET:
            field_dict["REV_AT_EPOCH"] = rev_at_epoch
        if bstar is not UNSET:
            field_dict["BSTAR"] = bstar
        if mean_motion_dot is not UNSET:
            field_dict["MEAN_MOTION_DOT"] = mean_motion_dot
        if mean_motion_ddot is not UNSET:
            field_dict["MEAN_MOTION_DDOT"] = mean_motion_ddot

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        object_name = d.pop("OBJECT_NAME")

        object_id = d.pop("OBJECT_ID")

        epoch = d.pop("EPOCH")

        mean_motion = d.pop("MEAN_MOTION")

        eccentricity = d.pop("ECCENTRICITY")

        inclination = d.pop("INCLINATION")

        ra_of_asc_node = d.pop("RA_OF_ASC_NODE")

        arg_of_pericenter = d.pop("ARG_OF_PERICENTER")

        mean_anomaly = d.pop("MEAN_ANOMALY")

        norad_cat_id = d.pop("NORAD_CAT_ID")

        ephemeris_type = d.pop("EPHEMERIS_TYPE", UNSET)

        classification_type = d.pop("CLASSIFICATION_TYPE", UNSET)

        element_set_no = d.pop("ELEMENT_SET_NO", UNSET)

        rev_at_epoch = d.pop("REV_AT_EPOCH", UNSET)

        bstar = d.pop("BSTAR", UNSET)

        mean_motion_dot = d.pop("MEAN_MOTION_DOT", UNSET)

        mean_motion_ddot = d.pop("MEAN_MOTION_DDOT", UNSET)

        celestrak_gp_element = cls(
            object_name=object_name,
            object_id=object_id,
            epoch=epoch,
            mean_motion=mean_motion,
            eccentricity=eccentricity,
            inclination=inclination,
            ra_of_asc_node=ra_of_asc_node,
            arg_of_pericenter=arg_of_pericenter,
            mean_anomaly=mean_anomaly,
            norad_cat_id=norad_cat_id,
            ephemeris_type=ephemeris_type,
            classification_type=classification_type,
            element_set_no=element_set_no,
            rev_at_epoch=rev_at_epoch,
            bstar=bstar,
            mean_motion_dot=mean_motion_dot,
            mean_motion_ddot=mean_motion_ddot,
        )

        celestrak_gp_element.additional_properties = d
        return celestrak_gp_element

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
