import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_kml_records_format import GetKmlRecordsFormat
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    layer: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    format_: GetKmlRecordsFormat | Unset = GetKmlRecordsFormat.KML,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["layer"] = layer

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params["bbox"] = bbox

    json_as_of: str | Unset = UNSET
    if not isinstance(as_of, Unset):
        json_as_of = as_of.isoformat()
    params["asOf"] = json_as_of

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/kml/records.kml",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    layer: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    format_: GetKmlRecordsFormat | Unset = GetKmlRecordsFormat.KML,
) -> Response[Problem]:
    """Current records as KML or KMZ

     One `Style` per kind (icon href `<base>/v1/map/icons/{name}.png`, relative inside KMZ; colour as
    `aabbggrr`; heading from `rotateField`) and one `Placemark` per record (`name` = title,
    `TimeStamp/when` = observedAt, `ExtendedData` for fieldSpecs/tileProperties fields, plain escaped
    `description`). Capped at 5,000 Placemarks, newest observedAt first; when truncated the Document
    carries `Snippet` "truncated: showing 5000 of N" and `ExtendedData truncated=true`. An antimeridian
    bbox (`west > east`) is split. One of `layer`, `source` or `kind` is required. All upstream strings
    are escaped for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text.
    The Document `description` carries the included sources' attributions. Private filtering and
    `EgressPolicy.allows(FEED)` apply; keys in URLs are redacted from logs.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        as_of (datetime.datetime | Unset):
        format_ (GetKmlRecordsFormat | Unset):  Default: GetKmlRecordsFormat.KML.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        layer=layer,
        source=source,
        kind=kind,
        bbox=bbox,
        as_of=as_of,
        format_=format_,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    layer: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    format_: GetKmlRecordsFormat | Unset = GetKmlRecordsFormat.KML,
) -> Problem | None:
    """Current records as KML or KMZ

     One `Style` per kind (icon href `<base>/v1/map/icons/{name}.png`, relative inside KMZ; colour as
    `aabbggrr`; heading from `rotateField`) and one `Placemark` per record (`name` = title,
    `TimeStamp/when` = observedAt, `ExtendedData` for fieldSpecs/tileProperties fields, plain escaped
    `description`). Capped at 5,000 Placemarks, newest observedAt first; when truncated the Document
    carries `Snippet` "truncated: showing 5000 of N" and `ExtendedData truncated=true`. An antimeridian
    bbox (`west > east`) is split. One of `layer`, `source` or `kind` is required. All upstream strings
    are escaped for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text.
    The Document `description` carries the included sources' attributions. Private filtering and
    `EgressPolicy.allows(FEED)` apply; keys in URLs are redacted from logs.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        as_of (datetime.datetime | Unset):
        format_ (GetKmlRecordsFormat | Unset):  Default: GetKmlRecordsFormat.KML.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        layer=layer,
        source=source,
        kind=kind,
        bbox=bbox,
        as_of=as_of,
        format_=format_,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    layer: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    format_: GetKmlRecordsFormat | Unset = GetKmlRecordsFormat.KML,
) -> Response[Problem]:
    """Current records as KML or KMZ

     One `Style` per kind (icon href `<base>/v1/map/icons/{name}.png`, relative inside KMZ; colour as
    `aabbggrr`; heading from `rotateField`) and one `Placemark` per record (`name` = title,
    `TimeStamp/when` = observedAt, `ExtendedData` for fieldSpecs/tileProperties fields, plain escaped
    `description`). Capped at 5,000 Placemarks, newest observedAt first; when truncated the Document
    carries `Snippet` "truncated: showing 5000 of N" and `ExtendedData truncated=true`. An antimeridian
    bbox (`west > east`) is split. One of `layer`, `source` or `kind` is required. All upstream strings
    are escaped for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text.
    The Document `description` carries the included sources' attributions. Private filtering and
    `EgressPolicy.allows(FEED)` apply; keys in URLs are redacted from logs.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        as_of (datetime.datetime | Unset):
        format_ (GetKmlRecordsFormat | Unset):  Default: GetKmlRecordsFormat.KML.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        layer=layer,
        source=source,
        kind=kind,
        bbox=bbox,
        as_of=as_of,
        format_=format_,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    layer: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    format_: GetKmlRecordsFormat | Unset = GetKmlRecordsFormat.KML,
) -> Problem | None:
    """Current records as KML or KMZ

     One `Style` per kind (icon href `<base>/v1/map/icons/{name}.png`, relative inside KMZ; colour as
    `aabbggrr`; heading from `rotateField`) and one `Placemark` per record (`name` = title,
    `TimeStamp/when` = observedAt, `ExtendedData` for fieldSpecs/tileProperties fields, plain escaped
    `description`). Capped at 5,000 Placemarks, newest observedAt first; when truncated the Document
    carries `Snippet` "truncated: showing 5000 of N" and `ExtendedData truncated=true`. An antimeridian
    bbox (`west > east`) is split. One of `layer`, `source` or `kind` is required. All upstream strings
    are escaped for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text.
    The Document `description` carries the included sources' attributions. Private filtering and
    `EgressPolicy.allows(FEED)` apply; keys in URLs are redacted from logs.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        as_of (datetime.datetime | Unset):
        format_ (GetKmlRecordsFormat | Unset):  Default: GetKmlRecordsFormat.KML.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            layer=layer,
            source=source,
            kind=kind,
            bbox=bbox,
            as_of=as_of,
            format_=format_,
        )
    ).parsed
