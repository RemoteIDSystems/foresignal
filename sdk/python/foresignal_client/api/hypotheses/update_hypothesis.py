from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.hypothesis import Hypothesis
from ...models.hypothesis_patch import HypothesisPatch
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    hypothesis_id: str,
    *,
    body: HypothesisPatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/hypotheses/{hypothesis_id}".format(
            hypothesis_id=quote(str(hypothesis_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Hypothesis | Problem | None:
    if response.status_code == 200:
        response_200 = Hypothesis.from_dict(response.json())

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

    if response.status_code == 415:
        response_415 = Problem.from_dict(response.json())

        return response_415

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Hypothesis | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    hypothesis_id: str,
    *,
    client: AuthenticatedClient,
    body: HypothesisPatch,
) -> Response[Hypothesis | Problem]:
    """Update a hypothesis

     JSON merge patch of statement, label, prior basis or alternatives; a probability override is
    recorded as an `operator` revision and logged.

    **Scope:** `agent`.

    Args:
        hypothesis_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (HypothesisPatch): JSON merge patch for a hypothesis; probability overrides are
            logged as revisions

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Hypothesis | Problem]
    """

    kwargs = _get_kwargs(
        hypothesis_id=hypothesis_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    hypothesis_id: str,
    *,
    client: AuthenticatedClient,
    body: HypothesisPatch,
) -> Hypothesis | Problem | None:
    """Update a hypothesis

     JSON merge patch of statement, label, prior basis or alternatives; a probability override is
    recorded as an `operator` revision and logged.

    **Scope:** `agent`.

    Args:
        hypothesis_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (HypothesisPatch): JSON merge patch for a hypothesis; probability overrides are
            logged as revisions

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Hypothesis | Problem
    """

    return sync_detailed(
        hypothesis_id=hypothesis_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    hypothesis_id: str,
    *,
    client: AuthenticatedClient,
    body: HypothesisPatch,
) -> Response[Hypothesis | Problem]:
    """Update a hypothesis

     JSON merge patch of statement, label, prior basis or alternatives; a probability override is
    recorded as an `operator` revision and logged.

    **Scope:** `agent`.

    Args:
        hypothesis_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (HypothesisPatch): JSON merge patch for a hypothesis; probability overrides are
            logged as revisions

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Hypothesis | Problem]
    """

    kwargs = _get_kwargs(
        hypothesis_id=hypothesis_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    hypothesis_id: str,
    *,
    client: AuthenticatedClient,
    body: HypothesisPatch,
) -> Hypothesis | Problem | None:
    """Update a hypothesis

     JSON merge patch of statement, label, prior basis or alternatives; a probability override is
    recorded as an `operator` revision and logged.

    **Scope:** `agent`.

    Args:
        hypothesis_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (HypothesisPatch): JSON merge patch for a hypothesis; probability overrides are
            logged as revisions

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Hypothesis | Problem
    """

    return (
        await asyncio_detailed(
            hypothesis_id=hypothesis_id,
            client=client,
            body=body,
        )
    ).parsed
