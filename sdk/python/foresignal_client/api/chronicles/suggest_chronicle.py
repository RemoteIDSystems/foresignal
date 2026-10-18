from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_suggest_request import ChronicleSuggestRequest
from ...models.chronicle_suggestion import ChronicleSuggestion
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ChronicleSuggestRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/chronicles/suggest",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChronicleSuggestion | Problem | None:
    if response.status_code == 200:
        response_200 = ChronicleSuggestion.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ChronicleSuggestion | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ChronicleSuggestRequest,
) -> Response[ChronicleSuggestion | Problem]:
    """Suggest a draft chronicle

     Returns a draft (not created) for a situation, or an area plus topic: area from the situation
    geometry buffered or its regions, layers from member sources and kinds with default capture modes
    (TRACK → downsampled 60 s, events → full, series → full, rasters → every 4th frame), linked topics
    and series, and a storage estimate.

    **Scope:** `agent`.

    Args:
        body (ChronicleSuggestRequest): Ask for a draft chronicle from a situation or an area and
            topic

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleSuggestion | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ChronicleSuggestRequest,
) -> ChronicleSuggestion | Problem | None:
    """Suggest a draft chronicle

     Returns a draft (not created) for a situation, or an area plus topic: area from the situation
    geometry buffered or its regions, layers from member sources and kinds with default capture modes
    (TRACK → downsampled 60 s, events → full, series → full, rasters → every 4th frame), linked topics
    and series, and a storage estimate.

    **Scope:** `agent`.

    Args:
        body (ChronicleSuggestRequest): Ask for a draft chronicle from a situation or an area and
            topic

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleSuggestion | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ChronicleSuggestRequest,
) -> Response[ChronicleSuggestion | Problem]:
    """Suggest a draft chronicle

     Returns a draft (not created) for a situation, or an area plus topic: area from the situation
    geometry buffered or its regions, layers from member sources and kinds with default capture modes
    (TRACK → downsampled 60 s, events → full, series → full, rasters → every 4th frame), linked topics
    and series, and a storage estimate.

    **Scope:** `agent`.

    Args:
        body (ChronicleSuggestRequest): Ask for a draft chronicle from a situation or an area and
            topic

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleSuggestion | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ChronicleSuggestRequest,
) -> ChronicleSuggestion | Problem | None:
    """Suggest a draft chronicle

     Returns a draft (not created) for a situation, or an area plus topic: area from the situation
    geometry buffered or its regions, layers from member sources and kinds with default capture modes
    (TRACK → downsampled 60 s, events → full, series → full, rasters → every 4th frame), linked topics
    and series, and a storage estimate.

    **Scope:** `agent`.

    Args:
        body (ChronicleSuggestRequest): Ask for a draft chronicle from a situation or an area and
            topic

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleSuggestion | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
