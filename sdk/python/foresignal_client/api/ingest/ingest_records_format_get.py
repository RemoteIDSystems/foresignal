from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    format_: str,
    *,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    time: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    spd: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    heading: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    dev: str | Unset = UNSET,
    password: str | Unset = UNSET,
    ID: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["id"] = id

    params["deviceid"] = deviceid

    params["lat"] = lat

    params["lon"] = lon

    params["location"] = location

    params["timestamp"] = timestamp

    params["time"] = time

    params["speed"] = speed

    params["spd"] = spd

    params["bearing"] = bearing

    params["heading"] = heading

    params["dir"] = dir_

    params["altitude"] = altitude

    params["alt"] = alt

    params["accuracy"] = accuracy

    params["acc"] = acc

    params["batt"] = batt

    params["charge"] = charge

    params["valid"] = valid

    params["dev"] = dev

    params["PASSWORD"] = password

    params["ID"] = ID

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ingest/{source_id}/{format_}".format(
            source_id=quote(str(source_id), safe=""),
            format_=quote(str(format_), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | str | None:
    if response.status_code == 200:
        response_200 = response.text
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

    if response.status_code == 405:
        response_405 = Problem.from_dict(response.json())

        return response_405

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
) -> Response[Problem | str]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    time: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    spd: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    heading: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    dev: str | Unset = UNSET,
    password: str | Unset = UNSET,
    ID: str | Unset = UNSET,
) -> Response[Problem | str]:
    """Push a fix through a query-string format

     GET form of the formats that declare it: `osmand` (Traccar Client, OsmAnd) and `gpslogger` (039),
    `wu-pws` and `gmc` (059); other formats → 405. Fields come from the query string (see `OsmAndQuery`,
    `GpsLoggerFix`; the device presets map the query as a flat object of strings). Replies: an empty 200
    (OsmAnd, GPSLogger), `200 text/plain success` (wu-pws), `200 text/plain OK.` (gmc, unverified). The
    WU protocol's `PASSWORD` parameter carries the ingest key (`ID` is ignored). GET pushes from a key
    holder must come from `ingestAllowCidrs` (default: private ranges; else 403 INGEST_SOURCE_DENIED).
    Keys in query strings (`key`, `PASSWORD`, `PASSKEY`, `auth`, `token`) are redacted from every log
    and fetch log.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        id (str | Unset):
        deviceid (str | Unset):
        lat (float | Unset):
        lon (float | Unset):
        location (str | Unset):
        timestamp (str | Unset):
        time (str | Unset):
        speed (float | Unset):
        spd (float | Unset):
        bearing (float | Unset):
        heading (float | Unset):
        dir_ (float | Unset):
        altitude (float | Unset):
        alt (float | Unset):
        accuracy (float | Unset):
        acc (float | Unset):
        batt (float | Unset):
        charge (bool | Unset):
        valid (bool | Unset):
        dev (str | Unset):
        password (str | Unset):
        ID (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        format_=format_,
        id=id,
        deviceid=deviceid,
        lat=lat,
        lon=lon,
        location=location,
        timestamp=timestamp,
        time=time,
        speed=speed,
        spd=spd,
        bearing=bearing,
        heading=heading,
        dir_=dir_,
        altitude=altitude,
        alt=alt,
        accuracy=accuracy,
        acc=acc,
        batt=batt,
        charge=charge,
        valid=valid,
        dev=dev,
        password=password,
        ID=ID,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    time: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    spd: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    heading: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    dev: str | Unset = UNSET,
    password: str | Unset = UNSET,
    ID: str | Unset = UNSET,
) -> Problem | str | None:
    """Push a fix through a query-string format

     GET form of the formats that declare it: `osmand` (Traccar Client, OsmAnd) and `gpslogger` (039),
    `wu-pws` and `gmc` (059); other formats → 405. Fields come from the query string (see `OsmAndQuery`,
    `GpsLoggerFix`; the device presets map the query as a flat object of strings). Replies: an empty 200
    (OsmAnd, GPSLogger), `200 text/plain success` (wu-pws), `200 text/plain OK.` (gmc, unverified). The
    WU protocol's `PASSWORD` parameter carries the ingest key (`ID` is ignored). GET pushes from a key
    holder must come from `ingestAllowCidrs` (default: private ranges; else 403 INGEST_SOURCE_DENIED).
    Keys in query strings (`key`, `PASSWORD`, `PASSKEY`, `auth`, `token`) are redacted from every log
    and fetch log.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        id (str | Unset):
        deviceid (str | Unset):
        lat (float | Unset):
        lon (float | Unset):
        location (str | Unset):
        timestamp (str | Unset):
        time (str | Unset):
        speed (float | Unset):
        spd (float | Unset):
        bearing (float | Unset):
        heading (float | Unset):
        dir_ (float | Unset):
        altitude (float | Unset):
        alt (float | Unset):
        accuracy (float | Unset):
        acc (float | Unset):
        batt (float | Unset):
        charge (bool | Unset):
        valid (bool | Unset):
        dev (str | Unset):
        password (str | Unset):
        ID (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return sync_detailed(
        source_id=source_id,
        format_=format_,
        client=client,
        id=id,
        deviceid=deviceid,
        lat=lat,
        lon=lon,
        location=location,
        timestamp=timestamp,
        time=time,
        speed=speed,
        spd=spd,
        bearing=bearing,
        heading=heading,
        dir_=dir_,
        altitude=altitude,
        alt=alt,
        accuracy=accuracy,
        acc=acc,
        batt=batt,
        charge=charge,
        valid=valid,
        dev=dev,
        password=password,
        ID=ID,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    time: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    spd: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    heading: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    dev: str | Unset = UNSET,
    password: str | Unset = UNSET,
    ID: str | Unset = UNSET,
) -> Response[Problem | str]:
    """Push a fix through a query-string format

     GET form of the formats that declare it: `osmand` (Traccar Client, OsmAnd) and `gpslogger` (039),
    `wu-pws` and `gmc` (059); other formats → 405. Fields come from the query string (see `OsmAndQuery`,
    `GpsLoggerFix`; the device presets map the query as a flat object of strings). Replies: an empty 200
    (OsmAnd, GPSLogger), `200 text/plain success` (wu-pws), `200 text/plain OK.` (gmc, unverified). The
    WU protocol's `PASSWORD` parameter carries the ingest key (`ID` is ignored). GET pushes from a key
    holder must come from `ingestAllowCidrs` (default: private ranges; else 403 INGEST_SOURCE_DENIED).
    Keys in query strings (`key`, `PASSWORD`, `PASSKEY`, `auth`, `token`) are redacted from every log
    and fetch log.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        id (str | Unset):
        deviceid (str | Unset):
        lat (float | Unset):
        lon (float | Unset):
        location (str | Unset):
        timestamp (str | Unset):
        time (str | Unset):
        speed (float | Unset):
        spd (float | Unset):
        bearing (float | Unset):
        heading (float | Unset):
        dir_ (float | Unset):
        altitude (float | Unset):
        alt (float | Unset):
        accuracy (float | Unset):
        acc (float | Unset):
        batt (float | Unset):
        charge (bool | Unset):
        valid (bool | Unset):
        dev (str | Unset):
        password (str | Unset):
        ID (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        format_=format_,
        id=id,
        deviceid=deviceid,
        lat=lat,
        lon=lon,
        location=location,
        timestamp=timestamp,
        time=time,
        speed=speed,
        spd=spd,
        bearing=bearing,
        heading=heading,
        dir_=dir_,
        altitude=altitude,
        alt=alt,
        accuracy=accuracy,
        acc=acc,
        batt=batt,
        charge=charge,
        valid=valid,
        dev=dev,
        password=password,
        ID=ID,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    id: str | Unset = UNSET,
    deviceid: str | Unset = UNSET,
    lat: float | Unset = UNSET,
    lon: float | Unset = UNSET,
    location: str | Unset = UNSET,
    timestamp: str | Unset = UNSET,
    time: str | Unset = UNSET,
    speed: float | Unset = UNSET,
    spd: float | Unset = UNSET,
    bearing: float | Unset = UNSET,
    heading: float | Unset = UNSET,
    dir_: float | Unset = UNSET,
    altitude: float | Unset = UNSET,
    alt: float | Unset = UNSET,
    accuracy: float | Unset = UNSET,
    acc: float | Unset = UNSET,
    batt: float | Unset = UNSET,
    charge: bool | Unset = UNSET,
    valid: bool | Unset = UNSET,
    dev: str | Unset = UNSET,
    password: str | Unset = UNSET,
    ID: str | Unset = UNSET,
) -> Problem | str | None:
    """Push a fix through a query-string format

     GET form of the formats that declare it: `osmand` (Traccar Client, OsmAnd) and `gpslogger` (039),
    `wu-pws` and `gmc` (059); other formats → 405. Fields come from the query string (see `OsmAndQuery`,
    `GpsLoggerFix`; the device presets map the query as a flat object of strings). Replies: an empty 200
    (OsmAnd, GPSLogger), `200 text/plain success` (wu-pws), `200 text/plain OK.` (gmc, unverified). The
    WU protocol's `PASSWORD` parameter carries the ingest key (`ID` is ignored). GET pushes from a key
    holder must come from `ingestAllowCidrs` (default: private ranges; else 403 INGEST_SOURCE_DENIED).
    Keys in query strings (`key`, `PASSWORD`, `PASSKEY`, `auth`, `token`) are redacted from every log
    and fetch log.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        id (str | Unset):
        deviceid (str | Unset):
        lat (float | Unset):
        lon (float | Unset):
        location (str | Unset):
        timestamp (str | Unset):
        time (str | Unset):
        speed (float | Unset):
        spd (float | Unset):
        bearing (float | Unset):
        heading (float | Unset):
        dir_ (float | Unset):
        altitude (float | Unset):
        alt (float | Unset):
        accuracy (float | Unset):
        acc (float | Unset):
        batt (float | Unset):
        charge (bool | Unset):
        valid (bool | Unset):
        dev (str | Unset):
        password (str | Unset):
        ID (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            format_=format_,
            client=client,
            id=id,
            deviceid=deviceid,
            lat=lat,
            lon=lon,
            location=location,
            timestamp=timestamp,
            time=time,
            speed=speed,
            spd=spd,
            bearing=bearing,
            heading=heading,
            dir_=dir_,
            altitude=altitude,
            alt=alt,
            accuracy=accuracy,
            acc=acc,
            batt=batt,
            charge=charge,
            valid=valid,
            dev=dev,
            password=password,
            ID=ID,
        )
    ).parsed
