from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ingest_records_path_key_get_format import IngestRecordsPathKeyGetFormat
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyGetFormat,
    *,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    time: str | Unset = UNSET,
    spd: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    dev: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["id"] = id

    params["deviceid"] = deviceid

    params["lat"] = lat

    params["lon"] = lon

    params["location"] = location

    params["timestamp"] = timestamp

    params["speed"] = speed

    params["bearing"] = bearing

    params["altitude"] = altitude

    params["accuracy"] = accuracy

    params["batt"] = batt

    params["charge"] = charge

    params["valid"] = valid

    params["time"] = time

    params["spd"] = spd

    params["dir"] = dir_

    params["alt"] = alt

    params["acc"] = acc

    params["dev"] = dev

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ingest/{source_id}/k/{key}/{format_}".format(
            source_id=quote(str(source_id), safe=""),
            key=quote(str(key), safe=""),
            format_=quote(str(format_), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 200:
        response_200 = cast(Any, None)
        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyGetFormat,
    *,
    client: AuthenticatedClient,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    time: str | Unset = UNSET,
    spd: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    dev: str | Unset = UNSET,
) -> Response[Any | Problem]:
    """Push a fix with a path key

     Path-key form of `ingestRecordsFormatGet` for OsmAnd/Traccar Client, GPSLogger and the GQ GMC
    counter (059), which cannot send credentials. `id`/`deviceid` must match a device key's bound entity
    (403 DEVICE_ENTITY_MISMATCH). An `eik_` key in the path needs the source's `allowPathKey` (else
    404); the sender must be in `ingestAllowCidrs` (default: private ranges; else 403
    INGEST_SOURCE_DENIED). The firmware sends cleartext HTTP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        source_id (str):
        key (str):
        format_ (IngestRecordsPathKeyGetFormat):
        id (str | Unset):
        deviceid (str | Unset):
        lat (float | Unset):
        lon (float | Unset):
        location (str | Unset):
        timestamp (str | Unset):
        speed (float | Unset):
        bearing (float | Unset):
        altitude (float | Unset):
        accuracy (float | Unset):
        batt (float | Unset):
        charge (bool | Unset):
        valid (bool | Unset):
        time (str | Unset):
        spd (float | Unset):
        dir_ (float | Unset):
        alt (float | Unset):
        acc (float | Unset):
        dev (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        key=key,
        format_=format_,
        id=id,
        deviceid=deviceid,
        lat=lat,
        lon=lon,
        location=location,
        timestamp=timestamp,
        speed=speed,
        bearing=bearing,
        altitude=altitude,
        accuracy=accuracy,
        batt=batt,
        charge=charge,
        valid=valid,
        time=time,
        spd=spd,
        dir_=dir_,
        alt=alt,
        acc=acc,
        dev=dev,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyGetFormat,
    *,
    client: AuthenticatedClient,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    time: str | Unset = UNSET,
    spd: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    dev: str | Unset = UNSET,
) -> Any | Problem | None:
    """Push a fix with a path key

     Path-key form of `ingestRecordsFormatGet` for OsmAnd/Traccar Client, GPSLogger and the GQ GMC
    counter (059), which cannot send credentials. `id`/`deviceid` must match a device key's bound entity
    (403 DEVICE_ENTITY_MISMATCH). An `eik_` key in the path needs the source's `allowPathKey` (else
    404); the sender must be in `ingestAllowCidrs` (default: private ranges; else 403
    INGEST_SOURCE_DENIED). The firmware sends cleartext HTTP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        source_id (str):
        key (str):
        format_ (IngestRecordsPathKeyGetFormat):
        id (str | Unset):
        deviceid (str | Unset):
        lat (float | Unset):
        lon (float | Unset):
        location (str | Unset):
        timestamp (str | Unset):
        speed (float | Unset):
        bearing (float | Unset):
        altitude (float | Unset):
        accuracy (float | Unset):
        batt (float | Unset):
        charge (bool | Unset):
        valid (bool | Unset):
        time (str | Unset):
        spd (float | Unset):
        dir_ (float | Unset):
        alt (float | Unset):
        acc (float | Unset):
        dev (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        source_id=source_id,
        key=key,
        format_=format_,
        client=client,
        id=id,
        deviceid=deviceid,
        lat=lat,
        lon=lon,
        location=location,
        timestamp=timestamp,
        speed=speed,
        bearing=bearing,
        altitude=altitude,
        accuracy=accuracy,
        batt=batt,
        charge=charge,
        valid=valid,
        time=time,
        spd=spd,
        dir_=dir_,
        alt=alt,
        acc=acc,
        dev=dev,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyGetFormat,
    *,
    client: AuthenticatedClient,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    time: str | Unset = UNSET,
    spd: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    dev: str | Unset = UNSET,
) -> Response[Any | Problem]:
    """Push a fix with a path key

     Path-key form of `ingestRecordsFormatGet` for OsmAnd/Traccar Client, GPSLogger and the GQ GMC
    counter (059), which cannot send credentials. `id`/`deviceid` must match a device key's bound entity
    (403 DEVICE_ENTITY_MISMATCH). An `eik_` key in the path needs the source's `allowPathKey` (else
    404); the sender must be in `ingestAllowCidrs` (default: private ranges; else 403
    INGEST_SOURCE_DENIED). The firmware sends cleartext HTTP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        source_id (str):
        key (str):
        format_ (IngestRecordsPathKeyGetFormat):
        id (str | Unset):
        deviceid (str | Unset):
        lat (float | Unset):
        lon (float | Unset):
        location (str | Unset):
        timestamp (str | Unset):
        speed (float | Unset):
        bearing (float | Unset):
        altitude (float | Unset):
        accuracy (float | Unset):
        batt (float | Unset):
        charge (bool | Unset):
        valid (bool | Unset):
        time (str | Unset):
        spd (float | Unset):
        dir_ (float | Unset):
        alt (float | Unset):
        acc (float | Unset):
        dev (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        key=key,
        format_=format_,
        id=id,
        deviceid=deviceid,
        lat=lat,
        lon=lon,
        location=location,
        timestamp=timestamp,
        speed=speed,
        bearing=bearing,
        altitude=altitude,
        accuracy=accuracy,
        batt=batt,
        charge=charge,
        valid=valid,
        time=time,
        spd=spd,
        dir_=dir_,
        alt=alt,
        acc=acc,
        dev=dev,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyGetFormat,
    *,
    client: AuthenticatedClient,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    time: str | Unset = UNSET,
    spd: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    dev: str | Unset = UNSET,
) -> Any | Problem | None:
    """Push a fix with a path key

     Path-key form of `ingestRecordsFormatGet` for OsmAnd/Traccar Client, GPSLogger and the GQ GMC
    counter (059), which cannot send credentials. `id`/`deviceid` must match a device key's bound entity
    (403 DEVICE_ENTITY_MISMATCH). An `eik_` key in the path needs the source's `allowPathKey` (else
    404); the sender must be in `ingestAllowCidrs` (default: private ranges; else 403
    INGEST_SOURCE_DENIED). The firmware sends cleartext HTTP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        source_id (str):
        key (str):
        format_ (IngestRecordsPathKeyGetFormat):
        id (str | Unset):
        deviceid (str | Unset):
        lat (float | Unset):
        lon (float | Unset):
        location (str | Unset):
        timestamp (str | Unset):
        speed (float | Unset):
        bearing (float | Unset):
        altitude (float | Unset):
        accuracy (float | Unset):
        batt (float | Unset):
        charge (bool | Unset):
        valid (bool | Unset):
        time (str | Unset):
        spd (float | Unset):
        dir_ (float | Unset):
        alt (float | Unset):
        acc (float | Unset):
        dev (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            key=key,
            format_=format_,
            client=client,
            id=id,
            deviceid=deviceid,
            lat=lat,
            lon=lon,
            location=location,
            timestamp=timestamp,
            speed=speed,
            bearing=bearing,
            altitude=altitude,
            accuracy=accuracy,
            batt=batt,
            charge=charge,
            valid=valid,
            time=time,
            spd=spd,
            dir_=dir_,
            alt=alt,
            acc=acc,
            dev=dev,
        )
    ).parsed
