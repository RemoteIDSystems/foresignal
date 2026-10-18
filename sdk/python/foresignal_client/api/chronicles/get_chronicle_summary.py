from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_summary import ChronicleSummary
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    chronicle_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicles/{chronicle_id}/summary".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChronicleSummary | Problem | None:
    if response.status_code == 200:
        response_200 = ChronicleSummary.from_dict(response.json())

        return response_200

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
) -> Response[ChronicleSummary | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ChronicleSummary | Problem]:
    """Chronicle summary

     Layer counts over time, top entities, situations, hypothesis sets, forecasts, storage and capture
    gaps.

    **Scope:** `read`.

    Args:
        chronicle_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleSummary | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
) -> ChronicleSummary | Problem | None:
    """Chronicle summary

     Layer counts over time, top entities, situations, hypothesis sets, forecasts, storage and capture
    gaps.

    **Scope:** `read`.

    Args:
        chronicle_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleSummary | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ChronicleSummary | Problem]:
    """Chronicle summary

     Layer counts over time, top entities, situations, hypothesis sets, forecasts, storage and capture
    gaps.

    **Scope:** `read`.

    Args:
        chronicle_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleSummary | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
) -> ChronicleSummary | Problem | None:
    """Chronicle summary

     Layer counts over time, top entities, situations, hypothesis sets, forecasts, storage and capture
    gaps.

    **Scope:** `read`.

    Args:
        chronicle_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleSummary | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
        )
    ).parsed
