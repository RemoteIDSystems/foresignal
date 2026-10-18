from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.relation import Relation
from ...models.relation_input import RelationInput
from ...types import Response


def _get_kwargs(
    *,
    body: RelationInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/relations",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Relation | None:
    if response.status_code == 201:
        response_201 = Relation.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | Relation]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: RelationInput,
) -> Response[Problem | Relation]:
    """Add an operator relation

     Adds an operator or agent relation (`provenance.operator`). `causes` is labelled at most `possible`
    unless the caller confirms as operator.

    **Scope:** `agent`.

    Args:
        body (RelationInput): An operator or agent relation; `causes` stays ≤ `possible` until an
            operator confirms

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Relation]
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
    body: RelationInput,
) -> Problem | Relation | None:
    """Add an operator relation

     Adds an operator or agent relation (`provenance.operator`). `causes` is labelled at most `possible`
    unless the caller confirms as operator.

    **Scope:** `agent`.

    Args:
        body (RelationInput): An operator or agent relation; `causes` stays ≤ `possible` until an
            operator confirms

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Relation
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: RelationInput,
) -> Response[Problem | Relation]:
    """Add an operator relation

     Adds an operator or agent relation (`provenance.operator`). `causes` is labelled at most `possible`
    unless the caller confirms as operator.

    **Scope:** `agent`.

    Args:
        body (RelationInput): An operator or agent relation; `causes` stays ≤ `possible` until an
            operator confirms

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Relation]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: RelationInput,
) -> Problem | Relation | None:
    """Add an operator relation

     Adds an operator or agent relation (`provenance.operator`). `causes` is labelled at most `possible`
    unless the caller confirms as operator.

    **Scope:** `agent`.

    Args:
        body (RelationInput): An operator or agent relation; `causes` stays ≤ `possible` until an
            operator confirms

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Relation
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
