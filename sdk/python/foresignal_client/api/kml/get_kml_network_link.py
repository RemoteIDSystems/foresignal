from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    layers: str | Unset = UNSET,
    refresh: int | Unset = 60,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["layers"] = layers

    params["refresh"] = refresh

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/kml/networklink.kml",
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
    layers: str | Unset = UNSET,
    refresh: int | Unset = 60,
) -> Response[Problem]:
    """Self-refreshing KML NetworkLink document

     Document with one `NetworkLink` per layer pointing at `/v1/kml/records.kml?layer={layer}` (feed-key
    prefixed when one is used), `refreshMode=onInterval`, `viewRefreshMode=onStop` (2 s) and
    `viewFormat=bbox=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]`. For Google Earth and Cesium
    `KmlDataSource`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layers (str | Unset):
        refresh (int | Unset):  Default: 60.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        layers=layers,
        refresh=refresh,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    layers: str | Unset = UNSET,
    refresh: int | Unset = 60,
) -> Problem | None:
    """Self-refreshing KML NetworkLink document

     Document with one `NetworkLink` per layer pointing at `/v1/kml/records.kml?layer={layer}` (feed-key
    prefixed when one is used), `refreshMode=onInterval`, `viewRefreshMode=onStop` (2 s) and
    `viewFormat=bbox=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]`. For Google Earth and Cesium
    `KmlDataSource`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layers (str | Unset):
        refresh (int | Unset):  Default: 60.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        layers=layers,
        refresh=refresh,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    layers: str | Unset = UNSET,
    refresh: int | Unset = 60,
) -> Response[Problem]:
    """Self-refreshing KML NetworkLink document

     Document with one `NetworkLink` per layer pointing at `/v1/kml/records.kml?layer={layer}` (feed-key
    prefixed when one is used), `refreshMode=onInterval`, `viewRefreshMode=onStop` (2 s) and
    `viewFormat=bbox=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]`. For Google Earth and Cesium
    `KmlDataSource`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layers (str | Unset):
        refresh (int | Unset):  Default: 60.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        layers=layers,
        refresh=refresh,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    layers: str | Unset = UNSET,
    refresh: int | Unset = 60,
) -> Problem | None:
    """Self-refreshing KML NetworkLink document

     Document with one `NetworkLink` per layer pointing at `/v1/kml/records.kml?layer={layer}` (feed-key
    prefixed when one is used), `refreshMode=onInterval`, `viewRefreshMode=onStop` (2 s) and
    `viewFormat=bbox=[bboxWest],[bboxSouth],[bboxEast],[bboxNorth]`. For Google Earth and Cesium
    `KmlDataSource`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layers (str | Unset):
        refresh (int | Unset):  Default: 60.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            layers=layers,
            refresh=refresh,
        )
    ).parsed
