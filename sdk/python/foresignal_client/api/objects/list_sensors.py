import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_sensors_kinds_item import ListSensorsKindsItem
from ...models.problem import Problem
from ...models.sensor_discovery_result import SensorDiscoveryResult
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    kinds: list[ListSensorsKindsItem] | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["bbox"] = bbox

    params["placeId"] = place_id

    json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to = to.isoformat()
    params["to"] = json_to

    json_kinds: list[str] | Unset = UNSET
    if not isinstance(kinds, Unset):
        json_kinds = []
        for kinds_item_data in kinds:
            kinds_item = kinds_item_data.value
            json_kinds.append(kinds_item)

    params["kinds"] = json_kinds

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/sensors",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SensorDiscoveryResult | None:
    if response.status_code == 200:
        response_200 = SensorDiscoveryResult.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SensorDiscoveryResult]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    kinds: list[ListSensorsKindsItem] | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SensorDiscoveryResult]:
    """Discover sensors observing an area

     Merged sensor envelopes: sources with 116 observability above 0.2 for the area, local receivers and
    SatDump stations in range, predicted satellite passes with curated capabilities, and STAC scenes and
    raster products in the window. Read-only; no tasking. `placeId` requires a token.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        place_id (str | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        kinds (list[ListSensorsKindsItem] | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SensorDiscoveryResult]
    """

    kwargs = _get_kwargs(
        bbox=bbox,
        place_id=place_id,
        from_=from_,
        to=to,
        kinds=kinds,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    kinds: list[ListSensorsKindsItem] | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SensorDiscoveryResult | None:
    """Discover sensors observing an area

     Merged sensor envelopes: sources with 116 observability above 0.2 for the area, local receivers and
    SatDump stations in range, predicted satellite passes with curated capabilities, and STAC scenes and
    raster products in the window. Read-only; no tasking. `placeId` requires a token.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        place_id (str | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        kinds (list[ListSensorsKindsItem] | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SensorDiscoveryResult
    """

    return sync_detailed(
        client=client,
        bbox=bbox,
        place_id=place_id,
        from_=from_,
        to=to,
        kinds=kinds,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    kinds: list[ListSensorsKindsItem] | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SensorDiscoveryResult]:
    """Discover sensors observing an area

     Merged sensor envelopes: sources with 116 observability above 0.2 for the area, local receivers and
    SatDump stations in range, predicted satellite passes with curated capabilities, and STAC scenes and
    raster products in the window. Read-only; no tasking. `placeId` requires a token.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        place_id (str | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        kinds (list[ListSensorsKindsItem] | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SensorDiscoveryResult]
    """

    kwargs = _get_kwargs(
        bbox=bbox,
        place_id=place_id,
        from_=from_,
        to=to,
        kinds=kinds,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    kinds: list[ListSensorsKindsItem] | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SensorDiscoveryResult | None:
    """Discover sensors observing an area

     Merged sensor envelopes: sources with 116 observability above 0.2 for the area, local receivers and
    SatDump stations in range, predicted satellite passes with curated capabilities, and STAC scenes and
    raster products in the window. Read-only; no tasking. `placeId` requires a token.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        place_id (str | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        kinds (list[ListSensorsKindsItem] | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SensorDiscoveryResult
    """

    return (
        await asyncio_detailed(
            client=client,
            bbox=bbox,
            place_id=place_id,
            from_=from_,
            to=to,
            kinds=kinds,
            limit=limit,
        )
    ).parsed
