from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.precursor_candidate import PrecursorCandidate
from ...models.precursor_candidate_input import PrecursorCandidateInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: PrecursorCandidateInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/precursors/candidates",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PrecursorCandidate | Problem | None:
    if response.status_code == 201:
        response_201 = PrecursorCandidate.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[PrecursorCandidate | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: PrecursorCandidateInput,
) -> Response[PrecursorCandidate | Problem]:
    """Register a precursor candidate

     Registers an event or series candidate pair with spatial rule and lag bounds; it is tested in the
    next mining run.

    **Scope:** `agent` or `admin`.

    Args:
        body (PrecursorCandidateInput): Register a candidate pair (event selectors or series refs,
            spatial rule, lag bounds)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PrecursorCandidate | Problem]
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
    body: PrecursorCandidateInput,
) -> PrecursorCandidate | Problem | None:
    """Register a precursor candidate

     Registers an event or series candidate pair with spatial rule and lag bounds; it is tested in the
    next mining run.

    **Scope:** `agent` or `admin`.

    Args:
        body (PrecursorCandidateInput): Register a candidate pair (event selectors or series refs,
            spatial rule, lag bounds)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PrecursorCandidate | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: PrecursorCandidateInput,
) -> Response[PrecursorCandidate | Problem]:
    """Register a precursor candidate

     Registers an event or series candidate pair with spatial rule and lag bounds; it is tested in the
    next mining run.

    **Scope:** `agent` or `admin`.

    Args:
        body (PrecursorCandidateInput): Register a candidate pair (event selectors or series refs,
            spatial rule, lag bounds)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PrecursorCandidate | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: PrecursorCandidateInput,
) -> PrecursorCandidate | Problem | None:
    """Register a precursor candidate

     Registers an event or series candidate pair with spatial rule and lag bounds; it is tested in the
    next mining run.

    **Scope:** `agent` or `admin`.

    Args:
        body (PrecursorCandidateInput): Register a candidate pair (event selectors or series refs,
            spatial rule, lag bounds)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PrecursorCandidate | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
